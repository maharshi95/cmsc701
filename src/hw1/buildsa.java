package hw1;

import hw1.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class buildsa {

    static void saveOutputs(String filename, @NotNull String genome, int prefixLength, int @NotNull [] suffixArray,
                            Map<Integer, Interval> prefixTable) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("" + genome.length());
            writer.write("\n");

            writer.write(genome);
            writer.write("\n");

            for (int idx : suffixArray) {
                writer.write("" + idx);
                writer.write(" ");
            }
            writer.write("\n");

            writer.write("" + prefixLength + " " + prefixTable.size() + "\n");
            for (Map.Entry<Integer, Interval> entry : prefixTable.entrySet()) {
                var idx = entry.getKey();
                var interval = entry.getValue();
                writer.write(idx + " " + interval.start + " " + interval.end + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            assert args[0].equals("--preftab"): "Invalid argument: " + args[0];
            try {
                prefixLength = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid prefix length: " + args[1] + ". Must be an integer.");
                System.exit(1);
            }
            assert prefixLength >= 1: "Invalid prefix length: " + args[1] + ". Must be >= 1.";
            reference = args[2];
            outputFilename = args[3];
        } else {
            System.err.println("Invalid number of arguments. Expected 2 or 4, got " + args.length);
            System.exit(1);
        }

        String genome = FastaSequence.parseFromFile(reference).getSequence();

        int[] suffixArray = SuffixArray.create(genome);

        var prefixTable = PrefixTable.create(genome, suffixArray, prefixLength);

        saveOutputs(outputFilename, genome, prefixLength, suffixArray, prefixTable);

    }
}
