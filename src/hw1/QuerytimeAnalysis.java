package hw1;

import hw1.utils.FastaSequence;
import hw1.utils.GeneIndex;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class QuerytimeAnalysis {

    public static double benchMark1kQueries(GeneIndex geneIndex, Query[] queries, String queryMode) {
        long tic, tac;
        List<int[]> hits;

        tic = System.currentTimeMillis();
        querysa.processQueries(queries, geneIndex, queryMode);
        tac = System.currentTimeMillis();
        double timeTaken = (tac - tic);
        double nk = queries.length / 1000.0;
        return (timeTaken / nk);
    }

    public static void main(String[] args) {
        String[] refNames = {"ecoli", "fruitfly", "saureus"};

        int[] queryLengths = {10, 50, 100, 200, 500, 1000, 2000, 10000, 20000, 50000};

        for(String refName: refNames) {
            var binaryFilename = "data/binaries/" + refName + "_k0.sa";
            GeneIndex geneIndex = GeneIndex.deserializeFromFile(binaryFilename);
            System.out.println(refName + " index loaded");
            System.out.println("Length\tNaive\tSimpAccel");
            for(var qlength: queryLengths) {
                if(refName.equals("saureus") && qlength > 20000)
                    continue;
                var queryFilename = "data/queries/" + refName + "_queries_" + qlength + ".fa";
                Query[] queries = querysa.readQueries(queryFilename);
                var timeTakenNaive = benchMark1kQueries(geneIndex, queries, "naive");
                var timeTakenSimpAccel = benchMark1kQueries(geneIndex, queries, "simpaccel");
                System.out.printf("%6d\t%5.2f\t%5.2f%n", qlength, timeTakenNaive, timeTakenSimpAccel);
            }
        }
        System.out.println("\n");
    }
}
