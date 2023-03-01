package hw1.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplayUtils {
    public static void displayPrefixTable(String text, int[] suffixArray, int prefixLength) {
        var prefixTable = PrefixTable.create(text, suffixArray, prefixLength);
        List<String> strings = new ArrayList<>();
        for (Map.Entry<Integer, Interval> entry : prefixTable.entrySet()) {
            var idx = entry.getKey();
            var prefix = text.substring(idx, idx + prefixLength);
            strings.add(prefix);
        }
        strings.sort(String::compareTo);

        for (Map.Entry<Integer, Interval> entry : prefixTable.entrySet()) {
            var idx = entry.getKey();
            var interval = entry.getValue();
            var prefix = text.substring(idx, idx + prefixLength);
            System.out.printf("%2d %s: [%2d %2d) %n", idx, prefix, interval.start, interval.end);
        }
    }

    public static void displaySuffixArray(String text, int @NotNull [] suffixArray) {
        System.out.println(text);
        System.out.println("Suffix Array:");
        for(int i=0; i<suffixArray.length; i++)
            System.out.printf("%2d: %2d %s%n", i, suffixArray[i], text.substring(suffixArray[i]));
    }
}
