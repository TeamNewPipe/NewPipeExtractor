package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;

class YouTubeChannelHelperTest implements InitYoutubeTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "@TheDailyShow",
        "thedailyshow",
        "channel/UCwWhs_6x42TyRM4Wstoq8HA",
        "UCwWhs_6x42TyRM4Wstoq8HA"
    })
    void resolveSuccessfulTheDailyShow(final String idOrPath) {
        final String id = assertDoesNotThrow(
            () -> YoutubeChannelHelper.resolveChannelId(idOrPath));
        assertEquals("UCwWhs_6x42TyRM4Wstoq8HA", id);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "@Gronkh",
        "gronkh",
        "channel/UCYJ61XIK64sp6ZFFS8sctxw",
        "UCYJ61XIK64sp6ZFFS8sctxw"
    })
    void resolveSuccessfulGronkh(final String idOrPath) {
        final String id = assertDoesNotThrow(
                () -> YoutubeChannelHelper.resolveChannelId(idOrPath));
        assertEquals("UCYJ61XIK64sp6ZFFS8sctxw", id);
    }

    @Test
    void resolveFailNonExistingTag() {
        assertThrows(ExtractionException.class, () -> YoutubeChannelHelper.resolveChannelId(
            "@nonExistingHandleThatWillNeverExist15464"));
    }
}
