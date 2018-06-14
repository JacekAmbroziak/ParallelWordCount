package com.jacek.wordcount;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.google.common.io.Resources.getResource;

public final class FileWordCountTest extends JavaBaseTest {
    private static File resourceFile(final String fileName) throws URISyntaxException {
        return new File(getResource(fileName).toURI());
    }

    @Test
    public void countWordsInIndividualFiles() throws Exception {
        final WordCountingService serialCounting = new SerialWordCounting();
        final File txtFile1 = resourceFile("rt-mutex-design.txt");
        final WordCounter wordCounter1 = serialCounting.countWords(Collections.singletonList(txtFile1));

        Assert.assertEquals(wordCounter1.getCount("mutex"), 129);
        Assert.assertEquals(wordCounter1.getCount("process"), 71);

        final File txtFile2 = resourceFile("xfs-delayed-logging-design.txt");
        final WordCounter wordCounter2 = serialCounting.countWords(Collections.singletonList(txtFile2));

        Assert.assertEquals(wordCounter2.getCount("process"), 2);

        final WordCounter wordCounter12 = serialCounting.countWords(ImmutableList.of(txtFile1, txtFile2));
        Assert.assertEquals(wordCounter12.getCount("process"), 73);

        final WordCounter wordCounter21 = serialCounting.countWords(ImmutableList.of(txtFile2, txtFile1));
        Assert.assertEquals(wordCounter21.getCount("process"), 73);
    }

    @Test
    public void countWordsInParallel() throws Exception {
        final WordCountingService serialCounting = new SerialWordCounting();
        final WordCountingService simpleParallel = new SimpleParallelWordCounting(2);
        final WordCountingService forkJoinParallel = new ForkJoinWordCounting(1);
        final File txtFile1 = resourceFile("rt-mutex-design.txt");
        final File txtFile2 = resourceFile("xfs-delayed-logging-design.txt");

        final ImmutableList<File> files = ImmutableList.of(txtFile1, txtFile2);
        final WordCounter wordCounter12 = serialCounting.countWords(files);
        final WordCounter wordCounterSimple = simpleParallel.countWords(files);
        final WordCounter wordCounterFJ = forkJoinParallel.countWords(files);

        Assert.assertEquals(wordCounter12.getCount("process"), 73);
        Assert.assertEquals(wordCounterSimple.getCount("process"), 73);
        Assert.assertEquals(wordCounterFJ.getCount("process"), 73);

        Assert.assertEquals(wordCounter12.topWords(40), wordCounterSimple.topWords(40));
        Assert.assertEquals(wordCounter12.topWords(40), wordCounterFJ.topWords(40));
    }
}
