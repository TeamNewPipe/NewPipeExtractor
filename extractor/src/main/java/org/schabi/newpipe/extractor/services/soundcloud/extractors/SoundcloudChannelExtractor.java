package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromVisualUrl;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChannelTabLinkHandlerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String userId;
    private JsonObject user;
    private static final String USERS_ENDPOINT = SOUNDCLOUD_API_V2_URL + "users/";

    public SoundcloudChannelExtractor(final StreamingService service,
                                      final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {

        userId = getLinkHandler().getId();
        final String apiUrl = USERS_ENDPOINT + userId + "?client_id="
                + SoundcloudParsingHelper.clientId();

        final String response = downloader.get(apiUrl, getExtractorLocalization()).responseBody();
        try {
            user = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
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

    @Nonnull
    @Override
    public List<Image> getAvatars() {
        return getAllImagesFromArtworkOrAvatarUrl(user.getString("avatar_url"));
    }

    @Nonnull
    @Override
    public List<Image> getBanners() {
        return getAllImagesFromVisualUrl(user.getObject("visuals")
                .getArray("visuals")
                .getObject(0)
                .getString("visual_url"));
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    @Override
    public long getSubscriberCount() {
        return user.getLong("followers_count", 0);
    }

    @Override
    public String getDescription() {
        return user.getString("description", "");
    }

    @Override
    public String getParentChannelName() {
        return "";
    }

    @Override
    public String getParentChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public List<Image> getParentChannelAvatars() {
        return List.of();
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return user.getBoolean("verified");
    }

    @Nonnull
    @Override
    public List<ListLinkHandler> getTabs() throws ParsingException {
        final String url = getUrl();
        final String urlTracks = url
                + SoundcloudChannelTabLinkHandlerFactory.getUrlSuffix(ChannelTabs.TRACKS);
        final String urlPlaylists = url
                + SoundcloudChannelTabLinkHandlerFactory.getUrlSuffix(ChannelTabs.PLAYLISTS);
        final String urlAlbums = url
                + SoundcloudChannelTabLinkHandlerFactory.getUrlSuffix(ChannelTabs.ALBUMS);
        final String id = getId();

        return List.of(
                new ListLinkHandler(urlTracks, urlTracks, id,
                        List.of(ChannelTabs.TRACKS), ""),
                new ListLinkHandler(urlPlaylists, urlPlaylists, id,
                        List.of(ChannelTabs.PLAYLISTS), ""),
                new ListLinkHandler(urlAlbums, urlAlbums, id,
                        List.of(ChannelTabs.ALBUMS), ""));
    }
}
