package com.dreammdeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingContextKey{
    private String _key;
    private String _type;
    private DateTime _startDate;
    private DateTime _endDate;

}