package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriter implements Runnable {

    private static final Logger logger = getLogger(FileWriter.class);

    public final String resultFileName;
    public Exchanger<List<String>> exchanger;

    FileWriter (String resultFileName, Exchanger<List<String>> exchanger) {
        this.resultFileName = resultFileName;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        writeToFile();
        logger.info("Finish writer thread {}", currentThread().getName());
    }


    private void writeToFile() {
        try (BufferedWriter out = new BufferedWriter(new java.io.FileWriter(resultFileName))) {

            while (!Thread.currentThread().interrupted()) {
                List<String> buffer = exchanger.exchange(new ArrayList(), 1, TimeUnit.SECONDS);
                for (String line: buffer) {
                    out.write(line + "\n");
                }
            }
        } catch (InterruptedException | TimeoutException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
