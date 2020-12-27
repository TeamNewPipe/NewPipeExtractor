package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class MediaCCCLiveStreamKiosk extends KioskExtractor<StreamInfoItem> {
    public JsonArray doc;

    public MediaCCCLiveStreamKiosk(StreamingService streamingService, ListLinkHandler linkHandler, String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String site = downloader.get("https://streaming.media.ccc.de/streams/v2.json",
                getExtractorLocalization()).responseBody();
        // for testing, from
        /*site = "[\n" +
                "  {\n" +
                "    \"conference\": \"32C3\",\n" +
                "    \"slug\": \"32c3\",\n" +
                "    \"author\": \"CCC\",\n" +
                "    \"description\": \"Live-Streaming vom 32C3\",\n" +
                "    \"keywords\": \"32C3, Hacking, Chaos Computer Club, Video, Music, Podcast, Media, Streaming, Hacker, Hamburg\",\n" +
                "    \"startsAt\": \"2015-12-27T05:00:00+0000\",\n" +
                "    \"endsAt\": \"2015-12-30T20:00:00+0000\",\n" +
                "    \"groups\": [\n" +
                "      {\n" +
                "        \"group\": \"Lecture Rooms\",\n" +
                "        \"rooms\": [\n" +
                "          {\n" +
                "            \"slug\": \"hall1\",\n" +
                "            \"schedulename\": \"Hall 1\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/conferences/archconf/2020/6390-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/hall1?forceopen=yess\",\n" +
                "            \"display\": \"Hall 1\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"hd-native\",\n" +
                "                \"display\": \"Hall 1 FullHD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_native_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_native_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"hd-translated\",\n" +
                "                \"display\": \"Hall 1 FullHD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_translated_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_translated_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-native\",\n" +
                "                \"display\": \"Hall 1 SD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_native_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_native_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-translated\",\n" +
                "                \"display\": \"Hall 1 SD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_translated_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_translated_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-native\",\n" +
                "                \"display\": \"Hall 1 Slides\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_native_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_native_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-translated\",\n" +
                "                \"display\": \"Hall 1 Slides (Translation)\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_translated_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s1_translated_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-native\",\n" +
                "                \"display\": \"Hall 1 Audio\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_native.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_native.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-translated\",\n" +
                "                \"display\": \"Hall 1 Audio (Translation)\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_translated.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s1_translated.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"hall2\",\n" +
                "            \"schedulename\": \"Hall 2\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/conferences/archconf/2020/6390-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/hall2?forceopen=yess\",\n" +
                "            \"display\": \"Hall 2\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"hd-native\",\n" +
                "                \"display\": \"Hall 2 FullHD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_native_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_native_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"hd-translated\",\n" +
                "                \"display\": \"Hall 2 FullHD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_translated_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_translated_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-native\",\n" +
                "                \"display\": \"Hall 2 SD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_native_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_native_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-translated\",\n" +
                "                \"display\": \"Hall 2 SD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_translated_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_translated_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-native\",\n" +
                "                \"display\": \"Hall 2 Slides\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_native_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_native_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-translated\",\n" +
                "                \"display\": \"Hall 2 Slides (Translation)\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_translated_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s2_translated_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-native\",\n" +
                "                \"display\": \"Hall 2 Audio\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_native.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_native.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-translated\",\n" +
                "                \"display\": \"Hall 2 Audio (Translation)\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_translated.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s2_translated.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"hallg\",\n" +
                "            \"schedulename\": \"Hall G\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/events/privacyweek/2020/368-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/hallg?forceopen=yess\",\n" +
                "            \"display\": \"Hall G\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"hd-native\",\n" +
                "                \"display\": \"Hall G FullHD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_native_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_native_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"hd-translated\",\n" +
                "                \"display\": \"Hall G FullHD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_translated_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_translated_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-native\",\n" +
                "                \"display\": \"Hall G SD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_native_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_native_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-translated\",\n" +
                "                \"display\": \"Hall G SD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_translated_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_translated_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-native\",\n" +
                "                \"display\": \"Hall G Slides\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_native_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_native_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-translated\",\n" +
                "                \"display\": \"Hall G Slides (Translation)\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_translated_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s3_translated_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-native\",\n" +
                "                \"display\": \"Hall G Audio\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_native.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_native.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-translated\",\n" +
                "                \"display\": \"Hall G Audio (Translation)\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_translated.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s3_translated.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"hall6\",\n" +
                "            \"schedulename\": \"Hall 6\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/conferences/archconf/2020/6307-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/hall6?forceopen=yess\",\n" +
                "            \"display\": \"Hall 6\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"hd-native\",\n" +
                "                \"display\": \"Hall 6 FullHD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_native_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_native_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"hd-translated\",\n" +
                "                \"display\": \"Hall 6 FullHD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_translated_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_translated_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-native\",\n" +
                "                \"display\": \"Hall 6 SD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_native_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_native_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-translated\",\n" +
                "                \"display\": \"Hall 6 SD Video (Translation)\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_translated_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_translated_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-native\",\n" +
                "                \"display\": \"Hall 6 Slides\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_native_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_native_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"slides-translated\",\n" +
                "                \"display\": \"Hall 6 Slides (Translation)\",\n" +
                "                \"type\": \"slides\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_translated_slides.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s4_translated_slides.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-native\",\n" +
                "                \"display\": \"Hall 6 Audio\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_native.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_native.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-translated\",\n" +
                "                \"display\": \"Hall 6 Audio (Translation)\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": true,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_translated.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s4_translated.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"group\": \"Live Music\",\n" +
                "        \"rooms\": [\n" +
                "          {\n" +
                "            \"slug\": \"lounge\",\n" +
                "            \"schedulename\": \"Lounge\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/events/BigBrotherAwards/2020/2-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/lounge?forceopen=yess\",\n" +
                "            \"display\": \"Lounge\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Lounge Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/lounge.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/lounge.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"ambient\",\n" +
                "            \"schedulename\": \"Ambient\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/congress/2019/11175-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/ambient?forceopen=yess\",\n" +
                "            \"display\": \"Ambient\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Ambient Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/ambient.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/ambient.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"dome\",\n" +
                "            \"schedulename\": \"Dome\",\n" +
                "            \"thumb\": \"https://static.media.ccc.de/media/regional/c4/openchaos/2011-hd.jpg\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/dome?forceopen=yess\",\n" +
                "            \"display\": \"Dome\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Dome Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/dome.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/dome.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"chaos-west\",\n" +
                "            \"schedulename\": \"Chaos-West\",\n" +
                "            \"thumb\": \"http://localhost:8000/streams/thumbs/chaos-west.png\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/chaos-west?forceopen=yess\",\n" +
                "            \"display\": \"Chaos-West\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Chaos-West Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/chaos-west.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/chaos-west.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"radio\",\n" +
                "            \"schedulename\": \"Radio Fairydust\",\n" +
                "            \"thumb\": \"http://localhost:8000/streams/thumbs/radio.png\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/radio?forceopen=yess\",\n" +
                "            \"display\": \"Radio Fairydust\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Radio Fairydust Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/radio.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/radio.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      },\n" +
                "      {\n" +
                "        \"group\": \"Live Podcasts\",\n" +
                "        \"rooms\": [\n" +
                "          {\n" +
                "            \"slug\": \"sendezentrum\",\n" +
                "            \"schedulename\": \"Bühne\",\n" +
                "            \"thumb\": \"http://localhost:8000/streams/thumbs/s5.png\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/sendezentrum?forceopen=yess\",\n" +
                "            \"display\": \"Sendezentrum\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"hd-native\",\n" +
                "                \"display\": \"Sendezentrum FullHD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1920,\n" +
                "                  1080\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s5_native_hd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s5_native_hd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"sd-native\",\n" +
                "                \"display\": \"Sendezentrum SD Video\",\n" +
                "                \"type\": \"video\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": [\n" +
                "                  1024,\n" +
                "                  576\n" +
                "                ],\n" +
                "                \"urls\": {\n" +
                "                  \"webm\": {\n" +
                "                    \"display\": \"WebM\",\n" +
                "                    \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s5_native_sd.webm\"\n" +
                "                  },\n" +
                "                  \"hls\": {\n" +
                "                    \"display\": \"HLS\",\n" +
                "                    \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/hls/s5_native_sd.m3u8\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"slug\": \"audio-native\",\n" +
                "                \"display\": \"Sendezentrum Audio\",\n" +
                "                \"type\": \"audio\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s5_native.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/s5_native.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          },\n" +
                "          {\n" +
                "            \"slug\": \"podcastertisch\",\n" +
                "            \"schedulename\": \"Podcaster-Tisch\",\n" +
                "            \"thumb\": \"http://localhost:8000/streams/thumbs/podcastertisch.png\",\n" +
                "            \"link\": \"http://localhost:8000/streams/32c3/podcastertisch?forceopen=yess\",\n" +
                "            \"display\": \"Sendezentrum - Podcastertisch\",\n" +
                "            \"streams\": [\n" +
                "              {\n" +
                "                \"slug\": \"music-native\",\n" +
                "                \"display\": \"Sendezentrum - Podcastertisch Radio\",\n" +
                "                \"type\": \"music\",\n" +
                "                \"isTranslated\": false,\n" +
                "                \"videoSize\": null,\n" +
                "                \"urls\": {\n" +
                "                  \"mp3\": {\n" +
                "                    \"display\": \"MP3\",\n" +
                "                    \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/podcastertisch.mp3\"\n" +
                "                  },\n" +
                "                  \"opus\": {\n" +
                "                    \"display\": \"Opus\",\n" +
                "                    \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                    \"url\": \"http://cdn.c3voc.de/podcastertisch.opus\"\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";*/
        /*site = "[\n" +
                "    {\n" +
                "        \"author\": \"CCC\",\n" +
                "        \"conference\": \"33C3\",\n" +
                "        \"description\": \"Live-Streaming vom 33C3\",\n" +
                "        \"endsAt\": \"2016-12-30T20:00:00+0000\",\n" +
                "        \"groups\": [\n" +
                "            {\n" +
                "                \"group\": \"Live\",\n" +
                "                \"rooms\": [\n" +
                "                    {\n" +
                "                        \"display\": \"Saal 1\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/hall1\",\n" +
                "                        \"schedulename\": \"Saal 1\",\n" +
                "                        \"slug\": \"hall1\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 FullHD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"hd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_native_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_native_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 FullHD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"hd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_translated_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_translated_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 SD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"sd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_native_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_native_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 SD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"sd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_translated_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_translated_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 Slides\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"slides-native\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_native_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_native_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 Slides (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"slides-translated\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s1_translated_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_translated_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 Audio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"audio-native\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_native.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_native.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 1 Audio (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"audio-translated\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_translated.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s1_translated.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/s1.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Saal 2\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/hall2\",\n" +
                "                        \"schedulename\": \"Saal 2\",\n" +
                "                        \"slug\": \"hall2\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 FullHD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"hd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_native_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_native_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 FullHD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"hd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_translated_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_translated_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 SD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"sd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_native_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_native_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 SD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"sd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_translated_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_translated_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 Slides\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"slides-native\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_native_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_native_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 Slides (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"slides-translated\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s2_translated_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_translated_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 Audio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"audio-native\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_native.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_native.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 2 Audio (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"audio-translated\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_translated.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s2_translated.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/s2.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Saal G\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/hallg\",\n" +
                "                        \"schedulename\": \"Saal G\",\n" +
                "                        \"slug\": \"hallg\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G FullHD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"hd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_native_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_native_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G FullHD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"hd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_translated_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_translated_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G SD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"sd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_native_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_native_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G SD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"sd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_translated_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_translated_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G Slides\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"slides-native\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_native_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_native_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G Slides (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"slides-translated\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s3_translated_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_translated_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G Audio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"audio-native\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_native.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_native.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal G Audio (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"audio-translated\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_translated.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s3_translated.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/s3.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Saal 6\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/hall6\",\n" +
                "                        \"schedulename\": \"Saal 6\",\n" +
                "                        \"slug\": \"hall6\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 FullHD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"hd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_native_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_native_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 FullHD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"hd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_translated_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_translated_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 SD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"sd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_native_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_native_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 SD Video (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"sd-translated\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_translated_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_translated_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 Slides\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"slides-native\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_native_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_native_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 Slides (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"slides-translated\",\n" +
                "                                \"type\": \"slides\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s4_translated_slides.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, XXX kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_translated_slides.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 Audio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"audio-native\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_native.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_native.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Saal 6 Audio (Translation)\",\n" +
                "                                \"isTranslated\": true,\n" +
                "                                \"slug\": \"audio-translated\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_translated.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s4_translated.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/s4.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Sendezentrum\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/sendezentrum\",\n" +
                "                        \"schedulename\": \"Sendezentrum\",\n" +
                "                        \"slug\": \"sendezentrum\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Sendezentrum FullHD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"hd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1920x1080, h264+AAC im MPEG-TS-Container via HTTP, 3 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s5_native_hd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1920x1080, VP8+Vorbis in WebM, 2.8 MBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s5_native_hd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1920,\n" +
                "                                    1080\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Sendezentrum SD Video\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"sd-native\",\n" +
                "                                \"type\": \"video\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"hls\": {\n" +
                "                                        \"display\": \"HLS\",\n" +
                "                                        \"tech\": \"1024x576, h264+AAC im MPEG-TS-Container via HTTP, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/hls/s5_native_sd.m3u8\"\n" +
                "                                    },\n" +
                "                                    \"webm\": {\n" +
                "                                        \"display\": \"WebM\",\n" +
                "                                        \"tech\": \"1024x576, VP8+Vorbis in WebM, 800 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s5_native_sd.webm\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": [\n" +
                "                                    1024,\n" +
                "                                    576\n" +
                "                                ]\n" +
                "                            },\n" +
                "                            {\n" +
                "                                \"display\": \"Sendezentrum Audio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"audio-native\",\n" +
                "                                \"type\": \"audio\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s5_native.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 64 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/s5_native.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/s5.png\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"group\": \"Live Music\",\n" +
                "                \"rooms\": [\n" +
                "                    {\n" +
                "                        \"display\": \"Section 9\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/section-9\",\n" +
                "                        \"schedulename\": \"Section 9\",\n" +
                "                        \"slug\": \"section-9\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Section 9 Radio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"music-native\",\n" +
                "                                \"type\": \"music\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/ambient.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/ambient.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/ambient.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"DisKo\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/disko\",\n" +
                "                        \"schedulename\": \"DisKo\",\n" +
                "                        \"slug\": \"disko\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"DisKo Radio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"music-native\",\n" +
                "                                \"type\": \"music\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/lounge.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/lounge.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/lounge.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Schneekugel\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/schneekugel\",\n" +
                "                        \"schedulename\": \"Schneekugel\",\n" +
                "                        \"slug\": \"schneekugel\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Schneekugel Radio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"music-native\",\n" +
                "                                \"type\": \"music\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/dome.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/dome.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/dome.png\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"display\": \"Chaoswest\",\n" +
                "                        \"link\": \"https://streaming.media.ccc.de/33c3/chaoswest\",\n" +
                "                        \"schedulename\": \"Chaoswest\",\n" +
                "                        \"slug\": \"chaoswest\",\n" +
                "                        \"streams\": [\n" +
                "                            {\n" +
                "                                \"display\": \"Chaoswest Radio\",\n" +
                "                                \"isTranslated\": false,\n" +
                "                                \"slug\": \"music-native\",\n" +
                "                                \"type\": \"music\",\n" +
                "                                \"urls\": {\n" +
                "                                    \"mp3\": {\n" +
                "                                        \"display\": \"MP3\",\n" +
                "                                        \"tech\": \"MP3-Audio, 192 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/chaoswest_lounge.mp3\"\n" +
                "                                    },\n" +
                "                                    \"opus\": {\n" +
                "                                        \"display\": \"Opus\",\n" +
                "                                        \"tech\": \"Opus-Audio, 96 kBit/s\",\n" +
                "                                        \"url\": \"https://cdn.c3voc.de/chaoswest_lounge.opus\"\n" +
                "                                    }\n" +
                "                                },\n" +
                "                                \"videoSize\": null\n" +
                "                            }\n" +
                "                        ],\n" +
                "                        \"thumb\": \"https://streaming.media.ccc.de/thumbs/chaoswest_lounge.png\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"keywords\": \"33C3, Hacking, Chaos Computer Club, Video, Music, Podcast, Media, Streaming, Hacker, Hamburg, Works, For, Me, Chaos, Everywhere\",\n" +
                "        \"slug\": \"33c3\",\n" +
                "        \"startsAt\": \"2016-12-27T05:00:00+0000\"\n" +
                "    }\n" +
                "]\n";*/
        try {
            doc = JsonParser.array().from(site);
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json.", jpe);
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        for (int c = 0; c < doc.size(); c++) {
            final JsonObject conference = doc.getObject(c);
            final JsonArray groups = conference.getArray("groups");
            for (int g = 0; g < groups.size(); g++) {
                final String group = groups.getObject(g).getString("group");
                final JsonArray rooms = groups.getObject(g).getArray("rooms");
                for (int r = 0; r < rooms.size(); r++) {
                    final JsonObject room = rooms.getObject(r);
                    collector.commit(new MediaCCCLiveStreamExtractor(conference, group, room));
                }
            }

        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return InfoItemsPage.emptyPage();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "live";
    }
}
