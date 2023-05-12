# %%
import rbloom
import numpy as np
import time
from tqdm.auto import tqdm
import pandas as pd
from dataprep import prepare_data_and_queries

# Find ith permutation of size n 0th permutation is the sorted list of chars

# %%
benchmark_datasets = []
sizes = [1000, 2000, 5000, 10000, 20000]
for size in tqdm(sizes):
    data_and_queries = prepare_data_and_queries(
        50, size, size, random_state=42, deterministic=False
    )
    data_keys = data_and_queries["data"]
    new_keys = data_and_queries["queries_unseen"]
    benchmark_datasets.append([data_keys, new_keys])


# %%

# Create a Bloom filter with a capacity of 1000 elements and a false positive probability of 0.01%
# Define a custom hash function


def run_benchmark_bloom_filters(data_keys, query_keys, error_rate: float):
    bf = rbloom.Bloom(
        expected_items=len(data_keys),
        false_positive_rate=error_rate,
    )

    for key in data_keys:
        bf.add(key)

    data_keyset = set(data_keys)

    false_pos_counts = 0
    tic = time.time()
    n_unseen = 0
    for key in query_keys:
        if key not in data_keyset:
            n_unseen += 1
        if key in bf and key not in data_keyset:
            false_pos_counts += 1
    tac = time.time()

    time_taken = tac - tic

    fp_rate = false_pos_counts / n_unseen if n_unseen > 0 else 0.0
    return {
        "false_pos_rate": fp_rate,
        "time_taken": time_taken,
        "size": bf.size_in_bits / 8,
    }


error_rates = [0.5**i for i in [7, 8, 10]]
mixtures = [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]
outputs = []
for size, (data_keys, new_keys) in tqdm(
    zip(sizes, benchmark_datasets), total=len(sizes)
):
    for mixture in tqdm(mixtures, leave=False, desc="Mixture"):
        n_new_keys = int(mixture * len(new_keys))
        query_keys = new_keys[:n_new_keys] + data_keys[: len(new_keys) - n_new_keys]
        for error_rate in error_rates:
            output = run_benchmark_bloom_filters(data_keys, query_keys, error_rate)
            output["data_size"] = len(data_keys)
            output["query_size"] = len(query_keys)
            output["mixture"] = mixture
            output["error_rate"] = error_rate
            outputs.append(output)

bf_benchmark_df = pd.DataFrame(outputs)
bf_benchmark_df.to_csv("results/bloom_filter_benchmark.csv", index=False)
# %%
