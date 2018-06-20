package com.jacek.wordcount;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class CounterAlternativePerformanceTest {

    private static class SimpleCounter {
        private final HashMap<String, Integer> counterHashMap = new HashMap<>(4096);

        void countWord(@NonNull final String word) {
            final Integer counter = counterHashMap.get(word);
            if (counter != null) {
                counterHashMap.put(word, counter + 1);
            } else {
                counterHashMap.put(word, 1);
            }
        }

        void mergeIn(final SimpleCounter other) {
            for (final Map.Entry<String, Integer> entry : other.counterHashMap.entrySet()) {
                final String key = entry.getKey();
                final Integer counter = counterHashMap.get(key);
                if (counter != null) {
                    counterHashMap.put(key, counter + entry.getValue());
                } else {
                    counterHashMap.put(key, entry.getValue());
                }
            }
        }
    }

    private static List<String> extractWords(final List<String> lines) {
        final ArrayList<String> tokens = new ArrayList<>();
        for (final String line : lines) {
            final StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                final String token = Punctuation.stripPunctuation(tokenizer.nextToken());
                if (token.length() > 0) {
                    tokens.add(token.toLowerCase());
                }
            }
        }
        return tokens;
    }

    private static void countTokens(final List<String> lines, final SimpleCounter counts) {
        for (final String line : lines) {
            final StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                final String token = Punctuation.stripPunctuation(tokenizer.nextToken());
                if (token.length() > 0) {
                    counts.countWord(token.toLowerCase());
                }
            }
        }
    }

    private static void countTokens(final List<String> lines, final WordCounter counts) {
        for (final String line : lines) {
            final StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                final String token = Punctuation.stripPunctuation(tokenizer.nextToken());
                if (token.length() > 0) {
                    counts.countWord(token.toLowerCase());
                }
            }
        }
    }

    @Test
    public void run() throws Exception {
        final List<String> allLines = TestUtils.unzipToLines(TestUtils.resourceFile("linux-4.9.95-docs.zip"));
        final List<String> words = extractWords(allLines);
        final int wordCount = words.size();
        System.out.println("wordCount = " + wordCount);
        int n = 100;

        System.out.println("tokenization + counting");
        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final SimpleCounter counts = new SimpleCounter();
                final Instant before = Instant.now();
                countTokens(allLines, counts);
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("SimpleCounter\tminTime = " + minTime + "\taverage " + totalTime / n);
        }
        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final WordCounter counts = new WordCounter();
                final Instant before = Instant.now();
                countTokens(allLines, counts);
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("WordCounter\tminTime = " + minTime + "\taverage " + totalTime / n);
        }
        System.out.println("just counting");
        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final SimpleCounter counts = new SimpleCounter();
                final Instant before = Instant.now();
                for (String word : words) {
                    counts.countWord(word);
                }
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("words: SimpleCounter\tminTime = " + minTime + "\taverage " + totalTime / n);

        }
        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final WordCounter counts = new WordCounter();
                final Instant before = Instant.now();
                for (String word : words) {
                    counts.countWord(word);
                }
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("words: WordCounter\tminTime = " + minTime + "\taverage " + totalTime / n);
        }
        
        System.out.println("compare merge");

        final WordCounter wordCounter = new WordCounter();
        final SimpleCounter simpleCounter = new SimpleCounter();

        for (String word : words) {
            wordCounter.countWord(word);
            simpleCounter.countWord(word);
        }

        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final SimpleCounter counts = new SimpleCounter();
                final Instant before = Instant.now();
                counts.mergeIn(simpleCounter);
                counts.mergeIn(simpleCounter);
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("words merge: SimpleCounter\tminTime = " + minTime + "\taverage " + totalTime / n);

        }
        {
            long minTime = Long.MAX_VALUE;
            long totalTime = 0L;
            for (int i = n; --i >= 0; ) {
                final WordCounter counts = new WordCounter();
                final Instant before = Instant.now();
                counts.mergeIn(wordCounter);
                counts.mergeIn(wordCounter);
                final long time = Duration.between(before, Instant.now()).toMillis();
                minTime = Math.min(minTime, time);
                totalTime += time;
            }
            System.out.println("words merge: WordCounter\tminTime = " + minTime + "\taverage " + totalTime / n);
        }
    }
}
