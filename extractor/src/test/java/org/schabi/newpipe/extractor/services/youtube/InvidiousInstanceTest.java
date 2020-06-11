package org.schabi.newpipe.extractor.services.youtube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;

import static org.junit.Assert.*;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousInstanceTest.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousInstanceTest {

    @BeforeClass
    public static void setUp() {
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void testInvidio_usIsValid() {
        InvidiousInstance invidio_us = new InvidiousInstance("https://invidio.us");
        assertTrue(invidio_us.isValid());
    }

    @Test
    public void testInvidio_usGetName() throws InvalidInstanceException {
        InvidiousInstance invidious = new InvidiousInstance("https://invidio.us");
        invidious.fetchInstanceMetaData();
        assertEquals("invidious", invidious.getName());
    }

    @Test
    public void testYoutube_comIsValid() {
        InvidiousInstance youtube = new InvidiousInstance("https://youtube.com");
        assertFalse(youtube.isValid());
    }
}
