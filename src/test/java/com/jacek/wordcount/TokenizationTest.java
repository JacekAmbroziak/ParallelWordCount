package com.jacek.wordcount;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Jacek R. Ambroziak
 */
public final class TokenizationTest extends JavaBaseTest {
    @Test
    public void tokenizeLines() throws IOException {
        final WordCounter wc = new WordCounter();
        Common.countWordsFromReader(new StringReader("<a b> ,,B \tc a, b. A!"), wc);

        Assert.assertEquals(wc.getAllWords(), new HashSet<>(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(wc.getCount("a"), 3);
        Assert.assertEquals(wc.getCount("b"), 3);
        Assert.assertEquals(wc.getCount("c"), 1);
        Assert.assertEquals(wc.getCount("d"), 0);
    }
}
