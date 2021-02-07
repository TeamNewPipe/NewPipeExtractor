package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;

import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

public class Description implements Serializable {

    public static final int HTML = 1;
    public static final int MARKDOWN = 2;
    public static final int PLAIN_TEXT = 3;
    public static final Description emptyDescription = new Description(EMPTY_STRING, PLAIN_TEXT);

    private String content;
    private int type;

    public Description(String content, int type) {
        this.type = type;
        if (content == null) {
            this.content = "";
        } else {
            this.content = content;
        }
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}
