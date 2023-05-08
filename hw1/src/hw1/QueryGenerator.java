package hw1;

import hw1.utils.*;
import hw1.utils.sa.SuffixArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class QueryGenerator {

    public static Query[] makeStrongQueries(String genome, int topk) {

        int[] suffixArray = SuffixArray.create(genome);

        int[] prefixLengths = {25, 30, 32, 35, 40, 50, 60, 70, 80, 90, 100, 150, 200, 250, 300};

        int repeatCount = 10000 / prefixLengths.length;

        List<Query> queries = new ArrayList<>();
        int query_idx = 0;

        String header;
        String querySeq;

        for (int prefixLength : prefixLengths) {
            System.out.println("Processing prefix length " + prefixLength + " ...");

            var prefixTable = PrefixTable.create(genome, suffixArray, prefixLength);

            //sort the prefix table by the Interval.size() where Interval is the value of the Map. Take the first 10
            //entries and print them out. This is the top 10 most frequent prefixes.

            for (int i = 0; i < topk; i++) {
                int max = 0;
                int maxIndex = 0;
                for (Map.Entry<Integer, Interval> entry : prefixTable.entrySet()) {
                    var interval = entry.getValue();
                    if (interval.size() > max) {
                        max = interval.size();
                        maxIndex = entry.getKey();
                    }
                }
                var geneIndex = suffixArray[maxIndex];
                prefixTable.remove(maxIndex);

                querySeq = genome.substring(geneIndex, geneIndex + prefixLength);
                header = "query_" + query_idx + " size " + max;
                queries.add(new Query(header, querySeq));
                query_idx++;

                for (int j = 0; j < repeatCount; j++) {
                    var mutation_chunk_size = (int) (prefixLength * 0.2);
                    var mutation_start_index = prefixLength - mutation_chunk_size;
                    int mutationIndex = mutation_start_index + (int) (Math.random() * mutation_chunk_size);
                    // Sample one of the A, C, G, T
                    var mutation = "ACGT".charAt((int) (Math.random() * 4));
                    querySeq = querySeq.substring(0, mutationIndex) + mutation + querySeq.substring(mutationIndex + 1);
                    header = "query_" + query_idx + " size " + max + " mutation at " + mutationIndex;
                    queries.add(new Query(header, querySeq));
                    query_idx++;
                }
            }
        }
        return queries.toArray(new Query[0]);
    }

    static void writeQueriesToFile(String filename, Query[] queries) {
        try {
            FileWriter writer = new FileWriter(filename);
            for (Query query : queries) {
                writer.write(">" + query.getHeader() + "\n");
                writer.write(query.getSequence());
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        String filename = "data/ecoli.fa";

        String genome = FastaSequence.parseFromFile(filename).getSequence();

        writeQueriesToFile(
                "data/ecoli_queries_strong.fa",
                Objects.requireNonNull(
                        makeStrongQueries(genome, 20)
                )
        );
    }
}

