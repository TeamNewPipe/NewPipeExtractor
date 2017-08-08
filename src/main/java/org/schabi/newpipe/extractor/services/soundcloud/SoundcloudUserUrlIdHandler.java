package org.schabi.newpipe.extractor.services.soundcloud;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

public class SoundcloudUserUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudUserUrlIdHandler instance = new SoundcloudUserUrlIdHandler();
    private final String URL_PATTERN = "^https?://(www\\.)?soundcloud.com/[0-9a-z_-]+" +
            "(/((tracks|albums|sets|reposts|followers|following)/?)?)?([#?].*)?$";

    public static SoundcloudUserUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id) throws ParsingException {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/" + id);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String getId(String url) throws ParsingException {
        Utils.checkUrl(URL_PATTERN, url);

        try {
            return SoundcloudParsingHelper.resolveIdWithEmbedPlayer(url);
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException {
        Utils.checkUrl(URL_PATTERN, complexUrl);

        try {
            Element ogElement = Jsoup.parse(NewPipe.getDownloader().download(complexUrl))
                    .select("meta[property=og:url]").first();

            return ogElement.attr("content");
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public boolean acceptUrl(String url) {
        return Parser.isMatch(URL_PATTERN, url.toLowerCase());
    }
}
