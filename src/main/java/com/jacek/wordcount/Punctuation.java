package com.jacek.wordcount;

/**
 * Utility class defining a set punctuation characters
 * and providing a method to strip leading and trailing punctuation characters from input String
 */
final class Punctuation {
    private final static String EMPTY_STRING = "";
    private final static String PUNCTUATION_CHARS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private final static boolean[] IS_PUNCTUATION = new boolean[128];

    static {
        // initialize IS_PUNCTUATION array
        for (int i = PUNCTUATION_CHARS.length(); --i >= 0; ) {
            IS_PUNCTUATION[PUNCTUATION_CHARS.charAt(i)] = true;
        }
    }

    private static boolean isPunctuation(final int ch) {
        return ch < 128 && IS_PUNCTUATION[ch];
    }

    /**
     * @param input string possibly starting/ending with punctuation
     * @return substring of input with leading and trailing punctuation characters removed
     */
    static String stripPunctuation(final String input) {
        if (input != null) {
            int j = input.length();
            // look for trailing punctuation chars in input
            while (--j >= 0 && isPunctuation(input.charAt(j))) {
                // continue
            }
            if (j < 0) {    // string was empty or contained only punctuation
                return EMPTY_STRING;
            } else {    // j >= 0, points at some non-punctuation char
                int i = 0;
                // look for leading punctuation characters
                while (i < j && isPunctuation(input.charAt(i))) {
                    ++i;
                }
                return input.substring(i, j + 1);
            }
        } else {
            return null;
        }
    }
}
