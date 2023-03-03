package hw1;

import hw1.utils.CommonUtils;
import hw1.utils.sa.FastSuffixArray;
import hw1.utils.FastaSequence;
import hw1.utils.GeneIndex;

import java.util.Arrays;
import java.util.List;

public class analysis {
    public static void main(String[] args) {

        long tic, tac;

        int prefixLength = 8;

        tic = System.currentTimeMillis();
        String genomeName = "ecoli";
        FastaSequence fs = FastaSequence.parseFromFile("data/ecoli.fa");
        tac = System.currentTimeMillis();

        System.out.println("Genome reading time: " + (tac - tic) + " ms");


        String genome = fs.getSequencesAsString();

        System.out.println("Genome length: " + genome.length());

//        tic = System.currentTimeMillis();
////        var suffixArray = FastSuffixArray.create(fs.getSequence());
//        tac = System.currentTimeMillis();
//        System.out.println("Suffix Array creation time: " + (tac - tic) + " ms");

//        for (prefixLength = 20; prefixLength <= 40; prefixLength += 5) {
//            tic = System.currentTimeMillis();
//            var prefixTable = PrefixTable.create(genome, suffixArray, prefixLength);
//            tac = System.currentTimeMillis();
//            System.out.println("Prefix Table creation time for prefix length " + prefixLength + ": " + (tac - tic) + " ms");
//
//            String outputfile = "data/analysis/" + genomeName + "_" + prefixLength + ".sa";
//
//            buildsa.saveBinary(outputfile, genome, prefixLength, suffixArray, prefixTable);
//
//        }

        prefixLength = 0;
        GeneIndex geneIndex = new GeneIndex(genome, prefixLength);

        int[] fastSA = FastSuffixArray.create(genome.toCharArray());

        for(int i = 0; i < fastSA.length; i++) {
            if (fastSA[i] != geneIndex.getSuffixArray()[i]) {
                throw new RuntimeException("Mismatch at " + i + ": " + fastSA[i] + " vs " + geneIndex.getSuffixArray()[i]);
            }
        }

        String query = "TCAAAAAAAT";


        // verified
        System.out.println("Genome length:" + geneIndex.getGenome().length());
        System.out.println(Arrays.toString(CommonUtils.findAllOccurrencesGold(geneIndex.getGenome(), query)));
//        geneIndex.get = null;
        System.out.println(geneIndex.findInitialRange(query));
        var hits = geneIndex.findAllOccurrencesNaive(query);
        var speed_hits = geneIndex.findAllOccurrencesSimpleAccel(query);
        Arrays.sort(hits);
        Arrays.sort(speed_hits);
        System.out.println("Naive: " + Arrays.toString(hits));
        System.out.println("Speedy: " + Arrays.toString(speed_hits));

//        System.out.println(geneIndex);

        Query[] queries = querysa.readQueries("data/queries/ecoli_queries_strong.fa");

        System.out.println("Read " + queries.length + " queries");

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
