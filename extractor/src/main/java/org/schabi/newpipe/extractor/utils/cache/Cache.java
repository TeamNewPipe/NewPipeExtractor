package org.schabi.newpipe.extractor.utils.cache;

public interface Cache<K, V> {
    void put(K key, V value);
    V get(K key);
    int size();
    boolean isEmpty();
    void clear();
}
