package com.jacek.wordcount;

import com.google.common.collect.Comparators;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * HashMap backed collection of token occurrence counters
 * Similar to a HashMap of Integers, but avoiding excessive object creation
 * and rehashing of updated values on incrementation and merging
 * Mutable and NOT thread safe!
 *
 * @author Jacek R. Ambroziak
 */
final class WordCounter {
    private final HashMap<String, Counter> counterHashMap = new HashMap<>(4096);
    private long cumulativeMillis = 0L;
    private int cumulativeMergeCount = 0;

    /**
     * like Integer but mutable to support efficient incrementation
     */
    private static final class Counter implements Comparable<Counter> {
        private int value;

        Counter(final int value) {
            this.value = value;
        }

        void increment() {
            ++value;
        }

        void incrementBy(final Counter other) {
            value += other.value;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }

        int getValue() {
            return value;
        }

        @Override
        public int compareTo(final Counter o) {
            return Integer.compare(value, o.value);
        }
    }

    void countWord(final String word) {
        final Counter counter = counterHashMap.get(word);
        if (counter != null) {
            counter.increment();
        } else {
            counterHashMap.put(word, new Counter(1));
        }
    }

    /**
     * Exportable, immutable state of a word counter
     */
    public static final class WordCount implements Comparable<WordCount> {
        static final Comparator<WordCount> COMPARATOR = new WordCountComparator();
        final String word;
        final int count;

        public String getWord() {
            return word;
        }

        public int getCount() {
            return count;
        }

        public WordCount(final String word, final int count) {
            checkArgument(word != null);
            checkArgument(count >= 0);
            this.word = word;
            this.count = count;
        }

        static WordCount fromEntry(final Map.Entry<String, Counter> entry) {
            return new WordCount(entry.getKey(), entry.getValue().getValue());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            } else {
                final WordCount wc = (WordCount) o;
                return count == wc.count && word.equals(wc.word);   // word is non null
            }
        }

        @Override
        public int hashCode() {
            return word.hashCode() + count;
        }

        @Override
        public String toString() {
            return String.format("\"%s\":%d", word, count);
        }

        @Override
        public int compareTo(final WordCount o) {
            final int delta = count - o.count;
            return delta != 0 ? delta : o.word.compareTo(word); // break ties lexicographically
        }

        private static class WordCountComparator implements Comparator<WordCount> {
            @Override
            public int compare(final WordCount o1, final WordCount o2) {
                return o1.compareTo(o2);
            }
        }
    }

    /**
     * Modifies this object by adding another set of counts
     *
     * @param other counts to be added to this
     */
    WordCounter mergeIn(final WordCounter other) {
        final Instant before = Instant.now();
        final HashMap<String, Counter> counts = this.counterHashMap;    // store in local variable
        for (final Map.Entry<String, Counter> entry : other.counterHashMap.entrySet()) {
            final String key = entry.getKey();
            final Counter counter = counts.get(key);
            if (counter != null) {
                counter.incrementBy(entry.getValue());
            } else {
                counts.put(key, entry.getValue());
            }
        }
        // gather performance data
        cumulativeMergeCount += other.cumulativeMergeCount + 1;
        cumulativeMillis += other.cumulativeMillis + Duration.between(before, Instant.now()).toMillis();
        return this;
    }

    int size() {
        return counterHashMap.size();
    }

    Set<String> getAllWords() {
        return counterHashMap.keySet();
    }

    Optional<WordCount> getWordCount(final String word) {
        final Counter counter = counterHashMap.get(word);
        return counter != null ? Optional.of(new WordCount(word, counter.value)) : Optional.empty();
    }

    int getCount(final String word) {
        final Counter counter = counterHashMap.get(word);
        return counter != null ? counter.getValue() : 0;
    }

    /**
     * This function is rather expensive to run; it is intended to run once
     * An alternative implementation would continually update eg. a heap of counters
     * to make more frequent sampling of current N best
     *
     * @param k nonnegative number of words of highest frequency in order of nonincreasing frequency
     * @return List of immutable WordCounts of highest frequency in order of nonincreasing frequency
     */
    List<WordCount> topWords(final int k) {
        checkArgument(k >= 0, "Argument was %s but expected nonnegative", k);
        return getWordCountStream().collect(Comparators.greatest(k, WordCount.COMPARATOR));
    }

    public Stream<WordCount> getWordCountStream() {
        return counterHashMap.entrySet().stream().map(WordCount::fromEntry);
    }

    String getPerformanceDataAsString() {
        return String.format("%d merges in %d milliseconds, (%.2f msec/merge)", cumulativeMergeCount, cumulativeMillis, (double) cumulativeMillis / cumulativeMergeCount);
    }

    public static void main(String[] args) {
        WordCounter wc1 = new WordCounter();
        WordCounter wc2 = new WordCounter();

        wc1.countWord("monday");
        wc1.countWord("tuesday");
        wc2.countWord("tuesday");
        wc2.countWord("wednesday");

        wc1.mergeIn(wc2);

        System.out.println("wc1 = " + wc1.counterHashMap);
        System.out.println("wc1 = " + wc1.topWords(0));
        System.out.println("wc1 = " + wc1.topWords(20));
    }
}
