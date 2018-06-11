package com.jacek.wordcount;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.jacek.wordcount.Common.countWordsInFiles;
import static com.jacek.wordcount.Common.textFilesInDir;

/**
 * @author Jacek R. Ambroziak
 */
final class SimpleParallelization {
    final static int NO_OF_THREADS = 8;

    private static final class CountTask implements Callable<WordCounter> {
        private final List<File> files;

        CountTask(final List<File> files) {
            this.files = files;
        }

        @Override
        public WordCounter call() throws Exception {
            final WordCounter counters = new WordCounter();
            countWordsInFiles(files, counters);
            return counters;
        }
    }

    static WordCounter countTokens(final List<File> files) throws Exception {
        final int fileCount = files.size();
        final int nFilesPerTask = (int) Math.ceil((double) fileCount / NO_OF_THREADS);
        final List<CountTask> tasks = new ArrayList<>(NO_OF_THREADS);
        // create Callable tasks for sublists of input
        for (int i = 0, start = 0; i < NO_OF_THREADS; i++) {
            final int end = Math.min(start + nFilesPerTask, fileCount);
            tasks.add(new CountTask(files.subList(start, end)));
            start = end;
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(NO_OF_THREADS);
        final List<Future<WordCounter>> futures = executorService.invokeAll(tasks);
        final WordCounter wordCounter = futures.get(0).get();
        for (int i = 1; i < futures.size(); i++) {
            wordCounter.mergeIn(futures.get(i).get());
        }
        final int unfinished = executorService.shutdownNow().size();
        assert unfinished == 0 : "there should be no unfinished tasks";
        return wordCounter;
    }

    public static void main(String[] args) {
        try {
            final List<File> fileList = textFilesInDir("/usr/src");
            for (int i = 0; i < 20; i++) {
                final Instant before = Instant.now();
                final WordCounter wordCounter = countTokens(fileList);
                System.out.println("time Simple = " + Duration.between(before, Instant.now()).toMillis() + " ms");

                System.out.println("token count  = " + wordCounter.size());
                System.out.println("wordCounters = " + wordCounter.getPerformanceDataAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
