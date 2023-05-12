# %%
import os
import time
import numpy as np
from bitarray import bitarray
from bbhash_table import BBHashTable
import hashlib

from pyrsistent import b
import dataprep
from tqdm.auto import tqdm
from importlib import reload

dataprep = reload(dataprep)
from dataprep import prepare_data_and_queries

# %%
benchmark_datasets = []
sizes = [1000, 5000, 10000, 50000]
for size in tqdm(sizes):
    data_and_queries = prepare_data_and_queries(
        50, size, size, random_state=42, deterministic=False
    )
    data_keys = data_and_queries["data"]
    new_keys = data_and_queries["queries_unseen"]
    benchmark_datasets.append([data_keys, new_keys])


# %%
def naive_hash(s, nbits: int = 64):
    return hash(s) & (2**nbits - 1)


def hash_string_to_ulong(string, nbits=64):
    # Convert the string to a byte array.
    byte_array = string.encode("utf-8")

    # Create a hash object.
    hash_object = hashlib.sha512()

    # Update the hash object with the byte array.
    hash_object.update(byte_array)

    # Get the hash value.
    hash_value = hash_object.digest()

    # Convert the hash value to a ulong.
    ulong_hash_value = int.from_bytes(hash_value, "big") % (2**nbits)

    return ulong_hash_value


class FingerprintArray:
    def __init__(self, k_bits: int):
        self.k_bits = k_bits
        n = 2**k_bits
        self.array = bitarray(n)
        self.array.setall(0)

    def _make_hash(self, key):
        h = hash_string_to_ulong(key)
        h = h & (2**self.k_bits - 1)
        return h

    def __contains__(self, key):
        h = self._make_hash(key)
        return self.array[h] == 1

    def add(self, key):
        h = self._make_hash(key)
        self.array[h] = 1

    def count(self):
        return np.sum(self.array)

    def usage(self):
        return self.count() / len(self.array)


class MPHFTable:
    def __init__(self, keys: list[str], hash_func: callable):
        self.table = BBHashTable()
        self.hash_func = hash_func
        self.table.initialize([self.hash_func(key) for key in keys])

    def __contains__(self, key):
        h = self.hash_func(key)
        return self.table[h] is not None

    def __getitem__(self, key):
        h = self.hash_func(key)
        return self.table[h]

    def __setitem__(self, key, value):
        h = self.hash_func(key)
        self.table[h] = value


def run_benchmark_mphf(data_keys, query_keys, mphf_base):
    hash_func = lambda x: naive_hash(x, mphf_base)

    mph_table = MPHFTable(data_keys, hash_func)
    datakeys_set = set(data_keys)

    # store fingerprints
    for i, key in enumerate(data_keys):
        mph_table[key] = i
    # retrieve & count for all (which will include hashes not in MPHF)
    tic = time.time()
    n_keys_new = 0
    false_pos_counts = 0
    for key in query_keys:
        if key not in datakeys_set:
            n_keys_new += 1
        if key in mph_table and key not in datakeys_set:
            false_pos_counts += 1
    fp_rate = false_pos_counts / n_keys_new if n_keys_new > 0 else 0.0
    tac = time.time()

    tmp_filename = "/tmp/mphf_table.bin"
    mph_table.table.mphf.save(tmp_filename)
    mphf_size = os.path.getsize(tmp_filename)

    return {
        "false_pos_rate": fp_rate * 100,
        "query_time": tac - tic,
        "data_size": len(data_keys),
        "mphf_size": mphf_size,
        "use_fingerprint": False,
    }


def run_benchmark_with_fingerprint(data_keys, query_keys, mphf_base, fp_nbits):
    hash_func = lambda x: naive_hash(x, mphf_base)

    mph_table = MPHFTable(data_keys, hash_func)
    fp_array = FingerprintArray(fp_nbits)

    datakeys_set = set(data_keys)

    # store fingerprints
    for i, key in enumerate(data_keys):
        mph_table[key] = i
        fp_array.add(key)

    # retrieve & count for all (which will include hashes not in MPHF)
    tic = time.time()
    n_keys_new = 0
    false_pos_counts = 0
    for key in query_keys:
        if key not in datakeys_set:
            n_keys_new += 1
        if key in mph_table and key in fp_array and key not in datakeys_set:
            false_pos_counts += 1
    fp_rate = false_pos_counts / n_keys_new if n_keys_new > 0 else 0.0
    tac = time.time()

    tmp_filename = "/tmp/mphf_table.bin"
    mph_table.table.mphf.save(tmp_filename)
    mphf_size = os.path.getsize(tmp_filename)

    return {
        "false_pos_rate": fp_rate * 100,
        "query_time": tac - tic,
        "fp_nbits": fp_nbits,
        "fp_usage": fp_array.usage(),
        "data_size": len(data_keys),
        "mphf_size": mphf_size,
        "fp_array_size": np.ceil(len(fp_array.array) / 64) * 8,
        "use_fingerprint": True,
    }


mixtures = [1.0]
fp_nbits = [8, 10, 12, 14, 16]

mphf_benchmark_outputs = []
fingerprint_benchmark_outputs = []
for size, benchmark_dataset in tqdm(zip(sizes, benchmark_datasets), total=len(sizes)):
    for mixture in mixtures:
        data_keys, new_keys = benchmark_dataset
        n_new_keys = int(mixture * len(new_keys))
        query_keys = new_keys[:n_new_keys] + data_keys[: len(new_keys) - n_new_keys]
        mphf_output = run_benchmark_mphf(data_keys, new_keys, 16)
        mphf_output["mixture"] = mixture
        mphf_benchmark_outputs.append(mphf_output)
        for fp_nbits in [8, 10, 12, 14, 16]:
            mphf_fp_output = run_benchmark_with_fingerprint(
                data_keys, new_keys, 16, fp_nbits
            )
            fingerprint_benchmark_outputs.append(mphf_fp_output)
            fingerprint_benchmark_outputs[-1]["mixture"] = mixture

# %%
import pandas as pd

benchmark_df = pd.DataFrame(mphf_benchmark_outputs)
benchmark_df.to_csv("results/mphf_results.csv")

fp_benchmark_df = pd.DataFrame(fingerprint_benchmark_outputs)
fp_benchmark_df.to_csv("results/mphf_fp_results.csv")

from matplotlib import pyplot as plt
