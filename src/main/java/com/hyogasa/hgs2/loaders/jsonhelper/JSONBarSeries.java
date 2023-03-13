package com.hyogasa.hgs2.loaders.jsonhelper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JSONBarSeries {
    private String name;
    private JSONArray ohlc;

    public static JSONBarSeries from(BarSeries series, String coinName) {
        JSONBarSeries result = new JSONBarSeries();
        result.name = coinName;
        List<Bar> barData = series.getBarData();

        //System.out.println("barData - " + barData);

        for (Bar bar : barData) {
            JSONBarData exportableBarData = JSONBarData.from(bar, coinName);
            System.out.println("exportableBarData - " + exportableBarData.coinName);
        }

        System.out.println("result - " + result.ohlc.toJSONString());


        return result;
    }


    public BarSeries toBarSeries() {
        BaseBarSeries result = new BaseBarSeriesBuilder().withName(this.name).build();
//
//        for (String data : result.ohlc) {
//            data.addTo(result);
//        }
        return result;
    }

}
