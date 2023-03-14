package com.hyogasa.hgs2.upbitWS;

import com.google.gson.JsonArray;
import com.hyogasa.hgs2.api.ApiClient;
import com.hyogasa.hgs2.loaders.csvhelper.CsvBarLoader;
import com.hyogasa.hgs2.model.dao.UpbitMapper;
import com.hyogasa.hgs2.ta4.TimeSeries;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;


public class UpbitWsClient {
    private static final Logger LOG = Logger.getLogger(UpbitWsClient.class.getName());

    protected String accKey;
    protected String secKey;
    private ApiClient api;

    public UpbitWsClient(String AccKey, String SecKey) {
        this.accKey = AccKey;
        this.secKey = SecKey;
        api = new ApiClient(accKey, secKey);
    }

    private String market = "/v1/market/all";
    private String ticker = "/v1/ticker";

    public void AccWebSocket() {
        try {
//            HashMap<String, String> rgParams = new HashMap<String, String>();
//            String result = "";
//
//            result = api.callApi(market, "GET", rgParams);
//
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // [Date 형식으로 변경 : 24 시간 포맷 형태]
//            String nowDate = sdf.format(new Date(System.currentTimeMillis()));
//
//            JSONParser jsonParser = new JSONParser();
//            JSONArray jsonArray = (JSONArray) jsonParser.parse(result);
//            JSONObject jsonObj = new JSONObject();
//
//            String sParam = "";
//
//            String[] arr = new String[jsonArray.size()];
//            for (int i = 0; i < jsonArray.size(); i++) {
//                jsonObj.putAll((Map) jsonArray.get(i));
//
//                if (jsonObj.get("market").toString().indexOf("KRW-") == 0) {
//                    sParam += "," + jsonObj.get("market");
//                }
//            }
//
//            //현재가 정보 가져오기(24시간 거래대금 가져오기 xx억 이상 데이터만 거래하기 위함)
//            HashMap<String, String> tickerRgParams = new HashMap<String, String>();
//            tickerRgParams.put("markets", sParam.substring(1));
//            String resultTicker = "";
//
//            resultTicker = api.callApi(ticker, "GET", tickerRgParams);
//
//            JSONArray tickArray = (JSONArray) jsonParser.parse(resultTicker);
//            JSONObject tickObj = new JSONObject();
//
//            for(int i=0; i<tickArray.size(); i++) {
//                tickObj.putAll((Map) tickArray.get(i));
//
//                if(new BigDecimal(tickObj.get("acc_trade_price_24h").toString()).compareTo(new BigDecimal("5000000000")) > 0) {
//                    arr[i] = String.valueOf(tickObj.get("market"));
//                }
//            }

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

            System.out.println("coinArray - " + coinArray);

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
            e.printStackTrace();
        }
//        }catch (UnsupportedEncodingException uee) {
//            //uee.printStackTrace();
//            System.out.println("UnsupportedEncodingException 발생 - " + uee.getMessage());
//        }catch (NoSuchAlgorithmException nae) {
//            //nae.printStackTrace();
//            System.out.println("NoSuchAlgorithmException 발생 - " + nae.getMessage());
//        } catch (ParseException pe) {
//            //pe.printStackTrace();
//            System.out.println("ParseException 발생 - " + pe.getMessage());
//        }


    }
}
