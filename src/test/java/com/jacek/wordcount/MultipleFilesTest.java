package com.jacek.wordcount;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.Set;

public final class MultipleFilesTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    private List<File> fileList;

    @Before
    public void setUp() throws Exception {
        final File tmpDir = tmpFolder.getRoot();
        fileList = TestUtils.unzipToDir(TestUtils.resourceFile("linux-4.9.95-docs.zip"), tmpDir);
        fileList.add(new File("kwas"));
    }

    @Test
    public void processMultipleFiles() throws Exception {
        // baseline
        final WordCountingService serialCounting = new SerialWordCounting();
        final WordCounter wordCounter1 = serialCounting.countWords(fileList);

        // use fork/join
        final int singleTaskMaxSize = 200;
        final WordCountingService forkJoinParallel = new ForkJoinWordCounting(singleTaskMaxSize);
        final WordCounter wordCounter2 = forkJoinParallel.countWords(fileList);

        final Set<String> allWords1 = wordCounter1.getAllWords();
        Assert.assertEquals(allWords1, wordCounter2.getAllWords());

        Assert.assertTrue(allWords1.stream()
                .allMatch(word -> wordCounter1.getCount(word) == wordCounter2.getCount(word)));

        // use simple Futures
        final int noOfThreads = 8;
        final WordCountingService simpleParallel = new SimpleParallelWordCounting(noOfThreads);
        final WordCounter wordCounter3 = simpleParallel.countWords(fileList);

        Assert.assertEquals(allWords1, wordCounter3.getAllWords());

        Assert.assertTrue(allWords1.stream()
                .allMatch(word -> wordCounter1.getCount(word) == wordCounter3.getCount(word)));
    }
}
