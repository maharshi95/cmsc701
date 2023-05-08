package hw1.utils;

import hw1.utils.sa.FastSuffixArray;
import hw1.utils.sa.SuffixArray;
import java.io.*;
import java.util.*;

import static hw1.utils.CommonUtils.binarySearch;

public class GeneIndex implements Serializable {

    char[] genome;

    int[] suffixArray;

    private Map<String, Interval> prefixTable;

    int prefixLength = 0;

    /**
     * Returns the number of contiguously matching prefix characters between the `query` and the suffix starting at
     * `start`. The `currentLCP` parameter is used to skip over the characters that have already been matched.
     *
     * @param start      The start index of the suffix in the genome.
     * @param query      The query string.
     * @param currentLCP The current longest common prefix length.
     * @return Longest common prefix length.
     */
    private int computeLCP(int start, String query, int currentLCP) {
        var i = currentLCP;
        int limit = Math.min(genome.length - start, query.length());
        while (i < limit && query.charAt(i) == genome[start + i]) {
            i++;
        }
        return i;
    }

    private int computeLCP(int start, String query) {
        return computeLCP(start, query, 0);
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

    public GeneIndex(char[] genome, int[] suffixArray, int prefixLength, Map<String, Interval> prefixTable) {
        this.genome = genome;
        this.suffixArray = suffixArray;
        this.prefixTable = prefixTable;
        this.prefixLength = prefixLength;
    }


    public GeneIndex(String genome, int prefixLength) {
        this.genome = genome.toCharArray();
        this.suffixArray = FastSuffixArray.create(this.genome);

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

    public Map<String, Interval> getPrefixTable() {
        return prefixTable;
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
    public int compareSuffixWithQuery(int suffix, String query, int p) {
        if (suffix < 0 || suffix >= suffixArray.length)
            throw new IllegalArgumentException("Invalid suffix index: " + suffix);
        if (p < 0 || p > query.length())
            throw new IllegalArgumentException("Invalid position p for comparing the suffix with Query: " + p);

        int i = p;
        while (i < query.length()) {
            if (suffixArray[suffix] + i >= genome.length) return -1; // suffix is shorter than query (suffix < query
            char ch = genome[suffixArray[suffix] + i];
            if (ch != query.charAt(i)) return Character.compare(ch, query.charAt(i));
            i++;
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
    private int compareSuffixWithQuery(int suffixIndex, String query) {
        return compareSuffixWithQuery(suffixIndex, query, 0);
    }


    public Interval findInitialRange(String query) {
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

        int start = binarySearch(lo, hi, i -> compareSuffixWithQuery(i, query) >= 0);
        int end = binarySearch(lo, hi, i -> compareSuffixWithQuery(i, query) > 0);

        int[] result = new int[end - start];
        System.arraycopy(suffixArray, start, result, 0, end - start);
        return result;
    }

    private boolean statefulBinarySearchPredicate(int suffixIndex, String query, int lcp, char bound) {
        int cmp = compareSuffixWithQuery(suffixIndex, query, lcp);
        return bound == 'L' ? cmp >= 0 : cmp > 0;
    }

    private int statefulBinary(int lo, int hi, int lcp, String query, char bound) {
        boolean check;
        int mid;

        check = statefulBinarySearchPredicate(hi, query, lcp, bound);
        if (!check)
            return hi + 1;

        while (lo < hi) {
            lcp = computeJointLCP(suffixArray[lo], suffixArray[hi], query, lcp);
            if (lcp == -1) // lcp(lo, hi) < lcp(lo, p)
                return bound == 'L' ? -1 : lo; // return -1 if bound is 'L' and lo if bound is 'R'
            mid = (lo + hi) >>> 1;
            check = statefulBinarySearchPredicate(mid, query, lcp, bound);
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

//        int lcp = Math.min(computeLCP(suffixArray[lo], query), computeLCP(suffixArray[hi], query));
        int lcp = computeJointLCP(suffixArray[lo], suffixArray[hi], query, 0);
        if (lcp == -1) // lcp(lo, hi) < lcp(lo, p)
            return new int[0];

        int start = statefulBinary(lo, hi, lcp, query, 'L');
        if (start == -1)
            return new int[0];
        int end = statefulBinary(lo, hi, lcp, query, 'U');

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
     * Deserialize a GeneIndex from a file and return it.
     * Refer to {@link} for the serialization format.
     *
     * @param filename the name of the file to deserialize from
     * @return the deserialized GeneIndex
     */
    public static GeneIndex deserializeFromFile(String filename) {
        File file = new File(filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            var genome = (String) ois.readObject();
            var suffixArray = (int[]) ois.readObject();
            var prefixLength = (int) ois.readObject();

            @SuppressWarnings("unchecked")
            var prefixTable = (Map<Integer, Interval>) ois.readObject();

            Map<String, Interval> prefixStringTable = new HashMap<>();
            for (var entry : prefixTable.entrySet()) {
                int prefixIndex = entry.getKey();
                int start = entry.getValue().start;
                int end = entry.getValue().end;
                var prefix = genome.substring(prefixIndex, prefixIndex + prefixLength);
                prefixStringTable.put(prefix, new Interval(start, end));
            }
            return new GeneIndex(genome.toCharArray(), suffixArray, prefixLength, prefixStringTable);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private String substring(int start, int size) {
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
     *
     * @param suffixIndex The index of the suffix in the suffix array.
     * @param size        The size of the suffix chunk.
     * @return The suffix chunk.
     */
    private String suffixChunk(int suffixIndex, int size) {
        return substring(suffixArray[suffixIndex], size);
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

        System.out.println("LCP of abrac and abrak " + geneIndex.computeLCP(0, "abrac"));

//        queries = new String[]{"ca"};
        geneIndex.display();
        for (int i = 0; i < queries.length; i++) {
            String query = queries[i];
//            System.out.println("Query: " + query);
            int[] goldResult = CommonUtils.findAllOccurrencesGold(genome, query);

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
