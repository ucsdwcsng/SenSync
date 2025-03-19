package gui;

import com.dtw.FastDTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.EuclideanDistance;
import com.dtw.WarpPath;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jfree.data.json.impl.JSONArray;

public class TagData {

    private final List<Map<String, Object>> tagRecords;
    private final int bufferSize;
    private final int windowSize;
    private Configs config;
    
    private Set<String> epcs;
    private Map<String, Object> sensorConfig;

    private List<Double> phaseDifferences = new ArrayList<>();
    
    // // Non-parameterized constructor (uses default sensorDef from configs)
    // public TagData() {
    //     this(new Configs().getSensorDef());  // Get default sensor from the config and call the parameterized constructor
    // }

    public TagData() {
        this.config = Configs.getCfgInstance();
        
        this.bufferSize = config.getMaxTagHistory();
        
        // Load EPCs from the configuration based on the sensor definition
        refreshSensorConfigs();
        System.out.println(config.getSensorDef() + " with epcs to capture:" + epcs);

        // Load the window size from the config and calculate windowSize = sensor.window * read_rate
        double window = (Double) sensorConfig.get("window");  // Read `window` from the config
        int readRate = config.getReadRate(); // Fetch the read rate from config
        this.windowSize = (int) (window * readRate);  // Calculate windowSize = window * read_rate
        
        this.tagRecords = Collections.synchronizedList(new ArrayList<>());
    }

    // Method to refresh both sensorConfig and EPCs from the configuration
    private void refreshSensorConfigs() {
        String sensorDef = config.getSensorDef();  // Get current sensor definition
        this.sensorConfig = config.getSensorConfig(sensorDef);  // Refresh sensor configuration
        List<String> epcList = (List<String>) sensorConfig.get("epcs");  // Get updated EPC list
        this.epcs = new HashSet<>(epcList);  // Update the EPC set
    }

    // Add a tag to the records
    public void addTag(String epc, String timestamp, double channel, double phase, double rssi) {
        refreshSensorConfigs();
        if (!epcs.contains(epc)) {
            return;
        }

        Map<String, Object> tagRecord = new HashMap<>();
        tagRecord.put("epc", epc);
        tagRecord.put("timestamp", timestamp);  // String timestamp
        tagRecord.put("channel", channel);
        tagRecord.put("phase", phase);
        tagRecord.put("rssi", rssi);
        tagRecords.add(tagRecord);

        // Check if tagRecords exceeds the buffer size, and clear/remove records if necessary
        if (tagRecords.size() > bufferSize) {
            // Clear the oldest record(s) or trim the list to fit the buffer size
            tagRecords.remove(0); // Removes the oldest entry (FIFO)
        }
    }

    // Helper method to convert double[] to TimeSeries
    private TimeSeries createTimeSeries(double[] sequence) {
        return new TimeSeries(sequence);
    }

    // Performs dynamic time warping (DTW) matching between two sequences
    private double[][] dtwMatching(double[] sequence1, double[] sequence2) {
        DistanceFunction distFunc = new EuclideanDistance();
        WarpPath warpPath = FastDTW.getWarpPathBetween(createTimeSeries(sequence1), createTimeSeries(sequence2), windowSize, distFunc);

        double[] warpedSeq1 = new double[warpPath.size()];
        double[] warpedSeq2 = new double[warpPath.size()];

        for (int i = 0; i < warpPath.size(); i++) {
            warpedSeq1[i] = sequence1[warpPath.get(i).getCol()];
            warpedSeq2[i] = sequence2[warpPath.get(i).getRow()];
        }

        return new double[][]{warpedSeq1, warpedSeq2};
    }

    // Calculate the average phase difference across all channels using FastDTW and phase matching
    public double calculateAvgPhaseDifference() {
        // Ensure there are enough records to calculate
        if (tagRecords.size() < 2) {
            return -1000;
        }

        // Get the last windowSize tag records, or all records if fewer are available
        try {
            List<Map<String, Object>> tagRecordsSubset;
            if (tagRecords.size() <= windowSize) {
                tagRecordsSubset = new ArrayList<>(tagRecords);
            } else {
                tagRecordsSubset = tagRecords.subList(tagRecords.size() - windowSize, tagRecords.size());
            }
            // EPC selection
            Iterator<String> epcIterator = epcs.iterator();
            if (epcs.size() < 2) {
                throw new IllegalArgumentException("At least two EPCs are required to calculate phase differences.");
            }
        
            String epc1 = epcIterator.next();
            String epc2 = epcIterator.next();
        
            Map<Double, List<Double>> phasesByChannelEpc1 = new HashMap<>();
            Map<Double, List<Double>> phasesByChannelEpc2 = new HashMap<>();
        
            // Group phases by channels for both EPCs
            for (Map<String, Object> tagRecord : tagRecordsSubset) {
                double channel = (Double) tagRecord.get("channel");
                double phase = (Double) tagRecord.get("phase");
        
                if (epc1.equals(tagRecord.get("epc"))) {
                    phasesByChannelEpc1.computeIfAbsent(channel, k -> new ArrayList<>()).add(phase);
                }
                if (epc2.equals(tagRecord.get("epc"))) {
                    phasesByChannelEpc2.computeIfAbsent(channel, k -> new ArrayList<>()).add(phase);
                }
            }
        
            double totalPhaseDiff = 0.0;
            int totalMatches = 0;
        
            // Iterate over all channels present in both EPCs
            for (double channel : phasesByChannelEpc1.keySet()) {
                if (phasesByChannelEpc2.containsKey(channel)) {
                    List<Double> phaseList1 = phasesByChannelEpc1.get(channel);
                    List<Double> phaseList2 = phasesByChannelEpc2.get(channel);
        
                    // Convert phase lists to arrays
                    double[] phases1 = phaseList1.stream().mapToDouble(Double::doubleValue).toArray();
                    double[] phases2 = phaseList2.stream().mapToDouble(Double::doubleValue).toArray();

                    boolean isDtw = config.isDtw();
                    double[] warpedSeq1 = new double[0];
                    double[] warpedSeq2 = new double[0];
                    // Perform DTW matching between the two phase sequences
                    if (isDtw) {
                        double[][] warpedPhases = dtwMatching(phases1, phases2);
                        warpedSeq1 = warpedPhases[0];
                        warpedSeq2 = warpedPhases[1];
                    }
                    else {
                        int targetLength = Math.min(phases1.length, phases2.length);
                        warpedSeq1 = Arrays.copyOf(phases1, targetLength);
                        warpedSeq2 = Arrays.copyOf(phases2, targetLength);
                    }
        
                    // Calculate phase differences after alignment
                    for (int i = 0; i < warpedSeq1.length; i++) {
                        double diff = Math.abs(warpedSeq1[i] - warpedSeq2[i]);
                        if (diff > 270) {
                            diff = Math.abs(diff - 360);
                        } else if (diff > 135) {
                            diff = Math.abs(diff - 180);
                        }
                        totalPhaseDiff += diff;
                        totalMatches++;
                    }
                }
            }
        
            // Return the average phase difference across all channels
            Double avgPhaseDiff = totalMatches > 0 ? totalPhaseDiff / totalMatches : 0;
            phaseDifferences.add(avgPhaseDiff);

            return avgPhaseDiff;
        } catch (Exception e) {
            // Do nothing
            return -1000;
        }
    }

    // Clear all stored tag records
    public void clearData() {
        tagRecords.clear();
    }

    // Get all tag records
    public List<Map<String, Object>> getAllTags() {
        return tagRecords;
    }

    public void saveAllPhaseDifferences() {
        try {
            if (phaseDifferences.size() > 0 && config.isStoreData()) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String filepath = config.getLocalParentPath(config.getRepoName()) + "/data/" + "phases/";
                String filename = filepath + config.getProjectName() + "_" + config.getSensorDef() + "_" + timestamp + ".json";

                // Create a JSON array from the phaseDifferences list
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(phaseDifferences);
                
                // Write the JSON array to the file
                FileWriter file = new FileWriter(filename);
                file.write(jsonArray.toString());
                file.flush();
                file.close();
                System.out.println("Phase differences saved to " + filename);
            }
        }
        catch (Exception e) {
            // Do nothing
            e.printStackTrace();
        }
    }

    // Get the last N tag records, or all records if fewer are available
    public List<Map<String, Object>> getLastNTags(int N) {
        List<Map<String, Object>> tagRecordsSubset;

        if (tagRecords.size() <= N) {
            tagRecordsSubset = new ArrayList<>(tagRecords);
        } else {
            tagRecordsSubset = tagRecords.subList(tagRecords.size() - N, tagRecords.size());
        }
        return tagRecordsSubset;
    }

    // Method to return the size of tagRecords
    public int getTagRecordsSize() {
        return tagRecords.size();
    }
}
