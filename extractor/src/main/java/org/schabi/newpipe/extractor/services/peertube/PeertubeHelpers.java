// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.peertube;

import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.peertube.search.filter.PeertubeFilters;

import java.util.List;
import java.util.Optional;

public final class PeertubeHelpers {
    private PeertubeHelpers() {
    }

    public static Optional<FilterItem> getSepiaFilter(final List<FilterItem> selectedFilters) {
        return selectedFilters.stream()
                .filter(PeertubeFilters.PeertubeSepiaFilterItem.class::isInstance)
                .findFirst();
    }
}
