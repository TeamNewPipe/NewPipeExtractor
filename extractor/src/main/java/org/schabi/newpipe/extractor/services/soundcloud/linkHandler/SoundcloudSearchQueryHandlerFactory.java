package org.schabi.newpipe.extractor.services.soundcloud.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class SoundcloudSearchQueryHandlerFactory extends SearchQueryHandlerFactory {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String TRACKS = "tracks";
    public static final String USERS = "users";
    public static final String PLAYLISTS = "playlists";
    public static final String ALL = "all";

    public static final int ITEMS_PER_PAGE = 10;

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        try {
            String url = "https://api-v2.soundcloud.com/search";

            if (contentFilter.size() > 0) {
                switch (contentFilter.get(0)) {
                    case TRACKS:
                        url += "/tracks";
                        break;
                    case USERS:
                        url += "/users";
                        break;
                    case PLAYLISTS:
                        url += "/playlists";
                        break;
                    case ALL:
                    default:
                        break;
                }
            }

            return url + "?q=" + URLEncoder.encode(id, CHARSET_UTF_8)
                    + "&client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=" + ITEMS_PER_PAGE
                    + "&offset=0";

        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (ReCaptchaException e) {
            throw new ParsingException("ReCaptcha required", e);
        } catch (IOException | ExtractionException e) {
            throw new ParsingException("Could not get client id", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                TRACKS,
                USERS,
                PLAYLISTS};
    }
}
