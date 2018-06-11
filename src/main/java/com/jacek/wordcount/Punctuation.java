package com.jacek.wordcount;

/**
 * @author Jacek R. Ambroziak
 */
final class Punctuation {
    private final static String EMPTY_STRING = "";
    private final static String PUNCTUATION_CHARS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private final static boolean[] IS_PUNCTUATION = new boolean[128];

    static {
        // initialize punctuation array
        for (int i = PUNCTUATION_CHARS.length(); --i >= 0; ) {
            IS_PUNCTUATION[PUNCTUATION_CHARS.charAt(i)] = true;
        }
    }

    private static boolean isPunctuation(final int ch) {
        return ch < 128 && IS_PUNCTUATION[ch];
    }

    static String stripPunctuation(final String input) {
        int j = input.length();
        while (--j >= 0 && isPunctuation(input.charAt(j))) {
            // continue
        }
        if (j < 0) {
            return EMPTY_STRING;
        } else {    // j >= 0, points at some non-punctuation char
            int i = 0;
            while (i < j && isPunctuation(input.charAt(i))) {
                ++i;
            }
            return input.substring(i, j + 1);
        }
    }
}
