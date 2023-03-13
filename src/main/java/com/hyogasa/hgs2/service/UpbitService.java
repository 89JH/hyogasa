package com.hyogasa.hgs2.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.hyogasa.hgs2.api.ApiClient;
import com.hyogasa.hgs2.api.Util;
import com.hyogasa.hgs2.bean.Bean01;
import com.hyogasa.hgs2.model.dao.UpbitMapper;
import com.hyogasa.hgs2.model.dto.abratio;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Time;
import org.codehaus.jackson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UpbitService {
    private final UpbitMapper upbitMapper;

    static ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
            , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

    private static String market = "/v1/market/all";
    private static String ticker = "/v1/ticker";
    private static String candle = "/v1/candles/minutes/1"; // 1분
    private static String ticks = "/v1/trades/ticks"; // 최근체결내역
    private static String account = "/v1/accounts"; // 계좌 조회
    private static String orders = "/v1/orders";    // 주문 생성

    // batch insert 하기 위한 db정보 세팅
    private static String dsClassName = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
    private static String dsUrl = "jdbc:log4jdbc:mysql://localhost:3306/test?characterEncoding=UTF-8";
    private static String dsUser = "root";
    private static String dsPass = "fksl123";

    //Refill.intervally toekn = 10, 1회충전시 10개의 토큰을 충전
    //Duration.ofSeconds = 1, 1초마다 토큰을 충전
    //private static Refill refill = Refill.intervally(10, Duration.ofSeconds(1));

    //private static Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1)));
    private static Bandwidth limit = Bandwidth.simple(10, Duration.ofSeconds(1));
    private static Bandwidth limit2 = Bandwidth.simple(600, Duration.ofMinutes(1));

    private static RequestLimitRule limitRule = RequestLimitRule.of(Duration.ofSeconds(1), 10);

    private static RequestRateLimiter rateLimiter = new InMemorySlidingWindowRequestRateLimiter(limitRule);

    private static Bucket bucket = Bucket.builder()
            .addLimit(limit)
            .build();

    // 전체 코인 조회
    //@Scheduled(cron = "0 0/1 * * * *")
    //@Scheduled(cron = "* * * * * *")
    public void allCoin() throws IOException, NoSuchAlgorithmException, ParseException, SQLException, InterruptedException {
        HashMap<String, String> rgParams = new HashMap<String, String>();
        String result = "";

        result = api.callApi(market, "GET", rgParams);

        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss"); // [Date 형식으로 변경 : 24 시간 포맷 형태]
        String nowDate = sdf.format(new Date(System.currentTimeMillis()));

        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(result);
        JSONObject jsonObj = new JSONObject();

        String sParam = "";

        System.out.println("시작 - " + LocalTime.now());
        for(int i=0; i<jsonArray.size(); i++) {
            jsonObj.putAll((Map) jsonArray.get(i));

            if(jsonObj.get("market").toString().indexOf("KRW-") == 0) {
                sParam += "," + jsonObj.get("market");
            }
        }

        System.out.println("종료 - " + LocalTime.now());

        //현재가 정보 가져오기(24시간 거래대금 가져오기 xx억 이상 데이터만 거래하기 위함)
        HashMap<String, String> tickerRgParams = new HashMap<String, String>();
        tickerRgParams.put("markets", sParam.substring(1));
        String resultTicker = "";

        resultTicker = api.callApi(ticker, "GET", tickerRgParams);

        JSONArray tickerArray = (JSONArray) jsonParser.parse(resultTicker);
        JSONObject tickerObj = new JSONObject();

        PreparedStatement pstmt = null;
        Connection con = null;

        try {
            Class.forName(dsClassName);
            String sql = "INSERT INTO INPUT_DATA2(CRE_DATE, NAME, PRICE, VOLUME, 등록일자) VALUES(?, ?, ?, ?, ?)";
            con = DriverManager.getConnection(dsUrl, dsUser, dsPass);
            pstmt = con.prepareStatement(sql);

            int rrCnt = 0;

            System.out.println("tickerArray 시작 - " + LocalTime.now());
            for(int i=0; i<tickerArray.size(); i++) {
                tickerObj.putAll((Map) tickerArray.get(i));
                HashMap<String, String> candleRgParams = new HashMap<String, String>();
                // 거래금액 50억 이상인 항목은 추후 체크
                //if(new BigDecimal(tickerObj.get("acc_trade_price_24h").toString()).compareTo(new BigDecimal("8000000000")) > 0) {
                if(new BigDecimal(tickerObj.get("acc_trade_price_24h").toString()).compareTo(new BigDecimal("5000000000")) > 0) {
                    candleRgParams.put("market", tickerObj.get("market").toString());
                    candleRgParams.put("count", "2");

                    String resultCandle = "";
                    resultCandle = api.callApi(candle, "GET", candleRgParams);
                    JSONArray candleArray = (JSONArray) jsonParser.parse(resultCandle);
                    JSONObject candleObj = new JSONObject();

                    for(int j=0; j<candleArray.size(); j++) {
                        candleObj.putAll((Map) candleArray.get(j));
                        if(j == 1) {
                            pstmt.setString(1, candleObj.get("candle_date_time_kst").toString().replaceAll("T", " "));
                            pstmt.setString(2, candleObj.get("market") .toString());
                            pstmt.setString(3, String.valueOf(new BigDecimal(Double.parseDouble(candleObj.get("trade_price").toString()))));
                            pstmt.setString(4, String.valueOf(new BigDecimal(Double.parseDouble(candleObj.get("candle_acc_trade_price").toString()))));
                            pstmt.setString(5, nowDate);

                            pstmt.addBatch();
                        }
                    }

                    rrCnt = reqChk();
                    if(rrCnt == 0) {
                        Thread.sleep(1000);
                    }
                }
            }

            pstmt.executeBatch();
            pstmt.clearBatch();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(pstmt != null) {
                pstmt.close();
            }

            if(con != null) {
                con.close();
            }
        }
        System.out.println("tickerArray 종료 - " + LocalTime.now());

        //System.out.println(LocalTime.now());
    }

    // api 요청수 체크
    public int reqChk() {
        api.header = api.header.substring(1, api.header.length() - 1);
        String[] headArray = api.header.split(" ");
        int result = 0;

        for(int a=0; a<headArray.length; a++) {
            if(headArray[a].indexOf("sec") > -1) {
                result = Integer.parseInt(headArray[a].substring(headArray[a].indexOf("=") + 1));
            }
        }

        return result;
    }

    // 10초마다 체결 데이터 조회
    //@Scheduled(cron = "0/10 * * * * *")
    public void sec10Chk() throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException, InterruptedException, java.text.ParseException {
        System.out.println("10초마다 확인 - " + LocalTime.now());
        BigDecimal krwBalance = new BigDecimal("0.01"); // 주문가능원화 to BigDecimal
        
        int rrCnt = 0;  // 호출가능횟수 확인용
        String coinName = "";   // 매수한 코인명
        BigDecimal avgPrice = new BigDecimal("0.01"); // 코인 평균 매수가
        BigDecimal balance = new BigDecimal("0.01"); // 코인 매수 수량

        // 보유 원화 확인 원화가 있으면 매수 / 없으면 매도 프로세스 진행
        String acResult = "";
        HashMap<String, String> acParams = new HashMap<String, String>();
        acResult = api.callApi(account, "GET", acParams);

        JSONParser acParser = new JSONParser();
        JSONArray acArray = (JSONArray) acParser.parse(acResult);
        JSONObject acObj = new JSONObject();

        // 이전 캔들 데이터 조회(시간, 종목명, 가격, 볼륨)
        List<Map<String, Object>> sList = new ArrayList<Map<String, Object>>();
        sList = upbitMapper.getPreCandle();

        for(int i=0; i<acArray.size(); i++) {
            acObj.putAll((Map) acArray.get(i));

            if(acObj.get("currency").equals("KRW")) { // 원화 체크
                krwBalance = new BigDecimal(String.valueOf(acObj.get("balance")));
            }else { // 그 외 코인들 체크
                if(new BigDecimal(String.valueOf(acObj.get("avg_buy_price")))
                        .multiply(new BigDecimal(String.valueOf(acObj.get("balance"))))
                        .compareTo(new BigDecimal("10000")) > 0) {  // 10000원 이상 매수한 코인만 처리
                    coinName = (String) acObj.get("currency");
                    avgPrice = new BigDecimal(String.valueOf(acObj.get("avg_buy_price")));
                    balance = new BigDecimal(String.valueOf(acObj.get("balance")));
                }
            }
        }

        if(krwBalance.compareTo(new BigDecimal("80000")) > 0) { // 50000원 이상 있으면 매수 프로세스
            Map<String, Object> ss = new HashMap<>();

            HashMap<String, String> tickParams = new HashMap<String, String>();
            String tickResult = "";

            JSONParser parser = new JSONParser();
            JSONArray tickArray = new JSONArray();
            JSONObject tickObj = new JSONObject();

            double askCnt = 0; // 매도 카운팅
            double askRatio = 0.0; // 매도세기
            double askVol = 0; // 체결강도 구하기 위한 매도 체결 볼륨
            double bidCnt = 0; // 매수 카운팅
            double bidRatio = 0.0; // 매수세기
            double bidVol = 0; // 체결강도 구하기 위한 매수 체결 볼륨
            double vp = 0; // 체결강도(Volume Power)

            BigDecimal tickVal = new BigDecimal("0.01"); // 체결 건별 매수 금액 확인용
            BigDecimal preVol = new BigDecimal("0.01");
            BigDecimal tradePrice = new BigDecimal("0.01");

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for(int j=0; j<sList.size(); j++) {
                tickParams.put("market", (String) sList.get(j).get("NAME"));
                tickParams.put("count", "200");
                tickResult = api.callApi(ticks, "GET", tickParams);
                tickArray = (JSONArray) parser.parse(tickResult);

                BigDecimal tickVol = new BigDecimal("0.01"); // 현재 분봉의 볼륨 가지고 잇기 위함
                preVol = new BigDecimal(String.valueOf(sList.get(j).get("TOTAL")));

                for(int k=0; k<tickArray.size(); k++) {
                    tickObj = (JSONObject) tickArray.get(k);
                    tickVal = new BigDecimal(String.valueOf(tickObj.get("trade_price"))).multiply(new BigDecimal(String.valueOf(tickObj.get("trade_volume"))));
                    Date tickLocTime = sdf.parse(Util.convertUtctoLocal(tickObj.get("trade_date_utc").toString() + "T" + tickObj.get("trade_time_utc").toString() + "Z"));

                    // 최근 체결 가격 저장용
                    if(k == 0) {
                        tradePrice = new BigDecimal(String.valueOf(tickObj.get("trade_price")));
                    }

                    Long minute = (now.getTime() - tickLocTime.getTime()) / 60000;

                    // 현재 minute = 조회된 minute 같은경우 볼륨 add
                    if(now.getMinutes() == tickLocTime.getMinutes()) {
                        tickVol = tickVol.add(new BigDecimal(String.valueOf(tickObj.get("trade_price"))).multiply(new BigDecimal(String.valueOf(tickObj.get("trade_volume")))));
                    }

                    // 현재시간부터 3분 이내 데이터만 활용
                    if(minute <= 3) {
                        Double chkVal = Double.parseDouble(tickVal.toPlainString());
//                            System.out.println("chkVal - " + chkVal);
//                            System.out.println("매수매도구분 / " + tickObj.get("ask_bid"));
                        // 가중치 부여(매수) / 10000만원 이상 - 1.5 / 1000만원 이상 - 1.2 / 500만원 이상 - 1 / 100만원 이상 - 0.8 / 50만원 이상 - 0.6 / 50만원 이하 - 0.3
                        if(tickObj.get("ask_bid").equals("BID")) {
                            bidVol += Double.parseDouble(String.valueOf(tickObj.get("trade_volume")));
                            if(chkVal / 100000000 >= 1) {
                                bidCnt += 1.5;
                            }else if(chkVal / 10000000 >= 1) {
                                bidCnt += 1.2;
                            }else if(chkVal / 5000000 >= 1) {
                                bidCnt += 1.0;
                            }else if(chkVal / 1000000 >= 1) {
                                bidCnt += 0.8;
                            }else if(chkVal / 500000 >= 1) {
                                bidCnt += 0.6;
                            }else if(chkVal / 50000 >= 1) {
                                bidCnt += 0.3;
                            }else {
                                bidCnt += 0.1;
                            }
                        }else if(tickObj.get("ask_bid").equals("ASK")) {
                            askVol += Double.parseDouble(String.valueOf(tickObj.get("trade_volume")));
                            // 가중치 부여(매도) / 1000만원 이상 - 1.5 / 500만원 이상 - 1.2 / 100만원 이상 - 1 / 50만원 이상 - 0.8 / 50만원 미만 - 0.6
                            if(chkVal / 10000000 >= 1) {
                                askCnt += 1.5;
                            }else if(chkVal / 5000000 >= 1) {
                                askCnt += 1.2;
                            }else if(chkVal / 1000000 >= 1) {
                                askCnt += 1.0;
                            }else if(chkVal / 500000 >= 1) {
                                askCnt += 0.8;
                            }else if(chkVal / 50000 >= 1) {
                                askCnt += 0.4;
                            }else {
                                askCnt += 0.1;
                            }
                        }
                    }
                }

                // 호출가능 횟수 확인
                rrCnt = reqChk();
                if(rrCnt == 0) {
                    Thread.sleep(1000);
                }

                bidRatio = Math.round((bidCnt / (bidCnt + askCnt)) * 100.0);
                vp = Math.round((bidVol/askVol)*100.0);

                // 매수세가 75 이상이거나, 55이상에 이전봉보다 거래량이 큰 경우 매수(확실하지 않은경우 거래량이 많아지면 매수)
                // 20230226 50%로 잠시 수정 해봄
                if(bidRatio > 65 && vp >= 80) { //{ || (bidRatio > 55 && tickVol.compareTo(preVol) > 0)) { // 매수세 70 이상이고 이전 캔들보다 거래량 초과했을 경우 매수 진행 70이상인 항목 잠시 보류
                    BigDecimal bidVolume = new BigDecimal("0.01");

                    tradePrice = tradePrice.multiply(new BigDecimal("1.01"));

                    // 아래 부분이 틀렸으면 v2에서 제대로 진행
                    if(tradePrice.compareTo(new BigDecimal("2000000")) >= 0) {   // 1. 50만원부터 백단위 올림
                        tradePrice = tradePrice.setScale(-3, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("100000")) >= 0) {    // 2. 5만원부터 십단위 반올림
                        tradePrice = tradePrice.setScale(-2, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("1000")) >= 0) {     // 3. 5천원 부터 일단위 반올림
                        tradePrice = tradePrice.setScale(-1, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("100")) >= 0) {     // 5. 1백원 부터 소수 2번째 자리 반올림
                        tradePrice = tradePrice.setScale(0, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("10")) >= 0) {     // 6. 1십원 부터 소수 3번째 자리 반올림
                        tradePrice = tradePrice.setScale(1, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("1")) >= 0) {     // 7. 1원 부터 소수 4번째 자리 반올림
                        tradePrice = tradePrice.setScale(2, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("0.1")) >= 0) {     // 8. 0원 부터 소수 4번째 자리 반올림
                        tradePrice = tradePrice.setScale(3, BigDecimal.ROUND_HALF_UP);
                    }else if(tradePrice.compareTo(new BigDecimal("0")) >= 0) {     // 8. 0원 부터 소수 4번째 자리 반올림
                        tradePrice = tradePrice.setScale(4, BigDecimal.ROUND_HALF_UP);
                    }

                    bidVolume = krwBalance.divide(tradePrice.multiply(new BigDecimal("1.01")), 2, BigDecimal.ROUND_DOWN);

                    System.out.println("매수 체크 - " + now + " / " + vp + " / " + bidRatio + " / " + sList.get(j).get("NAME") + "매수가격 : " + tradePrice.toPlainString() + " / 매수볼륨 : " + bidVolume.toPlainString() + " / 이전봉값 : " + preVol + " / 현재봉값 : " + tickVol);
                    HashMap<String, String> trdParams = new HashMap<String, String>();
                    trdParams.put("side", "bid");
                    trdParams.put("market", String.valueOf(sList.get(j).get("NAME")));
                    trdParams.put("volume", bidVolume.toPlainString());
                    trdParams.put("price", tradePrice.toPlainString());
                    trdParams.put("ord_type", "limit");
                    trdParams.put("identifier", ("JHJ" + now.getTime()));
                    String result = api.callApi("/v1/orders", "POST", trdParams);

                    System.out.println("매수 결과 - " + result);

                    return;
                }
            }

        }else { // 없으면 매도 프로세스
            // - 손실 프로세스
            if(coinName.equals(null) || coinName.equals("")) {
                return;
            }

            HashMap<String, String> tickParams = new HashMap<String, String>();
            String tickResult = "";
            tickParams.put("market", "KRW-" + coinName);
            tickParams.put("count", "200");
            tickResult = api.callApi(ticks, "GET", tickParams);

            JSONParser askParser = new JSONParser();
            JSONArray askArray = (JSONArray) askParser.parse(tickResult);
            JSONObject askObj = new JSONObject();
            BigDecimal askVal = new BigDecimal("0.01"); // 체결 건별 매수 금액 확인용

            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            double askCnt = 0; // 매도 카운팅
            double askRatio = 0.0; // 매도세기
            double bidCnt = 0; // 매수 카운팅
            double bidRatio = 0.0; // 매수세기
            BigDecimal tradePrice = new BigDecimal("0.01");

            int chkCnt = 0; // 매도 진행체크하기 위함

            for(int i=0; i<askArray.size(); i++) {
                askObj = (JSONObject) askArray.get(i);
                askVal = new BigDecimal(String.valueOf(askObj.get("trade_price"))).multiply(new BigDecimal(String.valueOf(askObj.get("trade_volume"))));
                Date tickLocTime = sdf.parse(Util.convertUtctoLocal(askObj.get("trade_date_utc").toString() + "T" + askObj.get("trade_time_utc").toString() + "Z"));

                // 최근 체결 가격 저장용
                if(i == 0) {
                    tradePrice = new BigDecimal(String.valueOf(askObj.get("trade_price")));
                }

                Long minute = (now.getTime() - tickLocTime.getTime()) / 60000;

                // 현재시간부터 3분 이내 데이터만 활용
                if(minute <= 3) {
                    Double chkVal = Double.parseDouble(askVal.toPlainString());
                    // 가중치 부여(매수) / 10000만원 이상 - 1.5 / 1000만원 이상 - 1.2 / 500만원 이상 - 1 / 100만원 이상 - 0.8 / 50만원 이상 - 0.6 / 50만원 이하 - 0.3
                    if(askObj.get("ask_bid").equals("BID")) {
                        if(chkVal / 100000000 >= 1) {
                            bidCnt += 1.5;
                        }else if(chkVal / 10000000 >= 1) {
                            bidCnt += 1.2;
                        }else if(chkVal / 5000000 >= 1) {
                            bidCnt += 1.0;
                        }else if(chkVal / 1000000 >= 1) {
                            bidCnt += 0.8;
                        }else if(chkVal / 500000 >= 1) {
                            bidCnt += 0.6;
                        }else if(chkVal / 50000 >= 1) {
                            bidCnt += 0.3;
                        }else {
                            bidCnt += 0.1;
                        }
                    }else if(askObj.get("ask_bid").equals("ASK")) {
                        // 가중치 부여(매도) / 1000만원 이상 - 1.5 / 500만원 이상 - 1.2 / 100만원 이상 - 1 / 50만원 이상 - 0.8 / 50만원 미만 - 0.6
                        if(chkVal / 10000000 >= 1) {
                            askCnt += 1.5;
                        }else if(chkVal / 5000000 >= 1) {
                            askCnt += 1.2;
                        }else if(chkVal / 1000000 >= 1) {
                            askCnt += 1.0;
                        }else if(chkVal / 500000 >= 1) {
                            askCnt += 0.8;
                        }else if(chkVal / 50000 >= 1) {
                            askCnt += 0.4;
                        }else {
                            askCnt += 0.1;
                        }
                    }
                }
            }

            bidRatio = Math.round((bidCnt / (bidCnt + askCnt)) * 100.0);

            // - 수익 프로세스
            // 1. 1.6% 수익 발생시 매도 진행. but 매수세 체크해서 매수세가 70 혹은 75% 이상이면 건너뜀(매수세가 60 이하로 떨어진 경우)
            // 0.4% := 0.3% 수수료가 0.1%임
            if(tradePrice.compareTo(avgPrice.multiply(new BigDecimal("1.004"))) > 0) {
                if(bidRatio < 70) {
                    chkCnt++;
                }
            // 0.8% 손해 발생시 매도, 10분이상 지난경우 매도는 나중에 추가
            // 0.9% := 1.0% 수수료가 0.1%임
            }else if(tradePrice.compareTo(avgPrice.multiply(new BigDecimal("0.991"))) < 0) {
                chkCnt++;
            // 1. 매도세가 강한(70 이상) 경우에도 매도 진행(이건 보류)
            }
//            else if(bidRatio < 30) {
//                chkCnt++;
//            }

            //tradePrice = tradePrice.multiply(new BigDecimal("0.99"));

            // 아래 부분이 틀렸으면 v2에서 제대로 진행
            if(tradePrice.compareTo(new BigDecimal("2000000")) >= 0) {    // 2. 5만원부터 십단위 내림
                tradePrice = tradePrice.setScale(-3, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("100000")) >= 0) {     // 3. 5천원 부터 일단위 내림
                tradePrice = tradePrice.setScale(-2, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("1000")) >= 0) {     // 4. 1천원 부터 소수 1번째 자리 내림
                tradePrice = tradePrice.setScale(-1, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("100")) >= 0) {     // 5. 1백원 부터 소수 2번째 자리 내림
                tradePrice = tradePrice.setScale(0, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("10")) >= 0) {     // 6. 1십원 부터 소수 3번째 자리 내림
                tradePrice = tradePrice.setScale(1, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("1")) >= 0) {     // 7. 1원 부터 소수 4번째 자리 내림
                tradePrice = tradePrice.setScale(2, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("0.1")) >= 0) {     // 8. 0원 부터 소수 4번째 자리 내림
                tradePrice = tradePrice.setScale(3, BigDecimal.ROUND_HALF_DOWN);
            }else if(tradePrice.compareTo(new BigDecimal("0")) >= 0) {     // 8. 0원 부터 소수 4번째 자리 내림
                tradePrice = tradePrice.setScale(4, BigDecimal.ROUND_HALF_DOWN);
            }

            if(chkCnt > 0) {
                System.out.println("매도 체크 - " + now + " / " + bidRatio + " / KRW-" + coinName + "매도가격 : " + tradePrice.toPlainString() + " / 매도볼륨 : " + balance.toPlainString()  + " / 매도가격 : " + tradePrice.toPlainString() );
                HashMap<String, String> trdParams = new HashMap<String, String>();
                trdParams.put("side", "ask");
                trdParams.put("market", "KRW-" + coinName);
                trdParams.put("volume", balance.toPlainString());
                trdParams.put("price", "");
                trdParams.put("ord_type", "market");
                //trdParams.put("ord_type", "limit");
                trdParams.put("identifier", ("JHJ" + now.getTime()));
                String result = api.callApi("/v1/orders", "POST", trdParams);

                System.out.println(tradePrice.toPlainString());

                System.out.println("매도 결과 - " + result);
            }
        }

    }

    public void chkRatio(String coinName) {

    }

    public List<abratio> getAbratioList() {
        List<abratio> tt = null;
        return tt;
        //upbitMapper.getAbratioList();
    }

    public static void main(String[] args) {
        System.out.println("첫번째 리필시간 - " );
    }
}
