package com.hyogasa.hgs2.service;

import com.hyogasa.hgs2.loaders.csvhelper.CsvBarLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.ParabolicSarIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

@Service
public class TradeUpbitService {

    @Scheduled(cron = "* * * * * *")
    public void trade() {
        // 2023-03-09 Duration은 60이 대략 맞음.
        ClosePriceIndicator closePrice = new ClosePriceIndicator(CsvBarLoader.loadSeries("KRW-BTC", 60));

//            System.out.println("cloPrice - " + closePrice.getValue(0));
//
        //System.out.println("closePrice - " + closePrice.getBarSeries().getBarData());
//
////
        ParabolicSarIndicator pi = new ParabolicSarIndicator(CsvBarLoader.loadSeries("KRW-BTC", 60));
        //MONEYFl
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator signal = new EMAIndicator(macd, 9);

        //ChaikinMoneyFlowIndicator mfi = new ChaikinMoneyFlowIndicator(closePrice, 1);

        //System.out.println("0번째 엔드타임 - " + closePrice.getBarSeries().getBar(closePrice.getBarSeries().getBarCount() - 1).getEndTime());
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//
                System.out.println("----------------------------- 시작 -----------------------------");
                System.out.println("KRW-BTC" + " / endTime - " + closePrice.getBarSeries().getBar(closePrice.getBarSeries().getBarCount() - 1).getEndTime());
                System.out.println("macd - " + macd.getValue(macd.getBarSeries().getBarCount() - 1));
                System.out.println("signal - " + signal.getValue(signal.getBarSeries().getBarCount() - 1));
                System.out.println("parabolic - " + pi.getValue(pi.getBarSeries().getBarCount() - 1));
                System.out.println("rsi - " + rsi.getValue(rsi.getBarSeries().getBarCount() - 1));
                System.out.println("----------------------------- 종료 -----------------------------");


        //double rsiVal = Double.parseDouble(rsi.getValue(rsi.getBarSeries().getBarCount() - 1).getDelegate().toString());

        //BigDecimal rsiVal = new BigDecimal(rsi.getValue(rsi.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);
        //BigDecimal macdVal = new BigDecimal(macd.getValue(macd.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);
        //BigDecimal signalVal = new BigDecimal(signal.getValue(signal.getBarSeries().getBarCount() - 1).getDelegate().toString()).setScale(2, RoundingMode.HALF_UP);





//            SMAIndicator shortSma = new SMAIndicator(closPrice, 5);
        //System.out.println("rsi barSeries -  " + rsi.getBarSeries().getName());

//            System.out.println("series - " + rsi.getBarSeries().getBarData().size());

//            System.out.println(rsi.getValue(rsi.getBarSeries().getBarCount() - 1));

//            System.out.println("barCount - " + series.getBarCount());
//            for(int i=0; i<series.getBarCount(); i++) {
//                System.out.println(i + " rsi - " + rsi.getValue(i));
//            }
//            System.out.println("rsi barSeries333 -  " + rsi.getBarSeries().getBarData());


        //System.out.println("rsi toString -  " + rsi.toString());
        //System.out.println("rsi0 -  " + rsi.getValue(0));


        //ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
    }
}
