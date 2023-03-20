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
            final String comment = "Thanks for creating such an informative video";

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
            extractor.getInitialPage()
                    .getItems()
                    .forEach(commentsInfoItem -> {
                        assertFalse(Utils.isBlank(commentsInfoItem.getUploaderUrl()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getUploaderName()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getUploaderAvatarUrl()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getCommentId()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getCommentText().getContent()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getName()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getTextualUploadDate()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getThumbnailUrl()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getUrl()));
                        assertEquals(-1, commentsInfoItem.getLikeCount());
                        assertTrue(Utils.isBlank(commentsInfoItem.getTextualLikeCount()));
                    });
        }

        private boolean findInComments(final InfoItemsPage<CommentsInfoItem> comments,
                                       final String comment) {
            return findInComments(comments.getItems(), comment);
        }

        private boolean findInComments(final List<CommentsInfoItem> comments,
                                       final String comment) {
            return comments.stream()
                    .anyMatch(commentsInfoItem ->
                            commentsInfoItem.getCommentText().getContent().contains(comment));
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
