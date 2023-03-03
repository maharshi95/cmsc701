package hw1;

import hw1.utils.*;
import hw1.utils.sa.FastSuffixArray;

import java.io.*;
import java.util.Map;

public class buildsa {

    public static void saveBinary(String filename, String genome, int prefixLength, int[] suffixArray, Map<Integer,
            Interval> prefixTable) {
        File file = new File(filename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(genome);
            oos.writeObject(suffixArray);
            oos.writeObject(prefixLength);
            oos.writeObject(prefixTable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        String reference = null;
        String outputFilename = null;

        int prefixLength = 0;

        if (args.length == 2) {
            reference = args[0];
            outputFilename = args[1];
        } else if (args.length == 4) {
            assert args[0].equals("--preftab") : "Invalid argument: " + args[0];
            try {
                prefixLength = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid prefix length: " + args[1] + ". Must be an integer.");
                System.exit(1);
            }
            assert prefixLength >= 1 : "Invalid prefix length: " + args[1] + ". Must be >= 1.";
            reference = args[2];
            outputFilename = args[3];
        } else {
            System.err.println("Invalid number of arguments. Expected 2 or 4, got " + args.length);
            System.exit(1);
        }

        long tic, tac;

        System.out.printf("Reference: %s\n", reference);
        double fileLength = new File(reference).length() / Math.pow(2, 20);
        System.out.printf("File size: %.2f MB\n", fileLength);

        tic = System.currentTimeMillis();
        String genome = FastaSequence.parseFromFile(reference).getSequencesAsString();
        genome = genome.toUpperCase();
        tac = System.currentTimeMillis();

        System.out.println("Genome length: " + genome.length());
        System.out.println("Time to read genome: " + (tac - tic) + " ms");

        tic = System.currentTimeMillis();
        int[] suffixArray = FastSuffixArray.create(genome.toCharArray());
        tac = System.currentTimeMillis();
        System.out.println("Time to create suffix array: " + (tac - tic) + " ms");

        tic = System.currentTimeMillis();
        var prefixTable = PrefixTable.create(genome, suffixArray, prefixLength);
        tac = System.currentTimeMillis();
        System.out.println("Time to create prefix table: " + (tac - tic) + " ms");

        saveBinary(outputFilename, genome, prefixLength, suffixArray, prefixTable);

        File file = new File(outputFilename);
        System.out.printf("Binary file size: %.2f MB\n", file.length() / Math.pow(2, 20));
    }
}
