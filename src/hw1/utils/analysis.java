package hw1.utils;

import hw1.Query;
import hw1.querysa;

import java.util.Arrays;
import java.util.List;

public class analysis {
    public static void main(String[] args) {

        int prefixLength = 8;
        FastaSequence fs = FastaSequence.parseFromFile("data/ecoli.fa");
        GeneIndex geneIndex = new GeneIndex(fs.getSequence(), prefixLength);

        String query = "AACGTGGCTGATGCGACTGAAGCGC";


        // verified
        System.out.println("Genome length:" + geneIndex.getGenome().length());
        System.out.println(Arrays.toString(GeneIndex.findAllOccurrencesGold(geneIndex.getGenome(), "AACGTGGCTGATGCGACTGAAGCGC")));

        var hits = geneIndex.findAllOccurrencesNaive(query);
        var speed_hits = geneIndex.findAllOccurrencesSimpleAccel(query);
        System.out.println("Naive: " + Arrays.toString(hits));
        System.out.println("Speedy: " + Arrays.toString(speed_hits));


        Query[] queries = querysa.readQueries("data/ecoli_queries_strong.fa");

        System.out.println("Read " + queries.length + " queries");


        long tic, tac;
        List<int[]> naiveHits;
        List<int[]> speedyHits;

        tic = System.currentTimeMillis();
        speedyHits = querysa.processQueries(queries, geneIndex, "simpaccel");
        tac = System.currentTimeMillis();
        System.out.println("Speedy: " + (tac - tic) + " ms");

        tic = System.currentTimeMillis();
        naiveHits = querysa.processQueries(queries, geneIndex, "naive");
        tac = System.currentTimeMillis();
        System.out.println("Naive: " + (tac - tic) + " ms");

        for (int i = 0; i < queries.length; i++) {
            Arrays.sort(naiveHits.get(i));
            Arrays.sort(speedyHits.get(i));
            if (!Arrays.equals(naiveHits.get(i), speedyHits.get(i))) {
                System.out.println("Error: " + queries[i].getHeader());
                System.out.println("Naive: " + Arrays.toString(naiveHits.get(i)));
                System.out.println("Speedy: " + Arrays.toString(speedyHits.get(i)));
            }
        }
    }
}
