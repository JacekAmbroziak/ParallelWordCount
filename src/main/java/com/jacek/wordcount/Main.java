package com.jacek.wordcount;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main program to count unique lowercased tokens in files specified as arguments
 */
final class Main {
    /**
     * @param args list of text files to count tokens in
     */
    public static void main(String[] args) {
        final List<File> files = new ArrayList<>();
        for (String arg : args) {
            files.add(new File(arg));
        }
        final int singleTaskMaxSize = 200;
        final WordCountingService wordCountingService = new ForkJoinWordCounting(singleTaskMaxSize);
        try {
            final WordCounter wordCounter = wordCountingService.countWords(files);
            System.out.println("token count   = " + wordCounter.size());
            System.out.println("wordCounters = " + wordCounter.getPerformanceDataAsString());
            System.out.println("wordCounters top 20 = " + wordCounter.topWords(20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
