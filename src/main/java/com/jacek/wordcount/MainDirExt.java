package com.jacek.wordcount;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Main program to count unique lower-cased tokens in files specified by directory + file extension like .txt or .c
 *
 * @author Jacek R. Ambroziak
 */
final class MainDirExt {
    /**
     * @param args 2 args: root dir for text files, and file extension
     */
    public static void main(String[] args) {
        checkArgument(args.length == 2, "arguments expected: directory, file extension");

        final String dir = args[0];
        final String ext = args[1];
        try {
            final List<File> files = Common.filesInDirWithExtension(dir, ext);
            final int singleTaskMaxSize = 200;
            final WordCountingService wordCountingService = new ForkJoinWordCounting(singleTaskMaxSize);
            final WordCounter wordCounter = wordCountingService.countWords(files);
            System.out.println("token count   = " + wordCounter.size());
            System.out.println("wordCounters = " + wordCounter.getPerformanceDataAsString());
            System.out.println("wordCounters top 20 = " + wordCounter.topWords(20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
