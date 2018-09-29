package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.io.IOException;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;
import org.schabi.newpipe.extractor.utils.Localization;

public class YoutubeCommentsExtractorTest {

    private static YoutubeCommentsExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
        extractor = (YoutubeCommentsExtractor) YouTube
                .getCommentsExtractor("https://www.youtube.com/watch?v=rrgFN3AxGfs");
    }

    @Test
    public void testGetComments() throws IOException, ExtractionException {
        boolean result = false;
        InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
        result = findInComments(comments, "i should really be in the top comment.lol");

        while (comments.hasNextPage() && !result) {
            comments = extractor.getPage(comments.getNextPageUrl());
            result = findInComments(comments, "i should really be in the top comment.lol");
        }

        assertTrue(result);
    }

    @Test
    public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
        boolean result = false;
        CommentsInfo commentsInfo = CommentsInfo.getInfo("https://www.youtube.com/watch?v=rrgFN3AxGfs");
        assertTrue("what the fuck am i doing with my life.wmv".equals(commentsInfo.getName()));
        result = findInComments(commentsInfo.getRelatedItems(), "i should really be in the top comment.lol");

        String nextPage = commentsInfo.getNextPageUrl();
        while (!StringUtil.isBlank(nextPage) && !result) {
            InfoItemsPage<CommentsInfoItem> moreItems = CommentsInfo.getMoreItems(YouTube, commentsInfo, nextPage);
            result = findInComments(moreItems.getItems(), "i should really be in the top comment.lol");
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
            assertFalse(StringUtil.isBlank(c.getPublishedTime()));
            assertFalse(StringUtil.isBlank(c.getThumbnailUrl()));
            assertFalse(StringUtil.isBlank(c.getUrl()));
            assertFalse(c.getLikeCount() == null);
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
