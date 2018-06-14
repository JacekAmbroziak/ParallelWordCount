package com.jacek.wordcount;

import java.io.File;
import java.util.List;

/**
 * Abstracted interface to various parallel and single threaded implementations of word counting
 */
public interface WordCountingService {
    WordCounter countWords(final List<File> files) throws Exception;
}
