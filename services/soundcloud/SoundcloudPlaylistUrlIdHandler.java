package org.schabi.newpipe.extractor.services.soundcloud;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;

public class SoundcloudPlaylistUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudPlaylistUrlIdHandler instance = new SoundcloudPlaylistUrlIdHandler();

    public static SoundcloudPlaylistUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String listId) throws RegexException, ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download("https://api-v2.soundcloud.com/playlists/" + listId
                + "?client_id=" + SoundcloudParsingHelper.clientId());
        JSONObject responseObject = new JSONObject(response);

        return responseObject.getString("permalink_url");
    }

    @Override
    public String getId(String url) throws ParsingException, ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download(url);
        Document doc = Jsoup.parse(response);

        Element androidElement = doc.select("meta[property=al:android:url]").first();
        String id = androidElement.attr("content").substring(23);

        return id;
    }

    @Override
    public String cleanUrl(String complexUrl) throws ParsingException, ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download(complexUrl);
        Document doc = Jsoup.parse(response);

        Element ogElement = doc.select("meta[property=og:url]").first();
        String url = ogElement.attr("content");

        return url;
    }

    @Override
    public boolean acceptUrl(String videoUrl) {
        return videoUrl.startsWith("https://soundcloud.com/")
                && !videoUrl.split("/")[4].equals("sets")
                && !videoUrl.split("/")[5].equals("");
    }
}
