package hw1;

public class Query {
    String header;
    String sequence;

    String description;

    public Query(String header, String sequence) {
        // Split header  by space only once
        String[] headerParts;
        try {
            headerParts = header.split(" ", 2);
        } catch (Exception e) {
            headerParts = new String[]{"", ""};
            headerParts[0] = header;
        }
        this.header = headerParts[0];
        this.description = headerParts[1];
        this.sequence = sequence;
    }

    public String getHeader() {
        return header;
    }

    public String getSequence() {
        return sequence;
    }

    /**
     * Returns a string representation of the query in FASTA format
     * Example: <br>
     * >query1<br>
     * ATCG
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(">" + header + "\n");
        sb.append(sequence + "\n");
        return sb.toString();
    }
}
