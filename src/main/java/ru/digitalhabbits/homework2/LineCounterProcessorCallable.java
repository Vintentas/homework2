package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LineCounterProcessorCallable implements Runnable {
    private final String line;
    List<String> buffer;
    CountDownLatch countDownLatch;

    LineProcessor lineProcessor = new LineCounterProcessor();

    Lock lock = new ReentrantLock();



    LineCounterProcessorCallable(String line, List<String> buffer, CountDownLatch countDownLatch) {
        this.line = line;
        this.buffer = buffer;
        this.countDownLatch = countDownLatch;

    }

    @Override
    public void run() {
        Pair <String, Integer> process = lineProcessor.process(line);
        String lastValue = process.getKey();
        String newValue = process.getKey() + " " + process.getValue();

        lock.lock();
        Collections.replaceAll(buffer, lastValue, newValue);
        lock.unlock();

        countDownLatch.countDown();
    }
}




