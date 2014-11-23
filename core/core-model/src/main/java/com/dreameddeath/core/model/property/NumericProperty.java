package com.dreameddeath.core.model.property;

/**
 * Created by ceaj8230 on 14/08/2014.
 */
public interface NumericProperty<T extends Number> extends Property<T> {
    public NumericProperty<T> inc(Number byVal);
    public NumericProperty<T> dec(Number byVal);
    public NumericProperty<T> mul(Number byVal);
    public NumericProperty<T> div(Number byVal);
}
