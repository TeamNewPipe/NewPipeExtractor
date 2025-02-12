package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;

import java.io.IOException;

import javax.annotation.Nonnull;

class YoutubeSignaturesTest {
    private static final String RESOURCE_PATH =
            DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/signatures";

    @BeforeEach
    void setUp() {
        YoutubeTestsUtils.ensureStateless();
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH));
    }

    @ValueSource(strings = {
            "QzUGs1qRTEI",
            ""
    })
    @ParameterizedTest
    void testSignatureTimestampExtraction(@Nonnull final String videoId) throws Exception {
        final Integer signatureTimestamp =
                YoutubeJavaScriptPlayerManager.getSignatureTimestamp(videoId);
        assertTrue(signatureTimestamp > 0, "signatureTimestamp is <= 0");
    }

    /*
    The first column of the CSV entries is a video ID
    The second one of these entries are not real signatures, but as the deobfuscation function
    manipulates strings, we can use random characters combined as strings to test the extraction
    and the execution of the function
     */
    @CsvSource(value = {
            "QzUGs1qRTEI,5QjJrWzVcOutYYNyxkDJVkzQDZQxNbbxGi4hRoh2h4PomQMQq9vo2WPHVpHgxRn7qT3WyhRiJa1k1t1DL3lynZtupHmG3wW4qh59faKjtY4UVu",
            ",7vIK4hG6NbcIEQP4ZIRjonOzuPHh7wTrEgBdEMYyfE4F5Pq0FiGdv04kptb587c8aToH345ETJ8dMbXnpOmjanP3nzgJ0iNg8oHIm8oeQODPSP"
    })
    @ParameterizedTest
    void testSignatureDeobfuscation(@Nonnull final String videoId,
                                    @Nonnull final String sampleString) throws Exception {
        // As the signature deobfuscation changes frequently with player versions, we can only test
        // that we get a different string than the original one
        assertNotEquals(sampleString,
                YoutubeJavaScriptPlayerManager.deobfuscateSignature(videoId, sampleString));
    }
}
