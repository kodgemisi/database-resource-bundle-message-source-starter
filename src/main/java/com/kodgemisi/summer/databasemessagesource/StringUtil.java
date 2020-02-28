package com.kodgemisi.summer.databasemessagesource;

class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Cannot be instantiated!");
    }

    static String getNamePart(String namePart) {
        // Intentionally not using isBlank in order to not mask/shadow any data inconsistency in the db.
        // Name parts are never supposed to be blank. They are either empty or valid strings.
        return isEmpty(namePart) ? "" : '_' + namePart;
    }

    static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * <p>Copied from {@link org.apache.commons.lang3.StringUtils#countMatches(java.lang.CharSequence, char)}</p>
     *
     * <p>Counts how many times the char appears in the given string.</p>
     *
     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", 0)  = 0
     * StringUtils.countMatches("abba", 'a')   = 2
     * StringUtils.countMatches("abba", 'b')  = 2
     * StringUtils.countMatches("abba", 'x') = 0
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param ch  the char to count
     * @return the number of occurrences, 0 if the CharSequence is {@code null}
     */
    static int countMatches(CharSequence str, char ch) {
        if (isEmpty(str)) {
            return 0;
        } else {
            int count = 0;

            for(int i = 0; i < str.length(); ++i) {
                if (ch == str.charAt(i)) {
                    ++count;
                }
            }

            return count;
        }
    }
}
