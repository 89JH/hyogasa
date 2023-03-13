package com.hyogasa.hgs2.api;

import org.apache.xerces.impl.dv.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public class Util {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String HMAC_SHA512 = "HmacSHA512";

    public static String base64Encode(byte[] bytes) {
		String bytesEncoded = Base64.encode(bytes);
		return bytesEncoded;
    }

    public static String hashToString(String data, byte[] key) {
		String result = null;
		Mac sha512_HMAC;
	
		try {
		    sha512_HMAC = Mac.getInstance("HmacSHA512");
		    System.out.println("key : " + new String(key));
		    SecretKeySpec secretkey = new SecretKeySpec(key, "HmacSHA512");
		    sha512_HMAC.init(secretkey);
	
		    byte[] mac_data = sha512_HMAC.doFinal(data.getBytes());
		    System.out.println("hex : " + bin2hex(mac_data));
		    result = Base64.encode(mac_data);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return result;
    }

    public static byte[] hmacSha512(String value, String key) {
		try {
		    SecretKeySpec keySpec = new SecretKeySpec(
			    key.getBytes(DEFAULT_ENCODING), HMAC_SHA512);
		    Mac mac = Mac.getInstance(HMAC_SHA512);
		    mac.init(keySpec);
		    return mac.doFinal(value.getBytes(DEFAULT_ENCODING));
		} catch (NoSuchAlgorithmException e) {
		    throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
		    throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
		    throw new RuntimeException(e);
		}
    }

    public static String asHex(byte[] bytes) {
    	return new String(Base64.encode(bytes));
    }

    public static String bin2hex(byte[] data) {
    	return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static String mapToQueryString(Map<String, String> map) {
		StringBuilder string = new StringBuilder();
	
		if (map.size() > 0) {
		    string.append("?");
		}
	
		for (Entry<String, String> entry : map.entrySet()) {
		    string.append(entry.getKey());
		    string.append("=");
		    string.append(entry.getValue());
		    string.append("&");
		}
	
		return string.toString();
    }

    // UTC 시간 KST시간으로 변환
	public static String convertUtctoLocal(String utcTime) {
		String localTime = "";

		try {
			// UTC DATE 포맷 지정
			String formatTimeZone = "yyyy-MM-dd'T'HH:mm:ss'Z'";
			Date date = new SimpleDateFormat(formatTimeZone).parse(utcTime);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTime(date);

			// 한국 KST 는 UTC 시간보다 9시간 빠름
			cal.add(Calendar.HOUR, 9);

			// 한국형 시간으로 변경된 값 저장 실시
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			localTime = sdformat.format(cal.getTime());

		}catch (Exception e) {
			e.printStackTrace();
		}

		return localTime;
	}


	// UTC 시간 KST시간으로 변환
	public static String convertUtctoLocal2(String utcTime) {
		String localTime = "";

		try {
			// UTC DATE 포맷 지정
			String formatTimeZone = "yyyy-MM-dd'T'HH:mm";
			Date date = new SimpleDateFormat(formatTimeZone).parse(utcTime);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTime(date);

			// 한국 KST 는 UTC 시간보다 9시간 빠름
			cal.add(Calendar.HOUR, 9);

			// 한국형 시간으로 변경된 값 저장 실시
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			localTime = sdformat.format(cal.getTime());

		}catch (Exception e) {
			e.printStackTrace();
		}

		return localTime;
	}

	// UTC 시간 KST시간으로 변환
	public static String convertUtctoLocal3(String utcTime) {
		String localTime = "";

		try {
			// UTC DATE 포맷 지정
			String formatTimeZone = "yyyy-MM-dd'T'HH:mm:ss'Z'";
			Date date = new SimpleDateFormat(formatTimeZone).parse(utcTime);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.setTime(date);

			// 한국 KST 는 UTC 시간보다 9시간 빠름
			cal.add(Calendar.HOUR, 9);

			// 한국형 시간으로 변경된 값 저장 실시
			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			localTime = sdformat.format(cal.getTime());

		}catch (Exception e) {
			e.printStackTrace();
		}

		return localTime;
	}
}
