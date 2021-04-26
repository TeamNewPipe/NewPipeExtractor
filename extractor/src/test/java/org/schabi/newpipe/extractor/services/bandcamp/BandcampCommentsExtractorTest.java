package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class BandcampCommentsExtractorTest {

    private static CommentsExtractor extractor;

    @BeforeClass
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = Bandcamp.getCommentsExtractor("https://floatingpoints.bandcamp.com/album/promises");
        extractor.fetchPage();
    }

    @Test
    public void hasComments() throws IOException, ExtractionException {
        assertTrue(extractor.getInitialPage().getItems().size() >= 3);
    }

    @Test
    public void testGetCommentsAllData() throws IOException, ExtractionException {
        ListExtractor.InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

        DefaultTests.defaultTestListOfItems(Bandcamp, comments.getItems(), comments.getErrors());
        for (CommentsInfoItem c : comments.getItems()) {
            assertFalse(Utils.isBlank(c.getUploaderUrl()));
            assertFalse(Utils.isBlank(c.getUploaderName()));
            assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
            assertFalse(Utils.isBlank(c.getCommentText()));
            assertFalse(Utils.isBlank(c.getName()));
            assertFalse(Utils.isBlank(c.getThumbnailUrl()));
            assertFalse(Utils.isBlank(c.getUrl()));
            assertEquals(-1, c.getLikeCount());
        }
    }
}
