package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

public class BandcampCommentsExtractorTest extends DefaultSimpleExtractorTest<CommentsExtractor> {

    @Override
    protected CommentsExtractor createExtractor() throws Exception {
        return Bandcamp.getCommentsExtractor("https://floatingpoints.bandcamp.com/album/promises");
    }

    @Test
    void hasComments() throws IOException, ExtractionException {
        assertTrue(extractor().getInitialPage().getItems().size() >= 3);
    }

    @Test
    void testGetCommentsAllData() throws IOException, ExtractionException {
        final ListExtractor.InfoItemsPage<CommentsInfoItem> comments = extractor().getInitialPage();
        assertTrue(comments.hasNextPage());

        DefaultTests.defaultTestListOfItems(Bandcamp, comments.getItems(), comments.getErrors());
        for (final CommentsInfoItem c : comments.getItems()) {
            assertFalse(Utils.isBlank(c.getUploaderName()));
            BandcampTestUtils.testImages(c.getUploaderAvatars());
            assertFalse(Utils.isBlank(c.getCommentText().getContent()));
            assertFalse(Utils.isBlank(c.getName()));
            BandcampTestUtils.testImages(c.getThumbnails());
            assertFalse(Utils.isBlank(c.getUrl()));
            assertEquals(-1, c.getLikeCount());
            assertTrue(Utils.isBlank(c.getTextualLikeCount()));
        }
    }
}
