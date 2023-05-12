import functools
import numpy as np
from typing import Optional


@functools.cache
def factorial(n):
    if n < 0:
        raise ValueError("n must be a non-negative integer")
    return 1 if n == 0 else n * factorial(n - 1)


def get_random_state(random_state: Optional[int | np.random.RandomState]):
    if random_state is None:
        random_state = np.random.RandomState(42)
    elif isinstance(random_state, int):
        random_state = np.random.RandomState(random_state)
    else:
        assert isinstance(random_state, np.random.RandomState)
    return random_state


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
    length: int,
    num_strings: int,
    random_state: Optional[int | np.random.RandomState] = None,
):
    random_state = get_random_state(random_state)

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


def generate_gene_strings_non_deterministic(length, num_strings, random_state):
    random_state = get_random_state(random_state)
    strings = set()
    while len(strings) < num_strings:
        buffer = []
        for i in range(length):
            char = random_state.choice(["A", "C", "G", "T"])
            buffer.append(char)
        strings.add("".join(buffer))
    strings = list(strings)
    random_state.shuffle(strings)
    return strings


def prepare_data_and_queries(
    length: int,
    n_data: int,
    n_queries: int,
    random_state: np.random.RandomState = None,
    deterministic: bool = True,
):
    n_total = n_data + n_queries
    if deterministic:
        keys = generate_gene_strings(length, n_total, random_state)
    else:
        keys = generate_gene_strings_non_deterministic(length, n_total, random_state)
    data = keys[:n_data]
    queries = keys[n_data:]
    return {
        "data": data,
        "queries_seen": data,
        "queries_unseen": queries,
    }
