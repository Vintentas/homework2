package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static org.slf4j.LoggerFactory.getLogger;

public class FileProcessor {
    private static final Logger logger = getLogger(FileProcessor.class);
    public static final int CHUNK_SIZE = 2 * getRuntime().availableProcessors();

    private Exchanger<List<String>> exchangerWithFileWriter = new Exchanger();
    private List<String> buffer = new ArrayList();

    public void process(@Nonnull String processingFileName, @Nonnull String resultFileName) {
        checkFileExists(processingFileName);

        final File file = new File(processingFileName);

        // TODO: NotImplemented: запускаем FileWriter в отдельном потоке -> done

        //Запускаем поток для записи в файл;
        FileWriter fileWriter = new FileWriter(resultFileName, exchangerWithFileWriter);
        ExecutorService executorServiceWriter = Executors.newSingleThreadExecutor();
        executorServiceWriter.submit(fileWriter);

        //Запускаем потоки для обработки строк;
        ExecutorService executorServiceProcesses = Executors.newFixedThreadPool(CHUNK_SIZE);


        try (final Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                CountDownLatch countDownLatch = new CountDownLatch(CHUNK_SIZE);

                // TODO: NotImplemented: вычитываем CHUNK_SIZE строк для параллельной обработки
                for (int size = 0; size < CHUNK_SIZE ; size++) {
                    buffer.add(scanner.nextLine());
                    if (!scanner.hasNext()) {
                        countDownLatch = new CountDownLatch(size+1);
                        break;
                    }
                }

                // TODO: NotImplemented: обрабатывать строку с помощью LineProcessor. Каждый поток обрабатывает свою строку.
                for (String line: buffer) {
                    executorServiceProcesses.submit(new LineCounterProcessorCallable(line, buffer, countDownLatch));
                }

                countDownLatch.await();

                // TODO: NotImplemented: добавить обработанные данные в результирующий файл -> done
                buffer = exchangerWithFileWriter.exchange(buffer);
            }
        } catch (IOException | InterruptedException exception) {
            logger.error("", exception);
        }


        // TODO: NotImplemented: остановить поток writerThread -> done
        try {
            Thread.sleep(500);

            executorServiceProcesses.shutdown();
            executorServiceWriter.shutdown();

            executorServiceProcesses.awaitTermination(1, TimeUnit.SECONDS);
            executorServiceWriter.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("Finish main thread {}", Thread.currentThread().getName());
    }

    private void checkFileExists(@Nonnull String fileName) {
        final File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File '" + fileName + "' not exists");
        }
    }
}
