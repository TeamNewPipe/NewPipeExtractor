package org.schabi.newpipe.extractor.utils.cache;

import com.grack.nanojson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundCloudCommentsCacheTest {
    @Test
    void testInstantiation() {
        assertThrows(RuntimeException.class, () -> new SoundCloudCommentsCache(-15));
        assertThrows(RuntimeException.class, () -> new SoundCloudCommentsCache(0));
        assertDoesNotThrow(() -> new SoundCloudCommentsCache(1));
        assertDoesNotThrow(() -> new SoundCloudCommentsCache(10));
    }

    @Test
    void testSize() {
        SoundCloudCommentsCache cache = new SoundCloudCommentsCache(10);
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
        cache.put("a", new JsonObject(), new JsonObject(), 1);
        assertEquals(1, cache.size());
        cache.put("b", new JsonObject(), new JsonObject(), 1);
        assertEquals(2, cache.size());
        cache.put("c", new JsonObject(), new JsonObject(), 1);
        assertEquals(3, cache.size());
        cache.put("a", new JsonObject(), new JsonObject(), 1);
        assertEquals(3, cache.size());
        cache.put("b", new JsonObject(), new JsonObject(), 1);
        assertEquals(3, cache.size());
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void testLRUStrategy() {
        final SoundCloudCommentsCache cache = new SoundCloudCommentsCache(4);
        cache.put("1", new JsonObject(), new JsonObject(), 1);
        cache.put("2", new JsonObject(), new JsonObject(), 2);
        cache.put("3", new JsonObject(), new JsonObject(), 3);
        cache.put("4", new JsonObject(), new JsonObject(), 4);
        cache.put("5", new JsonObject(), new JsonObject(), 5);
        assertNull(cache.get("1"));
        final SoundCloudCommentsCache.CachedCommentInfo cci = cache.get("2");
        assertNotNull(cci);
        cache.put("6", new JsonObject(), new JsonObject(), 6);
        assertNotNull(cache.get("2"));
        assertNull(cache.get("3"));
        cache.put("7", new JsonObject(), new JsonObject(), 7);
        cache.put("8", new JsonObject(), new JsonObject(), 8);
        cache.put("9", new JsonObject(), new JsonObject(), 9);
        assertNull(cache.get("1"));
        assertNull(cache.get("3"));
        assertNull(cache.get("4"));
        assertNull(cache.get("5"));
        assertNotNull(cache.get("2"));
    }

    @Test
    void testStorage() {
        final SoundCloudCommentsCache cache = new SoundCloudCommentsCache(10);
        cache.put("1", new JsonObject(), new JsonObject(), 1);
        cache.put("1", new JsonObject(), new JsonObject(), 2);
        assertEquals(2, cache.get("1").index);
        cache.put("1", new JsonObject(), new JsonObject(), 3);
        assertEquals(3, cache.get("1").index);
    }

    @Test
    void testClear() {
        final SoundCloudCommentsCache cache = new SoundCloudCommentsCache(10);
        cache.put("1", new JsonObject(), new JsonObject(), 1);
        cache.put("2", new JsonObject(), new JsonObject(), 2);
        cache.put("3", new JsonObject(), new JsonObject(), 3);
        cache.put("4", new JsonObject(), new JsonObject(), 4);
        cache.put("5", new JsonObject(), new JsonObject(), 5);
        cache.clear();
        assertTrue(cache.isEmpty());
        assertEquals(0, cache.size());
    }

}
