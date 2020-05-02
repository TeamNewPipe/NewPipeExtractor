package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubeCommentsExtractorTest {

    private static PeertubeCommentsExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (PeertubeCommentsExtractor) PeerTube
                .getCommentsExtractor("https://framatube.org/videos/watch/04af977f-4201-4697-be67-a8d8cae6fa7a");
    }

    @Test
    public void testGetComments() throws IOException, ExtractionException {
        boolean result = false;
        InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
        result = findInComments(comments, "@root A great documentary on a great guy.");

        while (comments.hasNextPage() && !result) {
            comments = extractor.getPage(comments.getNextPageUrl());
            result = findInComments(comments, "@root A great documentary on a great guy.");
        }

        assertTrue(result);
    }

    @Test
    public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
        boolean result = false;
        CommentsInfo commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/a8ea95b8-0396-49a6-8f30-e25e25fb2828");
        assertEquals("Comments", commentsInfo.getName());
        result = findInComments(commentsInfo.getRelatedItems(), "Loved it!!!");

        String nextPage = commentsInfo.getNextPageUrl();
        while (!Utils.isBlank(nextPage) && !result) {
            InfoItemsPage<CommentsInfoItem> moreItems = CommentsInfo.getMoreItems(PeerTube, commentsInfo, nextPage);
            result = findInComments(moreItems.getItems(), "Loved it!!!");
            nextPage = moreItems.getNextPageUrl();
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
            assertFalse(c.getLikeCount() != -1);
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
