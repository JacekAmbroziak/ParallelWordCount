package com.jacek.wordcount;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for the word count project
 *
 * @author Jacek R. Ambroziak
 */
final class Utils {
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
     * @param files text files to process
     * @param wordCounter target WordCounter to add word occurrences from text files of 1st arg
     */
    static void countWordsInFiles(final List<File> files, final WordCounter wordCounter) {
        for (final File file : files) {
            try {
                // we have an opportunity here to extract text from compressed formats, epub etc.
                final FileReader reader = new FileReader(file);
                countWordsFromReader(reader, wordCounter);
            } catch (IOException e) {
                System.out.println("e = " + e);
            }
        }
    }

    /**
     * @param dirName name of the directory to search
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
}
