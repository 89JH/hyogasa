package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class accountsTest {
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

        HashMap<String, String> rgParams = new HashMap<String, String>();
        String result = api.callApi("/v1/accounts", "GET", rgParams);

        JSONParser jsonParser = new JSONParser();
        JSONArray jArray = (JSONArray) jsonParser.parse(result);

        JSONObject jObj = new JSONObject();

        String cur = "";
        String lcur = "";

        String coinName = "";   // 매수한 코인명
        BigDecimal avgPrice = new BigDecimal("0.01"); // 코인 평균 매수가
        BigDecimal balance = new BigDecimal("0.01"); // 코인 매수 수량

        BigDecimal krwBalance = new BigDecimal("0.01"); // 주문가능원화 to BigDecimal
        for(int i=0; i<jArray.size(); i++) {
            System.out.println(jArray.get(i));
            jObj.putAll((Map) jArray.get(i));
            BigDecimal price = new BigDecimal(String.valueOf(jObj.get("avg_buy_price")));
            BigDecimal volume = new BigDecimal(String.valueOf(jObj.get("balance")));

            if(jObj.get("currency").equals("KRW")) { // 원화 체크
                krwBalance = new BigDecimal(String.valueOf(jObj.get("balance")));
            }else { // 그 외 코인들 체크
                if(new BigDecimal(String.valueOf(jObj.get("avg_buy_price")))
                        .multiply(new BigDecimal(String.valueOf(jObj.get("balance"))))
                        .compareTo(new BigDecimal("10000")) > 0) {  // 10000원 이상 매수한 코인만 처리
                    coinName = (String) jObj.get("currency");
                    avgPrice = new BigDecimal(String.valueOf(jObj.get("avg_buy_price")));
                    balance = new BigDecimal(String.valueOf(jObj.get("balance")));
                }
            }


            System.out.println("1111231 - " + price.multiply(volume).compareTo(new BigDecimal("0.00000017")));

            System.out.println("111 - " + String.valueOf(jObj.get("balance")));

            price = new BigDecimal(String.valueOf(jObj.get("avg_buy_price")));

            System.out.println("프라이스 - " + price);
            System.out.println("bal - " + volume);

            System.out.println("bal2  - " + new BigDecimal(String.valueOf(volume)).toString());

            System.out.println("밸런스 - " + price.multiply(volume));

            Double sss = Double.parseDouble(String.valueOf(jObj.get("avg_buy_price")));
            Double sss1 = Double.parseDouble(String.valueOf(jObj.get("balance")));

            System.out.println(new BigDecimal(sss*sss1).compareTo(new BigDecimal("0.00000016")));

            Double sss2 = sss*sss1;
            BigDecimal b2 = new BigDecimal( sss2);


            System.out.println("b2 - " + b2.setScale(10, BigDecimal.ROUND_HALF_UP).toPlainString());





            if(jObj.get("currency").toString().equals("KRW")) {
                cur = (String) jObj.get("balance");
            }else {
                lcur = (String) jObj.get("balance");
            }
        }

        System.out.println("cur - " + cur);
        System.out.println("lcur - " + lcur);

        System.out.println("size - " + jObj.size());

        for(int i=0; i<jObj.size(); i++) {

            System.out.println(i + " 번재 - " + jObj.get("currency"));
        }

        for(Object key : jObj.keySet()) {
            System.out.println(jObj.get(key));
        }


        System.out.println("krBal - " + krwBalance);
        System.out.println("coinName - " + coinName);
        System.out.println("avgPrice - " + avgPrice);
        System.out.println("balance - " + balance);

    }
}
