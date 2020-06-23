package org.schabi.newpipe.extractor.services.bbc_sounds

import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DashMpdParser
import org.schabi.newpipe.extractor.stream.DeliveryFormat
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

internal object BSDashMpdParser : DashMpdParser() {

    override fun getStreams(manifestUrl: String): Result {
        val manifest = NewPipe.getDownloader().get(manifestUrl).responseBody()
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val dashDoc = builder.parse(ByteArrayInputStream(manifest.toByteArray()))
        val representationList = dashDoc.getElementsByTagName("Representation")

        val audioStreams: MutableList<AudioStream> = ArrayList()

        for (i in 0 until representationList.length) {
            val representation = representationList.item(i) as? Element
            representation?.let {

                val adaptationSet = representation.parentNode as Element
                val mimeType = adaptationSet.getAttribute("mimeType")
                val mediaFormat = MediaFormat.getFromMimeType(mimeType)
                val abr = it.getAttribute("bandwidth").toInt()
                val deliveryFormat = DeliveryFormat.manualDASH(manifestUrl, manualDashFromRepresentation(dashDoc, it))
                val stream = AudioStream(deliveryFormat, mediaFormat, abr)
                if (!AudioStream.containSimilarStream(stream, audioStreams)) {
                    audioStreams.add(stream)
                }
            }
        }

        return Result(emptyList(), emptyList(), audioStreams)
    }

}