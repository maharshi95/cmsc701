## CMSC 701 Computational Genomics Homework 1

* My solution is written with Java 18
* I referred to Suffix Array Construction by Ukkonen's Algorithm by GeeksforGeeks which takes O(n * logn * logn) 
   time to construct the suffix array. I compared that with the package I used -- [Bioinformatika]( * https://github.com/v-v/karkkainen-sanders/blob/master/code/java/Bioinformatika) --- which constructs
    the suffix array in O(n) time. I found that the Bioinformatika package is much faster.
* I also referred and partly used the [FastaSequence.java](https://www.cs.utexas.edu/~mobios/cs329e/rosetta/src/FastaSequence.java) source file from CS329e to read the fasta file.

## How to run the code:

How to run the code:

There are two main entry points to the code:

`hw1.buildsa.java` and `hw1.querysa.java`

To avoid any confusion, I have included the `buildsa` and `querysa` binaries in the root directory that internally 
called javac and java to run the code.

Use the `buildsa` binary in the root directory to build the suffix array.
It uses the following signature:

```bash
buildsa --preftab <k> <reference_file> <buildsa_output_file>
```

Use `querysa` binary in the root directory to query the suffix array.

```bash
querysa <index> <queries> <query_mode> <output_file>
```

You can also use `querysa_batch` binary to run the queries in batch mode if you have already prepared the query 
files using `create_queries.py`