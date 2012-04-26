/*
 * Autopsy Forensic Browser
 * 
 * Copyright 2012 Basis Technology Corp.
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

package org.sleuthkit.autopsy.ingest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;


/**
 * Monitor health of the system and stop ingest if necessary
 */
public class IngestMonitor {
    private static final int INITIAL_INTERVAL_MS = 60000; //1 min.
    
    private static final Logger logger = Logger.getLogger(IngestMonitor.class.getName());
    
    private Timer timer;
    
    /**
     * Start the monitor
     */
    void start() {
        timer = new Timer(INITIAL_INTERVAL_MS, new MonitorAction());
        timer.start();
    }
    
    /**
     * Stop the monitor
     */
    void stop() {
        if (timer != null)
            timer.stop();
    }
    
    /**
     * Check if the monitor is running
     * @return true if the monitor is running, false otherwise
     */
    boolean isRunning() {
        return timer != null && timer.isRunning();
    }
    
    private class MonitorAction implements ActionListener {
        private final static long MIN_FREE_DISK_SPACE = 100L * 1024 * 1024; //100MB

        @Override
        public void actionPerformed(ActionEvent e) {
            final IngestManager manager = IngestManager.getDefault();
            
            //runs checks only if ingest is running
            if (manager.isIngestRunning() == false)
                return;
            
            if (checkDiskSpace() == false) {
                //stop ingest if running
                logger.log(Level.SEVERE, "Stopping ingest due to low disk space");
                manager.stopAll();
                manager.postMessage(IngestMessage.createManagerMessage("Stopping ingest due to low disk space", "Stopping ingest due to low disk space. Please ensure the system disk has at least 1GB free space (more for large images) and restart ingest."));
            }
        }
        
        /**
         * check disk space
         * @return true if OK, false otherwise
         */
        private boolean checkDiskSpace() {
            //assume root partition
            //TODO use better check, i.e. root partition + user partition + try to write temp file
            File root = new File(File.separator);
            long freeSpace;
            try {
                freeSpace = root.getFreeSpace();
            }
            catch (SecurityException e) {
                logger.log(Level.INFO, "Unable to check for free disk space (probably permission issue)", e);
                return true; //OK
            }
            //logger.log(Level.INFO, "Checking free disk apce: " + freeSpace + " need: " + Long.toString(MIN_FREE_DISK_SPACE));
            return freeSpace > MIN_FREE_DISK_SPACE;
        }
        
    }
    
    
}
