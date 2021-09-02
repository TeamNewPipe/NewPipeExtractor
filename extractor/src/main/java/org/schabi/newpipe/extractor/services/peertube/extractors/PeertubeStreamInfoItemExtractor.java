package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nullable;

public class PeertubeStreamInfoItemExtractor implements StreamInfoItemExtractor {

    protected final JsonObject item;
    private String baseUrl;

    public PeertubeStreamInfoItemExtractor(final JsonObject item, final String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getUrl() throws ParsingException {
        final String uuid = JsonUtils.getString(item, "uuid");
        return ServiceList.PeerTube.getStreamLHFactory().fromId(uuid, baseUrl).getUrl();
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return baseUrl + JsonUtils.getString(item, "thumbnailPath");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "name");
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getViewCount() {
        return item.getLong("views");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String name = JsonUtils.getString(item, "account.name");
        final String host = JsonUtils.getString(item, "account.host");

        return ServiceList.PeerTube.getChannelLHFactory()
                .fromId("accounts/" + name + "@" + host, baseUrl).getUrl();
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        final JsonObject account = item.getObject("account");
        if (account.has("avatar") && !account.isNull("avatar")) {
            return baseUrl + account.getObject("avatar").getString("path");
        }
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(item, "publishedAt");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();

        if (textualUploadDate == null) {
            return null;
        }

        return new DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate));
    }

    @Override
    public StreamType getStreamType() {
        return item.getBoolean("isLive") ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() {
        return item.getLong("duration");
    }

    protected void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
