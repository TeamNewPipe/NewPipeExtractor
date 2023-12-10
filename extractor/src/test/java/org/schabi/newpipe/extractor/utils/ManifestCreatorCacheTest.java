package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManifestCreatorCacheTest {
    @Test
    void basicMaximumSizeAndResetTest() {
        final ManifestCreatorCache<String, String> cache = new ManifestCreatorCache<>();

        // 30 elements set -> cache resized to 23 -> 5 new elements set to the cache -> 28
        cache.setMaximumSize(30);
        setCacheContent(cache);
        assertEquals(28, cache.size(),
                "Wrong cache size with default clear factor and 30 as the maximum size");
        cache.reset();

        assertEquals(0, cache.size(),
                "The cache has been not cleared after a reset call (wrong cache size)");
        assertEquals(ManifestCreatorCache.DEFAULT_MAXIMUM_SIZE, cache.getMaximumSize(),
                "Wrong maximum size after cache reset");
        assertEquals(ManifestCreatorCache.DEFAULT_CLEAR_FACTOR, cache.getClearFactor(),
                "Wrong clear factor after cache reset");
    }

    @Test
    void maximumSizeAndClearFactorSettersAndResettersTest() {
        final ManifestCreatorCache<String, String> cache = new ManifestCreatorCache<>();
        cache.setMaximumSize(20);
        cache.setClearFactor(0.5);

        setCacheContent(cache);
        // 30 elements set -> cache resized to 10 -> 5 new elements set to the cache -> 15
        assertEquals(15, cache.size(),
                "Wrong cache size with 0.5 as the clear factor and 20 as the maximum size");

        // Clear factor and maximum size getters tests
        assertEquals(0.5, cache.getClearFactor(),
                "Wrong clear factor gotten from clear factor getter");
        assertEquals(20, cache.getMaximumSize(),
                "Wrong maximum cache size gotten from maximum size getter");

        // Resetters tests
        cache.resetMaximumSize();
        assertEquals(ManifestCreatorCache.DEFAULT_MAXIMUM_SIZE, cache.getMaximumSize(),
                "Wrong maximum cache size gotten from maximum size getter after maximum size "
                        + "resetter call");

        cache.resetClearFactor();
        assertEquals(ManifestCreatorCache.DEFAULT_CLEAR_FACTOR, cache.getClearFactor(),
                "Wrong clear factor gotten from clear factor getter after clear factor resetter "
                        + "call");
    }

    /**
     * Adds sample strings to the provided manifest creator cache, in order to test clear factor and
     * maximum size.
     * @param cache the cache to fill with some data
     */
    private static void setCacheContent(final ManifestCreatorCache<String, String> cache) {
        int i = 0;
        while (i < 26) {
            cache.put(String.valueOf((char) ('a' + i)), "V");
            ++i;
        }

        i = 0;
        while (i < 9) {
            cache.put("a" + (char) ('a' + i), "V");
            ++i;
        }
    }
}
