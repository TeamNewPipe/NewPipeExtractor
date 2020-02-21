package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;
import java.util.Vector;

/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * StreamInfoItemsCollector.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class StreamInfoItemsCollector extends InfoItemsCollector<StreamInfoItem, StreamInfoItemExtractor> {

    public StreamInfoItemsCollector(int serviceId) {
        super(serviceId);
    }

    @Override
    public StreamInfoItem extract(StreamInfoItemExtractor extractor) throws ParsingException {
        if (extractor.isAd()) {
            throw new FoundAdException("Found ad");
        }

        // important information
        int serviceId = getServiceId();
        String url = extractor.getUrl();
        String name = extractor.getName();
        StreamType streamType = extractor.getStreamType();

        StreamInfoItem resultItem = new StreamInfoItem(serviceId, url, name, streamType);


        // optional information
        try {
            resultItem.setDuration(extractor.getDuration());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderName(extractor.getUploaderName());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setTextualUploadDate(extractor.getTextualUploadDate());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploadDate(extractor.getUploadDate());
        } catch (ParsingException e) {
            addError(e);
        }
        try {
            resultItem.setViewCount(extractor.getViewCount());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderUrl(extractor.getUploaderUrl());
        } catch (Exception e) {
            addError(e);
        }
        return resultItem;
    }

    @Override
    public void commit(StreamInfoItemExtractor extractor) {
        try {
            addItem(extract(extractor));
        } catch (FoundAdException ae) {
            //System.out.println("AD_WARNING: " + ae.getMessage());
        } catch (Exception e) {
            addError(e);
        }
    }

    public List<StreamInfoItem> getStreamInfoItemList() {
        List<StreamInfoItem> siiList = new Vector<>();
        for (InfoItem ii : super.getItems()) {
            if (ii instanceof StreamInfoItem) {
                siiList.add((StreamInfoItem) ii);
            }
        }
        return siiList;
    }
}
