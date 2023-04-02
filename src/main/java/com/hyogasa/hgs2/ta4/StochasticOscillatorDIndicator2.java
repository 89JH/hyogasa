package com.hyogasa.hgs2.ta4;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.num.Num;

public class StochasticOscillatorDIndicator2 extends CachedIndicator<Num> {
    private Indicator<Num> indicator;

    public StochasticOscillatorDIndicator2(StochasticOscillatorKIndicator2 k) {
        this(new SMAIndicator(k, 3));
    }

    public StochasticOscillatorDIndicator2(Indicator<Num> indicator) {
        super(indicator);
        this.indicator = indicator;
    }

    @Override
    protected Num calculate(int index) {
        System.out.println("stochD -- " + indicator.getValue(index));
        return indicator.getValue(index);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + indicator;
    }
}
