package com.jacek.wordcount;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.Set;

public final class MultipleFiles {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private List<File> fileList;

    @Before
    public void setUp() throws Exception {
        final File tmpDir = tmpFolder.getRoot();
        fileList = TestUtils.unzipToDir(TestUtils.resourceFile("linux-4.9.95-docs.zip"), tmpDir);
    }

    @Test
    public void processMultipleFiles() throws Exception {
        final WordCountingService serialCounting = new SerialWordCounting();
        final WordCounter wordCounter1 = serialCounting.countWords(fileList);

        final int singleTaskMaxSize = 200;
        final WordCountingService forkJoinParallel = new ForkJoinWordCounting(singleTaskMaxSize);
        final WordCounter wordCounter2 = forkJoinParallel.countWords(fileList);

        final Set<String> allWords1 = wordCounter1.getAllWords();
        Assert.assertEquals(allWords1, wordCounter2.getAllWords());

        Assert.assertTrue(allWords1.stream()
                .allMatch(word -> wordCounter1.getCount(word) == wordCounter2.getCount(word)));
    }
}
