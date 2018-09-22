package org.schabi.newpipe.extractor.comments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.ListInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.ExtractorHelper;

public class CommentsInfo extends ListInfo<CommentsInfoItem>{

	private CommentsInfo(int serviceId, ListLinkHandler listUrlIdHandler, String name) {
		super(serviceId, listUrlIdHandler, name);
		// TODO Auto-generated constructor stub
	}
	
	public static CommentsInfo getInfo(String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

	private static CommentsInfo getInfo(StreamingService serviceByUrl, String url) throws ExtractionException, IOException {
	    return getInfo(serviceByUrl.getCommentsExtractor(url));
	}

    private static CommentsInfo getInfo(CommentsExtractor commentsExtractor) throws IOException, ExtractionException {
        //for services which do not have a comments extractor
        if(null == commentsExtractor) {
            return null;
        }
        
        commentsExtractor.fetchPage();
        String name = commentsExtractor.getName();
        int serviceId = commentsExtractor.getServiceId();
        ListLinkHandler listUrlIdHandler = commentsExtractor.getUIHandler();
        CommentsInfo commentsInfo = new CommentsInfo(serviceId, listUrlIdHandler, name);
        commentsInfo.setCommentsExtractor(commentsExtractor);
            InfoItemsPage<CommentsInfoItem> initialCommentsPage = ExtractorHelper.getItemsPageOrLogError(commentsInfo,
                    commentsExtractor);
        commentsInfo.setComments(new ArrayList<>());
        commentsInfo.getComments().addAll(initialCommentsPage.getItems());
        //tmp
        commentsInfo.setRelatedItems(initialCommentsPage.getItems());
        commentsInfo.setNextPageUrl(initialCommentsPage.getNextPageUrl());
        
        commentsInfo.setHasMoreComments(initialCommentsPage.hasNextPage());
        commentsInfo.setNextCommentsPageUrl(initialCommentsPage.getNextPageUrl());
        return commentsInfo;
    }
    
    public static void loadMoreComments(CommentsInfo commentsInfo) {
        if (commentsInfo.hasMoreComments()) {
            if(null == commentsInfo.getCommentsExtractor()) {
                try {
                    commentsInfo.setCommentsExtractor(NewPipe.getService(commentsInfo.getServiceId()).getCommentsExtractor(commentsInfo.getUrl()));
                    commentsInfo.getCommentsExtractor().fetchPage();
                } catch (ExtractionException | IOException e) {
                    commentsInfo.addError(e);
                    return;
                }
            }
            try {
                InfoItemsPage<CommentsInfoItem> commentsPage = commentsInfo.getCommentsExtractor()
                        .getPage(commentsInfo.getNextCommentsPageUrl());
                commentsInfo.getComments().addAll(commentsPage.getItems());
                commentsInfo.setHasMoreComments(commentsPage.hasNextPage());
                commentsInfo.setNextCommentsPageUrl(commentsPage.getNextPageUrl());
            } catch (IOException | ExtractionException e) {
                commentsInfo.addError(e);
            }
        }
    }
    
    private transient CommentsExtractor commentsExtractor;
    private List<CommentsInfoItem> comments;
    private boolean hasMoreComments;
    private String nextCommentsPageUrl;
    
    public List<CommentsInfoItem> getComments() {
        return comments;
    }

    public void setComments(List<CommentsInfoItem> comments) {
        this.comments = comments;
    }

    public boolean hasMoreComments() {
        return hasMoreComments;
    }

    public void setHasMoreComments(boolean hasMoreComments) {
        this.hasMoreComments = hasMoreComments;
    }

    public CommentsExtractor getCommentsExtractor() {
        return commentsExtractor;
    }

    public void setCommentsExtractor(CommentsExtractor commentsExtractor) {
        this.commentsExtractor = commentsExtractor;
    }

    public String getNextCommentsPageUrl() {
        return nextCommentsPageUrl;
    }

    public void setNextCommentsPageUrl(String nextCommentsPageUrl) {
        this.nextCommentsPageUrl = nextCommentsPageUrl;
    }

}
