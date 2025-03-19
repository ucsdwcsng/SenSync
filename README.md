# SenSync
Code for interfacing Impinj Readers with Compute and GUI for displaying the sensing values

* Contains the source code for SenSync, an independent platform for real-time batteryless, wireless RFID based sensing
<!-- * Work can be referred to at: [SenSync](https://dl.acm.org/doi/10.1145/3666025.3699342) -->

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