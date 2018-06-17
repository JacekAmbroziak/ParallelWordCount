package com.jacek.wordcount;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

final class SerialWordCounting implements WordCountingService {
    @Override
    public WordCounter countWords(final List<File> files) {
        final WordCounter wordCounter = new WordCounter();
        Core.countWordsInFiles(files, wordCounter);
        return wordCounter;
    }
}
