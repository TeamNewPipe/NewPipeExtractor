package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService;
import org.schabi.newpipe.extractor.services.peertube.PeertubeService;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * ServiceList.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * A list of supported services.
 */
public final class ServiceList {
    private ServiceList() {
        //no instance
    }

    public static final YoutubeService YouTube;
    public static final SoundcloudService SoundCloud;
    public static final MediaCCCService MediaCCC;
    public static final PeertubeService PeerTube;

    /**
     * When creating a new service, put this service in the end of this list,
     * and give it the next free id.
     */
    private static List<StreamingService> services = new ArrayList<>(
            Arrays.asList(
                    YouTube = new YoutubeService(0),
                    SoundCloud = new SoundcloudService(1),
                    MediaCCC = new MediaCCCService(2),
                    PeerTube = new PeertubeService(3)
            ));

    public static final int builtinServices = 4;
    private static int nextService = 4;

    /**
     * Get all the supported services.
     *
     * @return an unmodifiable list of all the supported services
     */
    public static List<StreamingService> all() {
        return Collections.unmodifiableList(services);
    }

    public static void addService(final Class<StreamingService> service) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        services.add(service.getConstructor(new Class[]{int.class}).newInstance(nextService));
        nextService++;
    }

    public static void replaceService(final Class<StreamingService> service, final int serviceId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        services.set(serviceId, service.getConstructor(new Class[]{int.class}).newInstance(serviceId));
        nextService++;
    }
}
