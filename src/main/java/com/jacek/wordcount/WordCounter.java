package com.jacek.wordcount;

import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * HashMap backed collection of token occurrence counters
 * Similar to a HashMap of Integers, but avoiding excessive object creation
 * and rehashing of updated values on incrementation and merging
 * Mutable and NOT thread safe!
 *
 * @author Jacek R. Ambroziak
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

    public int size() {
        return counterHashMap.size();
    }

    public Set<String> getAllWords() {
        return counterHashMap.keySet();
    }

    public int getCount(final String word) {
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
