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

import static com.google.common.base.Preconditions.checkArgument;
import static com.jacek.wordcount.Core.countWordsInFiles;

/**
 * A natural implementation of parallelization of word count batches
 * with a thread pool and Futures
 */
final class SimpleParallelWordCounting implements WordCountingService {
    private final int noOfThreads;

    SimpleParallelWordCounting(final int noOfThreads) {
        checkArgument(noOfThreads >= 1);
        this.noOfThreads = noOfThreads;
    }

    /**
     * Callable task to be submitted to an ExecutorService
     */
    private static final class CountTask implements Callable<WordCounter> {
        private final List<File> files;

        CountTask(final List<File> files) {
            this.files = files;
        }

        @Override
        public WordCounter call() {
            final WordCounter counters = new WordCounter();
            countWordsInFiles(files, counters);
            return counters;
        }
    }

    public WordCounter countWords(final List<File> files) throws Exception {
        final int fileCount = files.size();
        final int nFilesPerTask = (int) Math.ceil((double) fileCount / noOfThreads);
        final List<CountTask> tasks = new ArrayList<>(noOfThreads);
        // create Callable tasks for sublists of input
        for (int i = 0, start = 0; i < noOfThreads; i++) {
            final int end = Math.min(start + nFilesPerTask, fileCount);
            tasks.add(new CountTask(files.subList(start, end)));
            start = end;
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);
        final List<Future<WordCounter>> futures = executorService.invokeAll(tasks);
        // wait for 1st result
        final WordCounter wordCounter = futures.get(0).get();
        // merge in subsequent results as they are or become ready
        for (int i = 1; i < futures.size(); i++) {
            wordCounter.mergeIn(futures.get(i).get());
        }
        final int unfinished = executorService.shutdownNow().size();
        assert unfinished == 0 : "there should be no unfinished tasks";
        return wordCounter;
    }

    public static void main(String[] args) {
        try {
            final List<File> fileList = Core.filesInDirWithExtension("/usr/src", ".txt");
            final SimpleParallelWordCounting counting = new SimpleParallelWordCounting(Runtime.getRuntime().availableProcessors());
            for (int i = 0; i < 20; i++) {
                final Instant before = Instant.now();
                final WordCounter wordCounter = counting.countWords(fileList);
                System.out.println("time Simple = " + Duration.between(before, Instant.now()).toMillis() + " ms");

                System.out.println("token count  = " + wordCounter.size());
                System.out.println("wordCounters = " + wordCounter.getPerformanceDataAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
