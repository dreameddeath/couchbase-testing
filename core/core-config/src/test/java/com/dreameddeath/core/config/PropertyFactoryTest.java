package com.dreameddeath.core.config;

import com.dreameddeath.core.config.impl.*;
import com.dreameddeath.core.exception.config.PropertyValueNotFound;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class PropertyFactoryTest {
    private static double delta=0.0000001d;

    @Test
    public void testBuildFullName() throws Exception {
        String result = PropertyFactory.buildPropName("prop.int",true);
        assertEquals(ConfigManagerFactory.CONFIGURATION_PROPERTY_PREFIX+"."+"prop.int",result);
    }

    @Test
    public void testAddConfigurationEntry() throws Exception {
        IntProperty prop = PropertyFactory.getIntProperty("prop.add",10,true);
        IntProperty rawProp = PropertyFactory.getIntProperty("prop.add", 20);

        assertEquals(10,prop.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.add"), prop.getName());
        assertEquals(20,rawProp.get());
        Assert.assertEquals("prop.add", rawProp.getName());

        ConfigManagerFactory.addConfigurationEntry("prop.add", 1);
        ConfigManagerFactory.addRawConfigurationEntry("prop.add", 2);
        assertEquals(1, prop.get());
        assertEquals(2, rawProp.get());
    }


    @Test(expected = PropertyValueNotFound.class)
    public void testNotFoundProperty() throws Exception {
        PropertyFactory.getStringProperty("toto.not_found",null).getMandatoryValue("Normal error");
    }

        @Test
    public void testGetBooleanProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final boolean firstValue=true;
        final boolean firstRawValue=false;
        final boolean secondValue=false;
        final boolean secondRawValue=true;

        BooleanProperty prop = PropertyFactory.getBooleanProperty("prop.boolean", firstValue,true);
        BooleanProperty rawProp = PropertyFactory.getBooleanProperty("prop.boolean", firstRawValue);
        BooleanProperty callBackProp = PropertyFactory.getBooleanProperty("callback.prop.boolean", firstValue, new PropertyChangedCallback<Boolean>() {
            @Override
            public void onChange(IProperty<Boolean> prop, Boolean oldValue, Boolean newValue) {
                assertEquals(oldValue,firstValue);
                assertEquals(newValue,secondValue);
                callbackCalled.set(true);
            }
        }, true);
        BooleanProperty callBackRawProp = PropertyFactory.getBooleanProperty("callback.prop.boolean", firstRawValue, new PropertyChangedCallback<Boolean>() {
            @Override
            public void onChange(IProperty<Boolean> prop, Boolean oldValue, Boolean newValue) {
                assertEquals(oldValue,firstRawValue);
                assertEquals(newValue,secondRawValue);
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.boolean"), prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        Assert.assertEquals("prop.boolean", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("callback.prop.boolean"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get());
        Assert.assertEquals("callback.prop.boolean", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.boolean", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.boolean", secondRawValue);
        assertEquals(secondValue, prop.get());
        assertEquals(secondRawValue, rawProp.get());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.boolean", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.boolean", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get());
        assertTrue(callbackRawCalled.get());
    }

    @Test
    public void testGetIntProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final int firstValue=10;
        final int firstRawValue=20;
        final int secondValue=1;
        final int secondRawValue=2;

        IntProperty prop = PropertyFactory.getIntProperty("prop.int", firstValue,true);
        IntProperty rawProp = PropertyFactory.getIntProperty("prop.int", firstRawValue);
        IntProperty callBackProp = PropertyFactory.getIntProperty("callback.prop.int", firstValue, new PropertyChangedCallback<Integer>() {
            @Override
            public void onChange(IProperty<Integer> prop, Integer oldValue, Integer newValue) {
                assertEquals(oldValue.intValue(),firstValue);
                assertEquals(newValue.intValue(),secondValue);
                callbackCalled.set(true);
            }
        }

        ,true);
        IntProperty callBackRawProp = PropertyFactory.getIntProperty("callback.prop.int", firstRawValue, new PropertyChangedCallback<Integer>() {
            @Override
            public void onChange(IProperty<Integer> prop, Integer oldValue, Integer newValue) {
                assertEquals(oldValue.intValue(),firstRawValue);
                assertEquals(newValue.intValue(),secondRawValue);
                callbackRawCalled.set(true);
            }
        }
        );


        //Check default value
        assertEquals(firstValue,prop.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.int"), prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        Assert.assertEquals("prop.int", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("callback.prop.int"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue,callBackRawProp.get());
        Assert.assertEquals("callback.prop.int", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.int", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.int", secondRawValue);
        assertEquals(secondValue, prop.get());
        assertEquals(secondRawValue, rawProp.get());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.int", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.int", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get());
        assertTrue(callbackRawCalled.get());
    }

    @Test
    public void testGetLongProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final long firstValue=10L;
        final long firstRawValue=20L;
        final long secondValue=1L;
        final long secondRawValue=2L;

        LongProperty prop = PropertyFactory.getLongProperty("prop.long", firstValue,true);
        LongProperty rawProp = PropertyFactory.getLongProperty("prop.long", firstRawValue);
        LongProperty callBackProp = PropertyFactory.getLongProperty("callback.prop.long", firstValue, new PropertyChangedCallback<Long>() {
            @Override
            public void onChange(IProperty<Long> prop, Long oldValue, Long newValue) {
                assertEquals(oldValue.longValue(),firstValue);
                assertEquals(newValue.longValue(),secondValue);
                callbackCalled.set(true);
            }
        },true);
        LongProperty callBackRawProp = PropertyFactory.getLongProperty("callback.prop.long", firstRawValue, new PropertyChangedCallback<Long>() {
            @Override
            public void onChange(IProperty<Long> prop, Long oldValue, Long newValue) {
                assertEquals(oldValue.longValue(),firstRawValue);
                assertEquals(newValue.longValue(),secondRawValue);
                callbackRawCalled.set(true);
            }
        }
        );


        //Check default value
        assertEquals(firstValue,prop.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.long"), prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        Assert.assertEquals("prop.long", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        Assert.assertEquals(ConfigManagerFactory.buildFullName("callback.prop.long"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get());
        Assert.assertEquals("callback.prop.long", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.long", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.long", secondRawValue);
        assertEquals(secondValue, prop.get());
        assertEquals(secondRawValue, rawProp.get());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.long", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.long", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get());
        assertTrue(callbackRawCalled.get());
    }

    @Test
    public void testGetStringProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final String firstValue="1st Value";
        final String firstRawValue="1st Raw Value";
        final String secondValue="2nd Value";
        final String secondRawValue="2nd Raw Value";

        StringProperty prop = PropertyFactory.getStringProperty("prop.string", firstValue,true);
        StringProperty rawProp = PropertyFactory.getStringProperty("prop.string", firstRawValue);
        StringProperty callBackProp = PropertyFactory.getStringProperty("callback.prop.string", firstValue, new PropertyChangedCallback<String>() {
            @Override
            public void onChange(IProperty<String> prop, String oldValue, String newValue) {
                assertEquals(oldValue,firstValue);
                assertEquals(newValue,secondValue);
                callbackCalled.set(true);
            }
        } ,true);
        StringProperty callBackRawProp = PropertyFactory.getStringProperty("callback.prop.string", firstRawValue, new PropertyChangedCallback<String>() {
            @Override
            public void onChange(IProperty<String> prop, String oldValue, String newValue) {
                assertEquals(oldValue,firstRawValue);
                assertEquals(newValue,secondRawValue);
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        assertEquals(ConfigManagerFactory.buildFullName("prop.string"),prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        assertEquals("prop.string", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.string"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get());
        assertEquals("callback.prop.string", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.string", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.string", secondRawValue);
        assertEquals(secondValue, prop.get());
        assertEquals(secondRawValue, rawProp.get());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.string", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.string", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get());
        assertTrue(callbackRawCalled.get());
    }

    @Test
    public void testGetFloatProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final float firstValue=10.0f;
        final float firstRawValue=20.0f;
        final float secondValue=1.0f;
        final float secondRawValue=2.0f;

        FloatProperty prop = PropertyFactory.getFloatProperty("prop.float", firstValue,true);
        FloatProperty rawProp = PropertyFactory.getFloatProperty("prop.float", firstRawValue);
        FloatProperty callBackProp = PropertyFactory.getFloatProperty("callback.prop.float", firstValue, new PropertyChangedCallback<Float>() {
            @Override
            public void onChange(IProperty<Float> prop, Float oldValue, Float newValue) {
                assertEquals(oldValue,firstValue,0.0001);
                assertEquals(newValue,secondValue,0.0001);
                callbackCalled.set(true);
            }
        },true);
        FloatProperty callBackRawProp = PropertyFactory.getFloatProperty("callback.prop.float", firstRawValue, new PropertyChangedCallback<Float>() {
            @Override
            public void onChange(IProperty<Float> prop, Float oldValue, Float newValue) {
                assertEquals(oldValue,firstRawValue,0.0001);
                assertEquals(newValue,secondRawValue,0.0001);
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get(),delta);
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.float"), prop.getName());
        assertEquals(firstRawValue,rawProp.get(),delta);
        Assert.assertEquals("prop.float", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        Assert.assertEquals(ConfigManagerFactory.buildFullName("callback.prop.float"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get(),delta);
        Assert.assertEquals("callback.prop.float", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.float", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.float", secondRawValue);
        assertEquals(secondValue, prop.get(),delta);
        assertEquals(secondRawValue, rawProp.get(),delta);
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.float", secondValue);
        assertEquals(secondValue, callBackProp.get(),delta);
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.float", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get(),delta);
        assertTrue(callbackRawCalled.get());
    }

    @Test
    public void testGetDoubleProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final double firstValue=10.0;
        final double firstRawValue=20.0;
        final double secondValue=1.0;
        final double secondRawValue=2.0;

        DoubleProperty prop = PropertyFactory.getDoubleProperty("prop.double", firstValue,true);
        DoubleProperty rawProp = PropertyFactory.getDoubleProperty("prop.double", firstRawValue);
        DoubleProperty callBackProp = PropertyFactory.getDoubleProperty("callback.prop.double", firstValue, new PropertyChangedCallback<Double>() {
            @Override
            public void onChange(IProperty<Double> prop, Double oldValue, Double newValue) {
                assertEquals(oldValue,firstValue,0.0001);
                assertEquals(newValue,secondValue,0.0001);
                callbackCalled.set(true);
            }
        },true);
        DoubleProperty callBackRawProp = PropertyFactory.getDoubleProperty("callback.prop.double", firstRawValue, new PropertyChangedCallback<Double>() {
            @Override
            public void onChange(IProperty<Double> prop, Double oldValue, Double newValue) {
                assertEquals(oldValue,firstRawValue,0.0001);
                assertEquals(newValue,secondRawValue,0.0001);
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue, prop.get(), delta);
        Assert.assertEquals(ConfigManagerFactory.buildFullName("prop.double"), prop.getName());
        assertEquals(firstRawValue,rawProp.get(),delta);
        Assert.assertEquals("prop.double", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        Assert.assertEquals(ConfigManagerFactory.buildFullName("callback.prop.double"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get(),delta);
        Assert.assertEquals("callback.prop.double", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.double", secondValue);
        ConfigManagerFactory.addRawConfigurationEntry("prop.double", secondRawValue);
        assertEquals(secondValue, prop.get(),delta);
        assertEquals(secondRawValue, rawProp.get(),delta);
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.double", secondValue);
        assertEquals(secondValue, callBackProp.get(),delta);
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.double", secondRawValue);
        assertEquals(secondRawValue, callBackRawProp.get(),delta);
        assertTrue(callbackRawCalled.get());
    }
    


    public final static String DYNAMIC_FOR_PROD_SUFFIX_STRING = " for Prod";
    public final static String DYNAMIC_FOR_PREPROD_SUFFIX_STRING = " for PreProd";
    public static String buildJson(String baseString){
        return  "[" +
                "{\n"+
                "   \"if\":{\"@domain\":[\"prod\"]},\n"+
                "   \"value\":\""+baseString+ DYNAMIC_FOR_PROD_SUFFIX_STRING +"\""+
                " },"+
                "{\n"+
                "   \"if\":{\""+ConfigManagerFactory.buildFullName("@domain")+"\":[\"preprod\"]},\n"+
                "   \"value\":\""+baseString+ DYNAMIC_FOR_PREPROD_SUFFIX_STRING +"\""+
                " },"+
                "{\n"+
                "   \"value\":\""+baseString+"\"\n"+
                " }"+
                "]";
    }

    /*@Test
    public void testGetContextualProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);

        final String firstValue="1st value";
        final String firstRawValue="1st Raw value";
        final String secondValue="2nd value";
        final String secondRawValue="2nd Raw value";

        ContextualProperty<String> prop = PropertyFactory.getContextualProperty("prop.customclass", firstValue);
        ContextualProperty<String> rawProp = PropertyFactory.getRawContextualProperty("prop.customclass", firstRawValue);
        ContextualProperty<String> callBackProp = PropertyFactory.getContextualProperty("callback.prop.customclass", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        ContextualProperty<String> callBackRawProp = PropertyFactory.getRawContextualProperty("callback.prop.customclass", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.getValue());
        assertEquals(ConfigManagerFactory.buildFullName("prop.customclass"),prop.getName());
        assertEquals(firstRawValue,rawProp.getValue());
        assertEquals("prop.customclass", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.getValue());
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.customclass"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.getValue());
        assertEquals("callback.prop.customclass", callBackRawProp.getName());
        assertFalse(callbackRawCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addConfigurationEntry("prop.customclass", buildJson(secondValue));
        ConfigManagerFactory.addRawConfigurationEntry("prop.customclass", buildJson(secondRawValue));
        assertEquals(secondValue, prop.getValue());
        assertEquals(secondRawValue, rawProp.getValue());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addConfigurationEntry("callback.prop.customclass", buildJson(secondValue));
        assertEquals(secondValue, callBackProp.getValue());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.customclass", buildJson(secondRawValue));
        assertEquals(secondRawValue, callBackRawProp.getValue());
        assertTrue(callbackRawCalled.get());

        //check context parameter with full
        ConfigManagerFactory.addConfigurationEntry("@domain", "preprod");
        assertEquals(secondValue+ DYNAMIC_FOR_PREPROD_SUFFIX_STRING, prop.getValue());
        assertEquals(secondRawValue+ DYNAMIC_FOR_PREPROD_SUFFIX_STRING, rawProp.getValue());
        assertEquals(secondValue+ DYNAMIC_FOR_PREPROD_SUFFIX_STRING, callBackProp.getValue());
        assertEquals(secondRawValue+ DYNAMIC_FOR_PREPROD_SUFFIX_STRING, callBackRawProp.getValue());

        //check context parameter
        ConfigManagerFactory.addRawConfigurationEntry("@domain","prod");
        assertEquals(secondValue+ DYNAMIC_FOR_PROD_SUFFIX_STRING, prop.getValue());
        assertEquals(secondRawValue+ DYNAMIC_FOR_PROD_SUFFIX_STRING, rawProp.getValue());
        assertEquals(secondValue+ DYNAMIC_FOR_PROD_SUFFIX_STRING, callBackProp.getValue());
        assertEquals(secondRawValue+ DYNAMIC_FOR_PROD_SUFFIX_STRING, callBackRawProp.getValue());

    }*/
}