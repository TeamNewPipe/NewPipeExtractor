package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.services.youtube.YoutubeService;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * A list of supported services.
 */
public final class ServiceList {
    private ServiceList() {
        //no instance
    }

    public static final YoutubeService YouTube = new YoutubeService(0);

    private static final List<StreamingService> SERVICES = unmodifiableList(
            asList((StreamingService) YouTube));

    /**
     * Get all the supported services.
     *
     * @return a unmodifiable list of all the supported services
     */
    public static List<StreamingService> all() {
        return SERVICES;
    }
}
