package com.hyogasa.hgs2.ta4;

import java.io.Serializable;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface Tick extends Serializable {

    /**
     * @return the open price of the period
     */
    Decimal getOpenPrice();

    /**
     * @return the min price of the period
     */
    Decimal getMinPrice();

    /**
     * @return the max price of the period
     */
    Decimal getMaxPrice();

    /**
     * @return the close price of the period
     */
    Decimal getClosePrice();

    /**
     * @return the whole traded volume in the period
     */
    Decimal getVolume();

    /**
     * @return the number of trades in the period
     */
    int getTrades();

    /**
     * @return the whole traded amount of the period
     */
    Decimal getAmount();

    /**
     * @return the time period of the tick
     */
    Duration getTimePeriod();

    /**
     * @return the begin timestamp of the tick period
     */
    ZonedDateTime getBeginTime();

    /**
     * @return the end timestamp of the tick period
     */
    ZonedDateTime getEndTime();

    /**
     * @param timestamp a timestamp
     * @return true if the provided timestamp is between the begin time and the end time of the current period, false otherwise
     */
    default boolean inPeriod(ZonedDateTime timestamp) {
        return timestamp != null
                && !timestamp.isBefore(getBeginTime())
                && timestamp.isBefore(getEndTime());
    }

    /**
     * @return a human-friendly string of the end timestamp
     */
    default String getDateName() {
        return getEndTime().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * @return a even more human-friendly string of the end timestamp
     */
    default String getSimpleDateName() {
        return getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * @return true if this is a bearish tick, false otherwise
     */
    default boolean isBearish() {
        Decimal openPrice = getOpenPrice();
        Decimal closePrice = getClosePrice();
        return (openPrice != null) && (closePrice != null) && closePrice.isLessThan(openPrice);
    }

    /**
     * @return true if this is a bullish tick, false otherwise
     */
    default boolean isBullish() {
        Decimal openPrice = getOpenPrice();
        Decimal closePrice = getClosePrice();
        return (openPrice != null) && (closePrice != null) && openPrice.isLessThan(closePrice);
    }

    /**
     * Adds a trade at the end of tick period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     */
    default void addTrade(double tradeVolume, double tradePrice) {
        addTrade(Decimal.valueOf(tradeVolume), Decimal.valueOf(tradePrice));
    }

    /**
     * Adds a trade at the end of tick period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     */
    default void addTrade(String tradeVolume, String tradePrice) {
        addTrade(Decimal.valueOf(tradeVolume), Decimal.valueOf(tradePrice));
    }

    /**
     * Adds a trade at the end of tick period.
     * @param tradeVolume the traded volume
     * @param tradePrice the price
     */
    void addTrade(Decimal tradeVolume, Decimal tradePrice);
}
