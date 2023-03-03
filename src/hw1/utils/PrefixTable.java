package hw1.utils;

import java.util.HashMap;
import java.util.Map;

public class PrefixTable {


    public static Map<Integer, Interval> create(String text, int[] S, int prefixLength) {
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
            var end = CommonUtils.binarySearch(suffixIndex + 1, S.length - 1, j -> !CommonUtils.haveSamePrefix(text, S[start], S[j], prefixLength));

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
