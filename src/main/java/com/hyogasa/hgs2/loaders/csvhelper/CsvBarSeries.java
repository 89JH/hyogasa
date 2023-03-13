package com.hyogasa.hgs2.loaders.csvhelper;

import com.hyogasa.hgs2.loaders.jsonhelper.JSONBarData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.util.List;

public class CsvBarSeries {
    public JSONObject ohlc = new JSONObject();

    public static CsvBarSeries from(BarSeries series, String coinName) {
        CsvBarSeries result = new CsvBarSeries();
        CsvBarData exportableBarData = CsvBarData.from(series.getBarData().get(0), coinName);

        result.ohlc.put("coinName", exportableBarData.coinName);
        result.ohlc.put("endTime", exportableBarData.endTime);
        result.ohlc.put("openPrice", exportableBarData.openPrice);
        result.ohlc.put("highPrice", exportableBarData.highPrice);
        result.ohlc.put("lowPrice", exportableBarData.lowPrice);
        result.ohlc.put("closePrice", exportableBarData.closePrice);
        result.ohlc.put("volume", exportableBarData.volume);
        result.ohlc.put("amount", exportableBarData.amount);

        return result;
    }


    public BarSeries toBarSeries() {
        BaseBarSeries result = new BaseBarSeriesBuilder().build();
//
//        for (String data : result.ohlc) {
//            data.addTo(result);
//        }
        return result;
    }

}
