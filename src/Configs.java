package gui;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Configs {
    private static Configs cfgInstance;

    private Document configDocument;

    private Boolean autoSelect = true;

    private Configs() {
        try {
            File xmlFile = new File("lib/params.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            configDocument = builder.parse(xmlFile);
            configDocument.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Configs getCfgInstance() {
        if (cfgInstance == null) {
            synchronized (Configs.class) {  // Ensure thread safety
                if (cfgInstance == null) {
                    cfgInstance = new Configs();  // Lazy initialization
                }
            }
        }
        return cfgInstance;
    }
    
    // Method to get a map of EPC values to sensor names
    public Map<String, String> getEpcSensorMap() {
        Map<String, String> epcSensorMap = new HashMap<>();

        try {
            // Get all <sensor> elements from the 'params.xml' document
            NodeList sensorList = configDocument.getElementsByTagName("sensor");
            
            // Loop through each <sensor> element
            for (int i = 0; i < sensorList.getLength(); i++) {
                Node sensorNode = sensorList.item(i);
                
                if (sensorNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element sensorElement = (Element) sensorNode;
                    
                    // Get the 'name' attribute of the <sensor>
                    String sensorName = sensorElement.getAttribute("name");
                    
                    // Get all <epc> elements under this <sensor>
                    NodeList epcList = sensorElement.getElementsByTagName("epc");
                    
                    // Loop through each <epc> element and add it to the map
                    for (int j = 0; j < epcList.getLength(); j++) {
                        String epcValue = epcList.item(j).getTextContent().trim();
                        epcSensorMap.put(epcValue, sensorName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return epcSensorMap;
    }
    
    public Map<String, Object> getSensorConfig(String sensorName) {
        Map<String, Object> sensorConfig = new HashMap<>();
        NodeList sensorNodes = configDocument.getElementsByTagName("sensor");
        
        for (int i = 0; i < sensorNodes.getLength(); i++) {
            Element sensor = (Element) sensorNodes.item(i);
            if (sensor.getAttribute("name").equals(sensorName)) {
                // Extract EPCs
                List<String> epcs = new ArrayList<>();
                NodeList epcNodes = sensor.getElementsByTagName("epc");
                for (int j = 0; j < epcNodes.getLength(); j++) {
                    epcs.add(epcNodes.item(j).getTextContent());
                }
                sensorConfig.put("epcs", epcs);

                // Extract classification
                Map<String, Integer> classification = new HashMap<>();
                NodeList classNodes = sensor.getElementsByTagName("classification").item(0).getChildNodes();
                for (int j = 0; j < classNodes.getLength(); j++) {
                    if (classNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        Element classElem = (Element) classNodes.item(j);
                        classification.put(classElem.getTagName(), Integer.parseInt(classElem.getTextContent()));
                    }
                }
                sensorConfig.put("classification", classification);
                
                // Extract window and y_range
                sensorConfig.put("window", Double.parseDouble(sensor.getElementsByTagName("window").item(0).getTextContent()));
                sensorConfig.put("y_range", Integer.parseInt(sensor.getElementsByTagName("y_range").item(0).getTextContent()));
                
                break;
            }
        }
        return sensorConfig;
    }
    
    public void setSensorDef(String sensor) {
        configDocument.getElementsByTagName("sensor_def").item(0).setTextContent(sensor);
    }
    
    public String getSensorDef() {
        return configDocument.getElementsByTagName("sensor_def").item(0).getTextContent();
    }

    public List<String> getAllSensorNames() {
        List<String> sensorNames = new ArrayList<>();
        
        try {
            // Get all <sensor> elements from the 'params.xml' document
            NodeList sensorList = configDocument.getElementsByTagName("sensor");
            
            // Loop through each <sensor> element
            for (int i = 0; i < sensorList.getLength(); i++) {
                Node sensorNode = sensorList.item(i);
                
                if (sensorNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element sensorElement = (Element) sensorNode;
                    
                    // Get the 'name' attribute of the <sensor> and add it to the list
                    String sensorName = sensorElement.getAttribute("name");
                    sensorNames.add(sensorName);
                }
            }
            sensorNames.add("auto");
        } catch (Exception e) {
            return new ArrayList<>();
        }
        
        return sensorNames;
    }
    
    public int getReadRate() {
        return Integer.parseInt(configDocument.getElementsByTagName("read_rate").item(0).getTextContent());
    }
    
    public int getMaxTagHistory() {
        return Integer.parseInt(configDocument.getElementsByTagName("max_tag_history").item(0).getTextContent());
    }
    
    public String getImpinjHostIP() {
        return configDocument.getElementsByTagName("host_ip").item(0).getTextContent();
    }
    
    public int getImpinjHostPort() {
        return Integer.parseInt(configDocument.getElementsByTagName("host_port").item(0).getTextContent());
    }
    
    public boolean isStoreData() {
        return Boolean.parseBoolean(configDocument.getElementsByTagName("store_data").item(0).getTextContent());
    }
    
    public boolean isDtw() {
        return Boolean.parseBoolean(configDocument.getElementsByTagName("is_dtw").item(0).getTextContent());
    }
    
    public String getRepoName() {
        return configDocument.getElementsByTagName("repo_name").item(0).getTextContent();
    }
    
    public String getProjectName() {
        return configDocument.getElementsByTagName("project").item(0).getTextContent();
    }
    
    public Map<String, Object> getReaderConfig() {
        Map<String, Object> readerConfig = new HashMap<>();
        Element reader = (Element) configDocument.getElementsByTagName("reader").item(0);
        readerConfig.put("antenna", Integer.parseInt(reader.getElementsByTagName("antenna").item(0).getTextContent()));
        readerConfig.put("rf_mode", Integer.parseInt(reader.getElementsByTagName("rf_mode").item(0).getTextContent()));
        readerConfig.put("session", Integer.parseInt(reader.getElementsByTagName("session").item(0).getTextContent()));
        readerConfig.put("tagPopulation", Integer.parseInt(reader.getElementsByTagName("tagPopulation").item(0).getTextContent()));
        return readerConfig;
    }
    
    public Map<String, Boolean> getReportConfig() {
        Map<String, Boolean> reportConfig = new HashMap<>();
        Element report = (Element) configDocument.getElementsByTagName("report").item(0);
        reportConfig.put("channel", Boolean.parseBoolean(report.getElementsByTagName("channel").item(0).getTextContent()));
        reportConfig.put("rssi", Boolean.parseBoolean(report.getElementsByTagName("rssi").item(0).getTextContent()));
        reportConfig.put("timestamp", Boolean.parseBoolean(report.getElementsByTagName("timestamp").item(0).getTextContent()));
        reportConfig.put("count", Boolean.parseBoolean(report.getElementsByTagName("count").item(0).getTextContent()));
        reportConfig.put("phase", Boolean.parseBoolean(report.getElementsByTagName("phase").item(0).getTextContent()));
        return reportConfig;
    }
    
    public Boolean getAutoSelect() {
        return this.autoSelect;
    }

    public void setAutoSelect(Boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    public static String getLocalParentPath(String targetDirName) {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        Path currentPath = Paths.get(currentDir);

        // We iterate through the Path, and store each directory until we hit the target name
        Path pathUpToTarget = Paths.get(currentPath.getRoot().toString());  // Initialize with root of the path

        // Add path components to the list in order to preserve path hierarchy
        for (Path pathPart : currentPath) {
            if(pathPart.toString().equals(currentPath.getRoot().toString())){
                continue; // Skip if root
            }

            pathUpToTarget = pathUpToTarget.resolve(pathPart);
            if (pathPart.toString().equals(targetDirName)) {
                return pathUpToTarget.toString();
            }
        }
      
        return null; // Return null if target is not found
    }
}
