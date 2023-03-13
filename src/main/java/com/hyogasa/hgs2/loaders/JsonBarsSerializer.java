package com.hyogasa.hgs2.loaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hyogasa.hgs2.loaders.jsonhelper.GsonBarData;
import com.hyogasa.hgs2.loaders.jsonhelper.GsonBarSeries;
import com.hyogasa.hgs2.loaders.jsonhelper.JSONBarSeries;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ta4j.core.BarSeries;

import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonBarsSerializer {

    private static final Logger LOG = Logger.getLogger(JsonBarsSerializer.class.getName());

    public static void persistSeries(BarSeries series, String filename, String cn) throws ParseException {
        JSONBarSeries exportableSeries = JSONBarSeries.from(series, cn);

        System.out.println("barSeries - " + exportableSeries.toBarSeries().getBar(0));

        //Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileWriter writer = null;
        File f = new File("src/main/resources/coin/" + cn + "/");

        JSONParser parser = new JSONParser();
        JSONObject jObj = new JSONObject();

        jObj = (JSONObject) parser.parse(String.valueOf(exportableSeries));

        // 폴더가 없으면
        if(!f.exists()) {
            f.mkdir(); // 폴더 생성
        }

        try {
            writer = new FileWriter("src/main/resources/coin/" + cn + "/" + filename + "_" + cn + ".json", true);
            writer.write(jObj.toJSONString());

//            JsonObject barObj = new JsonObject();
//            barObj.addProperty("endTime", exportableSeries.toBarSeries().getBar(0).getEndTime().toString());
//            barObj.addProperty("openPrice", exportableSeries.toBarSeries().getBar(0).getOpenPrice().getDelegate());
//            barObj.addProperty("highPrice", exportableSeries.toBarSeries().getBar(0).getHighPrice().getDelegate());
//            barObj.addProperty("lowPrice", exportableSeries.toBarSeries().getBar(0).getLowPrice().getDelegate());
//            barObj.addProperty("closePrice", exportableSeries.toBarSeries().getBar(0).getClosePrice().getDelegate());
//            barObj.addProperty("volume", exportableSeries.toBarSeries().getBar(0).getVolume().getDelegate());
//            barObj.addProperty("amount", exportableSeries.toBarSeries().getBar(0).getAmount().getDelegate());
////            gson.toJson(barObj, writer);
//            writer.write(barObj.toString());
            LOG.info("Bar series '" + series.getName() + "' successfully saved to '" + filename + "'");
        } catch (IOException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to store bars in JSON", e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static BarSeries loadSeries(String filename) {
        Gson gson = new Gson();
        FileReader reader = null;
        BarSeries result = null;
        try {
            reader = new FileReader("src/main/resources/item.json");
            GsonBarSeries loadedSeries = gson.fromJson(reader, GsonBarSeries.class);

            result = loadedSeries.toBarSeries();
            LOG.info("Bar series '" + result.getName() + "' successfully loaded. #Entries: " + result.getBarCount());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Unable to load bars from JSON", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
