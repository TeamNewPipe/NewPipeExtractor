package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import java.io.IOException;

import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(int id, String name) {
        super(id, name);
    }

    @Override
    public SearchEngine getSearchEngine() {
        return new SoundcloudSearchEngine(getServiceId());
    }

    @Override
    public UrlIdHandler getStreamUrlIdHandler() {
        return SoundcloudStreamUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getChannelUrlIdHandler() {
        return SoundcloudChannelUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getPlaylistUrlIdHandler() {
        return SoundcloudPlaylistUrlIdHandler.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(String url) throws IOException, ExtractionException {
        return new SoundcloudStreamExtractor(this, url);
    }

    @Override
    public ChannelExtractor getChannelExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return new SoundcloudChannelExtractor(this, url, nextStreamsUrl);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return new SoundcloudPlaylistExtractor(this, url, nextStreamsUrl);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new SoundcloudSuggestionExtractor(getServiceId());
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        KioskList.KioskExtractorFactory chartsFactory = new KioskList.KioskExtractorFactory() {
            @Override
            public KioskExtractor createNewKiosk(StreamingService streamingService,
                                                 String url,
                                                 String nextStreamUrl,
                                                 String id)
                    throws ExtractionException, IOException {
                return new SoundcloudChartsExtractor(SoundcloudService.this,
                        url,
                        nextStreamUrl,
                        id);
            }
        };

        KioskList list = new KioskList(getServiceId());

        // add kiosks here e.g.:
        final SoundcloudChartsUrlIdHandler h = new SoundcloudChartsUrlIdHandler();
        try {
            list.addKioskEntry(chartsFactory, h, "Top 50");
            list.addKioskEntry(chartsFactory, h, "New & hot");
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    public boolean isFeedUrl(String url) {
        return url.contains("sounds.rss");
    }

    public String getUrlFromFeed(String feedUrl) {
        Downloader dl = NewPipe.getDownloader();
        String userId = feedUrl.split(":users:")[1];
        String apiUrl;
        try {
            apiUrl = "https://api.soundcloud.com/users/" + userId +
                    "?client_id=" + SoundcloudParsingHelper.clientId();
        } catch (ReCaptchaException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Parser.RegexException e) {
            e.printStackTrace();
            return null;
        }
        String response;
        try {
            response = dl.download(apiUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ReCaptchaException e) {
            e.printStackTrace();
            return null;
        }
        JsonObject user;
        try {
            user = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            e.printStackTrace();
            return null;
        }
        return user.getString("permalink_url");
    }
}
