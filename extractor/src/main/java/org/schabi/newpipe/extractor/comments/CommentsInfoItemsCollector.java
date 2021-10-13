package org.schabi.newpipe.extractor.comments;

import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;

import java.util.ArrayList;
import java.util.List;

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
            resultItem.setTextualLikeCount(extractor.getTextualLikeCount());
        } catch (Exception e) {
            addError(e);
        }
        try {
            resultItem.setThumbnailUrl(extractor.getThumbnailUrl());
        } catch (Exception e) {
            addError(e);
        }

        try {
            resultItem.setHeartedByUploader(extractor.isHeartedByUploader());
        } catch (Exception e) {
            addError(e);
        }

        try {
            resultItem.setPinned(extractor.isPinned());
        } catch (Exception e) {
            addError(e);
        }

        try {
            resultItem.setStreamPosition(extractor.getStreamPosition());
        } catch (Exception e) {
            addError(e);
        }

        try {
            resultItem.setReplies(extractor.getReplies());
        } catch (Exception e) {
            addError(e);
        }

        try {
            if (resultItem.getReplies() != null && serviceId == YouTube.getServiceId()) {
                    final YoutubeCommentsExtractor youtubeCommentsExtractor =
                            (YoutubeCommentsExtractor) YouTube.getCommentsExtractor(
                                    resultItem.getReplies().getUrl());

                    resultItem.setRepliesInfoList(
                            youtubeCommentsExtractor.getPage(extractor.getReplies()).getItems());
            }
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
        return new ArrayList<>(super.getItems());
    }
}
