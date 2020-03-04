package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

@SuppressWarnings("WeakerAccess")
public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String userId;
    private JsonObject user;

    public SoundcloudChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

        userId = getLinkHandler().getId();
        String apiUrl = "https://api-v2.soundcloud.com/users/" + userId +
                "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = downloader.get(apiUrl, getExtractorLocalization()).responseBody();
        try {
            user = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return userId;
    }

    @Nonnull
    @Override
    public String getName() {
        return user.getString("username");
    }

    @Override
    public String getAvatarUrl() {
        return user.getString("avatar_url");
    }

    @Override
    public String getBannerUrl() {
        return user.getObject("visuals", new JsonObject())
                .getArray("visuals", new JsonArray())
                .getObject(0, new JsonObject())
                .getString("visual_url");
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    @Override
    public long getSubscriberCount() {
        return user.getNumber("followers_count", 0).longValue();
    }

    @Override
    public String getDescription() {
        return user.getString("description", "");
    }

    @Override
    public List<ChannelTabExtractor> getTabs() {
        List<ChannelTabExtractor> tabs = new ArrayList<>();

        tabs.add(new SoundcloudChannelPopularTracksExtractor(getService(), (ListLinkHandler) getLinkHandler()));
        tabs.add(new SoundcloudChannelTracksExtractor(getService(), (ListLinkHandler) getLinkHandler()));
        tabs.add(new SoundcloudChannelAlbumsExtractor(getService(), (ListLinkHandler) getLinkHandler()));
        tabs.add(new SoundcloudChannelPlaylistsExtractor(getService(), (ListLinkHandler) getLinkHandler()));

        return tabs;
    }
}
