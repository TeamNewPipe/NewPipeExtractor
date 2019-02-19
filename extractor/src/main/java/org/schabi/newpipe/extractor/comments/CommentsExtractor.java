package org.schabi.newpipe.extractor.comments;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.Localization;

public abstract class CommentsExtractor extends ListExtractor<CommentsInfoItem> {

	public CommentsExtractor(StreamingService service, ListLinkHandler uiHandler, Localization localization) {
		super(service, uiHandler, localization);
		// TODO Auto-generated constructor stub
	}

}
