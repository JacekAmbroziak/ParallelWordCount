package com.jacek.wordcount;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Jacek R. Ambroziak
 */
public final class WordCountTest extends JavaBaseTest {
    @Test
    public void testCounting() {
        final WordCounter wc = new WordCounter();
        wc.countWord("a");
        wc.countWord("b");
        wc.countWord("a");
        wc.countWord("b");
        wc.countWord("a");
        wc.countWord("c");
        Assert.assertEquals(wc.getAllWords(), new HashSet<>(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(wc.getCount("a"), 3);
        Assert.assertEquals(wc.getCount("b"), 2);
        Assert.assertEquals(wc.getCount("c"), 1);
    }

    @Test
    public void testMergeToEmpty() {
        final WordCounter wc0 = new WordCounter();
        final WordCounter wc1 = new WordCounter();
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("c");
        wc0.mergeIn(wc1);
        Assert.assertEquals(wc0.getAllWords(), new HashSet<>(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(wc0.getCount("a"), 3);
        Assert.assertEquals(wc0.getCount("b"), 2);
        Assert.assertEquals(wc0.getCount("c"), 1);
    }

    @Test
    public void testMerge() {
        final WordCounter wc0 = new WordCounter();
        wc0.countWord("a");
        wc0.countWord("d");
        wc0.countWord("d");

        final WordCounter wc1 = new WordCounter();
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("c");

        wc0.mergeIn(wc1);

        Assert.assertEquals(wc0.getAllWords(), new HashSet<>(Arrays.asList("a", "b", "c", "d")));
        Assert.assertEquals(wc0.getCount("a"), 4);
        Assert.assertEquals(wc0.getCount("b"), 2);
        Assert.assertEquals(wc0.getCount("c"), 1);
        Assert.assertEquals(wc0.getCount("d"), 2);
    }

    @Test
    public void testTopk() {
        final WordCounter wc0 = new WordCounter();
        wc0.countWord("a");
        wc0.countWord("d");
        wc0.countWord("d");

        final WordCounter wc1 = new WordCounter();
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("b");
        wc1.countWord("a");
        wc1.countWord("c");

        wc0.mergeIn(wc1);

        final List<WordCounter.WordCount> actual = wc0.topWords(4);
        Assert.assertEquals(actual, Arrays.asList(
                new WordCounter.WordCount("a", 4),
                new WordCounter.WordCount("b", 2),
                new WordCounter.WordCount("d", 2),
                new WordCounter.WordCount("c", 1)
        ));
    }
}
