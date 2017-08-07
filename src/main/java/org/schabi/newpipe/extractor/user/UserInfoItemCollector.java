package org.schabi.newpipe.extractor.user;

import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * UserInfoItemCollector.java is part of NewPipe.
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

public class UserInfoItemCollector extends InfoItemCollector {
    public UserInfoItemCollector(int serviceId) {
        super(serviceId);
    }

    public UserInfoItem extract(UserInfoItemExtractor extractor) throws ParsingException {
        UserInfoItem resultItem = new UserInfoItem();
        // important information
        resultItem.name = extractor.getUserName();

        resultItem.service_id = getServiceId();
        resultItem.url = extractor.getWebPageUrl();

        // optional information
        try {
            resultItem.subscriber_count = extractor.getSubscriberCount();
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.stream_count = extractor.getStreamCount();
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.thumbnail_url = extractor.getThumbnailUrl();
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.description = extractor.getDescription();
        } catch (Exception e) {
            addError(e);
        }
        return resultItem;
    }

    public void commit(UserInfoItemExtractor extractor) throws ParsingException {
        try {
            addItem(extract(extractor));
        } catch (Exception e) {
            addError(e);
        }
    }
}
