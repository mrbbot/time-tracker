package com.mrbbot.timetracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class TimeTracker extends JFrame implements ActionListener, Runnable {
    private Thread thread;
    private boolean running;

    private JComboBox<String> name;
    private JLabel time;
    private JButton startButton;
    private JButton stopButton;
    private int seconds = 108000;
    private Properties properties;

    private TimeTracker() {
        super("Time Tracker");
        setSize(500, 300);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        properties = new Properties();
        File file = new File("times.properties");
        try {
            if(!file.exists()) //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            properties.load(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    properties.store(new FileWriter(file), null);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                dispose();
                System.exit(0);
            }
        });

        ArrayList<String> keys = new ArrayList<>();
        for(Object key : properties.keySet())
            keys.add((String) key);
        String[] keysArray = new String[keys.size()];
        keys.toArray(keysArray);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        name = new JComboBox<>(keysArray);
        name.setEditable(true);
        mainPanel.add(name, BorderLayout.NORTH);

        time = new JLabel("00:00:00");
        time.setVerticalAlignment(SwingConstants.CENTER);
        time.setHorizontalAlignment(SwingConstants.CENTER);
        time.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        mainPanel.add(time, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 4, 5, 5));

        startButton = new JButton("Start");
        startButton.addActionListener(this);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);

        bottomPanel.add(new JLabel());
        bottomPanel.add(startButton);
        bottomPanel.add(stopButton);
        bottomPanel.add(new JLabel());

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        setVisible(true);
    }

    private void loadTime(String name) {
        seconds = Integer.parseInt(properties.getProperty(name, "0"));
    }

    private void saveTime(String name) {
        properties.setProperty(name, String.valueOf(seconds));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = (String) name.getSelectedItem();
        if(text == null || Objects.equals(text, "")) {
            JOptionPane.showMessageDialog(this, "Please specify a name!", "No Name", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (e.getActionCommand()) {
            case "Start":
                loadTime(text);

                name.setEnabled(false);
                startButton.setEnabled(false);

                running = true;
                thread = new Thread(this);
                thread.start();

                stopButton.setEnabled(true);
                break;
            case "Stop":
                saveTime(text);

                stopButton.setEnabled(false);

                running = false;
                thread.interrupt();
                try {
                    thread.join();
                } catch (InterruptedException ignored) {

                }
                thread = null;

                name.setEnabled(true);
                startButton.setEnabled(true);
                break;
        }
    }

    private String zeroify(int part) {
        String str = String.valueOf(part);
        if(part < 10)
            str = "0" + str;
        return str;
    }

    @Override
    public void run() {
        while(running) {
            int actualHours = seconds / 3600;
            int actualMinutes = (seconds % 3600) / 60;
            int actualSeconds = seconds - (actualHours * 3600) - (actualMinutes * 60);

            time.setText(
                    zeroify(actualHours) + ":" +
                    zeroify(actualMinutes) + ":" +
                    zeroify(actualSeconds)
            );

            seconds++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {

        }

        new TimeTracker();
    }
}
