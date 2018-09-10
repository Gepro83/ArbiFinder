package com.gepro.CollectionHelper;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapHelperTest {

    @Test
    void getKeys() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 2);
        map.put("d", 3);

        List<String> list = MapHelper.getKeys(map, 1);
        assertTrue(list.contains("a"));

        list = MapHelper.getKeys(map, 2);
        assertTrue(list.containsAll(Arrays.asList("b", "c")));

        list = MapHelper.getKeys(map, 0);
        assertNotNull(list);
        assertEquals(0, list.size());

        list = MapHelper.getKeys(map, null);
        assertNotNull(list);
        assertEquals(0, list.size());

        assertNull(MapHelper.getKeys(null, 1));

        map.put("e", null);
        map.put("f", null);
        list = MapHelper.getKeys(map, null);
        assertTrue(list.containsAll(Arrays.asList("e", "f")));
    }
}