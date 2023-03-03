package hw1.utils;

import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * This class will read first sequence from a Fasta format file.
 * This class is a modified version of the one defined
 * <a href="https://www.cs.utexas.edu/~mobios/cs329e/rosetta/src/FastaSequence.java">here</a>.
 */

public final class FastaSequence {

    private String[] header;
    private String[] sequence;

    private FastaSequence() {
    }

    public static FastaSequence parseFromFile(String filename) {
        FastaSequence fs = new FastaSequence();
        try(var fileInputStream = new FileInputStream(filename)) {
            fs.readSequenceFromInputStream(new BufferedInputStream(fileInputStream));
        } catch (IOException e) {
            System.out.println("Error when reading " + filename);
            e.printStackTrace();
        }
        return fs;
    }

    void readSequenceFromInputStream(InputStream inputStream) throws IOException {
        List<String> name = new ArrayList<String>();
        List<String> seq = new ArrayList<String>();


        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line = in.readLine();

        if (line == null)
            throw new IOException("Input stream is empty");

        if (line.charAt(0) != '>')
            throw new IOException("First line should start with '>'");
        else
            name.add(line.substring(1)); // remove '>' from the name
        for (line = in.readLine().trim(); line != null; line = in.readLine()) {
            if (line.length() > 0 && line.charAt(0) == '>') {
                seq.add(sb.toString());
                sb = new StringBuilder();
                name.add(line.substring(1)); // remove '>' from the name
            } else
                sb.append(line.trim());
        }
        if (sb.length() != 0)
            seq.add(sb.toString());

        header = new String[name.size()];
        sequence = new String[seq.size()];
        for (int i = 0; i < seq.size(); i++) {
            header[i] = name.get(i);
            sequence[i] = seq.get(i);
        }

    }

    //return first sequence as a String
    public String getSequence() {
        return sequence[0];
    }

    //return first xdescription as String
    public String getHeader() {
        return header[0];
    }

    //return sequence as a String
    public String getSequence(int i) {
        return sequence[i];
    }

    //return description as String
    public String getHeader(int i) {
        return header[i];
    }

    public String[] getSequences() {
        return sequence;
    }

    public String[] getHeaders() {
        return header;
    }

    public String getSequencesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String s : sequence) {
            sb.append(s);
        }
        return sb.toString();
    }

    public int size() {
        return sequence.length;
    }

    public static void main(String[] args) throws Exception {
        String fn = "";
        if (args.length > 0) fn = args[0];
        else {
            System.out.print("Enter the name of the FastaFile:");
            fn = (new BufferedReader(new InputStreamReader(System.in))).readLine();
        }
        FastaSequence fsf = FastaSequence.parseFromFile(fn);
        for (int i = 0; i < fsf.size(); i++) {
            System.out.println("One sequence read from file " + fn + " with length " + fsf.getSequence().length());
            System.out.println("description: \n" + fsf.getHeader(i));
            System.out.println("Sequence: \n" + fsf.getSequence(i));
        }
    }

}

