package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.DownloadResponse;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Localization;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

public class PeertubeStreamExtractor extends StreamExtractor {

    
    private JsonObject json;
    
    public PeertubeStreamExtractor(StreamingService service, LinkHandler linkHandler, Localization localization) {
        super(service, linkHandler, localization);
    }

    @Override
    public String getUploadDate() throws ParsingException {
        String date = JsonUtils.getString(json, "publishedAt");
        return PeertubeParsingHelper.toDateString(date);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return ServiceList.PeerTube.getBaseUrl() + JsonUtils.getString(json, "thumbnailPath");
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return JsonUtils.getString(json, "description");
        }catch(ParsingException e) {
            return "No description";
        }
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        return NO_AGE_LIMIT;
    }

    @Override
    public long getLength() throws ParsingException {
        Number value = JsonUtils.getNumber(json, "duration");
        return value.longValue();
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        //TODO fetch timestamp from url if present;
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        Number value = JsonUtils.getNumber(json, "views");
        return value.longValue();
    }

    @Override
    public long getLikeCount() throws ParsingException {
        Number value = JsonUtils.getNumber(json, "likes");
        return value.longValue();
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        Number value = JsonUtils.getNumber(json, "dislikes");
        return value.longValue();
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String name = JsonUtils.getString(json, "account.name");
        String host = JsonUtils.getString(json, "account.host");
        return PeertubeChannelLinkHandlerFactory.getInstance().fromId(name + "@" + host).getUrl();
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(json, "account.displayName");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        String value;
        try {
            value = JsonUtils.getString(json, "account.avatar.path");
        }catch(Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return ServiceList.PeerTube.getBaseUrl() + value;
    }

    @Override
    public String getDashMpdUrl() throws ParsingException {
        return "";
    }

    @Override
    public String getHlsUrl() throws ParsingException {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        assertPageFetched();
        List<VideoStream> videoStreams = new ArrayList<>();
        try {
            JsonArray streams = json.getArray("files", new JsonArray());
            for(Object s: streams) {
                if(!(s instanceof JsonObject)) continue;
                JsonObject stream = (JsonObject) s;
                String url = JsonUtils.getString(stream, "fileUrl");
                String torrentUrl = JsonUtils.getString(stream, "torrentUrl");
                String resolution = JsonUtils.getString(stream, "resolution.label");
                String extension = url.substring(url.lastIndexOf(".") + 1);
                MediaFormat format = MediaFormat.getFromSuffix(extension);
                VideoStream videoStream = new VideoStream(url, torrentUrl, format, resolution);
                if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                    videoStreams.add(videoStream);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }

        return videoStreams;
    }


    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        List<String> tags = getTags();
        String apiUrl = null;
        if(!tags.isEmpty()) {
            apiUrl = getRelatedStreamsUrl(tags);
            
        }else {
            apiUrl = getUploaderUrl() + "/videos?start=0&count=8";
        }
        if(!StringUtil.isBlank(apiUrl)) getStreamsFromApi(collector, apiUrl);
        return collector;
    }
    
    private List<String> getTags(){
        try {
            return (List) JsonUtils.getArray(json, "tags");
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    private String getRelatedStreamsUrl(List<String> tags) throws UnsupportedEncodingException {
        String url = ServiceList.PeerTube.getBaseUrl() + PeertubeSearchQueryHandlerFactory.SEARCH_ENDPOINT;
        StringBuilder params = new StringBuilder();
        params.append("start=0&count=8&sort=-createdAt");
        for(String tag : tags) {
            params.append("&tagsOneOf=");
            params.append(URLEncoder.encode(tag, "UTF-8"));
        }
        return url + "?" + params.toString();
    }

    private void getStreamsFromApi(StreamInfoItemsCollector collector, String apiUrl) throws ReCaptchaException, IOException, ParsingException {
        DownloadResponse response = getDownloader().get(apiUrl);
        JsonObject relatedVideosJson = null;
        if(null != response && !StringUtil.isBlank(response.getResponseBody())) {
            try {
                relatedVideosJson = JsonParser.object().from(response.getResponseBody());
            } catch (JsonParserException e) {
                throw new ParsingException("Could not parse json data for related videos", e);
            }
        }
        
        if(relatedVideosJson != null) {
            collectStreamsFrom(collector, relatedVideosJson);
        }
    }
    
    private void collectStreamsFrom(StreamInfoItemsCollector collector, JsonObject json) throws ParsingException {
        JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        }catch(Exception e) {
            throw new ParsingException("unable to extract related videos", e);
        }
        
        for(Object c: contents) {
            if(c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                PeertubeStreamInfoItemExtractor extractor = new PeertubeStreamInfoItemExtractor(item);
                //do not add the same stream in related streams
                if(!extractor.getUrl().equals(getUrl())) collector.commit(extractor);
            }
        }
        
    }
    

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        DownloadResponse response = downloader.get(getUrl());
        if(null != response && null != response.getResponseBody()) {
            setInitialData(response.getResponseBody());
        }else {
            throw new ExtractionException("Unable to extract peertube channel data");
        }
    }

    private void setInitialData(String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ExtractionException("Unable to extract peertube stream data", e);
        }
        if(null == json) throw new ExtractionException("Unable to extract peertube stream data");
        PeertubeParsingHelper.validate(json);
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "name");
    }

}
