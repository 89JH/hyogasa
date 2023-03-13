package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class marketTest {
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

        HashMap<String, String> rgParams = new HashMap<String, String>();
        String result = api.callApi("/v1/ticker?markets=KRW-BTC", "GET", rgParams);

        System.out.println("result - " + result);

    }
}
