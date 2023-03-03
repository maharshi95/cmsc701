package hw1;

import hw1.utils.FastaSequence;
import hw1.utils.GeneIndex;
import hw1.utils.Interval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class querysa {

    static Query[] readQueries(String filename, int minSize) {
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

    public static Query[] readQueries(String filename) {
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

    public static void main(String[] args) {

        assert args.length != 4 : "Usage: querysa <binary index file> <query file> <query mode> <output>";

        String binaryFilename = args[0];
        String queryMode = args[2];
        String queryFilename = args[1];
        String outputFilename = args[3];


        if (!queryMode.equals("naive") && !queryMode.equals("simpaccel"))
            throw new IllegalArgumentException("Query mode must be either 'naive' or 'simpaccel'");

        GeneIndex geneIndex = GeneIndex.deserializeFromFile(binaryFilename);

        System.out.println("Built GeneIndex from " + binaryFilename);

        Query[] queries = readQueries(queryFilename);

        System.out.println("Read " + queries.length + " queries");

        long tic, tac;
        List<int[]> hits;

        tic = System.currentTimeMillis();
        hits = processQueries(queries, geneIndex, queryMode);
        tac = System.currentTimeMillis();
        double timeTaken = (tac - tic);
        double nk = queries.length / 1000.0;

        System.out.printf("Time taken per 1k queries: %.2f ms%n", timeTaken / nk);

        writeQueryResults(outputFilename, queries, hits);
    }
}
