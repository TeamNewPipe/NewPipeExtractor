package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.DefaultTests.defaultTestImageCollection;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultSimpleExtractorTest;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PeertubeCommentsExtractorTest {
    public static class Default extends DefaultSimpleExtractorTest<PeertubeCommentsExtractor> {
        @Override
        protected PeertubeCommentsExtractor createExtractor() throws Exception {
            return (PeertubeCommentsExtractor) PeerTube
                .getCommentsExtractor("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv");
        }

        @Test
        void testGetComments() throws IOException, ExtractionException {
            final String comment = "I love this";

            InfoItemsPage<CommentsInfoItem> comments = extractor().getInitialPage();
            boolean result = findInComments(comments, comment);

            while (comments.hasNextPage() && !result) {
                comments = extractor().getPage(comments.getNextPage());
                result = findInComments(comments, comment);
            }

            assertTrue(result);
        }

        @Test
        void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            final String comment = "I love this. ❤";

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
            extractor().getInitialPage()
                    .getItems()
                    .forEach(commentsInfoItem -> {
                        assertFalse(Utils.isBlank(commentsInfoItem.getUploaderUrl()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getUploaderName()));
                        defaultTestImageCollection(commentsInfoItem.getUploaderAvatars());
                        assertFalse(Utils.isBlank(commentsInfoItem.getCommentId()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getCommentText().getContent()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getName()));
                        assertFalse(Utils.isBlank(commentsInfoItem.getTextualUploadDate()));
                        defaultTestImageCollection(commentsInfoItem.getThumbnails());
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

    public static class DeletedComments extends DefaultSimpleExtractorTest<PeertubeCommentsExtractor> {
        @Override
        protected PeertubeCommentsExtractor createExtractor() throws Exception {
            return (PeertubeCommentsExtractor) PeerTube
                .getCommentsExtractor("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
        }

        @Test
        void testGetComments() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor().getInitialPage();
            assertTrue(comments.getErrors().isEmpty());
        }

        @Test
        void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            final CommentsInfo commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3");
            assertTrue(commentsInfo.getErrors().isEmpty());
        }
    }

    /**
     * Test a video that has comments with nested replies.
     */
    public static class NestedComments extends DefaultSimpleExtractorTest<PeertubeCommentsExtractor> {
        private InfoItemsPage<CommentsInfoItem> comments;

        @Override
        protected PeertubeCommentsExtractor createExtractor() throws Exception {
            return (PeertubeCommentsExtractor) PeerTube
                .getCommentsExtractor("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv");
        }

        @Override
        protected void fetchExtractor(final PeertubeCommentsExtractor extractor) throws Exception {
            comments = extractor.getInitialPage();
        }

        protected InfoItemsPage<CommentsInfoItem> comments() {
            if (comments == null) {
                extractor(); // Initialize extractor to also init Comments
            }
            return comments;
        }

        @Test
        void testGetComments() throws IOException, ExtractionException {
            assertFalse(comments().getItems().isEmpty());
            final Optional<CommentsInfoItem> nestedCommentHeadOpt =
                findCommentWithId("34293", comments().getItems());
            assertTrue(nestedCommentHeadOpt.isPresent());
            assertTrue(findNestedCommentWithId("34294", nestedCommentHeadOpt.get()), "The nested " +
                "comment replies were not found");
        }

        @Test
        void testHasCreatorReply() {
            // NOTE: There is currently no creator replied comment
            assertCreatorReply("34293", false);
        }

        private void assertCreatorReply(final String id, final boolean expected) {
            final Optional<CommentsInfoItem> comment =
                findCommentWithId(id, comments().getItems());
            assertTrue(comment.isPresent());
            assertEquals(expected, comment.get().hasCreatorReply());
        }
    }

    private static Optional<CommentsInfoItem> findCommentWithId(
            final String id, final List<CommentsInfoItem> comments) {
        return comments
                .stream()
                .filter(c -> c.getCommentId().equals(id))
                .findFirst();
    }

    private static boolean findNestedCommentWithId(final String id, final CommentsInfoItem comment)
            throws IOException, ExtractionException {
        if (comment.getCommentId().equals(id)) {
            return true;
        }
        return PeerTube
                .getCommentsExtractor(comment.getUrl())
                .getPage(comment.getReplies())
                .getItems()
                .stream()
                .map(c -> {
                    try {
                        return findNestedCommentWithId(id, c);
                    } catch (final Exception ignored) {
                        return false;
                    }
                })
                .reduce((a, b) -> a || b)
                .orElse(false);
    }
}
