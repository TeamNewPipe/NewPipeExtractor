package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Instance;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

public class PeertubeInstance implements Instance {

    private final String url;
    private String name;
    public static final PeertubeInstance defaultInstance = new PeertubeInstance("https://framatube.org", "FramaTube");

    public PeertubeInstance(String url) {
        this(url, "PeerTube");
    }

    public PeertubeInstance(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean isValid() {
        try {
            fetchInstanceMetaData();
            return true;
        } catch (InvalidInstanceException e) {
            return false;
        }
    }

    public void fetchInstanceMetaData() throws InvalidInstanceException {
        Downloader downloader = NewPipe.getDownloader();
        Response response = null;

        try {
            response = downloader.get(url + "/api/v1/config");
        } catch (ReCaptchaException | IOException e) {
            throw new InvalidInstanceException("unable to configure instance " + url, e);
        }

        if (response == null || Utils.isBlank(response.responseBody())) {
            throw new InvalidInstanceException("unable to configure instance " + url);
        }

        try {
            JsonObject json = JsonParser.object().from(response.responseBody());
            this.name = JsonUtils.getString(json, "instance.name");
        } catch (JsonParserException | ParsingException e) {
            throw new InvalidInstanceException("unable to parse instance config", e);
        }
    }

    public String getName() {
        return name;
    }

}
