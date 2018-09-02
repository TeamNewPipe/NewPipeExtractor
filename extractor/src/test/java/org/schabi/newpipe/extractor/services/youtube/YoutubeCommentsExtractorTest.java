package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;

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

        while (comments.hasNextPage()) {
            comments = extractor.getPage(comments.getNextPageUrl());
            result = findInComments(comments, "i should really be in the top comment.lol");
        }

        assertTrue(result);
    }

    private boolean findInComments(InfoItemsPage<CommentsInfoItem> comments, String comment) {
        return comments.getItems().stream().filter(c -> c.getCommentText().contains(comment)).findAny().isPresent();
    }
}
