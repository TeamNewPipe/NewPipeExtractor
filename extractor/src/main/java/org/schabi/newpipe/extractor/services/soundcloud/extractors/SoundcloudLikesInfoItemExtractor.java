package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;

public class SoundcloudLikesInfoItemExtractor extends SoundcloudStreamInfoItemExtractor {

    public SoundcloudLikesInfoItemExtractor(final JsonObject itemObject) {
        super(itemObject.getObject("track"));
    }

}
