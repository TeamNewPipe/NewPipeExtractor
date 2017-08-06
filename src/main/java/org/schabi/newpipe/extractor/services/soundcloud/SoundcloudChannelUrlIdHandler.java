package org.schabi.newpipe.extractor.services.soundcloud;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;

public class SoundcloudChannelUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudChannelUrlIdHandler instance = new SoundcloudChannelUrlIdHandler();

    public static SoundcloudChannelUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String channelId) throws ParsingException {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/" + channelId);
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
    public boolean acceptUrl(String channelUrl) {
        String regex = "^https?://(www\\.)?soundcloud.com/[0-9a-z_-]+(/((tracks|albums|sets|reposts|followers|following)/?)?)?([#?].*)?$";
        return Parser.isMatch(regex, channelUrl.toLowerCase());
    }
}
