package hw1.utils;

import hw1.utils.sa.SuffixArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrefixTableTest {



    @Test
    void testPrefixTable() {
        String baseString = "abracadabra";
        String text = baseString.repeat(1);

        int[] suffixArray = SuffixArray.create(text);
        int prefixLength = 2;
        var prefixTable = PrefixTable.create(text, suffixArray, prefixLength);

        DisplayUtils.displaySuffixArray(text, suffixArray);
        System.out.println("\nPrefix Table:");
        DisplayUtils.displayPrefixTable(text, suffixArray, prefixLength);
        // Check if all the suffixes with the same prefix are in the same interval

        for (int idx : suffixArray) {
            if (idx + prefixLength > text.length()) {
                System.out.println("" + idx + " is out of bounds");
                assertFalse(prefixTable.containsKey(idx));
                continue;
            }

            var interval = prefixTable.get(idx);
            if (interval == null)
                continue;

            var prefix = text.substring(idx, idx + prefixLength);

            // Check if the suffixes with the same prefix are in the same interval
            for (int j = interval.start; j < interval.end; j++) {
                var idx2 = suffixArray[j];
                var otherPrefix = text.substring(idx2, idx2 + prefixLength);
                assertEquals(prefix, otherPrefix);
            }

            // Check if the suffixes with different prefixes are not in the same interval
            if (interval.end < text.length() - prefixLength) {
                var idx2 = suffixArray[interval.end];
                var otherPrefix = text.substring(idx2, idx2 + prefixLength);
                assertNotEquals(prefix, otherPrefix);
            }
        }

    }

}