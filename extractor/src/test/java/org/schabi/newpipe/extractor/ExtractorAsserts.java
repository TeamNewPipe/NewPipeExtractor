package org.schabi.newpipe.extractor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.Utils;

public class ExtractorAsserts {
    public static void assertEmptyErrors(String message, List<Throwable> errors) {
        if (!errors.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder(message);
            for (Throwable e : errors) {
                messageBuilder.append("\n  * ").append(e.getMessage());
            }
            messageBuilder.append(" ");
            throw new AssertionError(messageBuilder.toString(), errors.get(0));
        }
    }

    @Nonnull
    private static URL urlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new AssertionError("Invalid url: " + "\"" + url + "\"", e);
        }
    }

    public static void assertIsValidUrl(String url) {
        urlFromString(url);
    }

    public static void assertIsSecureUrl(String urlToCheck) {
        URL url = urlFromString(urlToCheck);
        assertEquals("https", url.getProtocol(), "Protocol of URL is not secure");
    }

    public static void assertNotEmpty(String stringToCheck) {
        assertNotEmpty(null, stringToCheck);
    }

    public static void assertNotEmpty(@Nullable String message, String stringToCheck) {
        assertNotNull(stringToCheck, message);
        assertFalse(stringToCheck.isEmpty(), message);
    }

    public static void assertNotEmpty(@Nullable final Collection<?> collectionToCheck) {
        assertNotEmpty(null, collectionToCheck);
    }

    public static void assertNotEmpty(@Nullable final String message,
                                      @Nullable final Collection<?> collectionToCheck) {
        assertNotNull(collectionToCheck);
        assertFalse(collectionToCheck.isEmpty(), message);
    }

    public static void assertEmpty(String stringToCheck) {
        assertEmpty(null, stringToCheck);
    }

    public static void assertEmpty(@Nullable String message, String stringToCheck) {
        if (stringToCheck != null) {
            assertTrue(stringToCheck.isEmpty(), message);
        }
    }

    public static void assertEmpty(@Nullable final Collection<?> collectionToCheck) {
        if (collectionToCheck != null) {
            assertTrue(collectionToCheck.isEmpty());
        }
    }

    public static void assertNotBlank(String stringToCheck) {
        assertNotBlank(stringToCheck, null);
    }

    public static void assertNotBlank(String stringToCheck, @Nullable String message) {
        assertFalse(Utils.isBlank(stringToCheck), message);
    }

    public static void assertGreater(final long expected, final long actual) {
        assertGreater(expected, actual, actual + " is not > " + expected);
    }

    public static <T extends Comparable<T>> void assertGreater(final T expected, final T actual,
            final String message) {
        assertTrue(actual.compareTo(expected) > 0, message);
    }

    public static void assertGreaterOrEqual(final long expected, final long actual) {
        assertGreaterOrEqual(expected, actual, actual + " is not >= " + expected);
    }

    public static void assertGreaterOrEqual(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual >= expected, message);
    }

    public static void assertLess(final long expected, final long actual) {
        assertLess(expected, actual, actual + " is not < " + expected);
    }

    public static void assertLess(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual < expected, message);
    }

    public static void assertLessOrEqual(final long expected, final long actual) {
        assertLessOrEqual(expected, actual, actual + " is not <= " + expected);
    }

    public static void assertLessOrEqual(
            final long expected,
            final long actual,
            final String message
    ) {
        assertTrue(actual <= expected, message);
    }

    // this assumes that sorting a and b in-place is not an issue, so it's only intended for tests
    public static void assertEqualsOrderIndependent(final List<String> expected,
                                                    final List<String> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        } else {
            assertNotNull(actual);
        }

        Collections.sort(expected);
        Collections.sort(actual);
        // using new ArrayList<> to make sure the type is the same
        assertEquals(new ArrayList<>(expected), new ArrayList<>(actual));
    }

    public static void assertContains(
            final String shouldBeContained,
            final String container) {
        assertNotNull(shouldBeContained, "shouldBeContained is null");
        assertNotNull(container, "container is null");
        assertTrue(container.contains(shouldBeContained),
                "'" + shouldBeContained + "' should be contained inside '" + container + "'");
    }

    public static void assertTabsContain(@Nonnull final List<ListLinkHandler> tabs,
                                         @Nonnull final String... expectedTabs) {
        final Set<String> tabSet = tabs.stream()
                .map(linkHandler -> {
                    assertEquals(1, linkHandler.getContentFilters().size(),
                            "Unexpected content filters for channel tab: "
                                    + linkHandler.getContentFilters());
                    return linkHandler.getContentFilters().get(0);
                })
                .collect(Collectors.toUnmodifiableSet());

        assertEquals(expectedTabs.length, tabSet.size(),
                "Different amount of tabs returned:\nExpected: "
                + Arrays.toString(expectedTabs) + "\nActual: " + tabSet);
        Arrays.stream(expectedTabs)
                .forEach(expectedTab -> assertTrue(tabSet.contains(expectedTab),
                        "Missing " + expectedTab + " tab (got " + tabSet + ")"));
    }

    public static void assertContainsImageUrlInImageCollection(
            @Nullable final String exceptedImageUrlContained,
            @Nullable final Collection<Image> imageCollection) {
        assertNotNull(exceptedImageUrlContained, "exceptedImageUrlContained is null");
        assertNotNull(imageCollection, "imageCollection is null");
        assertTrue(imageCollection.stream().anyMatch(image ->
                image.getUrl().equals(exceptedImageUrlContained)));
    }

    public static void assertContainsOnlyEquivalentImages(
            @Nullable final Collection<Image> firstImageCollection,
            @Nullable final Collection<Image> secondImageCollection) {
        assertNotNull(firstImageCollection);
        assertNotNull(secondImageCollection);
        assertEquals(firstImageCollection.size(), secondImageCollection.size());

        firstImageCollection.forEach(exceptedImage ->
                assertTrue(secondImageCollection.stream().anyMatch(image ->
                        exceptedImage.getUrl().equals(image.getUrl())
                                && exceptedImage.getHeight() == image.getHeight()
                                && exceptedImage.getWidth() == image.getWidth())));
    }

    public static void assertNotOnlyContainsEquivalentImages(
            @Nullable final Collection<Image> firstImageCollection,
            @Nullable final Collection<Image> secondImageCollection) {
        assertNotNull(firstImageCollection);
        assertNotNull(secondImageCollection);

        if (secondImageCollection.size() != firstImageCollection.size()) {
            return;
        }

        for (final Image unexpectedImage : firstImageCollection) {
            for (final Image image : secondImageCollection) {
                if (!image.getUrl().equals(unexpectedImage.getUrl())
                        || image.getHeight() != unexpectedImage.getHeight()
                        || image.getWidth() != unexpectedImage.getWidth()) {
                    return;
                }
            }
        }

        throw new AssertionError("All excepted images have an equivalent in the image list");
    }
}
