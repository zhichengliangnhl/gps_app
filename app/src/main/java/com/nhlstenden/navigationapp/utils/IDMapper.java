package com.nhlstenden.navigationapp.utils;

import com.nhlstenden.navigationapp.R;

import java.util.HashMap;
import java.util.Map;

public class IDMapper
{
    public static Map<Integer, String> getThemeMap()
    {
        Map<Integer, String> map = new HashMap<>();
        map.put(R.id.theme1, "classic");
        map.put(R.id.theme2, "macha");
        map.put(R.id.theme3, "savana");
        map.put(R.id.theme4, "aqua");
        map.put(R.id.theme5, "lavander");
        map.put(R.id.theme6, "sunset");
        map.put(R.id.theme7, "navy");
        map.put(R.id.theme8, "fakeHolland");
        map.put(R.id.theme9, "macchiato");
        map.put(R.id.theme10, "cookieCream");
        return map;
    }

    public static Map<Integer, String> getArrowMap()
    {
        Map<Integer, String> map = new HashMap<>();
        map.put(R.id.arrow1, "orange");
        map.put(R.id.arrow2, "red");
        map.put(R.id.arrow3, "yellow");
        map.put(R.id.arrow4, "green");
        map.put(R.id.arrow5, "cyan");
        map.put(R.id.arrow6, "blue");
        map.put(R.id.arrow7, "purple");
        map.put(R.id.arrow8, "rose");
        map.put(R.id.arrow9, "grey");
        map.put(R.id.arrow10, "white");
        return map;
    }
}
