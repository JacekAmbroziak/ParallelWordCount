package com.jacek.wordcount;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Main program to exercise and compare token counting in all .txt files in Linux kernel source
 *
 * @author Jacek R. Ambroziak
 */
final class SpeedTest {
    public static void main(String[] args) {
        try {
            final List<File> fileList = Common.textFilesInDir("/usr/src");
            for (int n = 20; --n >= 0; ) {
                {
                    final WordCounter wordCounter = new WordCounter();
                    final Instant before1 = Instant.now();
                    Common.countWordsInFiles(fileList, wordCounter);
                    System.out.println("time LINEAR = " + Duration.between(before1, Instant.now()).toMillis() + " ms");
                    System.out.println("fileList = " + fileList.size());

                    System.out.println("token count  = " + wordCounter.size());
                    System.out.println("wordCounters top 20 = " + wordCounter.topWords(20));
                }
                {
                    final int singleTaskMaxSize = 200;
                    final WordCountingService wordCountingService = new ParallelWordCounting(singleTaskMaxSize);
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
