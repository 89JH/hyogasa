package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class candleTest {
    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException {

        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

        HashMap<String, String> candleRgParams = new HashMap<String, String>();
        candleRgParams.put("market", "KRW-BTC,KRW-ETH");
        candleRgParams.put("count", "2");

        String resultCandle = api.callApi("/v1/candles/minutes/1", "GET", candleRgParams);

        System.out.println("result - " + resultCandle);

        JSONParser parser = new JSONParser();
        JSONArray jarray = (JSONArray) parser.parse(resultCandle);

        for(int i=0; i<jarray.size(); i++) {
            System.out.println(i + " 번째 - " + jarray.get(i));
        }


    }
}
