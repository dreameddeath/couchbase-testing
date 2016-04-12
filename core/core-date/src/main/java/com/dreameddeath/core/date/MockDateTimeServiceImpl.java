package com.dreameddeath.core.date;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

/**
 * Created by Christophe Jeunesse on 11/04/2016.
 */
public class MockDateTimeServiceImpl implements IDateTimeService {
    private Calculator calculator;

    public MockDateTimeServiceImpl() {
        this(Calculator.standardCalculator());
    }

    public MockDateTimeServiceImpl(Calculator calculator){
        this.calculator = calculator;
    }

    public void setCalculator(Calculator calculator){
        this.calculator = calculator;
    }

    @Override
    public DateTime getCurrentDate() {
        return calculator.getCurrentDate();
    }

    @Override
    public DateTime now() {
        return DateTime.now();
    }

    @Override
    public DateTime min() {
        return MIN_TIME;
    }

    @Override
    public DateTime max() {
        return MAX_TIME;
    }

    public interface Calculator {
        DateTime getCurrentDate();

        static Calculator fixedCalculator(DateTime referenceDate){
            return () -> referenceDate;
        }

        static Calculator fixedCalculator(String referenceDate){
            return fixedCalculator(new DateTime(referenceDate));
        }


        static Calculator standardCalculator(){
            return DateTime::now;
        }

        static Calculator offsetCalculator(DateTime referenceDate){
            return offsetCalculator(new Duration(DateTime.now(),referenceDate));
        }

        static Calculator offsetCalculator(ReadableDuration duration){
            return () -> DateTime.now().plus(duration);
        }
    }
}
