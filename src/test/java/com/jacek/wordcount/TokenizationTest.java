package com.jacek.wordcount;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

public final class TokenizationTest {
    @Test
    public void tokenizeLines() throws IOException {
        final WordCounter wc = new WordCounter();
        Core.countWordsFromReader(new StringReader("<a b> ,,B \tc a, b. A!"), wc);

        Assert.assertEquals(wc.getAllWords(), new HashSet<>(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(wc.getCount("a"), 3);
        Assert.assertEquals(wc.getCount("b"), 3);
        Assert.assertEquals(wc.getCount("c"), 1);
        Assert.assertEquals(wc.getCount("d"), 0);
    }
}
