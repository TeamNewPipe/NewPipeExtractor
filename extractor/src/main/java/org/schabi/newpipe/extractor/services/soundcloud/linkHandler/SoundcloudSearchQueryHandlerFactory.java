package org.schabi.newpipe.extractor.services.soundcloud.linkHandler;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

public class SoundcloudSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    public static final String TRACKS = "tracks";
    public static final String USERS = "users";
    public static final String PLAYLISTS = "playlists";
    public static final String ALL = "all";
    private static final Set<String> QUERY_TYPES = Set.of(TRACKS, USERS, PLAYLISTS);

    public static final int ITEMS_PER_PAGE = 10;

    @Override
    public String getUrl(final String id, final List<String> contentFilter, final String sortFilter)
            throws ParsingException {
        try {
            final var urlBuilder = SOUNDCLOUD_API_V2_URL.newBuilder()
                    .addPathSegment("search");

            if (!contentFilter.isEmpty() && QUERY_TYPES.contains(contentFilter.get(0))) {
                urlBuilder.addPathSegment(contentFilter.get(0));
            }

            return urlBuilder.addEncodedQueryParameter("q", Utils.encodeUrlUtf8(id))
                    .addQueryParameter("client_id", SoundcloudParsingHelper.clientId())
                    .addQueryParameter("limit", String.valueOf(ITEMS_PER_PAGE))
                    .addQueryParameter("offset", "0")
                    .toString();
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (final ReCaptchaException e) {
            throw new ParsingException("ReCaptcha required", e);
        } catch (final IOException | ExtractionException e) {
            throw new ParsingException("Could not get client id", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{ALL, TRACKS, USERS, PLAYLISTS};
    }
}
