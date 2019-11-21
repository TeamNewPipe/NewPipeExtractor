package org.schabi.newpipe.extractor.services.peertube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

import java.io.IOException;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor;

public class PeertubeCommentsExtractorTest {

    private static PeertubeCommentsExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (PeertubeCommentsExtractor) PeerTube
                .getCommentsExtractor("https://peertube.mastodon.host/videos/watch/04af977f-4201-4697-be67-a8d8cae6fa7a");
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
        CommentsInfo commentsInfo = CommentsInfo.getInfo("https://peertube.mastodon.host/videos/watch/a8ea95b8-0396-49a6-8f30-e25e25fb2828");
        assertTrue("Comments".equals(commentsInfo.getName()));
        result = findInComments(commentsInfo.getRelatedItems(), "Loved it!!!");

        String nextPage = commentsInfo.getNextPageUrl();
        while (!StringUtil.isBlank(nextPage) && !result) {
            InfoItemsPage<CommentsInfoItem> moreItems = CommentsInfo.getMoreItems(PeerTube, commentsInfo, nextPage);
            result = findInComments(moreItems.getItems(), "Loved it!!!");
            nextPage = moreItems.getNextPageUrl();
        }

        assertTrue(result);
    }
    
    @Test
    public void testGetCommentsAllData() throws IOException, ExtractionException {
        InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
        for(CommentsInfoItem c: comments.getItems()) {
            assertFalse(StringUtil.isBlank(c.getAuthorEndpoint()));
            assertFalse(StringUtil.isBlank(c.getAuthorName()));
            assertFalse(StringUtil.isBlank(c.getAuthorThumbnail()));
            assertFalse(StringUtil.isBlank(c.getCommentId()));
            assertFalse(StringUtil.isBlank(c.getCommentText()));
            assertFalse(StringUtil.isBlank(c.getName()));
            assertFalse(StringUtil.isBlank(c.getTextualPublishedTime()));
            assertFalse(StringUtil.isBlank(c.getThumbnailUrl()));
            assertFalse(StringUtil.isBlank(c.getUrl()));
            assertFalse(c.getLikeCount() != -1);
        }
    }

    private boolean findInComments(InfoItemsPage<CommentsInfoItem> comments, String comment) {
        return findInComments(comments.getItems(), comment);
    }

    private boolean findInComments(List<CommentsInfoItem> comments, String comment) {
        for(CommentsInfoItem c: comments) {
            if(c.getCommentText().contains(comment)) {
                return true;
            }
        }
        return false;
    }
}
