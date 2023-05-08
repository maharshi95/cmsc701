package hw1.utils;

import hw1.utils.sa.FastSuffixArray;
import hw1.utils.sa.SuffixArray;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class SuffixArrayTest {

    Integer[] createSuffixArrayBrute(String text) {
        Integer [] suffixArray = new Integer[text.length()];
        for (int i = 0; i < text.length(); i++) {
            suffixArray[i] = i;
        }
        Arrays.sort(suffixArray, Comparator.comparing(text::substring));
        return suffixArray;
    }
    @ParameterizedTest(name = "Test suffix array for {0}")
    @ValueSource(strings = {"banana", "mississippi", "AbRAcaDABra"})
    void testSuffixArray(String text) {

        int[] suffixArray = SuffixArray.create(text);
        Integer[] suffixArrayBrute = createSuffixArrayBrute(text);

        for(int i = 0; i < text.length(); i++) {
            assertEquals(suffixArrayBrute[i], suffixArray[i]);
        }

        int[] suffixArray2 = FastSuffixArray.create(text.toCharArray());

        for(int i = 0; i < text.length(); i++) {
            assertEquals(suffixArrayBrute[i], suffixArray2[i]);
        }

    }
}
