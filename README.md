# SenSync
Code for interfacing Impinj Readers with Compute and GUI for displaying the sensing values

* Contains the source code for SenSync, an independent platform for real-time batteryless, wireless RFID based sensing
* Base work for the hardware can be referred to at: [ZenseTag](https://dl.acm.org/doi/10.1145/3666025.3699342)

## How to Compile and Run the Java Programs
**Ensure Java dependencies are available**
**Make sure that the current working directory is the repo parent and the right branch is selected** 
   ```bash
   SenSync (main) > pwd
   \home\Project\SenSync
   ```
   ```
   SenSync (main) > java --version
   java 22.0.2 2024-07-16
   Java(TM) SE Runtime Environment (build 22.0.2+9-70)
   Java HotSpot(TM) 64-Bit Server VM (build 22.0.2+9-70, mixed mode, sharing)
   ```
* For GUI:
   ```
   javac -cp "./lib/fastdtw.jar;./lib/octane.jar;./lib/jfreechart.jar;./lib/flatlaf.jar;./lib/websocket.jar;./lib/sl4j.jar" src/*.java
   java -cp "./lib/fastdtw.jar;./lib/octane.jar;./lib/jfreechart.jar;./lib/flatlaf.jar;./lib/websocket.jar;./lib/sl4j.jar;src" RealTimeGui
   ```

## Sensor Configuration File

This XML configuration file defines settings for a sensor-based system using RFID technology. The file includes sensor-specific configurations, system-wide parameters, and reader settings.

## XML Structure

### `<configurations>`
The root element that encapsulates all configurations.

### `<sensor_configs>`
Defines multiple sensors and their associated EPC (Electronic Product Code) tags.

#### `<sensor name="stub">`
- Contains a list of EPCs associated with the sensor.
- EPCs:
  - `ADDAFB63AC1F3841EC880467`
  - `ADDA1B63AC1F3841EC880467`

#### `<sensor name="soil">`
- Contains a list of EPCs (some commented out).
- Additional parameters:
  - `<window>`: `5` (measurement window size)
  - `<y_range>`: `75` (y-axis range for data representation)

#### `<sensor name="force">`
- Contains a list of EPCs (some commented out).
- Additional parameters:
  - `<window>`: `4`
  - `<y_range>`: `90`

#### `<sensor name="photo">`
- Contains a list of EPCs (some commented out).
- Additional parameters:
  - `<window>`: `2`
  - `<y_range>`: `45`

### Other Configuration Parameters

- `<sensor_def>`: Specifies a test mode (`test`).
- `<is_dtw>`: Determines if Dynamic Time Warping (DTW) is enabled (`true`).
- `<project>`: Name of the project (`zensetag`).
- `<read_rate>`: Defines the read rate (`700`).
- `<max_tag_history>`: Maximum number of historical tag records stored (`20000`).

### `<impinj>`
Defines the RFID reader's network settings.
- `<host_ip>`: `169.254.34.180`
- `<host_port>`: `5084`

### `<repo_name>`
- Name of the repository managing the project (`SenSync`).

### `<antenna_reader_configs>`
Configures the antenna reader.
- `<reader>`: Defines reader-specific parameters.
  - `<antenna>`: `0` (antenna index)
  - `<rf_mode>`: `0` (radio frequency mode)
  - `<session>`: `0` (reader session mode)
  - `<tagPopulation>`: `2` (expected tag population)
  
- `<report>`: Specifies which parameters to report.
  - `<channel>`: `true`
  - `<rssi>`: `true`
  - `<timestamp>`: `true`
  - `<count>`: `false`
  - `<phase>`: `true`

## Dependencies
- The system requires an **Impinj RFID reader** for tag detection.
- The **EPC values** must match those assigned to the physical RFID tags.
- The **network configuration** (IP and port) must be properly set to connect with the reader.
- The **Dynamic Time Warping (DTW) algorithm** is enabled, requiring computational resources.
- Data processing and visualization may rely on external software, depending on how this configuration is used.

## Notes
- EPCs marked as comments are currently disabled but can be re-enabled if needed.
- DTW processing is enabled (`true`), which may impact computational complexity.
- Ensure that the RFID reader's IP address and port match the hardware configuration.

This configuration file is essential for defining the behavior of the system and should be modified carefully to suit specific project requirements.
