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
final class Common {
    private static void countWordsFromReader(final Reader reader, final WordCounter wordCounter) throws IOException {
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
    }

    static void countWordsInFiles(final List<File> files, final WordCounter wordCounter) {
        for (final File file : files) {
            try {
                final FileReader reader = new FileReader(file);
                countWordsFromReader(reader, wordCounter);
                reader.close();
            } catch (IOException e) {
                System.out.println("e = " + e);
            }
        }
    }

    private static List<File> filesInDirWithExtension(final String dirName, final String extension) throws IOException {
        try (final Stream<Path> paths = Files.walk(Paths.get(dirName))) {
            return paths.filter(path -> path.getFileName().toString().endsWith(extension))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    static List<File> textFilesInDir(final String dirName) throws IOException {
        return filesInDirWithExtension(dirName, ".txt");
    }
}
