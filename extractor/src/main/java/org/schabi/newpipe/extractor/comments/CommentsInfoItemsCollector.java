package org.schabi.newpipe.extractor.comments;

import java.util.List;
import java.util.Vector;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * CommentsInfoItemsCollector.java is part of NewPipe.
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

public class CommentsInfoItemsCollector extends InfoItemsCollector<CommentsInfoItem, CommentsInfoItemExtractor> {

	public CommentsInfoItemsCollector(int serviceId) {
		super(serviceId);
	}

	@Override
	public CommentsInfoItem extract(CommentsInfoItemExtractor extractor) throws ParsingException {

		// important information
		int serviceId = getServiceId();
		String url = extractor.getUrl();
		String name = extractor.getName();

		CommentsInfoItem resultItem = new CommentsInfoItem(serviceId, url, name);

		// optional information
		try {
			resultItem.setCommentId(extractor.getCommentId());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setCommentText(extractor.getCommentText());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setAuthorName(extractor.getAuthorName());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setAuthorThumbnail(extractor.getAuthorThumbnail());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setAuthorEndpoint(extractor.getAuthorEndpoint());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setPublishedTime(extractor.getPublishedTime());
		} catch (Exception e) {
			addError(e);
		}
		try {
			resultItem.setLikeCount(extractor.getLikeCount());
		} catch (Exception e) {
			addError(e);
		}
		return resultItem;
	}

	@Override
	public void commit(CommentsInfoItemExtractor extractor) {
		try {
			addItem(extract(extractor));
		} catch (Exception e) {
			addError(e);
		}
	}

	public List<CommentsInfoItem> getCommentsInfoItemList() {
		List<CommentsInfoItem> siiList = new Vector<>();
		for (InfoItem ii : super.getItems()) {
			if (ii instanceof CommentsInfoItem) {
				siiList.add((CommentsInfoItem) ii);
			}
		}
		return siiList;
	}
}
