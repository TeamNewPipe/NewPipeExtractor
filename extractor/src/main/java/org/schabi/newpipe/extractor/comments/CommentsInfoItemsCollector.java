package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;
import java.util.Vector;

public class CommentsInfoItemsCollector
        extends InfoItemsCollector<CommentsInfoItem, CommentsInfoItemExtractor> {
    public CommentsInfoItemsCollector(final int serviceId) {
        super(serviceId);
    }

    @Override
    public CommentsInfoItem extract(final CommentsInfoItemExtractor extractor)
            throws ParsingException {
        // important information
        final int serviceId = getServiceId();
        final String url = extractor.getUrl();
        final String name = extractor.getName();

        final CommentsInfoItem resultItem = new CommentsInfoItem(serviceId, url, name);

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
            resultItem.setUploaderName(extractor.getUploaderName());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderAvatarUrl(extractor.getUploaderAvatarUrl());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setUploaderUrl(extractor.getUploaderUrl());
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
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setLikeCount(extractor.getLikeCount());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (Exception e) {
            addError(e);
        }

        return resultItem;
    }

    @Override
    public void commit(final CommentsInfoItemExtractor extractor) {
        try {
            addItem(extract(extractor));
        } catch (Exception e) {
            addError(e);
        }
    }

    public List<CommentsInfoItem> getCommentsInfoItemList() {
        final List<CommentsInfoItem> siiList = new Vector<>();
        for (final InfoItem ii : super.getItems()) {
            if (ii instanceof CommentsInfoItem) {
                siiList.add((CommentsInfoItem) ii);
            }
        }
        return siiList;
    }
}
