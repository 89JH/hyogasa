package com.hyogasa.hgs2;

import com.hyogasa.hgs2.upbitWS.UpbitWsClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Hgs2Application {
//	public static void getCrowling() {
//		String url = "https://coinmarketcap.com/ko/";
//
//		List<String> list = new ArrayList<>();
//		Connection con = Jsoup.connect(url);
//		Document doc = null;
//
//		try {
//			doc = con.get();
//
//			System.out.println(doc);
//		}catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		System.out.println(doc.select("cmc-table-row"));
//	}


	public static void main(String[] args) {
		SpringApplication.run(Hgs2Application.class, args);
		//getCrowling();

		// 실제 운영시 아래 데이터 수집하는 부분 주석 제거해야함.
		UpbitWsClient uwc = new UpbitWsClient();

		uwc.AccWebSocket();

		//BaseBarSeries
		//BarSeries
		//ClosePriceIndicator
		//RSIIndicator ss = new RSIIndicator();

	}

}
