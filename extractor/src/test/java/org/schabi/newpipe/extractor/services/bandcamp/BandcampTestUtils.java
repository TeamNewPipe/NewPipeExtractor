package org.schabi.newpipe.extractor.services.bandcamp;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.services.DefaultTests;

import javax.annotation.Nullable;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility class for Bandcamp tests.
 */
public final class BandcampTestUtils {
    private BandcampTestUtils() {
    }

    /**
     * Test that Bandcamp images of a {@link Collection} respect
     * {@link DefaultTests#defaultTestImageCollection(Collection) default requirements}, contain
     * the string {@code f4.bcbits.com/img} in their URL and end with {@code .jpg} or {@code .png}.
     *
     * @param images a Bandcamp {@link Image} {@link Collection}
     */
    public static void testImages(@Nullable final Collection<Image> images) {
        DefaultTests.defaultTestImageCollection(images);
        // Disable NPE warning because if the collection is null, an AssertionError would be thrown
        // by DefaultTests.defaultTestImageCollection
        //noinspection DataFlowIssue
        assertTrue(images.stream()
                .allMatch(image -> image.getUrl().contains("f4.bcbits.com/img")
                        && (image.getUrl().endsWith(".jpg") || image.getUrl().endsWith(".png"))));
    }
}
