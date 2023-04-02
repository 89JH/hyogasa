package com.hyogasa.hgs2.ta4;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.LowestValueIndicator;
import org.ta4j.core.num.Num;

public class GitHubStochastic extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;
    private final int barCount;
    private RSIIndicator highPriceIndicator;
    private RSIIndicator lowPriceIndicator;

    public GitHubStochastic(Indicator<Num> indicator, int barCount, RSIIndicator highPriceIndicator,
                                 RSIIndicator lowPriceIndicator) {
        super(indicator);
        this.indicator = indicator;
        this.barCount = barCount;
        this.highPriceIndicator = highPriceIndicator;
        this.lowPriceIndicator = lowPriceIndicator;
    }

    @Override
    protected Num calculate(int index) {
        HighestValueIndicator highestHigh = new HighestValueIndicator(highPriceIndicator, barCount);
        LowestValueIndicator lowestMin = new LowestValueIndicator(lowPriceIndicator, barCount);
        Num highestHighPrice = highestHigh.getValue(index);
        Num lowestLowPrice = lowestMin.getValue(index);
        System.out.println("ghs -- " + indicator.getValue(index).minus(lowestLowPrice).dividedBy(highestHighPrice.minus(lowestLowPrice))
                .multipliedBy(numOf(100)));
        return indicator.getValue(index).minus(lowestLowPrice).dividedBy(highestHighPrice.minus(lowestLowPrice))
                .multipliedBy(numOf(100));
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + " barCount: " + barCount;
    }
}
