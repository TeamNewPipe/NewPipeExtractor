package org.schabi.newpipe.extractor.services.youtube;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonObject;

import org.jsoup.nodes.Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class YoutubeDescriptionHelper {

    private YoutubeDescriptionHelper() {
    }

    public static final String LINK_CLOSE = "</a>";
    public static final String STRIKETHROUGH_OPEN = "<s>";
    public static final String STRIKETHROUGH_CLOSE = "</s>";
    public static final String BOLD_OPEN = "<b>";
    public static final String BOLD_CLOSE = "</b>";
    public static final String ITALIC_OPEN = "<i>";
    public static final String ITALIC_CLOSE = "</i>";

    /**
     * Can be a command run, or a style run.
     */
    static final class Run {
        @Nonnull final String open;
        @Nonnull final String close;
        final int pos;
        final boolean isClose;

        Run(
                @Nonnull final String open,
                @Nonnull final String close,
                final int pos,
                final boolean isClose
        ) {
            this.open = open;
            this.close = close;
            this.pos = pos;
            this.isClose = isClose;
        }

        public boolean sameOpen(@Nonnull final Run other) {
            return open.equals(other.open);
        }
    }

    /**
     * Parse a video description in the new "attributed" format, which contains the entire visible
     * plaintext ({@code content}) and an array of {@code commandRuns}.
     *
     * <p>
     * The {@code commandRuns} include the links and their position in the text.
     * </p>
     *
     * @param attributedDescription the JSON object of the attributed description
     * @return the parsed description, in HTML format, as a string
     */
    public static String attributedDescriptionToHtml(
            @Nullable final JsonObject attributedDescription
    ) {
        if (isNullOrEmpty(attributedDescription)) {
            return null;
        }

        final String content = attributedDescription.getString("content");
        if (content == null) {
            return null;
        }

        // all run pairs must always of length at least 1, or they should be discarded,
        // otherwise various assumptions made in runsToHtml may fail
        final List<Run> openers = new ArrayList<>();
        final List<Run> closers = new ArrayList<>();
        addAllCommandRuns(attributedDescription, openers, closers);
        addAllStyleRuns(attributedDescription, openers, closers);

        // Note that sorting this way might put closers with the same close position in the wrong
        // order with respect to their openers, causing unnecessary closes and reopens. E.g.
        // <b>b<i>b&i</i></b> is instead generated as <b>b<i>b&i</b></i><b></b> if the </b> is
        // encountered before the </i>. Solving this wouldn't be difficult, thanks to stable sort,
        // but would require additional sorting steps which would just make this slower for the
        // general case where it's unlikely there are coincident closes.
        Collections.sort(openers, Comparator.comparingInt(run -> run.pos));
        Collections.sort(closers, Comparator.comparingInt(run -> run.pos));

        return runsToHtml(openers, closers, content);
    }

    /**
     * Applies the formatting specified by the intervals stored in {@code openers} and {@code
     * closers} to {@code content} in order to obtain valid HTML even when intervals overlap. For
     * example &lt;b&gt;b&lt;i&gt;b&i&lt;/b&gt;i&lt;/i&gt; would not be valid HTML, so this function
     * instead generates &lt;b&gt;b&lt;i&gt;b&i&lt;/i&gt;&lt;/b&gt;&lt;i&gt;i&lt;/i&gt;.
     * <p>
     * Every opener in {@code openers} must have a corresponding closer in {@code closers}. Every
     * corresponding (opener, closer) pair must have a length of at least one (i.e. empty intervals
     * are not allowed).
     * </p>
     *
     * @param openers contains all of the places where a run begins, must have the same size of
     *                closers, must be ordered by {@link Run#pos}
     * @param closers contains all of the places where a run ends, must have the same size of
     *                openers, must be ordered by {@link Run#pos}
     * @param content the content to apply formatting to
     * @return the formatted content in HTML
     */
    static String runsToHtml(
            @Nonnull final List<Run> openers,
            @Nonnull final List<Run> closers,
            @Nonnull final String content
    ) {
        final Stack<Run> openRuns = new Stack<>();
        final Stack<Run> tempStack = new Stack<>();
        final StringBuilder textBuilder = new StringBuilder();
        int currentTextPos = 0;
        int openersIndex = 0;
        int closersIndex = 0;

        // openers and closers have the same length, but we will surely finish openers earlier than
        // closers, since every opened interval needs to be closed at some point and there can't be
        // empty intervals, hence check only closersIndex < closers.size()
        while (closersIndex < closers.size()) {
            final int minPos = openersIndex < openers.size()
                    ? Math.min(closers.get(closersIndex).pos, openers.get(openersIndex).pos)
                    : closers.get(closersIndex).pos;

            // append piece of text until current index
            textBuilder.append(content, currentTextPos, minPos);
            currentTextPos = minPos;

            if (closers.get(closersIndex).pos == minPos) {
                // even in case of position tie, first process closers
                final Run closer = closers.get(closersIndex);
                ++closersIndex;

                // because of the assumptions, this while wouldn't need the !openRuns.empty()
                // condition, because no run will close before being opened, but let's be sure
                while (!openRuns.empty()) {
                    final Run popped = openRuns.pop();
                    textBuilder.append(popped.close);
                    if (popped.sameOpen(closer)) {
                        break;
                    }
                    // we keep popping from openRuns, closing all of the runs we find,
                    // until we find the run that we really need to close ...
                    tempStack.push(popped);
                }
                while (!tempStack.empty()) {
                    // ... and then we reopen all of the runs that we didn't need to close
                    // e.g. in <b>b<i>b&i</b>i</i>, when </b> is encountered, </i></b><i> is printed
                    // instead, to make sure the HTML is valid, obtaining <b>b<i>b&i</i></b><i>i</i>
                    final Run popped = tempStack.pop();
                    textBuilder.append(popped.open);
                    openRuns.push(popped);
                }

            } else {
                // this will never be reached if openersIndex >= openers.size() because of the
                // way minPos is calculated
                textBuilder.append(openers.get(openersIndex).open);
                openRuns.push(openers.get(openersIndex));
                ++openersIndex;
            }
        }

        // append last piece of text
        textBuilder.append(content, currentTextPos, content.length());

        return textBuilder.toString()
                .replace("\n", "<br>")
                .replace("  ", " &nbsp;")
                // special link chips (e.g. for YT videos, YT channels or social media accounts):
                // u00a0 u00a0 [/•] u00a0 <link content> u00a0 u00a0
                .replace("\">\u00a0\u00a0/\u00a0", "\">")
                .replace("\">\u00a0\u00a0•\u00a0", "\">")
                .replace("\u00a0\u00a0</a>", "</a>");
    }

    private static void addAllCommandRuns(
            @Nonnull final JsonObject attributedDescription,
            @Nonnull final List<Run> openers,
            @Nonnull final List<Run> closers
    ) {
        attributedDescription.getArray("commandRuns")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEach(run -> {
                    final JsonObject navigationEndpoint = run.getObject("onTap")
                            .getObject("innertubeCommand");

                    final int startIndex = run.getInt("startIndex", -1);
                    final int length = run.getInt("length", 0);
                    if (startIndex < 0 || length < 1 || navigationEndpoint == null) {
                        return;
                    }

                    final String url = getUrlFromNavigationEndpoint(navigationEndpoint);
                    if (url == null) {
                        return;
                    }

                    final String open = "<a href=\"" + Entities.escape(url) + "\">";

                    openers.add(new Run(open, LINK_CLOSE, startIndex, false));
                    closers.add(new Run(open, LINK_CLOSE, startIndex + length, true));
                });
    }

    private static void addAllStyleRuns(
            @Nonnull final JsonObject attributedDescription,
            @Nonnull final List<Run> openers,
            @Nonnull final List<Run> closers
    ) {
        attributedDescription.getArray("styleRuns")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEach(run -> {
                    final int start = run.getInt("startIndex", -1);
                    final int length = run.getInt("length", 0);
                    if (start < 0 || length < 1) {
                        return;
                    }
                    final int end = start + length;

                    if (run.has("strikethrough")) {
                        openers.add(new Run(STRIKETHROUGH_OPEN, STRIKETHROUGH_CLOSE, start, false));
                        closers.add(new Run(STRIKETHROUGH_OPEN, STRIKETHROUGH_CLOSE, end, true));
                    }

                    if (run.getBoolean("italic", false)) {
                        openers.add(new Run(ITALIC_OPEN, ITALIC_CLOSE, start, false));
                        closers.add(new Run(ITALIC_OPEN, ITALIC_CLOSE, end, true));
                    }

                    if (run.has("weightLabel")
                            && !"FONT_WEIGHT_NORMAL".equals(run.getString("weightLabel"))) {
                        openers.add(new Run(BOLD_OPEN, BOLD_CLOSE, start, false));
                        closers.add(new Run(BOLD_OPEN, BOLD_CLOSE, end, true));
                    }
                });
    }
}
