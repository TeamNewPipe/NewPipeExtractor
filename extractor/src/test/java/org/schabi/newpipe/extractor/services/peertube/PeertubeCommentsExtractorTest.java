package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubeCommentsExtractorTest {
    public static class Default {
        private static PeertubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeCommentsExtractor) PeerTube
                    .getCommentsExtractor("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d");
        }

        @Test
        public void testGetComments() throws IOException, ExtractionException {
            InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            boolean result = findInComments(comments, "Cool.");

            while (comments.hasNextPage() && !result) {
                comments = extractor.getPage(comments.getNextPage());
                result = findInComments(comments, "Cool.");
            }

            assertTrue(result);
        }

        @Test
        public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            CommentsInfo commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
            assertEquals("Comments", commentsInfo.getName());

            boolean result = findInComments(commentsInfo.getRelatedItems(), "Cool");

            Page nextPage = commentsInfo.getNextPage();
            InfoItemsPage<CommentsInfoItem> moreItems = new InfoItemsPage<>(null, nextPage, null);
            while (moreItems.hasNextPage() && !result) {
                moreItems = CommentsInfo.getMoreItems(PeerTube, commentsInfo, nextPage);
                result = findInComments(moreItems.getItems(), "Cool");
                nextPage = moreItems.getNextPage();
            }

            assertTrue(result);
        }

        @Test
        public void testGetCommentsAllData() throws IOException, ExtractionException {
            InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            for (CommentsInfoItem c : comments.getItems()) {
                assertFalse(Utils.isBlank(c.getUploaderUrl()));
                assertFalse(Utils.isBlank(c.getUploaderName()));
                assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
                assertFalse(Utils.isBlank(c.getCommentId()));
                assertFalse(Utils.isBlank(c.getCommentText()));
                assertFalse(Utils.isBlank(c.getName()));
                assertFalse(Utils.isBlank(c.getTextualUploadDate()));
                assertFalse(Utils.isBlank(c.getThumbnailUrl()));
                assertFalse(Utils.isBlank(c.getUrl()));
                assertEquals(-1, c.getLikeCount());
                assertTrue(Utils.isBlank(c.getTextualLikeCount()));
            }
        }

        private boolean findInComments(InfoItemsPage<CommentsInfoItem> comments, String comment) {
            return findInComments(comments.getItems(), comment);
        }

        private boolean findInComments(List<CommentsInfoItem> comments, String comment) {
            for (CommentsInfoItem c : comments) {
                if (c.getCommentText().contains(comment)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class DeletedComments {
        private static PeertubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (PeertubeCommentsExtractor) PeerTube
                    .getCommentsExtractor("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
        }

        @Test
        public void testGetComments() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            assertTrue(comments.getErrors().isEmpty());
        }

        @Test
        public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            final CommentsInfo commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
            assertTrue(commentsInfo.getErrors().isEmpty());
        }
    }
}
