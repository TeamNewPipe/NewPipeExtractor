package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionItem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.subscription.SubscriptionExtractor.ContentSource.INPUT_STREAM;

/**
 * Extract subscriptions from a Google takout export (the user has to get the JSON out of the zip)
 */
public class YoutubeSubscriptionExtractor extends SubscriptionExtractor {
    private static final String BASE_CHANNEL_URL = "https://www.youtube.com/channel/";

    public YoutubeSubscriptionExtractor(final YoutubeService youtubeService) {
        super(youtubeService, Collections.singletonList(INPUT_STREAM));
    }

    @Override
    public String getRelatedUrl() {
        return "https://takeout.google.com/takeout/custom/youtube";
    }

    @Override
    public List<SubscriptionItem> fromInputStream(@Nonnull final InputStream contentInputStream)
            throws ExtractionException {
        final JsonArray subscriptions;
        try {
            subscriptions = JsonParser.array().from(contentInputStream);
        } catch (JsonParserException e) {
            throw new InvalidSourceException("Invalid json input stream", e);
        }

        boolean foundInvalidSubscription = false;
        final List<SubscriptionItem> subscriptionItems = new ArrayList<>();
        for (final Object subscriptionObject : subscriptions) {
            if (!(subscriptionObject instanceof JsonObject)) {
                foundInvalidSubscription = true;
                continue;
            }

            final JsonObject subscription = ((JsonObject) subscriptionObject).getObject("snippet");
            final String id = subscription.getObject("resourceId").getString("channelId", "");
            if (id.length() != 24) { // e.g. UCsXVk37bltHxD1rDPwtNM8Q
                foundInvalidSubscription = true;
                continue;
            }

            subscriptionItems.add(new SubscriptionItem(service.getServiceId(),
                    BASE_CHANNEL_URL + id, subscription.getString("title", "")));
        }

        if (foundInvalidSubscription && subscriptionItems.isEmpty()) {
            throw new InvalidSourceException("Found only invalid channel ids");
        }
        return subscriptionItems;
    }
}
