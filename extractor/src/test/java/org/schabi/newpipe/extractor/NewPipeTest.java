package org.schabi.newpipe.extractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.localization.Localization;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.NewPipe.getServiceByUrl;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class NewPipeTest {
    @Test
    public void getAllServicesTest() throws Exception {
        assertEquals(NewPipe.getServices().size(), ServiceList.all().size());
    }

    @Test
    public void testAllServicesHaveDifferentId() throws Exception {
        HashSet<Integer> servicesId = new HashSet<>();
        for (StreamingService streamingService : NewPipe.getServices()) {
            final String errorMsg =
                    "There are services with the same id = " + streamingService.getServiceId()
                            + " (current service > " + streamingService.getServiceInfo().getName() + ")";

            assertTrue(servicesId.add(streamingService.getServiceId()), errorMsg);
        }
    }

    @Test
    public void testGetCountryCodeAndGetLanguageCode() {
        try {
            HashSet<Integer> servicesId = new HashSet<>();
            for (StreamingService streamingService : NewPipe.getServices()) {
                Localization Localization1 = new Localization("W4JN-$nw<}7EGpwmm(EQ", "ndBdj-qEHp!#I]LDWP=,");
                String errorMsg = ((("There are services with the same id = " + streamingService.getServiceId()) + " (current service > ") + streamingService.getServiceInfo().getName()) + ")";
                streamingService.getTimeAgoParser(new Localization("W4JN-$nw<}7EGpwmm(EQ", "ndBdj-qEHp!#I]LDWP=,"));
            }
            Assertions.fail("testAllServicesHaveDifferentId_mg38 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assertions.assertEquals("Localization is not supported (\"Localization[W4JN-$nw<}7EGpwmm(EQ-ndBdj-qEHp!#I]LDWP=,]\")", expected.getMessage());
        }
    }

    @Test
    public void getServiceWithId() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getServiceId()), YouTube);
    }

    @Test
    public void getServiceWithName() throws Exception {
        assertEquals(NewPipe.getService(YouTube.getServiceInfo().getName()), YouTube);
    }

    @Test
    public void getServiceWithUrl() throws Exception {
        assertEquals(getServiceByUrl("https://www.youtube.com/watch?v=_r6CgaFNAGg"), YouTube);
        assertEquals(getServiceByUrl("https://www.youtube.com/channel/UCi2bIyFtz-JdI-ou8kaqsqg"), YouTube);
        assertEquals(getServiceByUrl("https://www.youtube.com/playlist?list=PLRqwX-V7Uu6ZiZxtDDRCi6uhfTH4FilpH"), YouTube);
        assertEquals(getServiceByUrl("https://www.google.it/url?sa=t&rct=j&q=&esrc=s&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DHu80uDzh8RY&source=video"), YouTube);

        assertEquals(getServiceByUrl("https://soundcloud.com/pegboardnerds"), SoundCloud);
        assertEquals(getServiceByUrl("https://www.google.com/url?sa=t&url=https%3A%2F%2Fsoundcloud.com%2Fciaoproduction&rct=j&q=&esrc=s&source=web&cd="), SoundCloud);
    }

    @Test
    public void getIdWithServiceName() throws Exception {
        assertEquals(NewPipe.getIdOfService(YouTube.getServiceInfo().getName()), YouTube.getServiceId());
    }

    @Test
    public void getServiceNameWithId() throws Exception {
        assertEquals(NewPipe.getNameOfService(YouTube.getServiceId()), YouTube.getServiceInfo().getName());
    }
}
