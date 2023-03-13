package com.hyogasa.hgs2.loaders.jsonhelper;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GsonBarSeries {

    private String name;
    public static List<GsonBarData> ohlc = new LinkedList<>();

    public static GsonBarSeries from(BarSeries series, String coinName) {
        GsonBarSeries result = new GsonBarSeries();
        result.name = coinName;
        List<Bar> barData = series.getBarData();

        for (Bar bar : barData) {
            GsonBarData exportableBarData = GsonBarData.from(bar);

            result.ohlc.add(exportableBarData);
        }

        for(int i=0; i<ohlc.size(); i++) {
            System.out.println(i + " 번째 - " + ohlc.get(i));
        }

        return result;
    }


    public BarSeries toBarSeries() {
        BaseBarSeries result = new BaseBarSeriesBuilder().withName(this.name).build();
        for (GsonBarData data : ohlc) {
            data.addTo(result);
        }
        return result;
    }

}
