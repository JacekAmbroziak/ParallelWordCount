package com.jacek.wordcount;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.BreakIterator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Jacek R. Ambroziak
 */
public final class TokenizationPerformanceComparison {
    // simple whitespace regex
    private final static Pattern WS_SPLITTER = Pattern.compile("\\s+");
    // single regex to take care of separation by whitespace and not including leading/trailing punctuation
    // punctuation can start a token at beginning of line or end a token at end of line
    private final static Pattern WS_PUNCT_SPLITTER = Pattern.compile("^\\p{Punct}+|\\p{Punct}*\\s+\\p{Punct}*|\\p{Punct}+$");

    /**
     * Use regex to take care of both whitespace splitting and punctuation stripping
     */
    private static Stream<String> tokenizeLines1(final Stream<String> lines) {
        return lines.flatMap(WS_PUNCT_SPLITTER::splitAsStream).filter(token -> !token.isEmpty());
    }

    /**
     * Use regex for whitespace splitting but a handcoded stripPunctuation function
     */
    private static Stream<String> tokenizeLines2(final Stream<String> lines) {
        return lines.flatMap(WS_SPLITTER::splitAsStream)
                .map(Punctuation::stripPunctuation)
                .filter(token -> !token.isEmpty());
    }

    /**
     * Use StringTokenized for WS splitting and a handcoded stripPunctuation function
     */
    private static long tokenizeLines3(final List<String> lines) {
        long counter = 0L;
        for (final String line : lines) {
            final StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                final String token = Punctuation.stripPunctuation(tokenizer.nextToken());
                if (token.length() > 0) {
                    ++counter;
                }
            }
        }
        return counter;
    }

    /**
     * "Hand optimized" whitespace based tokenization + punctuation stripping
     *
     * @param lines
     * @return token count
     */
    static long tokenizeLines4(final List<String> lines) {
        long counter = 0L;
        for (final String line : lines) {
            for (int i = line.length(); --i >= 0; ) {
                // search for non whitespace
                if (Character.isWhitespace(line.charAt(i))) {
                    // continue
                } else {
                    // now search for whitespace
                    final int end = i + 1;
                    while (--i >= 0 && !Character.isWhitespace(line.charAt(i))) {
                        // continue
                    }
                    final String cleaned = Punctuation.stripPunctuation(line.substring(i + 1, end));
                    if (cleaned.length() > 0) {
                        ++counter;
                    }
                }
            }
        }
        return counter;
    }

    /**
     * Use BreakIterator for WS splitting and a hand-coded stripPunctuation function
     */
    private static long tokenizeLines5(final List<String> lines) {
        final BreakIterator wordBoundary = BreakIterator.getWordInstance();
        long counter = 0L;
        for (final String line : lines) {
            wordBoundary.setText(line);
            for (int start = wordBoundary.first(), end = wordBoundary.next();
                    end != BreakIterator.DONE;
                    start = end, end = wordBoundary.next()) {
                final String substring = line.substring(start, end).trim();
                final String token = Punctuation.stripPunctuation(substring);
                if (token.length() > 0) {
                    ++counter;
                }
            }
        }
        return counter;
    }

    private static List<String> textLinesInDir(final String dirName) throws IOException {
        final List<String> allLines = new ArrayList<>();
        for (final File file : Utils.filesInDirWithExtension(dirName, ".txt")) {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    allLines.add(line);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("e = " + e);
            }
        }
        return allLines;
    }

    @Test
    public static void main(String[] args) {
        try {
            final List<String> allLines = TestUtils.unzipToLines(TestUtils.resourceFile("linux-4.9.95-docs.zip"));
            System.out.println("allLines size = " + allLines.size());
            {
                final long count1 = tokenizeLines1(allLines.stream()).count();
                System.out.println("count1 = " + count1);
                final long count2 = tokenizeLines2(allLines.stream()).count();
                System.out.println("count2 = " + count2);
                final long count3 = tokenizeLines3(allLines);
                System.out.println("count3 = " + count3);
                final long count4 = tokenizeLines4(allLines);
                System.out.println("count4 = " + count4);
                final long count5 = tokenizeLines5(allLines);
                System.out.println("count5 = " + count5);
            }

            final int nRuns = 10;
            {
                long minTime = Long.MAX_VALUE;
                for (int i = nRuns; --i >= 0; ) {
                    final Instant before = Instant.now();
                    final long count1 = tokenizeLines1(allLines.stream()).count();
                    minTime = Math.min(minTime, Duration.between(before, Instant.now()).toMillis());
                }
                System.out.println("regex ws + punct");
                System.out.println("minTime1 = " + minTime);
            }
            {
                long minTime = Long.MAX_VALUE;
                for (int i = nRuns; --i >= 0; ) {
                    final Instant before = Instant.now();
                    final long count2 = tokenizeLines2(allLines.stream()).count();
                    minTime = Math.min(minTime, Duration.between(before, Instant.now()).toMillis());
                }
                System.out.println("regex ws only, separate strip punctuation");
                System.out.println("minTime2 = " + minTime);
            }
            {
                long minTime = Long.MAX_VALUE;
                for (int i = nRuns; --i >= 0; ) {
                    final Instant before = Instant.now();
                    final long count3 = tokenizeLines3(allLines);
                    minTime = Math.min(minTime, Duration.between(before, Instant.now()).toMillis());
                }
                System.out.println("StringTokenizer, separate strip punctuation");
                System.out.println("minTime3 = " + minTime);
            }
            {
                long minTime = Long.MAX_VALUE;
                for (int i = nRuns; --i >= 0; ) {
                    final Instant before = Instant.now();
                    final long count4 = tokenizeLines4(allLines);
                    minTime = Math.min(minTime, Duration.between(before, Instant.now()).toMillis());
                }
                System.out.println("hand coded ws split, separate strip punctuation");
                System.out.println("minTime4 = " + minTime);
            }
            {
                long minTime = Long.MAX_VALUE;
                for (int i = nRuns; --i >= 0; ) {
                    final Instant before = Instant.now();
                    final long count5 = tokenizeLines5(allLines);
                    minTime = Math.min(minTime, Duration.between(before, Instant.now()).toMillis());
                }
                System.out.println("BreakIterator");
                System.out.println("minTime5 = " + minTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public static void runIt() {
        
    }
}
