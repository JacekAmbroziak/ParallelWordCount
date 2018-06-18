package com.jacek.wordcount;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * HashMap backed collection of unique String occurrence counters
 * Similar to a HashMap of Integers, but avoiding excessive object creation
 * and rehashing of updated values on incrementation and merging
 *
 * While the intention is to count "words" this code counts any Strings
 *
 * Mutable and NOT thread safe!
 */
public final class WordCounter {
    private final HashMap<String, Counter> counterHashMap = new HashMap<>(4096);
    private long cumulativeMergeMillis = 0L;
    private int cumulativeMergeCount = 0;
    private long cumulativeBatchMillis = 0L;
    private long cumulativeBatchSize = 0L;
    private int cumulativeBatchCount = 0;

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

    /**
     * Increment an existing counter for the argument word if found
     * or start a new counter for the word initialized to this first occurrence
     *
     * @param word a non-null string an occurrence of which is to be counted
     */
    void countWord(@NonNull final String word) {
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

    void updateBatchStats(final long batchSize, final long batchMillis) {
        cumulativeBatchMillis += batchMillis;
        cumulativeBatchSize += batchSize;
        ++cumulativeBatchCount;
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
        // update performance data
        cumulativeMergeCount += other.cumulativeMergeCount + 1;
        cumulativeMergeMillis += other.cumulativeMergeMillis + Duration.between(before, Instant.now()).toMillis();

        cumulativeBatchMillis += other.cumulativeBatchMillis;
        cumulativeBatchSize += other.cumulativeBatchSize;
        cumulativeBatchCount += other.cumulativeBatchCount;

        return this;
    }

    /**
     * @return cardinality of unique words
     */
    public int size() {
        return counterHashMap.size();
    }

    /**
     * @return set of all words counted
     */
    public Set<String> getAllWords() {
        return counterHashMap.keySet();
    }

    /**
     * @param word non-null string
     * @return primitive non-negative int representing the number of times the argument was counted
     */
    public int getCount(@NonNull final String word) {
        final Counter counter = counterHashMap.get(word);
        return counter != null ? counter.getValue() : 0;
    }

    /**
     * @return ImmutableMap from words to their counts as Integers
     */
    public ImmutableMap<String, Integer> toMap() {
        return counterHashMap.entrySet()
                .stream()
                .collect(toImmutableMap(Map.Entry::getKey, e -> e.getValue().getValue()));
    }

    /**
     * @return the total number of all occurrences of all words ever counted or merged in
     */
    public long getTotalCount() {
        long total = 0L;
        for (Counter counter : counterHashMap.values()) {
            total += counter.getValue();
        }
        return total;
    }

    /**
     * This function is rather expensive to run; it is intended to run once
     * An alternative implementation would continually update eg. a heap of counters
     * to make more frequent sampling of current N best
     *
     * @param k nonnegative number of words of highest frequency in order of nonincreasing frequency
     * @return List of immutable WordCounts of highest frequency in order of nonincreasing frequency
     */
    public List<WordCount> topWords(final int k) {
        checkArgument(k >= 0, "Argument was %s but expected nonnegative", k);
        return getWordCountStream().collect(Comparators.greatest(k, WordCount.COMPARATOR));
    }

    public Stream<WordCount> getWordCountStream() {
        return counterHashMap.entrySet().stream().map(WordCount::fromEntry);
    }

    public String getPerformanceDataAsString() {
        return String.format("%d total batch size\n%d batches in %d milliseconds, (%.2f msec/batch)\n%d merges in %d milliseconds, (%.2f msec/merge)",
                cumulativeBatchSize,
                cumulativeBatchCount, cumulativeBatchMillis, (double) cumulativeBatchMillis / cumulativeBatchCount,
                cumulativeMergeCount, cumulativeMergeMillis, (double) cumulativeMergeMillis / cumulativeMergeCount
        );
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
