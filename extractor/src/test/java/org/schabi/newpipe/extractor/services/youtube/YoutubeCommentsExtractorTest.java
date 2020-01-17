package org.schabi.newpipe.extractor.services.youtube;

import org.jsoup.helper.StringUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeCommentsExtractorTest {

    private static final String urlYT = "https://www.youtube.com/watch?v=D00Au7k3i6o";
    private static final String urlInvidious = "https://invidio.us/watch?v=D00Au7k3i6o";
    private static final String urlInvidioush = "https://invidiou.sh/watch?v=D00Au7k3i6o";
    private static YoutubeCommentsExtractor extractorYT;
    private static YoutubeCommentsExtractor extractorInvidious;
    private static YoutubeCommentsExtractor extractorInvidioush;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractorYT = (YoutubeCommentsExtractor) YouTube
                .getCommentsExtractor(urlYT);
        extractorInvidious = (YoutubeCommentsExtractor) YouTube
                .getCommentsExtractor(urlInvidious);
        extractorInvidioush = (YoutubeCommentsExtractor) YouTube
                .getCommentsExtractor(urlInvidioush);
    }

    @Test
    public void testGetComments() throws IOException, ExtractionException {
        assertTrue(getCommentsHelper(extractorYT));
        assertTrue(getCommentsHelper(extractorInvidious));
        assertTrue(getCommentsHelper(extractorInvidioush));
    }

    private boolean getCommentsHelper(YoutubeCommentsExtractor extractor) throws IOException, ExtractionException {
        boolean result;
        InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
        result = findInComments(comments, "s1ck m3m3");

        while (comments.hasNextPage() && !result) {
            comments = extractor.getPage(comments.getNextPageUrl());
            result = findInComments(comments, "s1ck m3m3");
        }

        return result;
    }

    @Test
    public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
        assertTrue(getCommentsFromCommentsInfoHelper(urlYT));
        assertTrue(getCommentsFromCommentsInfoHelper(urlInvidious));
        assertTrue(getCommentsFromCommentsInfoHelper(urlInvidioush));
    }

    private boolean getCommentsFromCommentsInfoHelper(String url) throws IOException, ExtractionException {
        boolean result = false;
        CommentsInfo commentsInfo = CommentsInfo.getInfo(url);
        assertEquals("what the fuck am i doing with my life", commentsInfo.getName());
        result = findInComments(commentsInfo.getRelatedItems(), "s1ck m3m3");

        String nextPage = commentsInfo.getNextPageUrl();
        while (!StringUtil.isBlank(nextPage) && !result) {
            InfoItemsPage<CommentsInfoItem> moreItems = CommentsInfo.getMoreItems(YouTube, commentsInfo, nextPage);
            result = findInComments(moreItems.getItems(), "s1ck m3m3");
            nextPage = moreItems.getNextPageUrl();
        }
        return result;
    }

    @Test
    public void testGetCommentsAllData() throws IOException, ExtractionException {
        InfoItemsPage<CommentsInfoItem> comments = extractorYT.getInitialPage();

        DefaultTests.defaultTestListOfItems(YouTube.getServiceId(), comments.getItems(), comments.getErrors());
        for (CommentsInfoItem c : comments.getItems()) {
            assertFalse(StringUtil.isBlank(c.getAuthorEndpoint()));
            assertFalse(StringUtil.isBlank(c.getAuthorName()));
            assertFalse(StringUtil.isBlank(c.getAuthorThumbnail()));
            assertFalse(StringUtil.isBlank(c.getCommentId()));
            assertFalse(StringUtil.isBlank(c.getCommentText()));
            assertFalse(StringUtil.isBlank(c.getName()));
            assertFalse(StringUtil.isBlank(c.getTextualPublishedTime()));
            assertNotNull(c.getPublishedTime());
            assertFalse(StringUtil.isBlank(c.getThumbnailUrl()));
            assertFalse(StringUtil.isBlank(c.getUrl()));
            assertFalse(c.getLikeCount() < 0);
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
