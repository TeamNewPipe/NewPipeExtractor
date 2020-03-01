package org.schabi.newpipe.extractor.utils;


import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.junit.Test;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class JsonUtilsTest {

    @Test
    public void testGetValueFlat() throws JsonParserException, ParsingException {
        JsonObject obj = JsonParser.object().from("{\"name\":\"John\",\"age\":30,\"cars\":{\"car1\":\"Ford\",\"car2\":\"BMW\",\"car3\":\"Fiat\"}}");
        assertTrue("John".equals(JsonUtils.getValue(obj, "name")));
    }

    @Test
    public void testGetValueNested() throws JsonParserException, ParsingException {
        JsonObject obj = JsonParser.object().from("{\"name\":\"John\",\"age\":30,\"cars\":{\"car1\":\"Ford\",\"car2\":\"BMW\",\"car3\":\"Fiat\"}}");
        assertTrue("BMW".equals(JsonUtils.getValue(obj, "cars.car2")));
    }

    @Test
    public void testGetArray() throws JsonParserException, ParsingException {
        JsonObject obj = JsonParser.object().from("{\"id\":\"0001\",\"type\":\"donut\",\"name\":\"Cake\",\"ppu\":0.55,\"batters\":{\"batter\":[{\"id\":\"1001\",\"type\":\"Regular\"},{\"id\":\"1002\",\"type\":\"Chocolate\"},{\"id\":\"1003\",\"type\":\"Blueberry\"},{\"id\":\"1004\",\"type\":\"Devil's Food\"}]},\"topping\":[{\"id\":\"5001\",\"type\":\"None\"},{\"id\":\"5002\",\"type\":\"Glazed\"},{\"id\":\"5005\",\"type\":\"Sugar\"},{\"id\":\"5007\",\"type\":\"Powdered Sugar\"},{\"id\":\"5006\",\"type\":\"Chocolate with Sprinkles\"},{\"id\":\"5003\",\"type\":\"Chocolate\"},{\"id\":\"5004\",\"type\":\"Maple\"}]}");
        JsonArray arr = (JsonArray) JsonUtils.getValue(obj, "batters.batter");
        assertTrue(!arr.isEmpty());
    }

    @Test
    public void testGetValues() throws JsonParserException, ParsingException {
        JsonObject obj = JsonParser.object().from("{\"id\":\"0001\",\"type\":\"donut\",\"name\":\"Cake\",\"ppu\":0.55,\"batters\":{\"batter\":[{\"id\":\"1001\",\"type\":\"Regular\"},{\"id\":\"1002\",\"type\":\"Chocolate\"},{\"id\":\"1003\",\"type\":\"Blueberry\"},{\"id\":\"1004\",\"type\":\"Devil's Food\"}]},\"topping\":[{\"id\":\"5001\",\"type\":\"None\"},{\"id\":\"5002\",\"type\":\"Glazed\"},{\"id\":\"5005\",\"type\":\"Sugar\"},{\"id\":\"5007\",\"type\":\"Powdered Sugar\"},{\"id\":\"5006\",\"type\":\"Chocolate with Sprinkles\"},{\"id\":\"5003\",\"type\":\"Chocolate\"},{\"id\":\"5004\",\"type\":\"Maple\"}]}");
        JsonArray arr = (JsonArray) JsonUtils.getValue(obj, "topping");
        List<Object> types = JsonUtils.getValues(arr, "type");
        assertTrue(types.contains("Chocolate with Sprinkles"));

    }

}
