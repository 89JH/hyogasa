package com.hyogasa.hgs2.upbitWS;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Logger;


public class UpbitWsClient {
    private static final Logger LOG = Logger.getLogger(UpbitWsClient.class.getName());

    // 매일 13시쯤 거래대금 상위 5개도 거래 목록에 추가하는 로직 진행

    public void AccWebSocket() {
        try {
            String[] arr = new String[1];
            arr[0] = "KRW-BTC";

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("wss://api.upbit.com/websocket/v1")
                    .build();

            UpbitWebSocketListener webSocketListener = new UpbitWebSocketListener();

            JSONArray coinArray = new JSONArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] != null) {
                    coinArray.add(arr[i]);
                }
            }

            JSONArray jArray = new JSONArray();

            //2023-03-09 코인담는거부터 시작해보기

            /** Ticket 변수 생성 */
            JSONObject ticketObj = new JSONObject();
            ticketObj.put("ticket", UUID.randomUUID().toString());
            jArray.add(ticketObj);

            /** Ticker(현재가) 변수 생성 */
            JSONObject tickerObj = new JSONObject();
            tickerObj.put("type", SiseType.TICKER.getType());
            tickerObj.put("codes", coinArray);
//            tickerObj.put("isOnlySnapshot", false);
//            tickerObj.put("isOnlyRealtime", true);
            jArray.add(tickerObj);

            /** Trade(체결) 변수 생성(추후 사용하게 되면 풀어서 사용) */
//            JSONObject tradeObj = new JSONObject();
//            tradeObj.put("type", SiseType.TRADE.getType());
//            tradeObj.put("codes", coinArray);
//            jArray.add(tradeObj);

            /** ORDERBOOK(호가) 변수 생성(추후 사용하게 되면 풀어서 사용) */
//        JSONObject orderBookObj = new JSONObject();
//        orderBookObj.put("type", SiseType.ORDERBOOK.getType());
//        orderBookObj.put("codes", coinArray);
//        jArray.add(orderBookObj);

            webSocketListener.setParameter(jArray);

            client.newWebSocket(request, webSocketListener);
            client.dispatcher().executorService().shutdown();

        }catch (Exception e) {
            LOG.info(e.getMessage());
        }
    }
}
