package com.hyogasa.hgs2;

import com.hyogasa.hgs2.api.ApiClient;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class getHttpHeaderTest {
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        ApiClient api = new ApiClient("Oi0v0KwJU8GJZdbq72r2rWSJp4ctXjVAp0Wk38qx"
                , "f5EMue3x1bVEFTsawOJDfp6xh8tPpPLWtiibhRVx");

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("side", "ask");
        rgParams.put("market", "KRW-SAND");
        rgParams.put("volume", "6");
        rgParams.put("price", "1000");
        rgParams.put("ord_type", "limit");
        rgParams.put("identifier", "JHJ202302182");
        //"{\"side\":\"ask\",\"market\":\"KRW-SAND\",\"volume\":\"6\",\"price\":\"1000\",\"ord_type\":\"limit\",\"identifier\":\"JHJ202302181\"}"

    }
}
