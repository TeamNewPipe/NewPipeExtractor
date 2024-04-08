package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeDescriptionHelper.runsToHtml;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.youtube.YoutubeDescriptionHelper.Run;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YoutubeDescriptionHelperTest {

    private static void assertRunsToHtml(final String expectedHtml,
                                         final List<Run> openers,
                                         final List<Run> closers,
                                         final String content) {
        assertEquals(
                expectedHtml,
                runsToHtml(
                        openers.stream()
                                .sorted(Comparator.comparingInt(run -> run.pos))
                                .collect(Collectors.toList()),
                        closers.stream()
                                .sorted(Comparator.comparingInt(run -> run.pos))
                                .collect(Collectors.toList()),
                        content
                )
        );
    }

    @Test
    public void testNoRuns() {
        assertRunsToHtml(
                "abc *a* _c_ &lt;br&gt; <br> &lt;a href=\"#\"&gt;test&lt;/a&gt; &nbsp;&amp;amp;",
                List.of(),
                List.of(),
                "abc *a* _c_ <br>\u00a0\n\u00a0<a href=\"#\">test</a>  &amp;"
        );
    }

    @Test
    public void testNormalRuns() {
        assertRunsToHtml(
                "<A>hel<B>lo </B>nic</A>e <C>test</C>",
                List.of(new Run("<A>", "</A>", 0), new Run("<B>", "</B>", 3),
                        new Run("<C>", "</C>", 11)),
                List.of(new Run("<A>", "</A>", 9), new Run("<B>", "</B>", 6),
                        new Run("<C>", "</C>", 15)),
                "hello nice test"
        );
    }

    @Test
    public void testOverlappingRuns() {
        assertRunsToHtml(
                "01<A>23<B>45</B></A><B>67</B>89",
                List.of(new Run("<A>", "</A>", 2), new Run("<B>", "</B>", 4)),
                List.of(new Run("<A>", "</A>", 6), new Run("<B>", "</B>", 8)),
                "0123456789"
        );
    }

    @Test
    public void testTransformingRuns() {
        final Function<String, String> tA = content -> "whatever";
        final Function<String, String> tD
                = content -> Integer.parseInt(content) % 2 == 0 ? "even" : "odd";

        assertRunsToHtml(
                "0<A>whatever</A><C>4</C>5<D>odd</D>89",
                List.of(new Run("<A>", "</A>", 1, tA), new Run("<B>", "</B>", 2),
                        new Run("<C>", "</C>", 3), new Run("<D>", "</D>", 6, tD)),
                List.of(new Run("<A>", "</A>", 4, tA), new Run("<B>", "</B>", 3),
                        new Run("<C>", "</C>", 5), new Run("<D>", "</D>", 8, tD)),
                "0123456789"
        );
    }
}
