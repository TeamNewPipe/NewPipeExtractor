/*
 * SPDX-FileCopyrightText: 2017-2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2026 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.services.bandcamp.BandcampService
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService
import org.schabi.newpipe.extractor.services.peertube.PeertubeService
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService
import org.schabi.newpipe.extractor.services.youtube.YoutubeService

/**
 * A list of supported services.
 */
public object ServiceList {

    @JvmField
    public val YouTube: YoutubeService = YoutubeService(0)

    @JvmField
    public val SoundCloud: SoundcloudService = SoundcloudService(1)

    @JvmField
    public val MediaCCC: MediaCCCService = MediaCCCService(2)

    @JvmField
    public val PeerTube: PeertubeService = PeertubeService(3)

    @JvmField
    public val Bandcamp: BandcampService = BandcampService(4)

    // When creating a new service, put that service in the end of this list,
    // and give it the next free id
    private val SERVICES = listOf(
        YouTube,
        SoundCloud,
        MediaCCC,
        PeerTube,
        Bandcamp
    )

    /**
     * Get all the supported services
     */
    @JvmStatic
    public fun all(): List<StreamingService> = SERVICES
}
