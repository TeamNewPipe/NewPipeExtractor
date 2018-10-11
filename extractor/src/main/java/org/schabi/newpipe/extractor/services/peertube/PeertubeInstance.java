package org.schabi.newpipe.extractor.services.peertube;

import java.io.IOException;

import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.DownloadResponse;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

public class PeertubeInstance {
    
    private final String url;
    private String name;
    public static final PeertubeInstance defaultInstance = new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host");
    
    public PeertubeInstance(String url) throws IOException {
        this.url = url;
        String response = validateInstance(url);
        setInstanceMetaData(response);
    }
    
    private PeertubeInstance(String url , String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    
    private String validateInstance(String url) throws IOException {
        Downloader downloader = NewPipe.getDownloader();
        DownloadResponse response = null;
        
        try {
            response = downloader.get(url + "/api/v1/config");
        } catch (ReCaptchaException | IOException e) {
            throw new IOException("unable to configure instance " + url, e);
        }
        
        if(null == response || StringUtil.isBlank(response.getResponseBody())) {
            throw new IOException("unable to configure instance " + url);
        }
        
        return response.getResponseBody();
    }

    private void setInstanceMetaData(String responseBody) {
        JsonObject json;
        try {
            json = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            return;
        }
        
        if(null == json) return;
        
        try {
            this.name = JsonUtils.getString(json, "instance.name");
        } catch (ParsingException e) {
            return;
        }
    }

    public String getName() {
        return name;
    }
    
}
