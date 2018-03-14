package org.schabi.newpipe.extractor.services.youtube;

import org.junit.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.search.SearchResult;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertIsSecureUrl;

public abstract class BaseYoutubeSearchTest {

    protected static SearchResult result;

    @Test
    public void testResultList() {
        assertFalse("Got empty result list", result.resultList.isEmpty());
        for(InfoItem infoItem: result.resultList) {
            assertIsSecureUrl(infoItem.getUrl());
            assertIsSecureUrl(infoItem.getThumbnailUrl());
            assertFalse(infoItem.getName().isEmpty());
            assertFalse("Name is probably a URI: " + infoItem.getName(),
                    infoItem.getName().contains("://"));
            if(infoItem instanceof StreamInfoItem) {
                // test stream item
                StreamInfoItem streamInfoItem = (StreamInfoItem) infoItem;
                assertIsSecureUrl(streamInfoItem.getUploaderUrl());
                assertFalse(streamInfoItem.getUploadDate().isEmpty());
                assertFalse(streamInfoItem.getUploaderName().isEmpty());
            } else if(infoItem instanceof ChannelInfoItem) {
                // Nothing special to check?
            } else if(infoItem instanceof PlaylistInfoItem)  {
                // test playlist item
                long streamCount = ((PlaylistInfoItem) infoItem).getStreamCount();
                assertTrue(streamCount > 0);
            } else {
                fail("Unknown infoItem type: " + infoItem);
            }
        }
    }

    @Test
    public void testResultErrors() {
        assertNotNull(result.errors);
        if (!result.errors.isEmpty()) {
            for (Throwable error : result.errors) {
                error.printStackTrace();
            }
        }
        assertTrue(result.errors.isEmpty());
    }
}
