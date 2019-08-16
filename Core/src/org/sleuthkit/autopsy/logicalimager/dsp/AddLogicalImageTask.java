/*
 * Autopsy
 *
 * Copyright 2019 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.logicalimager.dsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorCallback;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataSourceProcessorProgressMonitor;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.LocalFilesDataSource;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskDataException;

/**
 * A runnable that - copy the logical image folder to a destination folder - add
 * SearchResults.txt and users.txt files to report - add an image data source to the
 * case database.
 */
final class AddLogicalImageTask implements Runnable {

    private final static Logger LOGGER = Logger.getLogger(AddLogicalImageTask.class.getName());
    private final static String SEARCH_RESULTS_TXT = "SearchResults.txt"; //NON-NLS
    private final static String USERS_TXT = "users.txt"; //NON-NLS
    private final static String MODULE_NAME = "Logical Imager"; //NON-NLS
    private final String deviceId;
    private final String timeZone;
    private final File src;
    private final File dest;
    private final DataSourceProcessorCallback callback;
    private final DataSourceProcessorProgressMonitor progressMonitor;
    private final Blackboard blackboard;
    private final Case currentCase;
    
    private volatile boolean cancelled;

    AddLogicalImageTask(String deviceId,
            String timeZone,
            File src, File dest,
            DataSourceProcessorProgressMonitor progressMonitor,
            DataSourceProcessorCallback callback
    ) throws NoCurrentCaseException {
        this.deviceId = deviceId;
        this.timeZone = timeZone;
        this.src = src;
        this.dest = dest;
        this.progressMonitor = progressMonitor;
        this.callback = callback;
        this.currentCase = Case.getCurrentCase();
        this.blackboard = this.currentCase.getServices().getArtifactsBlackboard();
    }

    /**
     * Add SearchResults.txt and users.txt to the case
     * report Adds the image to the case database.
     */
    @Messages({
        "# {0} - src", "# {1} - dest", "AddLogicalImageTask.copyingImageFromTo=Copying image from {0} to {1}",
        "AddLogicalImageTask.doneCopying=Done copying",
        "# {0} - src", "# {1} - dest", "AddLogicalImageTask.failedToCopyDirectory=Failed to copy directory {0} to {1}",
        "# {0} - file", "AddLogicalImageTask.addingToReport=Adding {0} to report",
        "# {0} - file", "AddLogicalImageTask.doneAddingToReport=Done adding {0} to report",
        "AddLogicalImageTask.ingestionCancelled=Ingestion cancelled",
        "# {0} - file", "AddLogicalImageTask.failToGetCanonicalPath=Fail to get canonical path for {0}",
        "# {0} - sparseImageDirectory", "AddLogicalImageTask.directoryDoesNotContainSparseImage=Directory {0} does not contain any images",
        "AddLogicalImageTask.noCurrentCase=No current case",
        "AddLogicalImageTask.addingInterestingFiles=Adding search results as interesting files",
        "AddLogicalImageTask.doneAddingInterestingFiles=Done adding search results as interesting files",
        "# {0} - SearchResults.txt", "# {1} - directory", "AddLogicalImageTask.cannotFindFiles=Cannot find {0} in {1}",
        "# {0} - reason", "AddLogicalImageTask.failedToAddInterestingFiles=Failed to add interesting files: {0}"
    })
    @Override
    public void run() {
        List<String> errorList = new ArrayList<>();
        List<Content> emptyDataSources = new ArrayList<>();

        try {
            progressMonitor.setProgressText(Bundle.AddLogicalImageTask_copyingImageFromTo(src.toString(), dest.toString()));
            FileUtils.copyDirectory(src, dest);
            progressMonitor.setProgressText(Bundle.AddLogicalImageTask_doneCopying());
        } catch (IOException ex) {
            // Copy directory failed
            String msg = Bundle.AddLogicalImageTask_failedToCopyDirectory(src.toString(), dest.toString());
            errorList.add(msg);
        }

        // Add the SearchResults.txt and users.txt to the case report
        String resultsFilename;
        if (Paths.get(dest.toString(), SEARCH_RESULTS_TXT).toFile().exists()) {
            resultsFilename = SEARCH_RESULTS_TXT;
        } else {
            errorList.add(Bundle.AddLogicalImageTask_cannotFindFiles(SEARCH_RESULTS_TXT, dest.toString()));
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
            return;
        }

        if (cancelled) {
            return;
        }

        progressMonitor.setProgressText(Bundle.AddLogicalImageTask_addingToReport(resultsFilename));
        String status = addReport(Paths.get(dest.toString(), resultsFilename), resultsFilename + " " + src.getName());
        if (status != null) {
            errorList.add(status);
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
            return;
        }
        progressMonitor.setProgressText(Bundle.AddLogicalImageTask_doneAddingToReport(resultsFilename));

        progressMonitor.setProgressText(Bundle.AddLogicalImageTask_addingToReport(USERS_TXT));
        status = addReport(Paths.get(dest.toString(), USERS_TXT), USERS_TXT + " " + src.getName());
        if (status != null) {
            errorList.add(status);
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
            return;
        }
        progressMonitor.setProgressText(Bundle.AddLogicalImageTask_doneAddingToReport(USERS_TXT));

        // Get all VHD files in the dest directory
        List<String> imagePaths = new ArrayList<>();
        for (File f : dest.listFiles()) {
            if (f.getName().endsWith(".vhd")) {
                try {
                    imagePaths.add(f.getCanonicalPath());
                } catch (IOException ioe) {
                    String msg = Bundle.AddLogicalImageTask_failToGetCanonicalPath(f.getName());
                    errorList.add(msg);
                    callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
                    return;
                }
            }
        }

        AddMultipleImageTask addMultipleImageTask = null;
        List<Content> newDataSources = new ArrayList<>();
        boolean createVHD;
        
        if (imagePaths.isEmpty()) {
            createVHD = false;
            // No VHD in src directory, try ingest directories using Logical File Set
            String[] directories = dest.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return Paths.get(dir.toString(), name).toFile().isDirectory();
                }
            });
//            for (String dir : directories) {
                imagePaths.add(Paths.get(dest.toString(), "root").toFile().getAbsolutePath());
//            }
            if (imagePaths.isEmpty()) {
                String msg = Bundle.AddLogicalImageTask_directoryDoesNotContainSparseImage(dest);
                errorList.add(msg);
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
                return;
            }

            // ingest the directories
            FileManager fileManager = Case.getCurrentCase().getServices().getFileManager();
            try {
                LocalFilesDataSource newDataSource = fileManager.addLocalFilesDataSource(deviceId, "", "", imagePaths, new ProgressUpdater());
                newDataSources.add(newDataSource);
            } catch (TskCoreException | TskDataException ex) {
                errorList.add(ex.getMessage());
                LOGGER.log(Level.SEVERE, String.format("Failed to add datasource: %s", ex.getMessage()), ex); // NON-NLS
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
            }

        } else {
            createVHD = true;
            
            // ingest the VHDs
            try {
                addMultipleImageTask = new AddMultipleImageTask(deviceId, imagePaths, timeZone , progressMonitor, callback);
                addMultipleImageTask.run();

                if (addMultipleImageTask.getResult() == DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS) {
                    callback.done(addMultipleImageTask.getResult(), addMultipleImageTask.getErrorMessages(), addMultipleImageTask.getNewDataSources());
                    return;
                }

            } catch (NoCurrentCaseException ex) {
                String msg = Bundle.AddLogicalImageTask_noCurrentCase();
                errorList.add(msg);
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.CRITICAL_ERRORS, errorList, emptyDataSources);
            }
        }

        try {
            progressMonitor.setProgressText(Bundle.AddLogicalImageTask_addingInterestingFiles());
            addInterestingFiles(dest, Paths.get(dest.toString(), resultsFilename), createVHD);
            progressMonitor.setProgressText(Bundle.AddLogicalImageTask_doneAddingInterestingFiles());
            if (addMultipleImageTask != null) {
                callback.done(addMultipleImageTask.getResult(), addMultipleImageTask.getErrorMessages(), addMultipleImageTask.getNewDataSources());
            } else {
                callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.NO_ERRORS, errorList, newDataSources);
            }
        } catch (IOException | TskCoreException ex) {
            errorList.add(Bundle.AddLogicalImageTask_failedToAddInterestingFiles(ex.getMessage()));
            LOGGER.log(Level.SEVERE, "Failed to add interesting files", ex); // NON-NLS
            callback.done(DataSourceProcessorCallback.DataSourceProcessorResult.NONCRITICAL_ERRORS, errorList, emptyDataSources);
        }
    }

    /**
     * Add a file specified by the reportPath to the case report.
     *
     * @param reportPath Path to the report to be added
     * @param reportName Name associated the report
     *
     * @returns null if success, or exception message if failure
     *
     */
    @Messages({
        "# {0} - file", "# {1} - exception message", "AddLogicalImageTask.failedToAddReport=Failed to add report {0}. Reason= {1}"
    })
    private String addReport(Path reportPath, String reportName) {
        if (!reportPath.toFile().exists()) {
            return null; // if the reportPath doesn't exist, just ignore it.
        }
        try {
            Case.getCurrentCase().addReport(reportPath.toString(), "LogicalImager", reportName); //NON-NLS
            return null;
        } catch (TskCoreException ex) {
            String msg = Bundle.AddLogicalImageTask_failedToAddReport(reportPath.toString(), ex.getMessage());
            LOGGER.log(Level.SEVERE, String.format("Failed to add report %s. Reason= %s", reportPath.toString(), ex.getMessage()), ex); // NON-NLS
            return msg;
        }
    }

    /**
     * Attempts to cancel the processing of the input image files. May result in
     * partial processing of the input.
     */
    void cancelTask() {
        LOGGER.log(Level.WARNING, "AddLogicalImageTask cancelled, processing may be incomplete"); // NON-NLS
        cancelled = true;
    }

    private Map<String, Long> imagePathsToDataSourceObjId(Map<Long, List<String>> imagePaths) {
        Map<String, Long> imagePathToObjIdMap = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : imagePaths.entrySet()) {
            Long key = entry.getKey();
            List<String> names = entry.getValue();
            for (String name : names) {
                imagePathToObjIdMap.put(name, key);
            }
        }
        return imagePathToObjIdMap;
    }

    @Messages({
        "# {0} - line number", "# {1} - fields length", "# {2} - expected length", "AddLogicalImageTask.notEnoughFields=File does not contain enough fields at line {0}, got {1}, expecting {2}",
        "# {0} - target image path", "AddLogicalImageTask.cannotFindDataSourceObjId=Cannot find obj_id in tsk_image_names for {0}"
    })
    private void addInterestingFiles(File src, Path resultsPath, boolean createVHD) throws IOException, TskCoreException {
        Map<Long, List<String>> imagePaths = currentCase.getSleuthkitCase().getImagePaths();
        Map<String, Long> imagePathToObjIdMap = imagePathsToDataSourceObjId(imagePaths);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                      new FileInputStream(resultsPath.toFile()), "UTF8"))) { // NON-NLS
            String line;
            br.readLine(); // skip the header line
            int lineNumber = 2;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\t", -1); // NON-NLS
                if (fields.length != 9) {
                    throw new IOException(Bundle.AddLogicalImageTask_notEnoughFields(lineNumber, fields.length, 9));
                }
                String vhdFilename = fields[0];
//                String fileSystemOffsetStr = fields[1];
                String fileMetaAddressStr = fields[2];
//                String extractStatusStr = fields[3];
                String ruleSetName = fields[4];
                String ruleName = fields[5];
//                String description = fields[6];
                String filename = fields[7];

                String query;
                String targetImagePath;
                if (createVHD) {
                    targetImagePath = Paths.get(src.toString(), vhdFilename).toString();
                    Long dataSourceObjId = imagePathToObjIdMap.get(targetImagePath);
                    if (dataSourceObjId == null) {
                        throw new TskCoreException(Bundle.AddLogicalImageTask_cannotFindDataSourceObjId(targetImagePath));
                    }
                    query = String.format("data_source_obj_id = '%s' AND meta_addr = '%s' AND name = '%s'", // NON-NLS
                        dataSourceObjId.toString(), fileMetaAddressStr, filename.replace("'", "''"));
                } else {
                    String parentPath = fields[8];
                    targetImagePath = Paths.get("root", vhdFilename).toString();
                    String tmpRootPath = targetImagePath.replace(".vhd", "").replace("\\", "/");
                    String searchParentPath = "/" + tmpRootPath + "/" + parentPath;
                    query =  String.format("name = '%s' AND parent_path = '%s'", // NON-NLS
                        filename.replace("'", "''"), searchParentPath.replace("'", "''"));
                }

                List<AbstractFile> matchedFiles = Case.getCurrentCase().getSleuthkitCase().findAllFilesWhere(query);
                for (AbstractFile file : matchedFiles) {
                    addInterestingFile(file, ruleSetName, ruleName);
                }
                lineNumber++;
            }
        }
//        IngestServices.getInstance().fireModuleDataEvent(new ModuleDataEvent(MODULE_NAME,
//                BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT));
    }

    private void addInterestingFile(AbstractFile file, String ruleSetName, String ruleName) throws TskCoreException {
        Collection<BlackboardAttribute> attributes = new ArrayList<>();
        BlackboardAttribute setNameAttribute = new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_SET_NAME, MODULE_NAME, ruleSetName);
        attributes.add(setNameAttribute);
        BlackboardAttribute ruleNameAttribute = new BlackboardAttribute(BlackboardAttribute.ATTRIBUTE_TYPE.TSK_CATEGORY, MODULE_NAME, ruleName);
        attributes.add(ruleNameAttribute);
        Blackboard tskBlackboard = Case.getCurrentCase().getSleuthkitCase().getBlackboard();
        if (!tskBlackboard.artifactExists(file, BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT, attributes)) {
            BlackboardArtifact artifact = file.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_INTERESTING_FILE_HIT);
            artifact.addAttributes(attributes);
            try {
                // index the artifact for keyword search
                blackboard.postArtifact(artifact, MODULE_NAME);
            } catch (Blackboard.BlackboardException ex) {
                LOGGER.log(Level.SEVERE, "Unable to index blackboard artifact " + artifact.getArtifactID(), ex); //NON-NLS
            }
        }
    }

    /**
     * Updates task progress as the file manager adds the local/logical files
     * and/or directories to the case database.
     */
    @Messages({
        "# {0} - parent path", "# {1} - filename", "AddLogicalImageTask.localFileAddProgress=Adding: {0}/{1}",
    })    
    private class ProgressUpdater implements FileManager.FileAddProgressUpdater {

        private int count;

        /**
         * Updates task progress (called by the file manager after it adds each
         * local file/directory to the case database).
         */
        @Override
        public void fileAdded(final AbstractFile file) {
            ++count;
            if (count % 10 == 0) {
                progressMonitor.setProgressText(
                    Bundle.AddLogicalImageTask_localFileAddProgress(
                        file.getParentPath(),
                        file.getName()
                    )
                );
            }
        }
    }
}
