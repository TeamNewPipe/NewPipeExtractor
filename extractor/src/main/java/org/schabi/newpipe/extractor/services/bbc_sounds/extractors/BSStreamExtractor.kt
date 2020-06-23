package org.schabi.newpipe.extractor.services.bbc_sounds.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bbc_sounds.linkHandler.BSStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.*
import java.util.*

class BSStreamExtractor(service: StreamingService?, linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {

    private lateinit var data: JsonObject
    private lateinit var basicInfoExtractor: StreamInfoItemExtractor
    private lateinit var audioStreams: List<AudioStream>

    override fun onFetchPage(downloader: Downloader) {
        try {
            data = JsonParser.`object`().from(downloader.get(linkHandler.url).responseBody())
        } catch (e: JsonParserException) {
            throw ExtractionException("could not parse data from ${linkHandler.url}", e)
        }

        basicInfoExtractor = BSExtractorHelper.getStreamInfoItemExtractor(data)
        audioStreams = BSExtractorHelper.fetchStreams(downloader, data)
    }

    override fun getUploadDate(): DateWrapper? {
        return basicInfoExtractor.uploadDate
    }

    override fun getDescription(): Description {
        return data.getObject("synopses")?.getString("long").let { Description(it, Description.PLAIN_TEXT) }
    }

    override fun getAgeLimit(): Int {
        return 0
    }

    override fun getLength(): Long {
        return basicInfoExtractor.duration
    }

    override fun getTimeStamp(): Long {
        return 0L
    }

    override fun getDashMpdUrl(): String {
        // selects best dash format
        return audioStreams.filter { it.deliveryFormat is DeliveryFormat.ManualDASH }
                .maxBy { it.averageBitrate }
                ?.let { it.deliveryFormat as DeliveryFormat.ManualDASH }
                ?.baseUrl ?: ""
    }

    override fun getVideoOnlyStreams(): MutableList<VideoStream>? {
        return Collections.emptyList()
    }

    override fun getSubtitles(format: MediaFormat?): MutableList<SubtitlesStream> {
        return Collections.emptyList()
    }

    override fun getStreamType(): StreamType {
        return basicInfoExtractor.streamType
    }

    override fun getRelatedStreams(): StreamInfoItemsCollector? {
        val collector = StreamInfoItemsCollector(serviceId)
        try {
            val response = JsonParser.`object`().from(downloader.get((service.streamLHFactory as BSStreamLinkHandlerFactory).getRelatedStreamsUrl(id)).responseBody())
            response?.let {
                it.getArray("data")?.forEach { item ->
                    (item as? JsonObject)?.apply {
                        collector.commit(BSExtractorHelper.getStreamInfoItemExtractor(this))
                    }
                }
            }
        } catch (e: Exception) {
        }
        return collector
    }

    override fun getHost(): String {
        return ""
    }

    override fun getCategory(): String {
        return ""
    }

    override fun getLicence(): String {
        return ""
    }

    override fun getName(): String {
        return basicInfoExtractor.name
    }

    override fun getTextualUploadDate(): String? {
        return basicInfoExtractor.textualUploadDate
    }

    override fun getThumbnailUrl(): String {
        return data.getString("image_url")?.replace("{recipe}", "1280x720") ?: ""
    }

    override fun getViewCount(): Long {
        return basicInfoExtractor.viewCount
    }

    override fun getLikeCount(): Long {
        return -1
    }

    override fun getDislikeCount(): Long {
        return -1
    }

    override fun getUploaderUrl(): String {
        return basicInfoExtractor.uploaderUrl
    }

    override fun getUploaderAvatarUrl(): String {
        return data.getObject("network")
                ?.getString("logo_url")
                ?.replace("{type}", "colour")
                ?.replace("{size}", "default")
                ?.replace("{format}", "svg") ?: ""
    }

    override fun getUploaderName(): String {
        return basicInfoExtractor.uploaderName
    }

    override fun getSubChannelUrl(): String {
        val id = data.getObject("container")?.getString("id")
        return id?.let { BSChannelLinkHandlerFactory.fromId(id).url } ?: ""
    }

    override fun getSubChannelAvatarUrl(): String {
        return data.getString("image_url")?.replace("{recipe}", "320x320") ?: ""
    }

    override fun getSubChannelName(): String {
        return data.getObject("container")?.getString("title") ?: ""
    }

    override fun getHlsUrl(): String {
        return ""
    }

    override fun getAudioStreams(): List<AudioStream> {
        assertPageFetched()
        return audioStreams
    }

    override fun getVideoStreams(): MutableList<VideoStream> {
        return Collections.emptyList()
    }

    override fun getSubtitlesDefault(): MutableList<SubtitlesStream> {
        return Collections.emptyList()
    }

    override fun getNextStream(): StreamInfoItem? {
        return null
    }

    override fun getErrorMessage(): String {
        return ""
    }

    override fun getPrivacy(): String {
        return ""
    }

    override fun getLanguageInfo(): Locale? {
        return null
    }

    override fun getTags(): MutableList<String> {
        return Collections.emptyList()
    }

    override fun getSupportInfo(): String {
        return ""
    }

    override fun getOriginalUrl(): String {
        return "${service.baseUrl}/play/$id"
    }

}