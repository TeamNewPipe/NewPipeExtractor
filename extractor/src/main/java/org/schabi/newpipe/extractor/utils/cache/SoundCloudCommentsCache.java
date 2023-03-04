package org.schabi.newpipe.extractor.utils.cache;

import com.grack.nanojson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * LRU cache which can contain a few items.
 */
public class SoundCloudCommentsCache {

    private final int maxSize;
    private final Map<String, CachedCommentInfo> store;
    public SoundCloudCommentsCache(final int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1");
        }
        store = new HashMap<>(size);
        maxSize = size;
    }

    public void put(@Nonnull final String key, @Nonnull final JsonObject comment,
                    @Nonnull final JsonObject json, final int index) {
        if (store.size() == maxSize) {
            store.remove(
                    store.entrySet().stream()
                    .reduce((a, b) -> a.getValue().lastHit < b.getValue().lastHit ? a : b)
                            .get().getKey());
        }
        store.put(key, new CachedCommentInfo(comment, json, index));
    }

    @Nullable
    public CachedCommentInfo get(final String key) {
        final CachedCommentInfo result = store.get(key);
        if (result == null) {
            return null;
        }
        result.lastHit = System.nanoTime();
        return result;
    }

    public int size() {
        return store.size();
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    public void clear() {
        store.clear();
    }

    public final class CachedCommentInfo {
        @Nonnull public final JsonObject comment;
        @Nonnull public final JsonObject json;
        public final int index;
        private long lastHit = System.nanoTime();

        private CachedCommentInfo(@Nonnull final JsonObject comment,
                                  @Nonnull final JsonObject json,
                                  final int index) {
            this.comment = comment;
            this.json = json;
            this.index = index;
        }
    }

}
