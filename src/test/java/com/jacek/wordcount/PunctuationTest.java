package com.jacek.wordcount;

import org.junit.Assert;
import org.junit.Test;

public final class PunctuationTest {
    @Test
    public void test() {
        Assert.assertEquals(Punctuation.stripPunctuation(null), null);
        Assert.assertEquals(Punctuation.stripPunctuation(""), "");
        Assert.assertEquals(Punctuation.stripPunctuation("abc"), "abc");
        Assert.assertEquals(Punctuation.stripPunctuation(",.!"), "");
        // complete set
        Assert.assertEquals(Punctuation.stripPunctuation("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"), "");
        Assert.assertEquals(Punctuation.stripPunctuation("/abc"), "abc");
        Assert.assertEquals(Punctuation.stripPunctuation("abc/"), "abc");
        Assert.assertEquals(Punctuation.stripPunctuation("/abc/"), "abc");
        // inside punctuation not stripped
        Assert.assertEquals(Punctuation.stripPunctuation("/ab/c/"), "ab/c");
        Assert.assertEquals(Punctuation.stripPunctuation("a+b-c"), "a+b-c");
        // space IS NOT punctuation!
        Assert.assertEquals(Punctuation.stripPunctuation(" abc "), " abc ");
        Assert.assertEquals(Punctuation.stripPunctuation(" ab\tc "), " ab\tc ");
        // multiple leading and trailing punctuation + embedded
        Assert.assertEquals(Punctuation.stripPunctuation("!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~" +
                "AB/C" +
                "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"), "AB/C");
    }
}
