package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

import com.formdev.flatlaf.FlatDarkLaf;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RealTimeGui extends JFrame {

    private XYSeries series;
    private JFreeChart chart;
    private TagData tagData;
    private Timer timer;
    private int visualXLength = 200;
    private int xData = 0;
    private Configs config;
    private int yRange = 120; 
    private JPanel buttonPanel;

    public RealTimeGui(TagData tagData) {
        this.config = Configs.getCfgInstance();
        this.tagData = tagData;
        this.series = new XYSeries("");

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        this.chart = ChartFactory.createXYLineChart(
                "ZenseTag: Real-Time Sensing Platform", 
                "Time",                       
                "Sensory Change",      
                dataset
        );

        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setRange(0, yRange);
        
        plot.setBackgroundPaint(new Color(48, 48, 48));
        plot.setDomainGridlinePaint(new Color(128, 128, 128));
        plot.setRangeGridlinePaint(new Color(128, 128, 128));

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.WHITE);
        // Remove the square shapes denoting the points
        renderer.setSeriesShapesVisible(0, false);
        // Set a thicker line
        float lineThickness = 5.0f;  // Adjust this value to change the line thickness
        renderer.setSeriesStroke(0, new BasicStroke(lineThickness));

        plot.setRenderer(renderer);

        setTitle("ZenseTag: Universal Multi-Modal Sensing Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(new Color(38, 50, 56));
        add(chartPanel, BorderLayout.CENTER);

        JLabel avgLabel = new JLabel("\u03C6: N/A");
        avgLabel.setForeground(new Color(176, 190, 197));
        avgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        add(avgLabel, BorderLayout.SOUTH);

        // Create and add the button panel
        createButtonPanel();
        add(buttonPanel, BorderLayout.NORTH);

        setSize(1366, 768);
        setVisible(true);

        // Add a WindowListener to save data when the GUI is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tagData.saveAllPhaseDifferences();
                stop();  // Stop the timer as well
                System.out.println("Data saved and resources released.");
            }
        });

        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updatePlot(avgLabel);
            }
        }, 100, 50);
    }

    private void createButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.setBackground(new Color(38, 50, 56));

        List<String> sensorNames = config.getAllSensorNames();
        for (String sensorName : sensorNames) {
            JButton button = new JButton(toTitleCase(sensorName));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0, 150, 136));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.addActionListener(e -> switchSensor(sensorName));
            buttonPanel.add(button);
        }
    }

    private void switchSensor(String sensorName) {
        if ("auto".equalsIgnoreCase(sensorName)) {
            config.setAutoSelect(true);
            refreshXYPlot(true);
            return;
        }
        config.setAutoSelect(false);
        config.setSensorDef(sensorName);
        refreshXYPlot(true);
    }
    
    private void refreshXYPlot(Boolean reset) {
        this.yRange = (int) config.getSensorConfig(config.getSensorDef()).get("y_range");
        XYPlot plot = chart.getXYPlot();
        plot.getRangeAxis().setRange(0, yRange);
        if (reset) {
            series.clear();
            xData = 0;
        }
    }
    
    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    
    private void updatePlot(JLabel avgLabel) {
        double avgPhaseDiff = tagData.calculateAvgPhaseDifference();
        if (avgPhaseDiff >= 0) {
            xData++;
            series.add(xData, avgPhaseDiff);
            if (series.getItemCount() > visualXLength) {
                series.remove(0);
            }
            avgLabel.setText(String.format("Sensor: " + toTitleCase(config.getSensorDef()) + " || \u03C6: %.2f", avgPhaseDiff));
        }
        try {
            refreshXYPlot(false);
        } 
        catch (Exception e) {}
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> {
            TagData tagData = new TagData();
            AntennaReader antennaReader = new AntennaReader(tagData);
            new Thread(antennaReader::startReading).start();
            try {
                Thread.sleep(2500);
                new RealTimeGui(tagData);
            } catch (Exception e) {
                // Do nothing
            }
        });
    }
}