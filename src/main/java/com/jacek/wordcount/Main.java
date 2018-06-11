package com.jacek.wordcount;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek R. Ambroziak
 */
final class Main {
    public static void main(String[] args) {
        final List<File> files = new ArrayList<>();
        for (String arg : args) {
            files.add(new File(arg));
        }
        final WordCounter wordCounter = WordCounting.parallelWordCount(files);
        System.out.println("token count   = " + wordCounter.size());
        System.out.println("wordCounters = " + wordCounter.getPerformanceDataAsString());
        System.out.println("wordCounters top 20 = " + wordCounter.topWords(20));
    }
}
