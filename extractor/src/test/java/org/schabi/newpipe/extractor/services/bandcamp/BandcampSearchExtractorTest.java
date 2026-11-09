// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Test for {@link BandcampSearchExtractor}
 */
public class BandcampSearchExtractorTest implements InitBandcampTest {

    /**
     * Tests searches with multiple pages
     */
    @Test
    void testMultiplePages() throws ExtractionException, IOException {
        // A query practically guaranteed to have the maximum amount of pages
        final SearchExtractor extractor = Bandcamp.getSearchExtractor("e");

        final Page page2 = extractor.getInitialPage().getNextPage();
        assertEquals("https://bandcamp.com/search?q=e&page=2", page2.getUrl());

        final Page page3 = extractor.getPage(page2).getNextPage();
        assertEquals("https://bandcamp.com/search?q=e&page=3", page3.getUrl());
    }

    public static class InfoItemTypeTest implements InitNewPipeTest {
        /**
         * Tests whether searching bandcamp for "best friend's basement" returns
         * the accordingly named song by Zach Benson
         */
        @Test
        void testStreamSearch() throws ExtractionException, IOException {
            final SearchExtractor extractor = Bandcamp.getSearchExtractor("best friend's basement");

            final ListExtractor.InfoItemsPage<InfoItem> page = extractor.getInitialPage();
            final StreamInfoItem bestFriendsBasement = (StreamInfoItem) page.getItems().get(0);

            // The track by Zach Benson should be the first result, no?
            assertEquals("Best Friend's Basement", bestFriendsBasement.getName());
            assertEquals("Zach Benson", bestFriendsBasement.getUploaderName());
            BandcampTestUtils.testImages(bestFriendsBasement.getThumbnails());
            assertEquals(InfoItem.InfoType.STREAM, bestFriendsBasement.getInfoType());
        }

        /**
         * Tests whether searching bandcamp for "C418" returns the artist's profile
         */
        @Test
        void testChannelSearch() throws ExtractionException, IOException {
            final SearchExtractor extractor = Bandcamp.getSearchExtractor("C418");
            final InfoItem c418 = extractor.getInitialPage()
                    .getItems().get(0);

            // C418's artist profile should be the first result, no?
            assertEquals("C418", c418.getName());
            BandcampTestUtils.testImages(c418.getThumbnails());
            assertEquals("https://c418.bandcamp.com", c418.getUrl());
        }

        /**
         * Tests whether searching bandcamp for "minecraft volume alpha" returns the corresponding album
         */
        @Test
        void testAlbumSearch() throws ExtractionException, IOException {
            final SearchExtractor extractor = Bandcamp.getSearchExtractor("minecraft volume alpha");
            final InfoItem minecraft = extractor.getInitialPage().getItems().get(0);

            // Minecraft volume alpha should be the first result, no?
            assertEquals("Minecraft - Volume Alpha", minecraft.getName());
            BandcampTestUtils.testImages(minecraft.getThumbnails());
            assertEquals(
                    "https://c418.bandcamp.com/album/minecraft-volume-alpha",
                    minecraft.getUrl());

            // Verify that playlist tracks counts get extracted correctly
            assertEquals(24, ((PlaylistInfoItem) minecraft).getStreamCount());
        }
    }

    public static class LocalizationTest implements InitBandcampTest {
        /**
         * A non-English content language still requests the service localization, and a localized
         * album length yields its leading track count instead of failing the search.
         */
        @Test
        void testSearchWithNonEnglishContentLanguageExactResult() throws ExtractionException, IOException {
            final List<Request> requests = new ArrayList<>();
            NewPipe.init(new Downloader() {
                @Override
                public Response execute(@Nonnull final Request request) {
                    requests.add(request);
                    return new Response(200, "OK", null,
                            "<div class=\"searchresult\"><div class=\"result-info\">"
                                    + "<div class=\"itemtype\">ALBUM</div>"
                                    + "<div class=\"length\">10 Titel, 38 Minuten</div>"
                                    + "</div></div>",
                            request.url());
                }
            }, new Localization("de", "DE"));

            final ListExtractor.InfoItemsPage<InfoItem> page = Bandcamp
                    .getSearchExtractor("Cigarettes After Sex")
                    .getInitialPage();

            final Request request = requests.get(0);
            assertEquals(Localization.DEFAULT, request.localization());
            assertTrue(request.headers().get("Accept-Language").get(0).startsWith("en-GB"));
            assertEquals(10, ((PlaylistInfoItem) page.getItems().get(0)).getStreamCount());
            assertTrue(page.getErrors().isEmpty());
        }

        @Test
        void testSearchWithNonEnglishContentLanguages() throws ExtractionException, IOException {

            final List<Localization> localizations = List.of(
                    new Localization("de", "DE"),
                    new Localization("fr", "FR"),
                    new Localization("es", "ES"),
                    new Localization("it", "IT")
            );

            for (final Localization localization : localizations) {
                NewPipe.setupLocalization(localization);

                final ListExtractor.InfoItemsPage<InfoItem> page = Bandcamp
                        .getSearchExtractor("Cigarettes After Sex")
                        .getInitialPage();

                for (final InfoItem infoItem: page.getItems()) {
                    if (infoItem instanceof PlaylistInfoItem playlistInfoItem) {
                        assertGreater(0, playlistInfoItem.getStreamCount());
                    }
                }

                assertTrue(page.getErrors().isEmpty());
            }

        }
    }

    public static class DefaultTest extends DefaultSearchExtractorTest {
        private static final String QUERY = "noise";

        @Override
        protected SearchExtractor createExtractor() throws Exception {
            return Bandcamp.getSearchExtractor(QUERY);
        }

        @Override public StreamingService expectedService() { return Bandcamp; }
        @Override public String expectedName() { return QUERY; }
        @Override public String expectedId() { return QUERY; }
        @Override public String expectedUrlContains() { return "bandcamp.com/search?q=" + QUERY; }
        @Override public String expectedOriginalUrlContains() { return "bandcamp.com/search?q=" + QUERY; }
        @Override public String expectedSearchString() { return QUERY; }
        @Nullable @Override public String expectedSearchSuggestion() { return null; }
    }
}
