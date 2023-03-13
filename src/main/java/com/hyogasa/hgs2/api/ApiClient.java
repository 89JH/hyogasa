package com.hyogasa.hgs2.api;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// 해당 클래스는 upbit 용임 다른 거래소에서 사용하려면 변경필요
public class ApiClient {
    protected String api_url = "https://api.upbit.com";
    protected String AccKey;
    protected String SecKey;

    public String header = "";

    public ApiClient(String Acckey, String SecKey) {
        this.AccKey = Acckey;
        this.SecKey = SecKey;
    }

    public String getNotParamJWT() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(SecKey);

        String jwtToken = JWT.create()
                .withClaim("access_key", AccKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .sign(algorithm);

        String authenticationTken = "Bearer " + jwtToken;

        return authenticationTken;
    }

    public String getJWT(String params) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Algorithm algorithm = Algorithm.HMAC256(SecKey);

        String queryString = params;

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("utf8"));

        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));

        String jwtToken = JWT.create()
                .withClaim("access_key", AccKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
                .sign(algorithm);

        String authenticationTken = "Bearer " + jwtToken;

        return authenticationTken;
    }

    public static String encodeURIComponent(String s) {
        String result = null;

        try {
            result = URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%26", "&")
                    .replaceAll("\\%3D", "=")
                    //.replaceAll("\\%2C", ",")
                    .replaceAll("\\%7E", "~");

        }catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    private HashMap<String, String> getHttpHeaders(String endpoint, String type, HashMap<String, String> rgData) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String strData = Util.mapToQueryString(rgData).replace("?", "");

        if(strData.length() > 0) {
            strData = strData.substring(0, strData.length()-1);
            strData = encodeURIComponent(strData);
            String str = endpoint + ";"	+ strData + ";";
        }

        HashMap<String, String> array = new HashMap<String, String>();
        array.put("accept", "application/json");
        array.put("content-type", "application/json");
        array.put("Authorization", type.equals("POST")?getJWT(strData) : getNotParamJWT());

        return array;

    }

    private String request(String strHost, String strMemod, HashMap<String, String> rgParams, HashMap<String, String> httpHeaders) {
        String response = "";

        // SSL 여부
        if (strHost.startsWith("https://")) {
            HttpRequest request = HttpRequest.get(strHost);
            // Accept all certificates
            request.trustAllCerts();
            // Accept all hostnames
            request.trustAllHosts();
        }

        if(strMemod.toUpperCase().equals("HEAD")) {

        }else {
            HttpRequest request = null;

            // POST/GET 설정
            if(strMemod.toUpperCase().equals("POST")) {

                request = new HttpRequest(strHost, "POST");
                request.readTimeout(10000);

                //System.out.println("POST ==> " + request.url());

                if(httpHeaders != null && !httpHeaders.isEmpty()) {
                    request.headers(httpHeaders);
                    //System.out.println("POST httpHead - " + httpHeaders.toString());
                }

                if(rgParams != null && !rgParams.isEmpty()) {
                    request.form(rgParams);
                    //System.out.println("POST params - " + rgParams.toString());
                }
            }else {
                request = HttpRequest.get(strHost
                        + Util.mapToQueryString(rgParams));
                request.readTimeout(10000);

                if(httpHeaders != null && !httpHeaders.isEmpty()) {
                    //httpHeaders.put("api-client-type", "2");
                    request.headers(httpHeaders);
                    //System.out.println("GET httpHead - " + httpHeaders.toString());
                }

                //System.out.println("Response was: " + response);
            }

            if(request.ok()) {
                header = String.valueOf(request.headers().get("Remaining-Req"));
                response = request.body();
            }else {
                response = "error : " + request.code() + ", message : "
                        + request.body();
            }
            request.disconnect();
        }

        return response;
    }

    public String callApi(String endpoint, String type, HashMap<String, String> params) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String rgResultDecode = "";
        HashMap<String, String> rgParams = new HashMap<String, String>();
        //rgParams.put("endpoint", endpoint);

        if (params != null) {
            rgParams.putAll(params);
        }

        String api_host = api_url + endpoint;
        HashMap<String, String> httpHeaders = getHttpHeaders(endpoint, type, rgParams);

        rgResultDecode = request(api_host, type, rgParams, httpHeaders);

        if (!rgResultDecode.startsWith("error")) {
            // json 파싱
            HashMap<String, String> result = null;
            List<String> lResult = null;
            try {

                //System.out.println("==== 결과 출력 ====");
                if(type.equals("POST")) {
                    result = new ObjectMapper().readValue(rgResultDecode,
                            HashMap.class);
                    //System.out.println("==== POST 결과 출력 ====");
                    //System.out.println("POST STATUS - "  + result.get("status"));

                }else if(type.equals("GET")) {
                    lResult = new ObjectMapper().readValue(rgResultDecode, List.class);
                    //System.out.println("==== GET 결과 출력 ====");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rgResultDecode;
    }
}
