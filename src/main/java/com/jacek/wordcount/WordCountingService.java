package com.jacek.wordcount;

import java.io.File;
import java.util.List;

/**
 * Abstracted interface to various parallel and single threaded implementations of word counting
 */
public interface WordCountingService {
    /**
     * Tokenize a batch of text files on white space, strip punctuation, cannonicalize to lower case
     * and return counts of unique words
     *
     * @param files a batch of files to process (can be a singleton list)
     * @return counts of unique words
     * @throws Exception
     */
    WordCounter countWords(final List<File> files) throws Exception;
}
