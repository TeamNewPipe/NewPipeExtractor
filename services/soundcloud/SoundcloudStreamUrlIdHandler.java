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

public class SoundcloudStreamUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudStreamUrlIdHandler instance = new SoundcloudStreamUrlIdHandler();
    private SoundcloudStreamUrlIdHandler() {
    }

    public static SoundcloudStreamUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String videoId) throws ParsingException {
        try {
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download("https://api-v2.soundcloud.com/tracks/" + videoId
                    + "?client_id=" + SoundcloudParsingHelper.clientId());
            JSONObject responseObject = new JSONObject(response);

            return responseObject.getString("permalink_url");
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download(url);
            Document doc = Jsoup.parse(response);

            Element androidElement = doc.select("meta[property=al:android:url]").first();
            String id = androidElement.attr("content").substring(20);

            return id;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException {
        try {
            Downloader dl = NewPipe.getDownloader();

            String response = dl.download(complexUrl);
            Document doc = Jsoup.parse(response);

            Element ogElement = doc.select("meta[property=og:url]").first();
            String url = ogElement.attr("content");

            return url;
        } catch (Exception e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    public boolean acceptUrl(String videoUrl) {
        String regex = "^https?://(www\\.)?soundcloud.com/[0-9a-z_-]+/(?!(tracks|albums|sets|reposts|followers|following)/?$)[0-9a-z_-]+/?([#?].*)?$";
        return Parser.isMatch(regex, videoUrl);
    }
}
