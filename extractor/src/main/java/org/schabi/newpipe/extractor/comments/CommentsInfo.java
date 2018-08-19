package org.schabi.newpipe.extractor.comments;

import java.io.IOException;

import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

public class CommentsInfo extends ListInfo<CommentsInfoItem>{

	private CommentsInfo(int serviceId, ListLinkHandler listUrlIdHandler, String name) {
		super(serviceId, listUrlIdHandler, name);
		// TODO Auto-generated constructor stub
	}
	
	public static CommentsInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

	private static CommentsInfo getInfo(StreamingService serviceByUrl, String url) {
		// TODO Auto-generated method stub
		return null;
	}

}
