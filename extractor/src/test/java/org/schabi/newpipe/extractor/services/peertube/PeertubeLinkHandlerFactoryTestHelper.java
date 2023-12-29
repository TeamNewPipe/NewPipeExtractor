package org.schabi.newpipe.extractor.services.peertube;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class PeertubeLinkHandlerFactoryTestHelper {

    public static void assertDoNotAcceptNonURLs(LinkHandlerFactory linkHandler)
            throws ParsingException {
        assertFalse(linkHandler.acceptUrl("orchestr/a/"));
        assertFalse(linkHandler.acceptUrl("/a/"));
        assertFalse(linkHandler.acceptUrl("something/c/"));
        assertFalse(linkHandler.acceptUrl("/c/"));
        assertFalse(linkHandler.acceptUrl("videos/"));
        assertFalse(linkHandler.acceptUrl("I-hate-videos/"));
        assertFalse(linkHandler.acceptUrl("/w/"));
        assertFalse(linkHandler.acceptUrl("ksmg/w/"));
        assertFalse(linkHandler.acceptUrl("a reandom search query"));
        assertFalse(linkHandler.acceptUrl("test 230 "));
        assertFalse(linkHandler.acceptUrl("986513"));
    }

    public static void assertDoNotAcceptNonURLs(ListLinkHandlerFactory linkHandler)
            throws ParsingException {
        assertFalse(linkHandler.acceptUrl("orchestr/a/"));
        assertFalse(linkHandler.acceptUrl("/a/"));
        assertFalse(linkHandler.acceptUrl("something/c/"));
        assertFalse(linkHandler.acceptUrl("/c/"));
        assertFalse(linkHandler.acceptUrl("videos/"));
        assertFalse(linkHandler.acceptUrl("I-hate-videos/"));
        assertFalse(linkHandler.acceptUrl("/w/"));
        assertFalse(linkHandler.acceptUrl("ksmg/w/"));
        assertFalse(linkHandler.acceptUrl("a reandom search query"));
        assertFalse(linkHandler.acceptUrl("test 230 "));
        assertFalse(linkHandler.acceptUrl("986513"));
    }
}
