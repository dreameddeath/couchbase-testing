package com.dreammdeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;


public class RatingContextRatePlan {
    private String _ratePlanCode;
    private DateTime _startDate;
    private DateTime _startEndDate;
}