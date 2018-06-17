package com.jacek.wordcount;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ImmutableCounterMapTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private List<File> fileList;

    @Before
    public void setUp() throws Exception {
        final File tmpDir = tmpFolder.getRoot();
        fileList = TestUtils.unzipToDir(TestUtils.resourceFile("linux-4.9.95-docs.zip"), tmpDir);
    }

    @Test
    public void verifyContentsEquivalence() throws Exception {
        // use fork/join
        final int singleTaskMaxSize = 200;
        final WordCountingService forkJoinParallel = new ForkJoinWordCounting(singleTaskMaxSize);
        final WordCounter wordCounter = forkJoinParallel.countWords(fileList);

        final Set<String> allWords = wordCounter.getAllWords();

        // extract immutable representation of word counts
        final Map<String, Integer> immutable = wordCounter.toMap();

        // verify that the word sets are the same
        Assert.assertEquals(allWords, immutable.keySet());

        // verify that all the counts are the same
        Assert.assertTrue(allWords.stream()
                .allMatch(word -> wordCounter.getCount(word) == immutable.get(word)));
    }
}
