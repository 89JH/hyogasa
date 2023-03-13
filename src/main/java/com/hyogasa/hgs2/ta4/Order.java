package com.hyogasa.hgs2.ta4;


import java.io.Serializable;

public class Order implements Serializable {

    private static final long serialVersionUID = -905474949010114150L;

    /**
     * The type of an {@link Order order}.
     * <p>
     * A BUY corresponds to a <i>BID</i> order.<p>
     * A SELL corresponds to an <i>ASK</i> order.
     */
    public enum OrderType {

        BUY {
            @Override
            public OrderType complementType() {
                return SELL;
            }
        },
        SELL {
            @Override
            public OrderType complementType() {
                return BUY;
            }
        };

        /**
         * @return the complementary order type
         */
        public abstract OrderType complementType();
    }

    /** Type of the order */
    private OrderType type;

    /** The index the order was executed */
    private int index;

    /** The price for the order */
    private Decimal price = Decimal.NaN;

    /** The amount to be (or that was) ordered */
    private Decimal amount = Decimal.NaN;

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     */
    protected Order(int index, OrderType type) {
        this.type = type;
        this.index = index;
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     * @param price the price for the order
     * @param amount the amount to be (or that was) ordered
     */
    protected Order(int index, OrderType type, Decimal price, Decimal amount) {
        this(index, type);
        this.price = price;
        this.amount = amount;
    }

    /**
     * @return the type of the order (BUY or SELL)
     */
    public OrderType getType() {
        return type;
    }

    /**
     * @return true if this is a BUY order, false otherwise
     */
    public boolean isBuy() {
        return type == OrderType.BUY;
    }

    /**
     * @return true if this is a SELL order, false otherwise
     */
    public boolean isSell() {
        return type == OrderType.SELL;
    }

    /**
     * @return the index the order is executed
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the price for the order
     */
    public Decimal getPrice() {
        return price;
    }

    /**
     * @return the amount to be (or that was) ordered
     */
    public Decimal getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 29 * hash + this.index;
        hash = 29 * hash + (this.price != null ? this.price.hashCode() : 0);
        hash = 29 * hash + (this.amount != null ? this.amount.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (this.price != other.price && (this.price == null || !this.price.equals(other.price))) {
            return false;
        }
        if (this.amount != other.amount && (this.amount == null || !this.amount.equals(other.amount))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Order{" + "type=" + type + ", index=" + index + ", price=" + price + ", amount=" + amount + '}';
    }

    /**
     * @param index the index the order is executed
     * @return a BUY order
     */
    public static Order buyAt(int index) {
        return new Order(index, OrderType.BUY);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, Decimal price, Decimal amount) {
        return new Order(index, OrderType.BUY, price, amount);
    }

    /**
     * @param index the index the order is executed
     * @return a SELL order
     */
    public static Order sellAt(int index) {
        return new Order(index, OrderType.SELL);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) sold
     * @return a SELL order
     */
    public static Order sellAt(int index, Decimal price, Decimal amount) {
        return new Order(index, OrderType.SELL, price, amount);
    }
}