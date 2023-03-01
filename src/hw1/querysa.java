package hw1;

import hw1.utils.FastaSequence;
import hw1.utils.GeneIndex;
import hw1.utils.Interval;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class querysa {
    public static GeneIndex loadFromFile(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new FileReader(filename));
        int genomeLength = in.nextInt();
        String genome = in.next();
        int[] suffixArray = new int[genomeLength];

        for (int i = 0; i < genomeLength; i++) {
            suffixArray[i] = in.nextInt();
        }
        int prefixLength = in.nextInt();
        int prefixTableSize = in.nextInt();

        Map<String, Interval> prefixTable = new HashMap<>();
        for (int i = 0; i < prefixTableSize; i++) {
            int prefixIndex = in.nextInt();
            int start = in.nextInt();
            int end = in.nextInt();
            var prefix = genome.substring(prefixIndex, prefixIndex + prefixLength);
            prefixTable.put(prefix, new Interval(start, end));
        }
        return new GeneIndex(genome.toCharArray(), suffixArray, prefixLength, prefixTable);
    }

    static Query @NotNull [] readQueries(String filename, int minSize) {
        var fasta = FastaSequence.parseFromFile(filename);
        var queries = new ArrayList<Query>();
        for (int i = 0; i < fasta.size(); i++) {
            var q = new Query(fasta.getHeader(i), fasta.getSequence(i));
            if (!q.header.contains("size")) {
                queries.add(q);
                continue;
            }
            var qName = q.header.split(" ");
            var size = Integer.parseInt(qName[2]);
            if (size >= minSize) {
                queries.add(q);
            }
        }
        return queries.toArray(new Query[0]);
    }

    public static Query @NotNull [] readQueries(String filename) {
        return readQueries(filename, 0);
    }

    public static List<int[]> processQueries(Query[] queries, GeneIndex geneIndex, String queryMode) {
        List<int[]> hits = new ArrayList<>();
        for (var query : queries) {
            int[] queryHits = switch (queryMode) {
                case "naive" -> geneIndex.findAllOccurrencesNaive(query.sequence);
                case "simpaccel" -> geneIndex.findAllOccurrencesSimpleAccel(query.sequence);
                default -> throw new IllegalArgumentException("Query mode must be either 'naive' or 'simpaccel'");
            };
            hits.add(queryHits);
        }
        return hits;
    }

    static void writeQueryResults(String filename, Query[] queries, List<int[]> hits) {
        try (var out = new java.io.PrintWriter(filename)) {
            for (int i = 0; i < queries.length; i++) {
                var query = queries[i];
                var queryHits = hits.get(i);
                out.printf("%s\t%d\t", query.header, queryHits.length);
                for (int i_hit = 0; i_hit < queryHits.length; i_hit++) {
                    out.printf("%d", queryHits[i_hit]);
                    if (i_hit < queryHits.length - 1)
                        out.printf("\t");

                }
                out.println();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        assert args.length != 4 : "Usage: querysa <binary index file> <query file> <query mode> <output>";

        String binaryFilename = args[0];
        String queryMode = args[2];
        String queryFilename = args[1];
        String outputFilename = args[3];


        if (!queryMode.equals("naive") && !queryMode.equals("simpaccel"))
            throw new IllegalArgumentException("Query mode must be either 'naive' or 'simpaccel'");

//        binaryFilename = "data/ecoli.sa";

        GeneIndex geneIndex = loadFromFile(binaryFilename);

        System.out.println("Built GeneIndex from " + binaryFilename);

        Query[] queries = readQueries(queryFilename);

        System.out.println("Read " + queries.length + " queries");

        long tic, tac;
        List<int[]> naiveHits;
        List<int[]> speedyHits;

        tic = System.currentTimeMillis();
        speedyHits = processQueries(queries, geneIndex, "simpaccel");
        tac = System.currentTimeMillis();
        System.out.println("Speedy: " + (tac - tic) + " ms");

        tic = System.currentTimeMillis();
        naiveHits = processQueries(queries, geneIndex, "naive");
        tac = System.currentTimeMillis();
        System.out.println("Naive: " + (tac - tic) + " ms");

        for (int i = 0; i < queries.length; i++) {
            Arrays.sort(naiveHits.get(i));
            Arrays.sort(speedyHits.get(i));
            if (!Arrays.equals(naiveHits.get(i), speedyHits.get(i))) {
                System.out.println("Error: " + queries[i].header);
                System.out.println("Naive: " + Arrays.toString(naiveHits.get(i)));
                System.out.println("Speedy: " + Arrays.toString(speedyHits.get(i)));
            }
        }

        writeQueryResults(outputFilename, queries, naiveHits);
    }
}
