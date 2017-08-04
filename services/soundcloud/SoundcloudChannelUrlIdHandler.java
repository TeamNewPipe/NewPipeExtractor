package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
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
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download("https://api-v2.soundcloud.com/user/" + channelId
                    + "?client_id=" + SoundcloudParsingHelper.clientId());
            JSONObject responseObject = new JSONObject(response);

            return responseObject.getString("permalink_url");
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String getId(String siteUrl) throws ParsingException {
        try {
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download(siteUrl);
            Document doc = Jsoup.parse(response);

            Element androidElement = doc.select("meta[property=al:android:url]").first();
            String id = androidElement.attr("content").substring(19);

            return id;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String cleanUrl(String siteUrl) throws ParsingException {
        try {
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download(siteUrl);
            Document doc = Jsoup.parse(response);

            Element ogElement = doc.select("meta[property=og:url]").first();
            String url = ogElement.attr("content");

            return url;
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
