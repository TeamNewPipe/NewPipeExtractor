package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link SoundcloudSubscriptionExtractor}
 */
public class SoundcloudSubscriptionExtractorTest {
    private static SoundcloudSubscriptionExtractor subscriptionExtractor;
    private static LinkHandlerFactory urlHandler;

    @BeforeClass
    public static void setupClass() {
        NewPipe.init(DownloaderTestImpl.getInstance());
        subscriptionExtractor = new SoundcloudSubscriptionExtractor(ServiceList.SoundCloud);
        urlHandler = ServiceList.SoundCloud.getChannelLHFactory();
    }

    @Test
    public void testFromChannelUrl() throws Exception {
        testList(subscriptionExtractor.fromChannelUrl("https://soundcloud.com/monstercat"));
        testList(subscriptionExtractor.fromChannelUrl("http://soundcloud.com/monstercat"));
        testList(subscriptionExtractor.fromChannelUrl("soundcloud.com/monstercat"));
        testList(subscriptionExtractor.fromChannelUrl("monstercat"));

        //Empty followings user
        testList(subscriptionExtractor.fromChannelUrl("some-random-user-184047028"));
    }

    @Test
    public void testInvalidSourceException() {
        List<String> invalidList = Arrays.asList(
                "httttps://invalid.com/user",
                ".com/monstercat",
                "ithinkthatthisuserdontexist",
                "",
                null
        );

        for (String invalidUser : invalidList) {
            try {
                subscriptionExtractor.fromChannelUrl(invalidUser);

                fail("didn't throw exception");
            } catch (IOException e) {
                // Ignore it, could be an unstable network on the CI server
            } catch (Exception e) {
                boolean isExpectedException = e instanceof SubscriptionExtractor.InvalidSourceException;
                assertTrue(e.getClass().getSimpleName() + " is not the expected exception", isExpectedException);
            }
        }
    }

    private void testList(List<SubscriptionItem> subscriptionItems) throws ParsingException {
        for (SubscriptionItem item : subscriptionItems) {
            assertNotNull(item.getName());
            assertNotNull(item.getUrl());
            assertTrue(urlHandler.acceptUrl(item.getUrl()));
            assertFalse(item.getServiceId() == -1);
        }
    }
}
