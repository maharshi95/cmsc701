package hw1.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public class CommonUtils {
    public static int binarySearch(int lo, int hi, IntPredicate predicate) {
        if (!predicate.test(hi))
            return hi + 1;

        while (lo < hi) {
            int mid = (lo + hi) / 2;
            if (predicate.test(mid)) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    /**
     * Checks if the suffix starting at s2 has the same prefix of length prefixLength as the suffix starting at s1.
     * Precondition: s1 + prefixLength <= text.length()
     * @param text The text
     * @param s1 The index of the first suffix
     * @param s2 The index of the second suffix
     * @param prefixLength The length of the prefix
     * @return true if the suffix starting at s2 has the same prefix of length prefixLength as the suffix starting at s1
     */
    static boolean haveSamePrefix(String text, int s1, int s2, int prefixLength) {
        if (s1 + prefixLength > text.length())
            throw new IllegalArgumentException("The first suffix should have at least prefixLength characters. Found: " + (text.length() - s1) + " characters");

        for (int i = 0; i < prefixLength; i++) {
            if (text.charAt(s1 + i) != text.charAt(s2 + i))
                return false;
        }
        return true;
    }

    /**
     * Finds all the occurences of `query` in the input text and returns the indices of the first character of each
     * occurrence. This is a naive implementation of the algorithm and is used for testing purposes only.
     *
     * @param text  The input text.
     * @param query The query string.
     * @return An array of indices of the first character of each occurrence of `query` in `text`.
     */
    public static int[] findAllOccurrencesGold(String text, String query) {
        List<Integer> result = new ArrayList<>();
        int i = 0;
        while (i < text.length() && i != -1) {
            i = text.indexOf(query, i);
            if (i != -1) {
                result.add(i);
                i++;
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }
}
