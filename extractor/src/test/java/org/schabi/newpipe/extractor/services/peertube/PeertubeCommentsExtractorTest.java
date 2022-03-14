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
                    .getCommentsExtractor("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv");
        }

        @Test
        void testGetComments() throws IOException, ExtractionException {
            final String comment = "I love this";

            InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            boolean result = findInComments(comments, comment);

            while (comments.hasNextPage() && !result) {
                comments = extractor.getPage(comments.getNextPage());
                result = findInComments(comments, comment);
            }

            assertTrue(result);
        }

        @Test
        void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            final String comment = "great video";

            final CommentsInfo commentsInfo =
                    CommentsInfo.getInfo("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv");
            assertEquals("Comments", commentsInfo.getName());

            boolean result = findInComments(commentsInfo.getRelatedItems(), comment);

            Page nextPage = commentsInfo.getNextPage();
            InfoItemsPage<CommentsInfoItem> moreItems = new InfoItemsPage<>(null, nextPage, null);
            while (moreItems.hasNextPage() && !result) {
                moreItems = CommentsInfo.getMoreItems(PeerTube, commentsInfo, nextPage);
                result = findInComments(moreItems.getItems(), comment);
                nextPage = moreItems.getNextPage();
            }

            assertTrue(result);
        }

        @Test
        void testGetCommentsAllData() throws IOException, ExtractionException {
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
        void testGetComments() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            assertTrue(comments.getErrors().isEmpty());
        }

        @Test
        void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            final CommentsInfo commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
            assertTrue(commentsInfo.getErrors().isEmpty());
        }
    }
}
