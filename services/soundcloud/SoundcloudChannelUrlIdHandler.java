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

public class SoundcloudChannelUrlIdHandler implements UrlIdHandler {

    private static final SoundcloudChannelUrlIdHandler instance = new SoundcloudChannelUrlIdHandler();

    public static SoundcloudChannelUrlIdHandler getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String channelId) throws RegexException, ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download("https://api-v2.soundcloud.com/user/" + channelId
                + "?client_id=" + SoundcloudParsingHelper.clientId());
        JSONObject responseObject = new JSONObject(response);

        return responseObject.getString("permalink_url");
    }

    @Override
    public String getId(String siteUrl) throws ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download(siteUrl);
        Document doc = Jsoup.parse(response);

        Element androidElement = doc.select("meta[property=al:android:url]").first();
        String id = androidElement.attr("content").substring(19);

        return id;
    }

    @Override
    public String cleanUrl(String siteUrl) throws ParsingException, ReCaptchaException, IOException {
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download(siteUrl);
        Document doc = Jsoup.parse(response);

        Element ogElement = doc.select("meta[property=og:url]").first();
        String url = ogElement.attr("content");

        return url;
    }

    @Override
    public boolean acceptUrl(String channelUrl) {
        return channelUrl.startsWith("https://soundcloud.com/")
                && (channelUrl.split("/").length == 4
                || channelUrl.endsWith("/tracks")
                || channelUrl.endsWith("/albums")
                || channelUrl.endsWith("/sets")
                || channelUrl.endsWith("/reposts")
                || channelUrl.endsWith("/followers")
                || channelUrl.endsWith("/following"));

    }
}
