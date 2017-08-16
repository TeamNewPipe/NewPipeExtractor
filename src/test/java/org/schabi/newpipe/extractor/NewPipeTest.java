package org.schabi.newpipe.extractor;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.NewPipe.getServiceByUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class NewPipeTest {
    @Test
    public void getAllServicesTest() throws Exception {
        assertEquals(NewPipe.getServices().length, ServiceList.values().length);
    }

    @Test
    public void testAllServicesHaveDifferentId() throws Exception {
        HashSet<Integer> servicesId = new HashSet<>();
        for (StreamingService streamingService : NewPipe.getServices()) {
            String errorMsg = "There are services with the same id = " + streamingService.getServiceId() + " (current service > " + streamingService.getServiceInfo().name + ")";

            assertTrue(errorMsg, servicesId.add(streamingService.getServiceId()));
        }
    }

    @Test
    public void getServiceWithId() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getId()), YouTube.getService());
        assertEquals(NewPipe.getService(SoundCloud.getId()), SoundCloud.getService());

        assertNotEquals(NewPipe.getService(SoundCloud.getId()), YouTube.getService());
    }

    @Test
    public void getServiceWithName() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getServiceInfo().name), YouTube.getService());
        assertEquals(NewPipe.getService(SoundCloud.getServiceInfo().name), SoundCloud.getService());

        assertNotEquals(NewPipe.getService(YouTube.getServiceInfo().name), SoundCloud.getService());
    }

    @Test
    public void getServiceWithUrl() throws Exception {
        assertEquals(getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"), YouTube.getService());
        assertEquals(getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"), YouTube.getService());
        assertEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), YouTube.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/shupemoosic/pegboard-nerds-try-this"), SoundCloud.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/deluxe314/sets/pegboard-nerds"), SoundCloud.getService());
        assertEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), SoundCloud.getService());

        assertNotEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), YouTube.getService());
        assertNotEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), SoundCloud.getService());
    }

    @Test
    public void getIdWithServiceName() throws Exception {
        assertEquals(NewPipe.getIdOfService(YouTube.getServiceInfo().name), YouTube.getId());
        assertEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().name), SoundCloud.getId());

        assertNotEquals(NewPipe.getIdOfService(SoundCloud.getServiceInfo().name), YouTube.getId());
    }

    @Test
    public void getServiceNameWithId() throws Exception {
        assertEquals(NewPipe.getNameOfService(YouTube.getId()), YouTube.getServiceInfo().name);
        assertEquals(NewPipe.getNameOfService(SoundCloud.getId()), SoundCloud.getServiceInfo().name);

        assertNotEquals(NewPipe.getNameOfService(YouTube.getId()), SoundCloud.getServiceInfo().name);
    }
}
