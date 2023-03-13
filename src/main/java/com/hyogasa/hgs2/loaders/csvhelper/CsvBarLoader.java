package com.hyogasa.hgs2.loaders.csvhelper;

import com.google.gson.Gson;
import com.hyogasa.hgs2.loaders.jsonhelper.GsonBarSeries;
import com.hyogasa.hgs2.loaders.jsonhelper.JSONBarSeries;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.io.*;
import java.nio.charset.Charset;
import java.time.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

public class CsvBarLoader {

    private static final Logger LOG = Logger.getLogger(CsvBarLoader.class.getName());

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static void persistSeries(BarSeries series, String filename, String cn, String ab) throws ParseException {
        CsvBarSeries exportableSeries = CsvBarSeries.from(series, cn);

        BufferedWriter bw = null;
        File f = new File("src/main/resources/coin/" + cn + "/");

        // 폴더가 없으면
        if(!f.exists()) {
            f.mkdir(); // 폴더 생성
        }

        try {
            // 2023-03-09 파일 명 앞에 일자 없애기 일자 변하면서 데이터가 정확해지지 않음.
            //File ff = new File("src/main/resources/coin/" + cn + "/" + filename + "_" + cn + ".csv");
            File ff = new File("src/main/resources/coin/" + cn + "/" + cn + "_trade.csv");
            bw = new BufferedWriter(new FileWriter(ff, true));
            String wData = "";

            wData = exportableSeries.ohlc.get("coinName")
                    + "," + exportableSeries.ohlc.get("endTime")
                    + "," + exportableSeries.ohlc.get("openPrice")
                    + "," + exportableSeries.ohlc.get("highPrice")
                    + "," + exportableSeries.ohlc.get("lowPrice")
                    + "," + exportableSeries.ohlc.get("closePrice")
                    + "," + exportableSeries.ohlc.get("volume")
                    + "," + exportableSeries.ohlc.get("amount")
                    + "," + ab;

            bw.write(wData);
            bw.newLine();

            //LOG.info("Bar series '" + exportableSeries.ohlc.get("coinName") + "' successfully saved to '" + filename + "'");
        } catch (IOException e) {
            e.printStackTrace();
            //LOG.log(Level.SEVERE, "Unable to store bars in JSON", e);
        } finally {
            if (bw != null) {
                try {
                    bw.flush();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static BarSeries loadSeries(String td, String cn, int duration) {
        // 2023-03-09 파일 명 앞에 일자 없애기 일자 변하면서 데이터가 정확해지지 않음.
        //String fileName = "src/main/resources/coin/" + cn + "/" + td + "_" + cn + ".csv";
        String fileName = "src/main/resources/coin/" + cn + "/" + cn + "_trade.csv";
        //InputStream is = CsvBarLoader.class.getClassLoader().getResourceAsStream(fileName);
        CSVReader csvReader = null;
        List<String[]> lines = null;

        try {
            csvReader = new CSVReader(new FileReader(fileName));
            lines = csvReader.readAll();
        }catch (Exception e) {
            //Logger.getLogger(CsvBarLoader.class.getName()).log(Level.SEVERE, "Unable to load trades from CSV", e);
        }finally {
            if(csvReader != null) {
                try{
                    csvReader.close();
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        BarSeries series = new BaseBarSeries();

        if((lines != null) && !lines.isEmpty()) {
            ZonedDateTime beginTime = ZonedDateTime
                    .ofInstant(Instant.ofEpochMilli(Long.parseLong(lines.get(0)[1])), ZoneId.systemDefault());

            ZonedDateTime endTime = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(Long.parseLong(lines.get(lines.size() - 1)[1])),
                    ZoneId.systemDefault());

            if (beginTime.isAfter(endTime)) {
                Instant beginInstant = beginTime.toInstant();
                Instant endInstant = endTime.toInstant();
                beginTime = ZonedDateTime.ofInstant(endInstant, ZoneId.systemDefault());
                endTime = ZonedDateTime.ofInstant(beginInstant, ZoneId.systemDefault());

                // Since the CSV file has the most recent trades at the top of the file, we'll
                // reverse the list to feed
                // the List<Bar> correctly.
                Collections.reverse(lines);
            }

            // build the list of populated bars
            buildSeries(series, beginTime, endTime, duration, lines);
        }

        return series;
    }

    /**
     * Builds a list of populated bars from csv data.
     *
     * @param beginTime the begin time of the whole period
     * @param endTime   the end time of the whole period
     * @param duration  the bar duration (in seconds)
     * @param lines     the csv data returned by CSVReader.readAll()
     */
    @SuppressWarnings("deprecation")
    private static void buildSeries(BarSeries series, ZonedDateTime beginTime, ZonedDateTime endTime, int duration, List<String[]> lines) {
        Duration barDuration = Duration.ofSeconds(duration);
        ZonedDateTime barEndTime = beginTime;
        // line number of trade data
        int i = 0;

        do {
            // build a bar
            barEndTime = barEndTime.plus(barDuration);
            Bar bar = new BaseBar(barDuration, barEndTime, series.function());
            do {
                // get a trade
                String[] tradeLine = lines.get(i);
                ZonedDateTime tradeTimeStamp = ZonedDateTime
                        .ofInstant(Instant.ofEpochMilli(Long.parseLong(tradeLine[1])), ZoneId.systemDefault());
                // if the trade happened during the bar

                if (bar.inPeriod(tradeTimeStamp)) {
                    // add the trade to the bar
                    double tradePrice = Double.parseDouble(tradeLine[5]);
                    double tradeVolume = Double.parseDouble(tradeLine[6]);
                    bar.addTrade(tradeVolume, tradePrice, series.function());
                } else {
                    // the trade happened after the end of the bar
                    // go to the next bar but stay with the same trade (don't increment i)
                    // this break will drop us after the inner "while", skipping the increment
                    break;
                }
                i++;
            } while (i < lines.size());
            // if the bar has any trades add it to the bars list
            // this is where the break drops to
            if (bar.getTrades() > 0) {
                series.addBar(bar);
            }
        } while (barEndTime.isBefore(endTime));
    }
}
