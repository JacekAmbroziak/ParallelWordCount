package com.jacek.wordcount;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void run() {
        try {
            final List<File> fileList = TestUtils.unzipToDir(
                    TestUtils.resourceFile("linux-4.9.95-docs.zip"),
                    tmpFolder.getRoot());
            System.out.println("fileList = " + fileList.size());
            final int nReps = 10;
            long minLIN = Long.MAX_VALUE;
            long minFAJ = Long.MAX_VALUE;
            long totalLIN = 0L;
            long totalFAJ = 0L;
            for (int n = nReps; --n >= 0; ) {
                {
                    final WordCountingService wordCountingService = new SerialWordCounting();
                    final Instant before = Instant.now();
                    final WordCounter wordCounter = wordCountingService.countWords(fileList);
                    final long timeMsec = Duration.between(before, Instant.now()).toMillis();
                    totalLIN += timeMsec;
                    minLIN = Math.min(minLIN, timeMsec);
                    System.out.println("time LINEAR\t\t= " + timeMsec + " ms");
                    if (n == 0) {
                        System.out.println();
                        System.out.println("token count  = " + wordCounter.size());
                        System.out.println("wordCounter = " + wordCounter.getPerformanceDataAsString());
                        System.out.println("wordCounter top 20 = " + wordCounter.topWords(20));
                        System.out.println(String.format("LIN: avg msec: %d, min msec: %d", totalLIN / nReps, minLIN));
                        System.out.println("\n");
                    }
                }
                {
                    final int singleTaskMaxSize = 200;
                    final WordCountingService wordCountingService = new ForkJoinWordCounting(singleTaskMaxSize);
                    final Instant before = Instant.now();
                    final WordCounter wordCounter = wordCountingService.countWords(fileList);
                    final long timeMsec = Duration.between(before, Instant.now()).toMillis();
                    totalFAJ += timeMsec;
                    minFAJ = Math.min(minFAJ, timeMsec);
                    System.out.println("time FORK/JOIN\t\t= " + timeMsec + " ms");
                    if (n == 0) {
                        System.out.println("token count   = " + wordCounter.size());
                        System.out.println("wordCounter = " + wordCounter.getPerformanceDataAsString());
                        System.out.println("wordCounter top 20 = " + wordCounter.topWords(20));
                        System.out.println(String.format("F/J: avg msec: %d, min msec: %d", totalFAJ / nReps, minFAJ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
