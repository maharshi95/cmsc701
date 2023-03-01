package hw1.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class PrefixTable {

    public static int binarySearch(int lo, int hi, Predicate<Integer> predicate) {
        if (!predicate.test(hi))
            return hi + 1;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (predicate.test(mid)) {
                hi = mid;
            } else {
                lo = mid + 1;
            }
        }
        return lo;
    }

    private static boolean haveSamePrefix(String text, int s1, int s2, int prefixLength) {
        if (s1 + prefixLength > text.length() || s2 + prefixLength > text.length())
            return false;

        for (int i = 0; i < prefixLength; i++) {
            if (text.charAt(s1 + i) != text.charAt(s2 + i))
                return false;
        }
        return true;
    }


    public static @NotNull Map<Integer, Interval> create(String text, int @NotNull [] S,
                                                         int prefixLength) {
        var table = new HashMap<Integer, Interval>();

        var prefixIndexMap = new HashMap<String, Integer>();

        int suffixIndex = 0;
        while (suffixIndex < S.length) {

            // Skip over the suffixes with length less than prefixLength
            while (suffixIndex < S.length && S[suffixIndex] + prefixLength > text.length())
                suffixIndex++;

            if (suffixIndex == S.length)
                continue; // No more suffixes with length >= prefixLength

            var start = suffixIndex;
            var end = binarySearch(suffixIndex + 1, S.length - 1, j -> !haveSamePrefix(text, S[start], S[j], prefixLength));

            var prefix = text.substring(S[suffixIndex], S[suffixIndex] + prefixLength);

            if (!prefixIndexMap.containsKey(prefix))
                prefixIndexMap.put(prefix, S[suffixIndex]);
            int prefixIndex = prefixIndexMap.get(prefix);

            table.put(prefixIndex, new Interval(start, end));

            suffixIndex = end;
        }
        return table;
    }
}
