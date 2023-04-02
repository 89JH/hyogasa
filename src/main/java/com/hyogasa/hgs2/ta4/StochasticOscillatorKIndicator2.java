package com.hyogasa.hgs2.ta4;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.num.Num;

public class StochasticOscillatorKIndicator2 extends CachedIndicator<Num> {
    private final Indicator<Num> indicator;

    public StochasticOscillatorKIndicator2(GitHubStochastic k) {
        this(new SMAIndicator(k, 3));
    }

    public StochasticOscillatorKIndicator2(Indicator<Num> indicator) {
        super(indicator);
        this.indicator = indicator;
    }

    @Override
    protected Num calculate(int index) {
        System.out.println("stochK -- " + indicator.getValue(index));
        return indicator.getValue(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
