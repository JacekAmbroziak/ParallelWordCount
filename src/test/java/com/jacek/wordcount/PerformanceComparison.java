package com.jacek.wordcount;

import org.junit.Test;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Main program to exercise and compare token counting in all .txt files in Linux kernel source
 *
 * @author Jacek R. Ambroziak
 */
public final class PerformanceComparison {
    @Test
    public void run() {
        try {
            final List<File> fileList = Utils.filesInDirWithExtension("/usr/src", ".txt");
            for (int n = 10; --n >= 0; ) {
                {
                    final WordCounter wordCounter = new WordCounter();
                    final Instant before1 = Instant.now();
                    Utils.countWordsInFiles(fileList, wordCounter);
                    System.out.println("time LINEAR = " + Duration.between(before1, Instant.now()).toMillis() + " ms");
                    System.out.println("fileList = " + fileList.size());

                    System.out.println("token count  = " + wordCounter.size());
                    System.out.println("wordCounters top 20 = " + wordCounter.topWords(20));
                }
                {
                    final int singleTaskMaxSize = 200;
                    final WordCountingService wordCountingService = new ForkJoinWordCounting(singleTaskMaxSize);
                    final Instant before = Instant.now();
                    final WordCounter wordCounter2 = wordCountingService.countWords(fileList);
                    System.out.println("time FORK/JOIN = " + Duration.between(before, Instant.now()).toMillis() + " ms");
                    System.out.println("token count   = " + wordCounter2.size());
                    System.out.println("wordCounters = " + wordCounter2.getPerformanceDataAsString());
                    System.out.println("wordCounters top 20 = " + wordCounter2.topWords(20));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
