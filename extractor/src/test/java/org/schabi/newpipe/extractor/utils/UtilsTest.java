package org.schabi.newpipe.extractor.utils;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {
    @Test
    void testMixedNumberWordToLong() throws ParsingException {
        assertEquals(10, Utils.mixedNumberWordToLong("10"));
        assertEquals(10.5e3, Utils.mixedNumberWordToLong("10.5K"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10.5M"), 0.0);
        assertEquals(10.5e6, Utils.mixedNumberWordToLong("10,5M"), 0.0);
        assertEquals(1.5e9, Utils.mixedNumberWordToLong("1,5B"), 0.0);
    }

    @Test
    void testJoin() {
        assertEquals("some,random,stuff", Utils.join(",", Arrays.asList("some", "random", "stuff")));
        assertEquals("some,random,not-null,stuff", Utils.nonEmptyAndNullJoin(",", new String[]{"some", "null", "random", "", "not-null", null, "stuff"}));
    }

    @Test
    void testGetBaseUrl() throws ParsingException {
        assertEquals("https://www.youtube.com", Utils.getBaseUrl("https://www.youtube.com/watch?v=Hu80uDzh8RY"));
        assertEquals("vnd.youtube", Utils.getBaseUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI"));
        assertEquals("vnd.youtube", Utils.getBaseUrl("vnd.youtube:jZViOEv90dI"));
        assertEquals("vnd.youtube", Utils.getBaseUrl("vnd.youtube://n8X9_MgEdCg"));
        assertEquals("https://music.youtube.com", Utils.getBaseUrl("https://music.youtube.com/watch?v=O0EDx9WAelc"));
    }

    @Test
    void testFollowGoogleRedirect() {
        assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY",
                Utils.followGoogleRedirectIfNeeded("https://www.google.it/url?sa=t&rct=j&q=&esrc=s&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DHu80uDzh8RY&source=video"));
        assertEquals("https://www.youtube.com/watch?v=0b6cFWG45kA",
                Utils.followGoogleRedirectIfNeeded("https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=video&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3D0b6cFWG45kA"));
        assertEquals("https://soundcloud.com/ciaoproduction",
                Utils.followGoogleRedirectIfNeeded("https://www.google.com/url?sa=t&url=https%3A%2F%2Fsoundcloud.com%2Fciaoproduction&rct=j&q=&esrc=s&source=web&cd="));

        assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY&param=xyz",
                Utils.followGoogleRedirectIfNeeded("https://www.youtube.com/watch?v=Hu80uDzh8RY&param=xyz"));
        assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY&url=hello",
                Utils.followGoogleRedirectIfNeeded("https://www.youtube.com/watch?v=Hu80uDzh8RY&url=hello"));
    }

    @Test
    void dashManifestCreatorCacheTest() {
        final ManifestCreatorCache<String, String> cache = new ManifestCreatorCache<>();
        cache.setMaximumSize(30);
        setCacheContent(cache);
        // 30 elements set -> cache resized to 23 -> 5 new elements set to the cache -> 28
        assertEquals(28, cache.size(),
                "Wrong cache size with default clear factor and 30 as the maximum size");

        cache.reset();
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

        // Re-setters tests
        cache.resetMaximumSize();
        assertEquals(ManifestCreatorCache.DEFAULT_MAXIMUM_SIZE, cache.getMaximumSize(),
                "Wrong maximum cache size gotten from maximum size getter after maximum size reset");

        cache.resetClearFactor();
        assertEquals(ManifestCreatorCache.DEFAULT_CLEAR_FACTOR, cache.getClearFactor(),
                "Wrong clear factor gotten from clear factor getter after clear factor reset");
    }

    private void setCacheContent(@Nonnull final ManifestCreatorCache<String, String> cache) {
        int i = 0;
        while (i < 26) {
            cache.put(Character.toString((char) (97 + i)), "V");
            ++i;
        }

        i = 0;
        while (i < 9) {
            cache.put("a" + (char) (97 + i), "V");
            ++i;
        }
    }
}
