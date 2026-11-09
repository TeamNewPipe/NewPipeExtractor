package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import java.util.Random;

public interface InitBandcampTest extends InitNewPipeTest {

    @BeforeAll
    @Override
    default void setUp() throws Exception {
        InitNewPipeTest.super.setUp();
        BandcampExtractorHelper.setNumberGenerator(new Random(1));
    }

}
