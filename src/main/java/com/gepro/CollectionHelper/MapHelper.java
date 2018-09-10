package com.gepro.CollectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapHelper {
    public static <K, V> List<K> getKeys(Map<K, V> map, V value){
        if(map == null) return null;

        List<K> list = new ArrayList<>();
        for(Map.Entry<K, V> entry : map.entrySet())
            if(entry.getValue() == value) list.add(entry.getKey());

        return list;
    }
}
