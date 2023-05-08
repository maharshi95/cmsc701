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

def get_random_cut(ref_text: str, length: int, mutation: bool = False):
    start = np.random.randint(0, len(genome) - query_length)
    query = list(ref_text[start:start + length])

    if mutation:
        k = int(0.8 * length)
        for i in range(k, length):
            if np.random.rand() < 0.3:
                query[i] = np.random.choice(['A', 'C', 'G', 'T'])
    return "".join(query)


if __name__ == '__main__':

    query_lengths = [10, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000]

    def get_reference(name: str):
        return 'data/references/{}.fa'.format(name)

    def get_output_filename(reference: str, query_length: int):
        return 'data/queries/{}_queries_{}.fa'.format(reference, query_length)


    references = ['saureus', 'ecoli', 'fruitfly']

    for reference in tqdm(references, desc='Generating queries'):
        genome = parse_fasta_file(get_reference(reference))[0]
        for query_length in tqdm(query_lengths, desc='Generating queries for length', leave=False):
            if query_length > len(genome):
                continue
            queries = []
            if query_length < 500:
                num_queries = 20000
            elif query_length < 5000:
                num_queries = 5000
            elif query_length < 50000:
                num_queries = 1000
            else:
                num_queries = 100
            for i in range(num_queries):
                query = get_random_cut(genome, query_length, mutation=i % 2 == 0)
                queries.append(query)
            write_queries_in_fasta_format(queries, get_output_filename(reference, query_length))

