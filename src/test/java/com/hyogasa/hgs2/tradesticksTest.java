package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class tradesticksTest {
    public static String convertUtctoLocal(String utcTime) {
        String localTime = "";

        try {
            // UTC DATE 포맷 지정
            String formatTimeZone = "yyyy-MM-dd'T'HH:mm:ss'Z'";
            Date date = new SimpleDateFormat(formatTimeZone).parse(utcTime);
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTime(date);

            // 한국 KST 는 UTC 시간보다 9시간 빠름
            cal.add(Calendar.HOUR, 9);

            // 한국형 시간으로 변경된 값 저장 실시
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            localTime = sdformat.format(cal.getTime());

        }catch (Exception e) {
            e.printStackTrace();
        }

        return localTime;
    }

    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException, java.text.ParseException {

        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

        HashMap<String, String> candleRgParams = new HashMap<String, String>();
        candleRgParams.put("market", "KRW-STX");
        candleRgParams.put("count", "200");

        String resultCandle = api.callApi("/v1/trades/ticks", "GET", candleRgParams);

        //System.out.println("result - " + resultCandle);


        JSONParser parser = new JSONParser();
        JSONArray jarray = (JSONArray) parser.parse(resultCandle);
        JSONObject jObj = new JSONObject();

        double askCnt = 0; // 매도 카운팅
        double bidCnt = 0; // 매수 카운팅
        double bidCnt2 = 0;
        double askCnt2 = 0;
        BigDecimal vol = new BigDecimal("0.01");

        BigDecimal tickVal = new BigDecimal("0.01");
        for(int i=0; i<jarray.size(); i++) {
            //System.out.println(i + " 번째 - " + jarray.get(i));
            jObj = (JSONObject) jarray.get(i);

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            System.out.println("now - " + now);

            Date ss = sdf.parse(convertUtctoLocal( jObj.get("trade_date_utc").toString() + "T" + jObj.get("trade_time_utc").toString() + "Z"));

            tickVal = new BigDecimal(String.valueOf(jObj.get("trade_price"))).multiply(new BigDecimal(String.valueOf(jObj.get("trade_volume"))));
            //System.out.println("ss - " + ss);
            //System.out.println("ss getTime - " + ss.getTime());

            ///System.out.println("분차이 - " + (now.getTime() - ss.getTime()) / 60000);

            Long minute = (now.getTime() - ss.getTime()) / 60000;

            //System.out.println(now.getMinutes() + " / " + ss.getMinutes());

//            String s1 = jObj.get("trade_date_utc").toString().replace("-", "");
//            String s2 = jObj.get("trade_time_utc").toString().replace(":", "");

            //System.out.println("현지시간 - " + convertUtctoLocal( jObj.get("trade_date_utc").toString() + "T" + jObj.get("trade_time_utc").toString() + "Z"));

            if(now.getMinutes() == ss.getMinutes()) {
                System.out.println(i + "번째 - " + convertUtctoLocal( jObj.get("trade_date_utc").toString() + "T" + jObj.get("trade_time_utc").toString() + "Z") + " / price - " + new BigDecimal(Double.parseDouble(String.valueOf(jObj.get("trade_price")))));
                System.out.println(convertUtctoLocal( jObj.get("trade_date_utc").toString() + "T" + jObj.get("trade_time_utc").toString() + "Z") + " / volume - " + new BigDecimal(Double.parseDouble(String.valueOf(jObj.get("trade_volume")))).setScale(8, BigDecimal.ROUND_HALF_UP));
                System.out.println();
                vol = vol.add(new BigDecimal(String.valueOf(jObj.get("trade_price"))).multiply(new BigDecimal(String.valueOf(jObj.get("trade_volume")))));// *  jObj.get("trade_volume");
            }

            // 현재시간 기준 3분 이내 거래 데이터만 활용
            if(minute <= 2) {
                System.out.println(ss + " / " + tickVal.toPlainString());
                if(jObj.get("ask_bid").equals("BID")) {
                    bidCnt2 += Double.parseDouble(String.valueOf(jObj.get("trade_volume")));
                    // 가중치 부여(매수) / 10000만원 이상 - 1.5 / 1000만원 이상 - 1.2 / 500만원 이상 - 1 / 100만원 이상 - 0.8 / 50만원 이상 - 0.6 / 50만원 이하 - 0.3
                    if(Double.parseDouble(String.valueOf(vol)) / 100000000 >= 1) {
                        bidCnt += 1.5;
                    }else if(Double.parseDouble(String.valueOf(vol)) / 10000000 >= 1) {
                        bidCnt += 1.2;
                    }else if(Double.parseDouble(String.valueOf(vol)) / 5000000 >= 1) {
                        bidCnt += 1.0;
                    }else if(Double.parseDouble(String.valueOf(vol)) / 1000000 >= 1) {
                        bidCnt += 0.8;
                    }else if(Double.parseDouble(String.valueOf(vol)) / 500000 >= 1) {
                        bidCnt += 0.6;
                    }else if(Double.parseDouble(String.valueOf(vol)) / 50000 >= 1) {
                        bidCnt += 0.3;
                    }else {
                        bidCnt += 0.1;
                    }


                }else if(jObj.get("ask_bid").equals("ASK")) {
                    askCnt2 += Double.parseDouble(String.valueOf(jObj.get("trade_volume")));
                    // 가중치 부여(매도) / 1000만원 이상 - 1.5 / 500만원 이상 - 1.2 / 100만원 이상 - 1 / 50만원 이상 - 0.8 / 50만원 미만 - 0.6
                    if(Double.parseDouble(String.valueOf(vol)) / 10000000 >= 1) {
                        askCnt += 1.5;
                    }else if (Double.parseDouble(String.valueOf(vol)) / 5000000 >= 1) {
                        askCnt += 1.2;
                    }else if (Double.parseDouble(String.valueOf(vol)) / 1000000 >= 1) {
                        askCnt += 1.0;
                    }else if (Double.parseDouble(String.valueOf(vol)) / 500000 >= 1) {
                        askCnt += 0.8;
                    }else if (Double.parseDouble(String.valueOf(vol)) / 50000 >= 1) {
                        askCnt += 0.4;
                    }else {
                        askCnt += 0.1;
                    }

                }
            }


        }


        System.out.println("bidCnt - " + bidCnt2);
        System.out.println("askCnt - " + askCnt2);

        System.out.println("체결강도 - " + Math.round((bidCnt2/askCnt2) * 100.0));

        System.out.println("bidCnt - " + bidCnt);
        System.out.println("askCnt - " + askCnt);
        System.out.println(vol + " 매수세 - " + Math.round((bidCnt/(bidCnt + askCnt))*100.0));
        System.out.println(vol + " 매도세 - " + Math.round((askCnt/(bidCnt + askCnt))*100.0));



    }
}
