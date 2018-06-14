package com.jacek.wordcount;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class uses the ForkJoin framework to rationally divide work into smaller chunks
 * that can be executed concurrently.
 *
 * @author Jacek R. Ambroziak
 */
final class ForkJoinWordCounting implements WordCountingService {
    // a parameter to experiment with:
    // for lists of files of that size or smaller 
    private final int singleTaskMaxSize;

    ForkJoinWordCounting(final int singleTaskMaxSize) {
        checkArgument(singleTaskMaxSize > 0);
        this.singleTaskMaxSize = singleTaskMaxSize;
    }

    /**
     * A RecursiveTask implementation responsible for parallelization w/i the Fork/Join framework
     * It's compute method either performs smaller tasks directly
     * or schedules subtasks to be performed concurrently for their results to be merged when available
     */
    private final class CountingTask extends RecursiveTask<WordCounter> {
        private final List<File> files;

        CountingTask(final List<File> files) {
            this.files = new ArrayList<>(files);
        }

        @Override
        protected WordCounter compute() {
            final int taskSize = files.size();
            // if small enough compute directly w/o splitting
            if (taskSize <= singleTaskMaxSize) {
                final WordCounter wordCounter = new WordCounter();
                Common.countWordsInFiles(files, wordCounter);
                return wordCounter;
            } else {
                final int halfSize = taskSize / 2;
                final CountingTask subtask1 = new CountingTask(files.subList(0, halfSize));
                final CountingTask subtask2 = new CountingTask(files.subList(halfSize, taskSize));
                // start work on first subtask
                subtask1.fork();
                // initiate computation of second task
                final WordCounter wordCounter2 = subtask2.compute();
                // retrieve result of async computation of first task
                final WordCounter wordCounter1 = subtask1.join();
                // mutate wordCounts2 and return it; merging happens in the current thread
                return wordCounter2.mergeIn(wordCounter1);
            }
        }
    }

    public WordCounter countWords(final List<File> files) {
        final ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            return forkJoinPool.invoke(new CountingTask(files));
        } finally {
            forkJoinPool.shutdown();
        }
    }
}
