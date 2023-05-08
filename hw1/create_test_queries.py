import argparse
import numpy as np
from tqdm import tqdm

def parse_fasta_file(filename):
    """Parse a fasta file and return a list of sequences"""
    sequences = []
    current_sequence = []
    with open(filename, 'r') as f:
        for (i, line) in enumerate(f):
            if line.startswith('>'):
                if i > 0:
                    sequences.append(''.join(current_sequence))
                    current_sequence = []
            else:
                current_sequence.append(line.strip())
    if current_sequence:
        sequences.append(''.join(current_sequence))
    return sequences

def write_queries_in_fasta_format(queries, filename, chunk_size=500):
    """Write a list of queries in fasta format"""
    with open(filename, 'w') as f:
        for (i, query) in enumerate(queries):
            f.write('>query_{}\n'.format(i))
            for j in range(0, len(query), chunk_size):
                f.write(query[j:j + chunk_size])
                f.write('\n')

if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('--genome', type=str, default='data/ecoli.fa', help='Genome file in fasta format')
    parser.add_argument('--queries', type=str, default='data/ecoli_queries_weak.fa', help='Output file for queries in '
                                                                                      'fasta format')
    parser.add_argument('--num-queries', type=int, default=200000, help='Number of queries to generate')
    args = parser.parse_args()

    sequences = parse_fasta_file(args.genome)

    print('Found {} sequences'.format(len(sequences)))

    assert len(sequences) == 1
    genome = sequences[0]

    query_length_distribution = {
        10: 1,
        50: 1,
        100: 1,
        200: 2,
        500: 2,
        1000: 2,
        2000: 1,
    }

    n_queries_by_length = {
        length: int(num_queries * args.num_queries / sum(query_length_distribution.values()))
        for (length, num_queries) in query_length_distribution.items()
    }
    n_queries_by_length[2000] += args.num_queries - sum(n_queries_by_length.values())

    queries = []
    for (query_length, num_queries) in tqdm(n_queries_by_length.items()):
        for i in range(num_queries):

            if i % 3 == 0:
                # Generate 33 % of queries with a random start position
                start = np.random.randint(0, len(genome) - query_length)
                query = genome[start:start + query_length]
                assert type(query) == str

            elif i % 3 == 1:
                # Generate 33% of queries as random sequences
                query = ''.join(np.random.choice(['A', 'C', 'G', 'T'], size=query_length))
                assert type(query) == str

            else:
                # Generate 33% of queries mutated from the reference sequence
                start = np.random.randint(0, len(genome) - query_length)
                query = genome[start:start + query_length]
                query = list(query)
                for j in range(query_length):
                    if np.random.rand() < 0.1:
                        query[j] = np.random.choice(['A', 'C', 'G', 'T'])
                query = ''.join(query)
                assert type(query) == str

            queries.append(query)

    write_queries_in_fasta_format(queries, args.queries)
