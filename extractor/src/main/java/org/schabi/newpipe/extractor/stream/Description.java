package org.schabi.newpipe.extractor.stream;

import java.io.Serializable;
import java.util.Objects;

public class Description implements Serializable {

    public static final int HTML = 1;
    public static final int MARKDOWN = 2;
    public static final int PLAIN_TEXT = 3;
    public static final Description EMPTY_DESCRIPTION = new Description("", PLAIN_TEXT);

    private final String content;
    private final int type;

    public Description(final String content, final int type) {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Description that = (Description) o;
        return type == that.type && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, type);
    }
}
