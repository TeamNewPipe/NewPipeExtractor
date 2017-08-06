package org.schabi.newpipe.extractor.services.soundcloud;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;

public class SoundcloudPlaylistUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudPlaylistUrlIdHandler instance = new SoundcloudPlaylistUrlIdHandler();

    public static SoundcloudPlaylistUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String listId) throws ParsingException {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/playlists/" + listId);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            return SoundcloudParsingHelper.resolveIdWithEmbedPlayer(url);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException {
        try {
            Element ogElement = Jsoup.parse(NewPipe.getDownloader().download(complexUrl))
                    .select("meta[property=og:url]").first();

            return ogElement.attr("content");
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public boolean acceptUrl(String videoUrl) {
        String regex = "^https?://(www\\.)?soundcloud.com/[0-9a-z_-]+/sets/[0-9a-z_-]+/?([#?].*)?$";
        return Parser.isMatch(regex, videoUrl.toLowerCase());
    }
}
