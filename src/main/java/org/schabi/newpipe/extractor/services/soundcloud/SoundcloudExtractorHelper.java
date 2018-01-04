package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;

import javax.annotation.Nonnull;

public class SoundcloudExtractorHelper {
    
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";


    private static String replaceHttpWithHttps(final String url) {
        if(!url.isEmpty() && url.startsWith(HTTP)) {
            return HTTPS + url.substring(HTTP.length());
        }
        return url;
    }

    @Nonnull
    static String getUploaderUrl(JsonObject object) {
        String url = object.getObject("user").getString("permalink_url", "");
        return replaceHttpWithHttps(url);
    }

    @Nonnull
    static String getAvatarUrl(JsonObject object) {
        String url = object.getObject("user", new JsonObject()).getString("avatar_url", "");
        return replaceHttpWithHttps(url);
    }

    public static String getUploaderName(JsonObject object) {
        return object.getObject("user").getString("username", "");
    }
}
