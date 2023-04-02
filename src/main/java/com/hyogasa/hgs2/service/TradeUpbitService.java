package com.hyogasa.hgs2.service;

import com.hyogasa.hgs2.api.ApiClient;
import com.hyogasa.hgs2.loaders.csvhelper.CsvBarLoader;
import com.hyogasa.hgs2.model.dao.UpbitMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class TradeUpbitService {
    private final UpbitMapper upbitMapper;
    private static final Logger LOG = Logger.getLogger(TradeUpbitService.class.getName());
    static ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
            , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

    private static String account = "/v1/accounts"; // 계좌 조회
    private static String orders = "/v1/orders"; // 주문 생성

    //@Scheduled(cron = "0/1 * * * * *")
    /** fixedDelay : 해당 메소드가 끝나는 시간 기준, 밀리세컨드 간격으로 실행.
     *  > 하나의 인스턴스만 항상 실행되도록 해야 할 상황에서 유용
     *
     * */
    //@Scheduled(fixedDelay = 1000)
    public void trade() {
        /** rsi가 40이하 터치 했다는 플래그로 사용 */
        int rsiCnt = 0;
        /** 주문 생성할때 key로 사용하기 위한 date */
        Date now = new Date();

        // 2023-03-09 Duration은 60이 대략 맞음.
        // 2023-03-27 Duration은 대략 66%? -> 60 := 40
        // 300 := 240
        BarSeries series = CsvBarLoader.loadSeries("KRW-BTC", 100);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        ParabolicSarIndicator pi = new ParabolicSarIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator signal = new EMAIndicator(macd, 9);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        SMAIndicator sma = new SMAIndicator(closePrice, 5);
        BollingerBandsMiddleIndicator bbmSMA = new BollingerBandsMiddleIndicator(sma);
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePrice, 5);
        BollingerBandsLowerIndicator bblSMA = new BollingerBandsLowerIndicator(bbmSMA, standardDeviation);
        BollingerBandsUpperIndicator bbuSMA = new BollingerBandsUpperIndicator(bbmSMA, standardDeviation);

        BigDecimal closeVal = new BigDecimal(String.valueOf(closePrice.getValue(closePrice.getBarSeries().getBarCount() - 1)));
        BigDecimal rsiVal = new BigDecimal(String.valueOf(rsi.getValue(rsi.getBarSeries().getBarCount() - 1))).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal macdVal = new BigDecimal(String.valueOf(macd.getValue(macd.getBarSeries().getBarCount() - 1))).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal signalVal = new BigDecimal(String.valueOf(signal.getValue(signal.getBarSeries().getBarCount() - 1))).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal paraVal = new BigDecimal(String.valueOf(pi.getValue(pi.getBarSeries().getBarCount() - 1)));
        BigDecimal bbuVal = new BigDecimal(String.valueOf(bbuSMA.getValue(bbuSMA.getBarSeries().getBarCount() - 1)));
        BigDecimal bbmVal = new BigDecimal(String.valueOf(bbmSMA.getValue(bbmSMA.getBarSeries().getBarCount() - 1)));
        BigDecimal bblVal = new BigDecimal(String.valueOf(bblSMA.getValue(bblSMA.getBarSeries().getBarCount() - 1)));


        System.out.println("----------------- 지표확인 시작 ---------------------");
        System.out.println("closeVal - " + closeVal);
        System.out.println("rsiVal - " + rsiVal);
        System.out.println("macdVal - " + macdVal);
        System.out.println("signalVal - " + signalVal);
        System.out.println("paraVal - " + paraVal);
        System.out.println("b-upper - " + bbuVal);
        System.out.println("b-middle - " + bbmVal);
        System.out.println("b-lower - " + bblVal);
        System.out.println("----------------- 지표확인 종료 ---------------------");

        // 보유자산 조회 시작
        BigDecimal krwBalance = new BigDecimal("0.01"); // 주문가능원화 to BigDecimal

        String coinName = "";   // 매수한 코인명
        BigDecimal avgPrice = new BigDecimal("0.01"); // 코인 평균 매수가
        BigDecimal balance = new BigDecimal("0.01"); // 코인 매수 수량

        // 보유 원화 확인 원화가 있으면 매수 / 없으면 매도 프로세스 진행
        String acResult = "";

        try {
            HashMap<String, String> acParams = new HashMap<String, String>();
            acResult = api.callApi(account, "GET", acParams);

            JSONParser acParser = new JSONParser();
            JSONArray acArray = (JSONArray) acParser.parse(acResult);
            JSONObject acObj = new JSONObject();

            for (int i = 0; i < acArray.size(); i++) {
                acObj.putAll((Map) acArray.get(i));

                if (acObj.get("currency").equals("KRW")) { // 원화 체크
                    krwBalance = new BigDecimal(String.valueOf(acObj.get("balance")));
                } else { // 그 외 코인들 체크
                    if (new BigDecimal(String.valueOf(acObj.get("avg_buy_price")))
                            .multiply(new BigDecimal(String.valueOf(acObj.get("balance"))))
                            .compareTo(new BigDecimal("10000")) > 0) {  // 10000원 이상 매수한 코인만 처리
                        coinName = (String) acObj.get("currency");
                        avgPrice = new BigDecimal(String.valueOf(acObj.get("avg_buy_price")));
                        balance = new BigDecimal(String.valueOf(acObj.get("balance")));
                    }
                }
            }

        } catch (UnsupportedEncodingException uee) {
            LOG.info("계좌정보 uee - " + uee.getMessage());
        } catch (NoSuchAlgorithmException nae) {
            LOG.info("계좌정보 nae - " + nae.getMessage());
        } catch (ParseException pe) {
            LOG.info("계좌정보 pe - " + pe.getMessage());
        }
        // 보유자산 조회 종료

        //rsi가 40 이하로 내려간 경우
        if(rsiVal.compareTo(new BigDecimal("40")) <= 0) {
            rsiCnt++;
        }

        // 2. krw가 있으면 매수 프로세스 없으면 매도 프로세스 진행
//        if(krwBalance.compareTo(new BigDecimal("50000")) > 0) { // 50000원 이상 있으면 매수 프로세스
//            LOG.info("매수 프로세스 시작");
//            /** 3. 매수 프로세스
//             * pre. 아래 프로세스를 타기 전에 rsi가 40 이하를 찍고 반등하는 프로세스 추가? <- 이건 개발해보고 진행상황 보기(20230324 추가함)
//             * a. rsi 50 이상 60 이하, macd > signal, 현재가 > 파라볼릭
//             *  > 3가지 항목 모두 만족해야만 매수 진행
//             *
//             * b. rsi 23 이하인경우 무조건 매수 진행? ㄴㄴ 지표 하나만 보고 거래하면 안맞는 경우가 더 많음
//             * */
//            // rsi가 25%이하이면서, macd > signal 인경우
//            if((rsiVal.compareTo(new BigDecimal("50")) >= 0 && rsiVal.compareTo(new BigDecimal("60")) < 0) /** rsi 비교 */
//                    && macdVal.compareTo(signalVal) > 0 /** macd > signal 비교 */
//                    && closeVal.compareTo(paraVal) > 0 /** 현재가 > 파라볼릭 비교 */) {
//                // 매수 진행
//                try {
//                    BigDecimal bidVolume = new BigDecimal("0.01");
//                    bidVolume = krwBalance.divide(closeVal.multiply(new BigDecimal("1.01")), 5, BigDecimal.ROUND_DOWN);
//
//                    HashMap<String, String> trdParams = new HashMap<String, String>();
//                    trdParams.put("side", "bid");
//                    trdParams.put("market", "KRW-BTC");
//                    trdParams.put("volume", bidVolume.toPlainString());
//                    trdParams.put("price", closeVal.toPlainString());
//                    trdParams.put("ord_type", "limit");
//                    trdParams.put("identifier", ("JHJ" + now.getTime()));
//                    String result = api.callApi(orders, "POST", trdParams);
//
//                    rsiCnt = 0;
//                    LOG.info("매수 결과 - " + result);
//                }catch (UnsupportedEncodingException uee) {
//                    LOG.info("매수 uee - " + uee.getMessage());
//                }catch (NoSuchAlgorithmException nae) {
//                    LOG.info("매수 nae - " + nae.getMessage());
//                }
//            }
//
//            LOG.info("매수 프로세스 종료");
//        }else { // 원화가 없으면 매도 프로세스 진행
//            LOG.info("매도 프로세스 진행");
//            int askCnt = 0;
//            /** 4. 매도 프로세스
//             * -- 손절 프로세스
//             * a. 수익률이 - 0.4% 이상되면 무조건 손절 진행(%는 추후 확인해보고 변경해보기)
//             *
//             * -- 수익 프로세스
//             * pre. 수익률이 0.4% 이상인데 rsi 60이하, macd > signal, 현재가 > 파라볼릭
//             *  > 현재 포지션 유지
//             * a. 수익률이 0.4% 이면 매도 프로세스 진행
//             *  > rsi 65
//             * b. 수익률이 0.2% 혹은 0.3% 인데, rsi 65 이상 / signal > macd / 현재가 < 파라볼릭
//             *  > 한가지라도 충족되면 매도 진행 < 두가지 이상 충족되면 매도 진행
//             *
//             * 보류1. rsi가 65 이상부터 매도 프로세스 진행
//             *  > rsi가 65 이상이지만, macd > signal 이고, 파라볼릭sar 지표가 현재가격보다 낮은경우 포지션 유지
//             *
//             * 보류2. rsi 70이상, macd < signal, 현재가 < 파라볼릭
//             *  > 2가지 이상 매핑되면 매도 진행
//             * */
//
//            /** 매도 프로세스 수익률 -0.9% <- 손절은 일단 빼보자 ㄴㄴ 손절도 칼같이해야할듯함 */
//            if(closeVal.compareTo(avgPrice.multiply(new BigDecimal("0.991"))) <= 0) {
//                askCnt++;
//            }else {
//                /** 수익률이 0.4% 이상이면 */
//                if(closeVal.compareTo(avgPrice.multiply(new BigDecimal("1.004"))) >= 0) {
//                    /** pre. 수익률이 0.4% 이상인데 rsi 60이하, macd > signal, 현재가 > 파라볼릭 이면 포지션 유지 */
//                    if(rsiVal.compareTo(new BigDecimal("60")) < 0 && macdVal.compareTo(signalVal) > 0 && closeVal.compareTo(paraVal) > 0) {
//                        return;
//                    }else {
//                        askCnt++;
//                    }
//                /** 수익률이 0.2% 이상이면 */
//                }else if(closeVal.compareTo(avgPrice.multiply(new BigDecimal("1.002"))) >= 0) {
//                    if(rsiVal.compareTo(new BigDecimal("65")) >= 0 || macdVal.compareTo(signalVal) < 0 || closeVal.compareTo(paraVal) < 0) {   // rsi 65 이상
//                        askCnt++;
//                    }
//                }
//            }
//
//            /** 매도 주문 실행 */
//            if(askCnt != 0) {
//                try {
//                    HashMap<String, String> trdParams = new HashMap<String, String>();
//                    trdParams.put("side", "ask");
//                    trdParams.put("market", "KRW-BTC");
//                    trdParams.put("volume", String.valueOf(balance));
//                    trdParams.put("price", "");
//                    trdParams.put("ord_type", "market");
//                    //trdParams.put("ord_type", "limit");
//                    trdParams.put("identifier", ("JHJ" + now.getTime()));
//                    String result = api.callApi(orders, "POST", trdParams);
//
//                    LOG.info("매도 결과 - " + result);
//                } catch (UnsupportedEncodingException uee) {
//                    LOG.info("매도 uee - " + uee.getMessage());
//                } catch (NoSuchAlgorithmException nae) {
//                    LOG.info("매도 nae - " + nae.getMessage());
//                }
//            }
//
//            LOG.info("매도 프로세스 종료");
//        }
    }
}
