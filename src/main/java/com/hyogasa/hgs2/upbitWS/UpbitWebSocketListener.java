package com.hyogasa.hgs2.upbitWS;

import com.fasterxml.jackson.databind.JsonNode;
import com.hyogasa.hgs2.api.Util;
import com.hyogasa.hgs2.dbmd.model.dao.DbMapper;
import com.hyogasa.hgs2.dbmd.service.DbService;
import com.hyogasa.hgs2.loaders.WSLoader;
import com.hyogasa.hgs2.loaders.csvhelper.CsvBarLoader;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class UpbitWebSocketListener extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private String json;
    public DbService dbService;

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        System.out.printf("Socket Closed : %s / %s\r\n", code, reason);
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.send("PING");
        System.out.printf("Socket Closing : %s / %s\n", code, reason);
//        webSocket.close(NORMAL_CLOSURE_STATUS, null);
//        webSocket.cancel();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        t.printStackTrace();
        System.out.println("Socket Error : " + t.getMessage());
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        JsonNode jsonNode = JsonUtil.fromJson(text, JsonNode.class);;

        try {
            JSONParser parser = new JSONParser();
            JSONObject jObj = (JSONObject) parser.parse(String.valueOf(jsonNode));

            for(int i=0; i<jObj.size(); i++) {
                System.out.println("현재가 - " + jObj.get("trade_price"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println(jsonNode.toPrettyString());
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        JsonNode jsonNode = JsonUtil.fromJson(bytes.string(StandardCharsets.UTF_8), JsonNode.class);

        //System.out.println("jsonNode - " + jsonNode);

        try {
            JSONParser parser = new JSONParser();
            JSONObject jObj = (JSONObject) parser.parse(String.valueOf(jsonNode));

//            if(jObj.get("type").equals("ticker")) {
//                System.out.println("1 - " + jObj.get("trade_price"));
            String utcDate = jObj.get("trade_date").toString().substring(0, 4) + "-" + jObj.get("trade_date").toString().substring(4, 6) + "-" + jObj.get("trade_date").toString().substring(6, 8);
            String utcTime = jObj.get("trade_time").toString().substring(0, 2) + ":" + jObj.get("trade_time").toString().substring(2, 4) + ":" + jObj.get("trade_time").toString().substring(4, 6);
            String bTs = utcDate + "T" + utcTime + "Z";

//                System.out.println("utcDat - " + utcDate);
//                System.out.println("utcTim - " + utcTime);

            String ts = Util.convertUtctoLocal3(bTs);
            String op = String.valueOf(jObj.get("opening_price"));
            String hp = String.valueOf(jObj.get("high_price"));
            String lp = String.valueOf(jObj.get("low_price"));
            String cp = String.valueOf(jObj.get("trade_price"));
            String vol = String.valueOf(jObj.get("trade_volume"));

            BarSeries series = WSLoader.loadSeries(ts, op, hp, lp, cp, vol);
            // WebSocket 오프닝, 클로즈, 저가 사용 안하고 현재가만 사용해서 처리 가능
            // 추후 필요하면 type - ticker(현재가) 분기 태워서 사용
            //BarSeries series = WSLoader.loadSeries(ts, cp, cp, cp, cp, vol);

            //System.out.println("ts - " + ts.substring(0, 10).replaceAll("-", ""));

            String coinName = String.valueOf(jObj.get("code"));
            String tradeDate = ts.substring(0, 10).replaceAll("-", "");

            CsvBarLoader.persistSeries(series, tradeDate, coinName);
            // 2023-03-14 저장하는데까지는 느리지 않음.


            System.out.println("테스트1111");
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("CREDATE", ts);
            params.put("OP", op);
            params.put("HP", hp);
            params.put("LP", lp);
            params.put("TP", cp);
            params.put("VOL", vol);
            params.put("TT", String.valueOf(jObj.get("ask_bid")));
            System.out.println("테스트12222");
            dbService.insertData(params);


                // 2023-03-09 Duration은 60이 대략 맞음.
                //ClosePriceIndicator closePrice = new ClosePriceIndicator(CsvBarLoader.loadSeries(tradeDate, coinName, 60));

//            System.out.println("cloPrice - " + closePrice.getValue(0));
//
                //System.out.println("closePrice - " + closePrice.getBarSeries().getBarData());
//
////
                //ParabolicSarIndicator pi = new ParabolicSarIndicator(CsvBarLoader.loadSeries(tradeDate, coinName, 60));
                //MONEYFl
                //MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
                //EMAIndicator signal = new EMAIndicator(macd, 9);

                //ChaikinMoneyFlowIndicator mfi = new ChaikinMoneyFlowIndicator(closePrice, 1);

                //System.out.println("0번째 엔드타임 - " + closePrice.getBarSeries().getBar(closePrice.getBarSeries().getBarCount() - 1).getEndTime());
                //RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//
//                System.out.println("----------------------------- 시작 -----------------------------");
//                System.out.println("2 - " + jObj.get("trade_price"));
//                System.out.println(coinName + " / endTime - " + closePrice.getBarSeries().getBar(closePrice.getBarSeries().getBarCount() - 1).getEndTime());
//                System.out.println("macd - " + macd.getValue(macd.getBarSeries().getBarCount() - 1));
//                System.out.println("signal - " + signal.getValue(signal.getBarSeries().getBarCount() - 1));
//                System.out.println("parabolic - " + pi.getValue(pi.getBarSeries().getBarCount() - 1));
//                System.out.println("rsi - " + rsi.getValue(rsi.getBarSeries().getBarCount() - 1));
//                System.out.println("----------------------------- 종료 -----------------------------");


                //double rsiVal = Double.parseDouble(rsi.getValue(rsi.getBarSeries().getBarCount() - 1).getDelegate().toString());

                //BigDecimal rsiVal = new BigDecimal(rsi.getValue(rsi.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);
                //BigDecimal macdVal = new BigDecimal(macd.getValue(macd.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);
                //BigDecimal signalVal = new BigDecimal(signal.getValue(signal.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);





//            SMAIndicator shortSma = new SMAIndicator(closPrice, 5);
                //System.out.println("rsi barSeries -  " + rsi.getBarSeries().getName());

//            System.out.println("series - " + rsi.getBarSeries().getBarData().size());

//            System.out.println(rsi.getValue(rsi.getBarSeries().getBarCount() - 1));

//            System.out.println("barCount - " + series.getBarCount());
//            for(int i=0; i<series.getBarCount(); i++) {
//                System.out.println(i + " rsi - " + rsi.getValue(i));
//            }
//            System.out.println("rsi barSeries333 -  " + rsi.getBarSeries().getBarData());


                //System.out.println("rsi toString -  " + rsi.toString());
                //System.out.println("rsi0 -  " + rsi.getValue(0));


                //ClosePriceIndicator closePrice = new ClosePriceIndicator(series);


//            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //System.out.println(jsonNode.toPrettyString());
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        webSocket.send(getParameter());
//        webSocket.close(NORMAL_CLOSURE_STATUS, null); // 없을 경우 끊임없이 서버와 통신함
    }

//    public void setParameter(SiseType siseType, List<String> codes) {
//        this.json = JsonUtil.toJson(List.of(Ticket.of(UUID.randomUUID().toString()), Type.of(siseType, codes)));
//        System.out.println("this.json - " + this.json);
//    }

    public void setParameter(JSONArray jj) {
        this.json = JsonUtil.toJson(jj);
        System.out.println("this.json - " + this.json);
    }

    private String getParameter() {
        return this.json;
    }

    @Data(staticConstructor = "of")
    private static class Ticket {
        private final String ticket;
    }

    @Data(staticConstructor = "of")
    private static class Type {
        private final SiseType type;
        private final List<String> codes; // market
    }

}
