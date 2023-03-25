package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class limtTradeTest {
    public static void main(String[] args) throws IOException, ParseException, NoSuchAlgorithmException {
        OkHttpClient client = new OkHttpClient();

        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, "{\"side\":\"ask\",\"market\":\"KRW-SAND\",\"volume\":\"6\",\"price\":\"1000\",\"ord_type\":\"limit\",\"identifier\":\"JHJ202302181\"}");
//        Request request = new Request.Builder()
//                .url("https://api.upbit.com/v1/orders")
//                .post(body)
//                .addHeader("accept", "application/json")
//                .addHeader("content-type", "application/json")
//                .addHeader("Authorization", api.getJWT("side=ask&market=KRW-SAND&volume=6&price=1000&ord_type=limit&identifier=JHJ202302181"))
//                .build();

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("side", "bid");
        rgParams.put("market", "KRW-BTC");
        rgParams.put("volume", "0.002");
        rgParams.put("price", "37450000");
        rgParams.put("ord_type", "limit");
        rgParams.put("identifier", "JHJ2023032403");
        String result = api.callApi("/v1/orders", "POST", rgParams);


        System.out.println("result- " + result);
    }
}
