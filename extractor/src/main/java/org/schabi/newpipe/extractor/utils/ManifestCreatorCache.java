package org.schabi.newpipe.extractor.utils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Serializable serializable} cache class used by the extractor to cache manifests
 * generated with extractor's manifests generators.
 *
 * <p>
 * It relies internally on a {@link ConcurrentHashMap} to allow concurrent access to the cache.
 * </p>
 *
 * @param <K> the type of cache keys, which must be {@link Serializable serializable}
 * @param <V> the type of the second element of {@link Pair pairs} used as values of the cache,
 *            which must be {@link Serializable serializable}
 */
public final class ManifestCreatorCache<K extends Serializable, V extends Serializable>
        implements Serializable {

    private static final long serialVersionUID = -6722068473037537626L;

    /**
     * The default maximum size of a manifest cache.
     */
    public static final int DEFAULT_MAXIMUM_SIZE = Integer.MAX_VALUE;

    /**
     * The default clear factor of a manifest cache.
     */
    public static final double DEFAULT_CLEAR_FACTOR = 0.75;

    /**
     * The {@link ConcurrentHashMap} used internally as the cache of manifests.
     */
    private final ConcurrentHashMap<K, Pair<Integer, V>> concurrentHashMap;

    /**
     * The maximum size of the cache.
     *
     * <p>
     * The default value is {@link #DEFAULT_MAXIMUM_SIZE}.
     * </p>
     */
    private int maximumSize = DEFAULT_MAXIMUM_SIZE;

    /**
     * The clear factor of the cache, which is a double between {@code 0} and {@code 1} excluded.
     *
     * <p>
     * The default value is {@link #DEFAULT_CLEAR_FACTOR}.
     * </p>
     */
    private double clearFactor = DEFAULT_CLEAR_FACTOR;

    /**
     * Creates a new {@link ManifestCreatorCache}.
     */
    public ManifestCreatorCache() {
        concurrentHashMap = new ConcurrentHashMap<>();
    }

    /**
     * Tests if the specified key is in the cache.
     *
     * @param key the key to test its presence in the cache
     * @return {@code true} if the key is in the cache, {@code false} otherwise.
     */
    public boolean containsKey(final K key) {
        return concurrentHashMap.containsKey(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if the cache
     * contains no mapping for the key.
     *
     * @param key the key to which getting its value
     * @return the value to which the specified key is mapped, or {@code null}
     */
    @Nullable
    public Pair<Integer, V> get(final K key) {
        return concurrentHashMap.get(key);
    }

    /**
     * Adds a new element to the cache.
     *
     * <p>
     * If the cache limit is reached, oldest elements will be cleared first using the load factor
     * and the maximum size.
     * </p>
     *
     * @param key   the key to put
     * @param value the value to associate to the key
     *
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     * for the key (note that a null return can also indicate that the cache previously associated
     * {@code null} with the key).
     */
    @Nullable
    public V put(final K key, final V value) {
        if (!concurrentHashMap.containsKey(key) && concurrentHashMap.size() == maximumSize) {
            final int newCacheSize = (int) Math.round(maximumSize * clearFactor);
            keepNewestEntries(newCacheSize != 0 ? newCacheSize : 1);
        }

        final Pair<Integer, V> returnValue = concurrentHashMap.put(key,
                new Pair<>(concurrentHashMap.size(), value));
        return returnValue == null ? null : returnValue.getSecond();
    }

    /**
     * Clears the cached manifests.
     *
     * <p>
     * The cache will be empty after this method is called.
     * </p>
     */
    public void clear() {
        concurrentHashMap.clear();
    }

    /**
     * Resets the cache.
     *
     * <p>
     * The cache will be empty and the clear factor and the maximum size will be reset to their
     * default values.
     * </p>
     *
     * @see #clear()
     * @see #resetClearFactor()
     * @see #resetMaximumSize()
     */
    public void reset() {
        clear();
        resetClearFactor();
        resetMaximumSize();
    }

    /**
     * Returns the number of cached manifests in the cache.
     *
     * @return the number of cached manifests
     */
    public int size() {
        return concurrentHashMap.size();
    }

    /**
     * Gets the maximum size of the cache.
     *
     * @return the maximum size of the cache
     */
    public long getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the maximum size of the cache.
     *
     * If the current cache size is more than the new maximum size, the percentage of one less the
     * clear factor of the maximum new size of manifests in the cache will be removed.
     *
     * @param maximumSize the new maximum size of the cache
     * @throws IllegalArgumentException if {@code maximumSize} is less than or equal to 0
     */
    public void setMaximumSize(final int maximumSize) {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("Invalid maximum size");
        }

        if (maximumSize < this.maximumSize && !concurrentHashMap.isEmpty()) {
            final int newCacheSize = (int) Math.round(maximumSize * clearFactor);
            keepNewestEntries(newCacheSize != 0 ? newCacheSize : 1);
        }

        this.maximumSize = maximumSize;
    }

    /**
     * Resets the maximum size of the cache to its {@link #DEFAULT_MAXIMUM_SIZE default value}.
     */
    public void resetMaximumSize() {
        this.maximumSize = DEFAULT_MAXIMUM_SIZE;
    }

    /**
     * Gets the current clear factor of the cache, used when the cache limit size is reached.
     *
     * @return the current clear factor of the cache
     */
    public double getClearFactor() {
        return clearFactor;
    }

    /**
     * Sets the clear factor of the cache, used when the cache limit size is reached.
     *
     * <p>
     * The clear factor must be a double between {@code 0} excluded and {@code 1} excluded.
     * </p>
     *
     * <p>
     * Note that it will be only used the next time the cache size limit is reached.
     * </p>
     *
     * @param clearFactor the new clear factor of the cache
     * @throws IllegalArgumentException if the clear factor passed a parameter is invalid
     */
    public void setClearFactor(final double clearFactor) {
        if (clearFactor <= 0 || clearFactor >= 1) {
            throw new IllegalArgumentException("Invalid clear factor");
        }

        this.clearFactor = clearFactor;
    }

    /**
     * Resets the clear factor to its {@link #DEFAULT_CLEAR_FACTOR default value}.
     */
    public void resetClearFactor() {
        this.clearFactor = DEFAULT_CLEAR_FACTOR;
    }

    /**
     * Reveals whether an object is equal to a {@code ManifestCreator} cache existing object.
     *
     * @param obj the object to compare with the current {@code ManifestCreatorCache} object
     * @return whether the object compared is equal to the current {@code ManifestCreatorCache}
     * object
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ManifestCreatorCache<?, ?> manifestCreatorCache =
                (ManifestCreatorCache<?, ?>) obj;
        return maximumSize == manifestCreatorCache.maximumSize
                && Double.compare(manifestCreatorCache.clearFactor, clearFactor) == 0
                && concurrentHashMap.equals(manifestCreatorCache.concurrentHashMap);
    }

    /**
     * Returns a hash code of the current {@code ManifestCreatorCache}, using its
     * {@link #maximumSize maximum size}, {@link #clearFactor clear factor} and
     * {@link #concurrentHashMap internal concurrent hash map} used as a cache.
     *
     * @return a hash code of the current {@code ManifestCreatorCache}
     */
    @Override
    public int hashCode() {
        return Objects.hash(maximumSize, clearFactor, concurrentHashMap);
    }

    /**
     * Returns a string version of the {@link ConcurrentHashMap} used internally as the cache.
     *
     * @return the string version of the {@link ConcurrentHashMap} used internally as the cache
     */
    @Override
    public String toString() {
        return concurrentHashMap.toString();
    }

    /**
     * Keeps only the newest entries in a cache.
     *
     * <p>
     * This method will first collect the entries to remove by looping through the concurrent hash
     * map
     * </p>
     *
     * @param newLimit the new limit of the cache
     */
    private void keepNewestEntries(final int newLimit) {
        final int difference = concurrentHashMap.size() - newLimit;
        final ArrayList<Map.Entry<K, Pair<Integer, V>>> entriesToRemove = new ArrayList<>();

        for (final Map.Entry<K, Pair<Integer, V>> entry : concurrentHashMap.entrySet()) {
            final Pair<Integer, V> value = entry.getValue();
            if (value.getFirst() < difference) {
                entriesToRemove.add(entry);
            } else {
                value.setFirst(value.getFirst() - difference);
            }
        }

        for (final Map.Entry<K, Pair<Integer, V>> entry : entriesToRemove) {
            concurrentHashMap.remove(entry.getKey(), entry.getValue());
        }
    }
}
