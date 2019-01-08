package com.gepro.ArbiFinder.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.knowm.xchart.internal.series.Series.DataType.Date;

public class TwoWayLogger {
    private static TwoWayLogger ourInstance = new TwoWayLogger();
    private FileOutputStream fileStream;

    public static TwoWayLogger getInstance() {
        return ourInstance;
    }

    private TwoWayLogger(){
        File file = new File("C:\\Users\\Arbeit\\IdeaProjects\\ArbiFinder2\\Output\\log.txt");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fileStream = new FileOutputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("couldnt write to log file!");
        }
    }

    public void write(String message) {
        message = new Date().toString() + " || " + message;
        System.out.println(message);
        try {
            message += System.lineSeparator();
            fileStream.write(message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("couldnt write to log file!");
        }
    }
}
