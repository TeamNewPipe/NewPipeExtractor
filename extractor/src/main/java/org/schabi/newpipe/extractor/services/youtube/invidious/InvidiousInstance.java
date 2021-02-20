package org.schabi.newpipe.extractor.services.youtube.invidious;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;

public class InvidiousInstance implements Instance {

    private final String url;
    private String name;
    private Boolean isValid = null;

    private static final String STATISTICS_DISABLED = "Statistics are not enabled.";

    private static InvidiousInstance defaultInstance = new InvidiousInstance("https://invidious.fdn.fr");

    public InvidiousInstance(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public InvidiousInstance(String url) {
        this(url, "invidious");
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isValid() {
        if (isValid != null) {
            return isValid;
        }

        URL url;
        try {
            url = Utils.stringToURL(this.url);
        } catch (MalformedURLException e) {
            return false;
        }

        if (isYoutubeURL(url) || isHooktubeURL(url) || isInvidiousRedirectUrl(url)) {
            return isValid = false;
        }

        try {
            fetchInstanceMetaData();
            return isValid = true;
        } catch (InvalidInstanceException e) {
            return isValid = false;
        }
    }

    @Override
    public void fetchInstanceMetaData() throws InvalidInstanceException {
        final Downloader downloader = NewPipe.getDownloader();
        Response response;

        try {
            response = downloader.get(url + "/api/v1/stats?fields=software,error");
        } catch (ReCaptchaException | IOException | IllegalArgumentException e) {
            throw new InvalidInstanceException("unable to configure instance " + url, e);
        }

        if (response == null || Utils.isBlank(response.responseBody())) {
            throw new InvalidInstanceException("unable to configure instance " + url);
        }

        try {
            JsonObject json = JsonParser.object().from(response.responseBody());
            if (json.has("software")) {
                this.name = JsonUtils.getString(json, "software.name");
            } else if (json.has("error")) {
                if (!STATISTICS_DISABLED.equals(json.getString("error"))) {
                    throw new ParsingException("Could not get stats from instance " + url);
                }
            }
        } catch (JsonParserException | ParsingException e) {
            throw new InvalidInstanceException("unable to parse instance config", e);
        }
    }

    public static InvidiousInstance getDefaultInstance() {
        return defaultInstance;
    }

}
