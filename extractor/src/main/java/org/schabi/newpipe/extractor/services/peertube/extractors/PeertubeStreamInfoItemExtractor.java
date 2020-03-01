package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class PeertubeStreamInfoItemExtractor implements StreamInfoItemExtractor {

    protected final JsonObject item;
    private final String baseUrl;

    public PeertubeStreamInfoItemExtractor(JsonObject item, String baseUrl) {
        this.item = item;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getUrl() throws ParsingException {
        String uuid = JsonUtils.getString(item, "uuid");
        return ServiceList.PeerTube.getStreamLHFactory().fromId(uuid, baseUrl).getUrl();
    }

    @Override
    public List<Image> getThumbnails() throws ParsingException {
        List<Image> images = new ArrayList<>();

        images.add(new Image(baseUrl + JsonUtils.getString(item, "thumbnailPath"), 223, 122)); // See https://github.com/Chocobozzz/PeerTube/blob/366caf8b71f3d82336b6ac243845c783ef673fc1/server/initializers/constants.ts#L548
        images.add(new Image(baseUrl + JsonUtils.getString(item, "previewPath"), 850, 480)); // See https://github.com/Chocobozzz/PeerTube/blob/366caf8b71f3d82336b6ac243845c783ef673fc1/server/initializers/constants.ts#L553

        return images;
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "name");
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getViewCount() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "views");
        return value.longValue();
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String name = JsonUtils.getString(item, "account.name");
        String host = JsonUtils.getString(item, "account.host");
        return ServiceList.PeerTube.getChannelLHFactory().fromId(name + "@" + host, baseUrl).getUrl();
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
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "duration");
        return value.longValue();
    }

}
