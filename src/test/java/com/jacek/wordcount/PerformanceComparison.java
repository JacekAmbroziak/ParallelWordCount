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
            for (int n = 10; --n >= 0; ) {
                {
                    final WordCounter wordCounter = new WordCounter();
                    final Instant before1 = Instant.now();
                    Core.countWordsInFiles(fileList, wordCounter);
                    System.out.println("time LINEAR\t\t= " + Duration.between(before1, Instant.now()).toMillis() + " ms");
                    if (n == 0) {
                        System.out.println("token count  = " + wordCounter.size());
                        System.out.println("wordCounter = " + wordCounter.getPerformanceDataAsString());
                        System.out.println("wordCounter top 20 = " + wordCounter.topWords(20));
                    }
                }
                {
                    final int singleTaskMaxSize = 200;
                    final WordCountingService wordCountingService = new ForkJoinWordCounting(singleTaskMaxSize);
                    final Instant before = Instant.now();
                    final WordCounter wordCounter = wordCountingService.countWords(fileList);
                    System.out.println("time FORK/JOIN\t\t= " + Duration.between(before, Instant.now()).toMillis() + " ms");
                    if (n == 0) {
                        System.out.println("token count   = " + wordCounter.size());
                        System.out.println("wordCounter = " + wordCounter.getPerformanceDataAsString());
                        System.out.println("wordCounter top 20 = " + wordCounter.topWords(20));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
