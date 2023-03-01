package hw1.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GeneIndex {

    char[] genome;

    int[] suffixArray;

    Map<String, Interval> prefixTable;

    int prefixLength = 0;

    /**
     * Returns the number of contiguously matching prefix characters between the `query` and the suffix starting at
     * `start`.
     *
     * @param start The start index of the suffix in the genome.
     * @param query The query string.
     * @return Longest common prefix length.
     */
    private int computeJointLCP(int start, String query, int currentLCP) {
        var i = currentLCP;
        int limit = Math.min(genome.length - start, query.length());
        while (i < limit && query.charAt(i) == genome[start + i]) {
            i++;
        }
        return i;
    }

    private int computeJointLCP(int start1, int start2, String query, int currentLCP) {
        var i = currentLCP;
        int limit = Math.min(genome.length - start1, genome.length - start2);
        limit = Math.min(limit, query.length());
        while (i < limit && genome[start1 + i] == genome[start2 + i]) {
            if (query.charAt(i) != genome[start1 + i])
                return -1;
            i++;
        }
        return i;
    }

    private int computeJointLCP(int start, String query) {
        return computeJointLCP(start, query, 0);
    }

    private int computeQuerySuffixLCP(int suffixIndex, String query) {
        var start = suffixArray[suffixIndex];
        return computeJointLCP(start, query);
    }

    private int computeQuerySuffixLCP(int suffixIndex, String query, int offset) {
        // Assumes that suffix and query already shares a prefix of length offset.

        // Debug block:
//        for (int i = 0; i < offset; i++) {
//            if (query.charAt(i) != genome[suffixArray[suffixIndex] + i]) {
//                var suffix_chunk = substring(suffixArray[suffixIndex], offset);
//                var query_chunk = substring(0, offset);
//                throw new IllegalArgumentException("The query and suffix do not share a prefix of length " + offset + ": " + suffix_chunk + " vs " + query_chunk);
//            }
//        }
//        var i = offset;
//        var start = suffixArray[suffixIndex];
//        while (i < query.length() && start + i < genome.length && query.charAt(i) == genome[start + i]) {
//            i++;
//        }
//        return i;
        return computeJointLCP(suffixArray[suffixIndex], query, offset);
    }

    public GeneIndex(char[] genome, int[] suffixArray, int prefixLength, Map<String, Interval> prefixTable) {
        this.genome = genome;
        this.suffixArray = suffixArray;
        this.prefixTable = prefixTable;
        this.prefixLength = prefixLength;
    }

    public String substring(int start, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + size && i < genome.length; i++) {
            sb.append(genome[i]);
        }
        return sb.toString();
    }

    /**
     * Returns the suffix chunk of size `size` starting at the given suffix index.
     * This is equivalent to calling {@link #substring(int, int)} with the suffix index and size.
     * Used for debugging purposes only.
     * @param suffixIndex The index of the suffix in the suffix array.
     * @param size The size of the suffix chunk.
     * @return The suffix chunk.
     */
    public String suffixChunk(int suffixIndex, int size) {
        return substring(suffixArray[suffixIndex], size);
    }

    public GeneIndex(String genome, int prefixLength) {
        this.genome = genome.toCharArray();
        this.suffixArray = SuffixArray.create(genome);

        if (prefixLength > genome.length())
            throw new IllegalArgumentException("Prefix length cannot be greater than genome length");

        if (prefixLength < 1)
            return;

        this.prefixLength = prefixLength;
        var prefixTable = PrefixTable.create(genome, suffixArray, prefixLength);
        this.prefixTable = new HashMap<>();
        for (var entry : prefixTable.entrySet()) {
            var interval = entry.getValue();
            String key = substring(entry.getKey(), prefixLength);
            this.prefixTable.put(key, interval.copy());
        }
    }

    public String getGenome() {
        return new String(genome);
    }

    public int[] getSuffixArray() {
        return suffixArray;
    }

    /**
     * Compare the query with the gene prefix of the suffix at the given index. <br><br>
     * <b>Preconditions:</b> <br>
     * <li> The query is at least as long as the prefix length
     * <li> The suffix array is sorted
     * <li> The query shares a prefix [of size upto p] as the suffix at the given index.
     *
     * @param suffix the index of the suffix in the suffix array
     * @param query  the query string
     * @param p      the length of a prefix shared by the query and the suffix. This need not be the longest prefix.
     * @return 0 if the query is equal to the gene prefix of the suffix at the given index,
     * -1 if the suffix is lexicographically smaller than the query, and
     * 1 if the suffix is lexicographically greater than the query.
     */
    private int compareSuffixWithQuery(int suffix, @NotNull String query, int p) {
        while (p < query.length()) {
            char ch = charAtSuffixPosition(suffix, p);
            if (ch == 0) return -1;
            if (ch != query.charAt(p)) return Character.compare(ch, query.charAt(p));
            p++;
        }
        return 0;
    }

    /**
     * Compare the query with the gene prefix of the suffix at the given index.
     *
     * @param suffixIndex the index of the suffix in the suffix array
     * @param query       the query string
     * @return 0 if the query is equal to the gene prefix of the suffix at the given index,
     * -1 if the suffix is lexicographically smaller than the query, and
     * 1 if the suffix is lexicographically greater than the query.
     */
    private int compareSuffixWithQuery(int suffixIndex, @NotNull String query) {
//        return substring(suffixArray[suffixIndex], query.length()).compareTo(query);
        return compareSuffixWithQuery(suffixIndex, query, 0);
    }


    private Interval findInitialRange(String query) {
        if (prefixTable == null || query.length() < prefixLength)
            return new Interval(0, suffixArray.length);

        return prefixTable.get(query.substring(0, prefixLength));
    }

    /**
     * Find all occurrences of the query in the genome using a naive binary search over the suffix array.
     *
     * @param query the query string
     * @return an array of indices of the occurrences of the query in the genome
     */
    public int[] findAllOccurrencesNaive(String query) {
        Interval range = findInitialRange(query);

        if (range == null) return new int[0];
        int lo = range.start;
        int hi = range.end - 1;

        int start = PrefixTable.binarySearch(lo, hi, i -> compareSuffixWithQuery(i, query) >= 0);
        int end = PrefixTable.binarySearch(lo, hi, i -> compareSuffixWithQuery(i, query) > 0);

        int[] result = new int[end - start];
        System.arraycopy(suffixArray, start, result, 0, end - start);
        return result;
    }

    private boolean statefulBinarySearchPredicate(int suffixIndex, String query, int lcp, char bound) {
        int cmp = compareSuffixWithQuery(suffixIndex, query, lcp);
        return bound == 'L' ? cmp >= 0 : cmp > 0;
    }

    private int statefulBinary(int lo, int hi, int lcp_lo, int lcp_hi, String query, char bound) {

        int cmp;
        boolean check;
        int mid, lcp;

        cmp = compareSuffixWithQuery(hi, query, lcp_hi);
        check = bound == 'L' ? cmp >= 0 : cmp > 0;
        if (!check)
            return hi + 1;

        lcp = Math.min(lcp_lo, lcp_hi);

        while (lo < hi) {
            lcp = computeJointLCP(suffixArray[lo], suffixArray[hi], query, lcp);
            if (lcp == -1) // lcp(lo, hi) < lcp(lo, p)
                return bound == 'L' ? -1 : lo; // return -1 if bound is 'L' and lo if bound is 'R'

            mid = (lo + hi) >>> 1;

            cmp = compareSuffixWithQuery(mid, query, lcp);
            check = bound == 'L' ? cmp >= 0 : cmp > 0;

            if (check)
                hi = mid;
             else
                lo = mid + 1;
        }
        return lo;
    }


    /**
     * Find all occurrences of the query in the genome using a naive binary search over the suffix array.
     *
     * @param query the query string
     * @return an array of indices of the occurrences of the query in the genome
     */
    public int[] findAllOccurrencesSimpleAccel(String query) {
        Interval range = findInitialRange(query);

        if (range == null) return new int[0];
        int lo = range.start;
        int hi = range.end - 1;

        int lcp_lo = computeQuerySuffixLCP(lo, query);
        int lcp_hi = computeQuerySuffixLCP(hi, query);

        int start = statefulBinary(lo, hi, lcp_lo, lcp_hi, query, 'L');
        if (start == -1)
            return new int[0];
        int end = statefulBinary(lo, hi, lcp_lo, lcp_hi, query, 'U');

        int[] result = new int[end - start];
        System.arraycopy(suffixArray, start, result, 0, end - start);
        return result;
    }

    private char charAtSuffixPosition(int suffixIndex, int position) {
        int genomeIndex = suffixArray[suffixIndex] + position;
        if (genomeIndex >= genome.length) return 0;
        return genome[genomeIndex];
    }

    /**
     * Compare the pth character of the suffix at the given index with the given character ch.
     *
     * @param ch          the character to compare with
     * @param suffixIndex the index of the suffix in the suffix array
     * @param p           the position of the character in the suffix
     * @return 0 if the pth character of the suffix at the given index is equal to ch, -1 if it is less than ch, 1 if it is greater than ch
     */
    int compareCharAtPosition(int suffixIndex, int p, char ch) {
        return Character.compare(charAtSuffixPosition(suffixIndex, p), ch);
    }

    public int[] findAllOccurrencesSpeedy(String query) {
        Interval range = findInitialRange(query);
        if (range == null) return new int[0];

        int lo = range.start;
        int hi = range.end;

        int p = query.length() >= prefixLength ? prefixLength : 0;
        while (p < query.length() && lo < hi) {
            char ch = query.charAt(p);
            if (charAtSuffixPosition(lo, p) == charAtSuffixPosition(hi - 1, p)) {
                if (ch != charAtSuffixPosition(lo, p))
                    return new int[0];
                p++;
                continue;
            }

            int pFinal = p;
            lo = PrefixTable.binarySearch(lo, hi - 1, i -> compareCharAtPosition(i, pFinal, ch) >= 0);
            if (lo == hi)
                return new int[0];
            hi = PrefixTable.binarySearch(lo, hi - 1, i -> compareCharAtPosition(i, pFinal, ch) > 0);
            p++;
        }

        int size = hi - lo;
        int[] result = new int[size];
        System.arraycopy(suffixArray, lo, result, 0, size);
        return result;

    }

    public static int[] findAllOccurrencesGold(@NotNull String text, String query) {
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

    void display() {
        System.out.println("Genome Size: " + genome.length);

        System.out.printf("Gen:");
        for (char c : genome) {
            System.out.printf("%2c ", c);
        }
        System.out.println();

        System.out.printf("Idx:");
        for (int i = 0; i < suffixArray.length; i++) {
            System.out.printf("%2d ", i);
        }
        System.out.println();

        System.out.printf("SA :");
        for (int j : suffixArray) {
            System.out.printf("%2d ", j);
        }
        System.out.println();


    }

    public static void main(String[] args) {
        String genome = "abrakadabrabracadabraabrabracadabra";

        // list all single character and double character queries
        var queries = new String[]{"a", "b", "r", "k", "d", "c", "ab", "ra", "ba", "br", "ad", "ra", "ab", "ra", "ca"
                , "ad", "ab", "ra", "ab", "ra", "ca", "ad", "ab", "ra", "abr", "dbr", "pf", "abra", "brac", "dabr",
                "bra", "dabra", "abrakadab", "aka", "akad", "abrakadabrabracadabraabra",
                "abracadabra", "cadabra", "abrabracadabra", "abrabracadabraabrabracadabra", "abra", "brac", "dabr", "abracadabra", "cadabra", "abrabracadabra", "abrabracadabraabrabracadabra"};


        BitSet naiveResults = new BitSet();
        BitSet speedyResults = new BitSet();

        var geneIndex = new GeneIndex(genome, 2);

        System.out.println("LCP of abrac and abrak " + geneIndex.computeJointLCP(0, "abrac"));

//        queries = new String[]{"ca"};
        geneIndex.display();
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
//            System.out.println("Query: " + query);
            int[] goldResult = findAllOccurrencesGold(genome, query);

            int[] naiveResult = geneIndex.findAllOccurrencesNaive(query);
            Arrays.sort(naiveResult);

            int[] speedyResult = geneIndex.findAllOccurrencesSimpleAccel(query);
            Arrays.sort(speedyResult);

            naiveResults.set(i, Arrays.equals(naiveResult, goldResult));
            speedyResults.set(i, Arrays.equals(speedyResult, goldResult));

            var result = speedyResult;

            if (result.length != goldResult.length) {
                System.out.println("Error: " + query);
                System.out.println("Result: " + Arrays.toString(result));
                System.out.println("Gold: " + Arrays.toString(goldResult));
                System.out.println();
            }
        }
        System.out.println("Naive: " + naiveResults.cardinality() + "/" + queries.length);
        System.out.println("Speedy: " + speedyResults.cardinality() + "/" + queries.length);
    }

}
