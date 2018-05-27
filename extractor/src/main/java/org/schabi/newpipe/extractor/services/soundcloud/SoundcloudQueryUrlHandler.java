package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.search.SearchQueryUrlHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SoundcloudQueryUrlHandler extends SearchQueryUrlHandler {
    public static final String CHARSET_UTF_8 = "UTF-8";

    public static final String TRACKS = "tracks";
    public static final String USERS = "users";
    public static final String PLAYLIST = "playlist";
    public static final String ANY = "any";

    @Override
    public String getUrl() throws ParsingException {
        try {
            String url = "https://api-v2.soundcloud.com/search";

            if(getContentFilter().size() > 0) {
                switch (getContentFilter().get(0)) {
                    case TRACKS:
                        url += "/tracks";
                        break;
                    case USERS:
                        url += "/users";
                        break;
                    case PLAYLIST:
                        url += "/playlists";
                        break;
                    case ANY:
                    default:
                        break;
                }
            }

            return url + "?q=" + URLEncoder.encode(id, CHARSET_UTF_8)
                    + "&client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=10";

        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (IOException e) {
            throw new ParsingException("Could not get client id", e);
        } catch (ReCaptchaException e) {
            throw new ParsingException("ReCaptcha required", e);
        }
    }

    @Override
    public String[] getAvailableContentFilter() {
        return new String[] {
            TRACKS,
            USERS,
            PLAYLIST,
            ANY};
    }
}
