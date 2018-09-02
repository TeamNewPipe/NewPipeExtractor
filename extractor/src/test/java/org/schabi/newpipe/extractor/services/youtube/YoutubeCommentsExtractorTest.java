package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;

public class YoutubeCommentsExtractorTest {

    private static YoutubeCommentsExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
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
    public void testGetCommentsFromStreamInfo() throws IOException, ExtractionException {
        boolean result = false;
        StreamInfo streamInfo = StreamInfo.getInfo("https://www.youtube.com/watch?v=rrgFN3AxGfs");

        result = findInComments(streamInfo.getComments(), "i should really be in the top comment.lol");

        while (streamInfo.hasMoreComments() && !result) {
            StreamInfo.loadMoreComments(streamInfo);
            result = findInComments(streamInfo.getComments(), "i should really be in the top comment.lol");
        }

        assertTrue(result);
    }

    private boolean findInComments(InfoItemsPage<CommentsInfoItem> comments, String comment) {
        return findInComments(comments.getItems(), comment);
    }

    private boolean findInComments(List<CommentsInfoItem> comments, String comment) {
        return comments.stream().filter(c -> c.getCommentText().contains(comment)).findAny().isPresent();
    }
}
