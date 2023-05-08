package hw1.utils;

import static org.junit.jupiter.api.Assertions.*;

class CommonUtilsTest {

    @org.junit.jupiter.api.Test
    void binarySearch() {
        int[] arr = {2, 3, 3, 3, 5, 5, 7};
        assertEquals(0, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 1));
        assertEquals(0, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 2));
        assertEquals(1, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 3));
        assertEquals(4, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] > 3));
        assertEquals(4, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 4));
        assertEquals(4, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 5));
        assertEquals(6, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] > 5));
        assertEquals(6, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 6));
        assertEquals(6, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 7));
        assertEquals(7, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 8));
        assertEquals(7, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 9));
        assertEquals(7, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 10));
        assertEquals(7, CommonUtils.binarySearch(0, arr.length - 1, i -> arr[i] >= 11));
    }

    @org.junit.jupiter.api.Test
    void haveSamePrefix() {
        String text = "ATCGATCGATCGTAG";
        assertTrue(CommonUtils.haveSamePrefix(text, 0, 4, 4));
        assertTrue(CommonUtils.haveSamePrefix(text, 0, 4, 8));
        assertFalse(CommonUtils.haveSamePrefix(text, 0, 4, 9));
        assertTrue(CommonUtils.haveSamePrefix(text, 0, 8, 4));
        assertTrue(CommonUtils.haveSamePrefix(text, 4, 8, 4));
        assertTrue(CommonUtils.haveSamePrefix(text, 0, 4, 3));
        assertTrue(CommonUtils.haveSamePrefix(text, 4, 8, 4));
        assertFalse(CommonUtils.haveSamePrefix(text, 0, 8, 5));
        assertTrue(CommonUtils.haveSamePrefix(text, 1, 9, 1));
        assertFalse(CommonUtils.haveSamePrefix(text, 1, 13, 2));
        assertFalse(CommonUtils.haveSamePrefix(text, 5, 13, 2));
        assertThrows(IllegalArgumentException.class, () -> CommonUtils.haveSamePrefix(text, 1, 13, 4));
    }
}