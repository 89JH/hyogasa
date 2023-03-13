package com.hyogasa.hgs2.loaders;

import com.hyogasa.hgs2.api.Util;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.List;

public class WSLoader {
    public static BarSeries loadSeries(String ts, String op, String hp, String lp, String cp, String vol) {
        BarSeries series = new BaseBarSeries();
        ZonedDateTime date = ZonedDateTime.of(LocalDateTime.parse(ts), ZoneId.of("Asia/Seoul"));
        double open = Double.parseDouble(op);
        double high = Double.parseDouble(hp);
        double low = Double.parseDouble(lp);
        double close = Double.parseDouble(cp);
        double volume = Double.parseDouble(vol);

        series.addBar(date, open, high, low, close, volume);

        return series;
    }
}
