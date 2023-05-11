# %%
import functools
import bloom_filter2
import numpy as np
import time


@functools.cache
def factorial(n):
    if n < 0:
        raise ValueError("n must be a non-negative integer")
    return 1 if n == 0 else n * factorial(n - 1)


def find_permutation(i: int, n: int):
    if i >= factorial(n):
        raise ValueError("i must be less than n!")
    if n == 1:
        return [i]
    chunk_size = factorial(n - 1)
    first_element = i // chunk_size
    next_i = i % chunk_size
    next_perm = find_permutation(next_i, n - 1)
    next_perm = [x if x < first_element else x + 1 for x in next_perm]
    return [first_element] + next_perm


def find_ith_seq(i: int, n: int, chars: list[str]):
    if i >= len(chars) ** n:
        raise ValueError("i must be less than len(chars) ** n.")
    if n == 1:
        return [chars[i]]
    chunk_size = len(chars) ** (n - 1)
    first_element = chars[i // chunk_size]
    next_i = i % chunk_size
    next_perm = find_ith_seq(next_i, n - 1, chars)
    return [first_element] + next_perm


def generate_gene_strings(
    length: int, num_strings: int, random_state: np.random.RandomState = None
):
    if random_state is None:
        random_state = np.random.RandomState(42)
    chars = ["A", "C", "G", "T"]

    n_total_permutations = len(chars) ** length
    # Sample from the set of all possible permutations

    samples = random_state.choice(n_total_permutations, num_strings, replace=False)
    keys = []
    for s in samples:
        seq = find_ith_seq(s, length, chars)
        key = "".join(seq)
        keys.append(key)
    assert len(keys) == len(set(keys))
    assert len(keys) == num_strings
    return keys


def prepare_data_and_queries(
    length: int, n_data: int, n_queries: int, random_state: np.random.RandomState = None
):
    n_total = n_data + n_queries
    keys = generate_gene_strings(length, n_total, random_state)
    data = keys[:n_data]
    queries = keys[n_data:]
    return {
        "data": data,
        "queries_seen": data,
        "queries_unseen": queries,
    }


# %%


# Find ith permutation of size n 0th permutation is the sorted list of chars

resp = prepare_data_and_queries(
    10, 10000, 10000, random_state=np.random.RandomState(42)
)
data = resp["data"]
queries_seen = resp["queries_seen"]
queries_unseen = resp["queries_unseen"]

# generate_gene_strings(5, 20)

# %%


# Create a Bloom filter with a capacity of 1000 elements and a false positive probability of 0.01%
# Define a custom hash function
hash_fn = lambda x: hash(x) % 1000

bf = bloom_filter2.BloomFilter(max_elements=len(data), error_rate=0.01)
bf_custom = bloom_filter2.BloomFilter(
    max_elements=len(data), error_rate=0.01, hash_fn=hash_fn
)

tic = time.time()
for key in data:
    bf.add(key)
tac = time.time()
print("Time to add {} elements: {:.2f} seconds".format(len(data), tac - tic))


false_neg_counts = 0
tic = time.time()
for key in queries_seen:
    if key not in bf:
        false_neg_counts += 1
tac = time.time()
print("False negative rate: {:.2f}%".format(100 * false_neg_counts / len(queries_seen)))
print("Time to query {} elements: {:.2f} seconds".format(len(queries_seen), tac - tic))

tic = time.time()
false_pos_counts = 0
for key in queries_unseen:
    if key in bf:
        false_pos_counts += 1
tac = time.time()
print(
    "False positive rate: {:.2f}%".format(100 * false_pos_counts / len(queries_unseen))
)
print(
    "Time to query {} elements: {:.2f} seconds".format(len(queries_unseen), tac - tic)
)

# %%

import numpy as np

mat = np.random.rand(1000, 1000)

np.savez_compressed("data/mat.npz", mat=mat)
np.savez("data/mat.npz", mat=mat)
