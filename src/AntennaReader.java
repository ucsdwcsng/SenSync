package gui;

import com.impinj.octane.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AntennaReader {

    private TagData tagData; // TagData object to store tag records
    private ImpinjReader reader;
    private Configs config;

    private Map<String, String> epcSensorMapping;

    public AntennaReader(TagData tagData) {
        this.tagData = tagData;
        this.reader = new ImpinjReader(); // Initialize reader

        // Get the Configs object
        this.config = Configs.getCfgInstance();

        this.epcSensorMapping = config.getEpcSensorMap();
    }

    // Method to start the RFID reader and continuously collect tag data
    public void startReading() {
        try {
            // Use the hostname from the XML config
            String hostname = config.getImpinjHostIP();
            int port = config.getImpinjHostPort(); // Port (if needed)

            // Connect to the reader
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get and configure reader settings
            Settings settings = reader.queryDefaultSettings();

            // Report configurations from XML
            Map<String, Boolean> reportConfig = config.getReportConfig();
            ReportConfig rcfg = settings.getReport();
            rcfg.setIncludeChannel(reportConfig.get("channel"));
            rcfg.setIncludePeakRssi(reportConfig.get("rssi"));
            rcfg.setIncludePhaseAngle(reportConfig.get("phase"));
            rcfg.setIncludeLastSeenTime(reportConfig.get("timestamp"));
            rcfg.setIncludeAntennaPortNumber(reportConfig.get("count"));
            rcfg.setMode(ReportMode.Individual);

            // Apply report configurations
            settings.setReport(rcfg);

            // Reader configurations from XML
            Map<String, Object> readerConfig = config.getReaderConfig();
            if (config.getProjectName().equalsIgnoreCase(config.getRepoName())) {
                settings.setSearchMode(SearchMode.DualTarget);
                settings.setRfMode((Integer) readerConfig.get("rf_mode"));
                settings.setSession((Integer) readerConfig.get("session"));
                settings.setTagPopulationEstimate((Integer) readerConfig.get("tagPopulation"));
            } else {
                settings.setSearchMode(SearchMode.SingleTarget);
                settings.setRfMode(4);
                settings.setSession(0);
                settings.setTagPopulationEstimate(4);
            }


            // Enable antenna #n and disable others
            AntennaConfigGroup acfg = settings.getAntennas();
            // Get the antenna index from the configuration
            int configuredAntenna = (Integer) config.getReaderConfig().get("antenna");
            // Iterate over the first 4 antenna configurations
            for (int i = 0; i < 4; i++) {
                if (config.getProjectName().equalsIgnoreCase(config.getRepoName())) {
                    acfg.getAntennaConfigs().get(i).setEnabled(i == configuredAntenna);
                }
                else {
                    acfg.getAntennaConfigs().get(i).setEnabled(true);
                }
            }

            // Apply the modified settings
            reader.applySettings(settings);

            // Set up a listener to process the tag reports and store them in TagData
            reader.setTagReportListener(new TagReportListenerImplementation());

            // Start reading tags continuously
            System.out.println("Starting the reader");
            reader.start();
            
            // Run the program continuously
            // Times out after 125min
            int duration = 7500;
            Thread.sleep(duration * 1000);
            
            
        } catch (OctaneSdkException | InterruptedException e) {
            System.out.println("Exception: " + e.getMessage());
        } finally {
            stopReader();
        }
    }
    
    // Stop the reader and disconnect
    public void stopReader() {
        try {
            System.out.println("Stopping the reader");
            reader.stop();
            reader.disconnect();
            System.out.println("Disconnected.");
        } catch (OctaneSdkException e) {
            System.out.println("Error while stopping the reader: " + e.getMessage());
        }
    }
    
    // Implementation of the TagReportListener interface
    public class TagReportListenerImplementation implements TagReportListener {
        
        @Override
        public void onTagReported(ImpinjReader reader, TagReport report) {
            for (Tag tag : report.getTags()) {
                // Extract data from the tag
                String epc = tag.getEpc().toString().replaceAll(" ", "");
                String timestamp = tag.getLastSeenTime().toString();
                double phase = Math.toDegrees(tag.getPhaseAngleInRadians());
                double channel = tag.getChannelInMhz();
                double rssi = tag.getPeakRssiInDbm();
                
                // Print the tag data to the console
                // System.out.println("EPC: " + epc + ", Channel: " + channel + " MHz, Phase: " + phase + " at " + rssi + " dB" + " for " + epcSensorMapping.get(epc));
                // System.out.println(config.getAutoSelect() + " " + config.getSensorDef());
                if (rssi > -60) {
                    if (epcSensorMapping.containsKey(epc)) {
                        if (config.getAutoSelect()) {
                            config.setSensorDef(epcSensorMapping.get(epc));
                        }
                        // Add the tag data to the TagData object
                        tagData.addTag(epc, timestamp, channel, phase, rssi);
                    }
                }
                
            }
        }
    }

    // Utility method to print all tags (optional, useful for debugging)
    public static void printAllTags(TagData tagData) {
        List<Map<String, Object>> allTags = tagData.getAllTags();  // Assuming getAllTags() returns a List of Map
        for (Map<String, Object> tagRecord : allTags) {
            System.out.println("Tag Record:");
            for (Map.Entry<String, Object> entry : tagRecord.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("---------------------------");
        }
    }
}
