package org.schabi.newpipe.extractor.stream;

import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class Description {
    private String content;
    private int type;

    public static final int HTML = 1;
    public static final int MARKDOWN = 2;
    public static final int PLAIN_TEXT = 3;
    public static final Description emptyDescription = new Description(PLAIN_TEXT, "");

    public Description(int serviceID, String content) {
        if (serviceID == PeerTube.getServiceId()) {
            this.type = MARKDOWN;
        } else if (serviceID == YouTube.getServiceId()) {
            this.type = HTML;
        } else {
            this.type = PLAIN_TEXT;
        }
        setContent(content);
    }

    private void setContent(String content) {
        if (content == null) {
            this.content = "";
        } else {
            this.content = content;
        }
    }

    public Description(String content, int type) {
        this.type = type;
        setContent(content);
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}
