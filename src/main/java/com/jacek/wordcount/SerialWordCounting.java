package com.jacek.wordcount;

import java.io.File;
import java.util.List;

final class SerialWordCounting implements WordCountingService {
    @Override
    public WordCounter countWords(final List<File> files) {
        final WordCounter wordCounter = new WordCounter();
        Utils.countWordsInFiles(files, wordCounter);
        return wordCounter;
    }
}
