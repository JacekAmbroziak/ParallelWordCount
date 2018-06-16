package com.jacek.wordcount;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ExceptionLogTest {
    private List<LogRecord> logRecords;

    @Before
    public void setUp() {
        final Logger log = Logger.getLogger(Core.class.getName());
        final List<LogRecord> records = new ArrayList<>();
        log.addHandler(new Handler() {
            @Override
            public void publish(final LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
        logRecords = records;
    }

    @Test
    public void run() throws Exception {
        final WordCountingService forkJoinParallel = new ForkJoinWordCounting(1);

        final ImmutableList<File> files = ImmutableList.of(
                TestUtils.resourceFile("rt-mutex-design.txt"),
                new File("not-there"),
                TestUtils.resourceFile("xfs-delayed-logging-design.txt"));

        final WordCounter wordCounterFJ = forkJoinParallel.countWords(files);

        Assert.assertEquals(wordCounterFJ.getCount("process"), 73);
        Assert.assertEquals(logRecords.size(), 1);
        Assert.assertTrue(logRecords.get(0).getMessage().contains("not-there"));
    }
}
