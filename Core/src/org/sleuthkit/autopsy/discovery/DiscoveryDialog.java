/*
 * Autopsy
 *
 * Copyright 2020 Basis Technology Corp.
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
package org.sleuthkit.autopsy.discovery;

import static java.awt.BorderLayout.CENTER;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.sleuthkit.autopsy.centralrepository.datamodel.CentralRepoException;
import org.sleuthkit.autopsy.centralrepository.datamodel.CentralRepository;
import org.sleuthkit.autopsy.coreutils.Logger;

final class DiscoveryDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private final static Logger logger = Logger.getLogger(DiscoveryDialog.class.getName());
    private final ImageFilterPanel imageFilterPanel = new ImageFilterPanel();
    private final VideoFilterPanel videoFilterPanel = new VideoFilterPanel();
    private final DocumentFilterPanel documentFilterPanel = new DocumentFilterPanel();
    private static final Color SELECTED_COLOR = new Color(216, 230, 242);
    private static final Color UNSELECTED_COLOR = new Color(240, 240, 240);
    private SearchWorker searchWorker = null;
    private static DiscoveryDialog discoveryDialog;
    private FileSearchData.FileType fileType = FileSearchData.FileType.IMAGE;

    private DiscoveryDialog() {
        this(null, true);
    }

    public static synchronized DiscoveryDialog getDiscoveryDialogInstance() {
        if (discoveryDialog == null) {
            discoveryDialog = new DiscoveryDialog();
        }
        return discoveryDialog;
    }

    /**
     * Creates new form DiscoveryDialog
     */
    private DiscoveryDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() instanceof String) {
                    String errorMessage = (String) evt.getNewValue();
                    if (StringUtils.isBlank(errorMessage)) {
                        setValid();
                    } else {
                        setInvalid(errorMessage);
                    }
                }

            }
        };
        imageFilterPanel.addPropertyChangeListener(listener);
        videoFilterPanel.addPropertyChangeListener(listener);
        documentFilterPanel.addPropertyChangeListener(listener);
        updateSearchSettings();

    }

    /**
     * Update the search settings to a default state.
     */
    private void updateSearchSettings() {
        imagesButton.setSelected(true);
        imagesButton.setEnabled(false);
        imagesButton.setBackground(SELECTED_COLOR);
        imagesButton.setForeground(Color.BLACK);
        videosButton.setSelected(false);
        videosButton.setEnabled(true);
        videosButton.setBackground(UNSELECTED_COLOR);
        documentsButton.setSelected(false);
        documentsButton.setEnabled(true);
        documentsButton.setBackground(UNSELECTED_COLOR);
        fileType = FileSearchData.FileType.IMAGE;
        remove(imageFilterPanel);
        remove(videoFilterPanel);
        remove(documentFilterPanel);
        add(imageFilterPanel, CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JPanel toolBarPanel = new javax.swing.JPanel();
        imagesButton = new javax.swing.JButton();
        videosButton = new javax.swing.JButton();
        documentsButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        searchButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        javax.swing.JButton cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        imagesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/pictures-icon.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(imagesButton, org.openide.util.NbBundle.getMessage(DiscoveryDialog.class, "DiscoveryDialog.imagesButton.text")); // NOI18N
        imagesButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/pictures-icon.png"))); // NOI18N
        imagesButton.setFocusable(false);
        imagesButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        imagesButton.setMaximumSize(new java.awt.Dimension(90, 43));
        imagesButton.setMinimumSize(new java.awt.Dimension(90, 43));
        imagesButton.setPreferredSize(new java.awt.Dimension(90, 43));
        imagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imagesButtonActionPerformed(evt);
            }
        });

        videosButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/video-icon.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(videosButton, org.openide.util.NbBundle.getMessage(DiscoveryDialog.class, "DiscoveryDialog.videosButton.text")); // NOI18N
        videosButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/video-icon.png"))); // NOI18N
        videosButton.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/video-icon.png"))); // NOI18N
        videosButton.setFocusable(false);
        videosButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        videosButton.setMaximumSize(new java.awt.Dimension(90, 43));
        videosButton.setMinimumSize(new java.awt.Dimension(90, 43));
        videosButton.setPreferredSize(new java.awt.Dimension(90, 43));
        videosButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videosButtonActionPerformed(evt);
            }
        });

        documentsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/documents-icon.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(documentsButton, org.openide.util.NbBundle.getMessage(DiscoveryDialog.class, "DiscoveryDialog.documentsButton.text")); // NOI18N
        documentsButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/documents-icon.png"))); // NOI18N
        documentsButton.setDisabledSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/sleuthkit/autopsy/images/documents-icon.png"))); // NOI18N
        documentsButton.setFocusable(false);
        documentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                documentsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolBarPanelLayout = new javax.swing.GroupLayout(toolBarPanel);
        toolBarPanel.setLayout(toolBarPanelLayout);
        toolBarPanelLayout.setHorizontalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolBarPanelLayout.createSequentialGroup()
                .addContainerGap(98, Short.MAX_VALUE)
                .addComponent(imagesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(videosButton, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(documentsButton)
                .addContainerGap(87, Short.MAX_VALUE))
        );
        toolBarPanelLayout.setVerticalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolBarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(videosButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(imagesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(documentsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        getContentPane().add(toolBarPanel, java.awt.BorderLayout.PAGE_START);

        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(DiscoveryDialog.class, "DiscoveryDialog.searchButton.text")); // NOI18N
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(DiscoveryDialog.class, "DiscoveryDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(searchButton)))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void imagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imagesButtonActionPerformed
//        resetTopComponent();
        remove(videoFilterPanel);
        remove(documentFilterPanel);
        add(imageFilterPanel, CENTER);
        imagesButton.setSelected(true);
        imagesButton.setEnabled(false);
        imagesButton.setBackground(SELECTED_COLOR);
        imagesButton.setForeground(Color.BLACK);
        videosButton.setSelected(false);
        videosButton.setEnabled(true);
        videosButton.setBackground(UNSELECTED_COLOR);
        documentsButton.setSelected(false);
        documentsButton.setEnabled(true);
        documentsButton.setBackground(UNSELECTED_COLOR);
        fileType = FileSearchData.FileType.IMAGE;
    }//GEN-LAST:event_imagesButtonActionPerformed

    private void videosButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videosButtonActionPerformed
        remove(imageFilterPanel);
        remove(documentFilterPanel);
        add(videoFilterPanel, CENTER);
        imagesButton.setSelected(false);
        imagesButton.setEnabled(true);
        imagesButton.setBackground(UNSELECTED_COLOR);
        videosButton.setSelected(true);
        videosButton.setEnabled(false);
        videosButton.setBackground(SELECTED_COLOR);
        videosButton.setForeground(Color.BLACK);
        documentsButton.setSelected(false);
        documentsButton.setEnabled(true);
        documentsButton.setBackground(UNSELECTED_COLOR);
        fileType = FileSearchData.FileType.VIDEO;
    }//GEN-LAST:event_videosButtonActionPerformed

    private void documentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_documentsButtonActionPerformed
        remove(imageFilterPanel);
        remove(documentFilterPanel);
        add(documentFilterPanel, CENTER);
        documentsButton.setSelected(true);
        documentsButton.setEnabled(false);
        documentsButton.setBackground(SELECTED_COLOR);
        documentsButton.setForeground(Color.BLACK);
        videosButton.setSelected(false);
        videosButton.setEnabled(true);
        videosButton.setBackground(UNSELECTED_COLOR);
        imagesButton.setSelected(false);
        imagesButton.setEnabled(true);
        imagesButton.setBackground(UNSELECTED_COLOR);
        fileType = FileSearchData.FileType.DOCUMENTS;
    }//GEN-LAST:event_documentsButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        // Get the selected filters
        final DiscoveryTopComponent tc = DiscoveryTopComponent.getTopComponent();
        if (tc == null) {
            setInvalid("No Top Component Found");
            return;
        }

        if (tc.isOpened() == false) {
            tc.open();
        }
        tc.resetTopComponent();
        List<FileSearchFiltering.FileFilter> filters;
        if (videosButton.isSelected()) {
            filters = videoFilterPanel.getFilters();
        } else if (documentsButton.isSelected()) {
            filters = documentFilterPanel.getFilters();
        } else {
            filters = imageFilterPanel.getFilters();
        }
        DiscoveryEventUtils.getDiscoveryEventBus().post(new DiscoveryEventUtils.SearchStartedEvent(fileType));

        // Get the grouping attribute and group sorting method
        FileSearch.AttributeType groupingAttr = FileSearch.GroupingAttributeType.FILE_SIZE.getAttributeType();
        FileGroup.GroupSortingAlgorithm groupSortAlgorithm = FileGroup.GroupSortingAlgorithm.BY_GROUP_NAME;

        // Get the file sorting method
        FileSorter.SortingMethod fileSort = FileSorter.SortingMethod.BY_FILE_NAME;
        CentralRepository centralRepoDb = null;
        if (CentralRepository.isEnabled()) {
            try {
                centralRepoDb = CentralRepository.getInstance();
            } catch (CentralRepoException ex) {
                centralRepoDb = null;
                logger.log(Level.SEVERE, "Error loading central repository database, no central repository options will be available for File Discovery", ex);
            }
        }
        searchWorker = new SearchWorker(centralRepoDb, filters, groupingAttr, groupSortAlgorithm, fileSort);
        searchWorker.execute();
        dispose();
        tc.toFront();
        tc.requestActive();
    }//GEN-LAST:event_searchButtonActionPerformed


    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    void cancelSearch() {
        if (searchWorker != null) {
            searchWorker.cancel(true);
        }
    }

    /**
     * The settings are valid so enable the Search button
     */
    private void setValid() {
        errorLabel.setText("");
        searchButton.setEnabled(true);
    }

    /**
     * The settings are not valid so disable the search button and display the
     * given error message.
     *
     * @param error
     */
    private void setInvalid(String error) {
        errorLabel.setText(error);
        searchButton.setEnabled(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton documentsButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton imagesButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton videosButton;
    // End of variables declaration//GEN-END:variables
}
