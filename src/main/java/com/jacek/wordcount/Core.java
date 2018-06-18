package com.jacek.wordcount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for the word count project
 */
final class Core {
    private static final Logger log = Logger.getLogger(Core.class.getName());

    /**
     * @param reader      a reader like FileReader, InputStreamReader, StringReader, etc.
     *                    This function closes the reader after exhausting it's contents
     * @param wordCounter
     * @throws IOException
     */
    static void countWordsFromReader(final Reader reader, final WordCounter wordCounter) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            final StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                final String token = Punctuation.stripPunctuation(tokenizer.nextToken());
                if (token.length() > 0) {
                    wordCounter.countWord(token.toLowerCase());
                }
            }
        }
        bufferedReader.close();
    }

    /**
     * @param files       text files to process
     * @param wordCounter target WordCounter to add word occurrences from text files of 1st arg
     */
    static void countWordsInFiles(final List<File> files, final WordCounter wordCounter) {
        final Instant before = Instant.now();
        for (final File file : files) {
            try {
                // we have an opportunity here to extract text from compressed formats, epub etc.
                final FileReader reader = new FileReader(file);
                countWordsFromReader(reader, wordCounter);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
        wordCounter.updateBatchStats(files.size(), Duration.between(before, Instant.now()).toMillis());
    }

    /**
     * @param dirName   name of the directory to search
     * @param extension file extension, eg. .txt, of files we search for and will include in the result
     * @return list of files in input directory or its subdirectories with names ending in extension
     * @throws IOException
     */
    static List<File> filesInDirWithExtension(final String dirName, final String extension) throws IOException {
        try (final Stream<Path> paths = Files.walk(Paths.get(dirName))) {
            return paths.filter(path -> path.getFileName().toString().endsWith(extension))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    private static List<String> textLinesInDir(final String dirName) throws IOException {
        final List<String> allLines = new ArrayList<>();
        for (final File file : filesInDirWithExtension(dirName, ".txt")) {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    allLines.add(line);
                }
                bufferedReader.close();
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
        return allLines;
    }
}
