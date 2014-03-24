package com.dreammdeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;


public class RatingContextCdrBucket{
    private String _uid; //Bucket Unique Id
    private Integer _bucketSize; //Size estimation
    private Integer _nbCdrs;

}