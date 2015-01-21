package com.dreamddeath.core.config;

import com.netflix.config.*;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ConfigManagerFactoryTest {
    private static double delta=0.0000001d;

    @Test
    public void testBuildFullName() throws Exception {
        String result = ConfigManagerFactory.buildFullName("prop.int");
        assertEquals(ConfigManagerFactory.CONFIGURATION_PROPERTY_PREFIX+"."+"prop.int",result);
    }

    @Test
    public void testAddConfigurationEntry() throws Exception {
        DynamicIntProperty prop = ConfigManagerFactory.getIntProperty("prop.add",10);
        DynamicIntProperty rawProp = ConfigManagerFactory.getRawIntProperty("prop.add", 20);

        assertEquals(10,prop.get());
        assertEquals(ConfigManagerFactory.buildFullName("prop.add"),prop.getName());
        assertEquals(20,rawProp.get());
        assertEquals("prop.add",rawProp.getName());

        ConfigManagerFactory.addConfigurationEntry("prop.add", 1);
        ConfigManagerFactory.addRawConfigurationEntry("prop.add", 2);
        assertEquals(1, prop.get());
        assertEquals(2, rawProp.get());
    }

    @Test
    public void testGetBooleanProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);
        final boolean firstValue=true;
        final boolean firstRawValue=false;
        final boolean secondValue=false;
        final boolean secondRawValue=true;

        DynamicBooleanProperty prop = ConfigManagerFactory.getBooleanProperty("prop.boolean", firstValue);
        DynamicBooleanProperty rawProp = ConfigManagerFactory.getRawBooleanProperty("prop.boolean", firstRawValue);
        DynamicBooleanProperty callBackProp = ConfigManagerFactory.getBooleanProperty("callback.prop.boolean", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicBooleanProperty callBackRawProp = ConfigManagerFactory.getRawBooleanProperty("callback.prop.boolean", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        assertEquals(ConfigManagerFactory.buildFullName("prop.boolean"),prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        assertEquals("prop.boolean", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.boolean"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get());
        assertEquals("callback.prop.boolean", callBackRawProp.getName());
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

        DynamicIntProperty prop = ConfigManagerFactory.getIntProperty("prop.int", firstValue);
        DynamicIntProperty rawProp = ConfigManagerFactory.getRawIntProperty("prop.int", firstRawValue);
        DynamicIntProperty callBackProp = ConfigManagerFactory.getIntProperty("callback.prop.int", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicIntProperty callBackRawProp = ConfigManagerFactory.getRawIntProperty("callback.prop.int", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        assertEquals(ConfigManagerFactory.buildFullName("prop.int"),prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        assertEquals("prop.int",rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.int"),callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue,callBackRawProp.get());
        assertEquals("callback.prop.int",callBackRawProp.getName());
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

        DynamicLongProperty prop = ConfigManagerFactory.getLongProperty("prop.long", firstValue);
        DynamicLongProperty rawProp = ConfigManagerFactory.getRawLongProperty("prop.long", firstRawValue);
        DynamicLongProperty callBackProp = ConfigManagerFactory.getLongProperty("callback.prop.long", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicLongProperty callBackRawProp = ConfigManagerFactory.getRawLongProperty("callback.prop.long", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        assertEquals(ConfigManagerFactory.buildFullName("prop.long"),prop.getName());
        assertEquals(firstRawValue,rawProp.get());
        assertEquals("prop.long", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.long"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get());
        assertEquals("callback.prop.long", callBackRawProp.getName());
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

        DynamicStringProperty prop = ConfigManagerFactory.getStringProperty("prop.string", firstValue);
        DynamicStringProperty rawProp = ConfigManagerFactory.getRawStringProperty("prop.string", firstRawValue);
        DynamicStringProperty callBackProp = ConfigManagerFactory.getStringProperty("callback.prop.string", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicStringProperty callBackRawProp = ConfigManagerFactory.getRawStringProperty("callback.prop.string", firstRawValue, new Runnable() {
            @Override
            public void run() {
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

        DynamicFloatProperty prop = ConfigManagerFactory.getFloatProperty("prop.float", firstValue);
        DynamicFloatProperty rawProp = ConfigManagerFactory.getRawFloatProperty("prop.float", firstRawValue);
        DynamicFloatProperty callBackProp = ConfigManagerFactory.getFloatProperty("callback.prop.float", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicFloatProperty callBackRawProp = ConfigManagerFactory.getRawFloatProperty("callback.prop.float", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get(),delta);
        assertEquals(ConfigManagerFactory.buildFullName("prop.float"),prop.getName());
        assertEquals(firstRawValue,rawProp.get(),delta);
        assertEquals("prop.float", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.float"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get(),delta);
        assertEquals("callback.prop.float", callBackRawProp.getName());
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

        DynamicDoubleProperty prop = ConfigManagerFactory.getDoubleProperty("prop.double", firstValue);
        DynamicDoubleProperty rawProp = ConfigManagerFactory.getRawDoubleProperty("prop.double", firstRawValue);
        DynamicDoubleProperty callBackProp = ConfigManagerFactory.getDoubleProperty("callback.prop.double", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicDoubleProperty callBackRawProp = ConfigManagerFactory.getRawDoubleProperty("callback.prop.double", firstRawValue, new Runnable() {
            @Override
            public void run() {
                callbackRawCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue, prop.get(), delta);
        assertEquals(ConfigManagerFactory.buildFullName("prop.double"),prop.getName());
        assertEquals(firstRawValue,rawProp.get(),delta);
        assertEquals("prop.double", rawProp.getName());
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        assertEquals(ConfigManagerFactory.buildFullName("callback.prop.double"), callBackProp.getName());
        assertFalse(callbackCalled.get());
        assertEquals(firstRawValue, callBackRawProp.get(),delta);
        assertEquals("callback.prop.double", callBackRawProp.getName());
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
    @Test
    public void testGetContextualProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final AtomicBoolean callbackRawCalled=new AtomicBoolean(false);

        final String firstValue="1st value";
        final String firstRawValue="1st Raw value";
        final String secondValue="2nd value";
        final String secondRawValue="2nd Raw value";

        DynamicContextualProperty<String> prop = ConfigManagerFactory.getContextualProperty("prop.customclass", firstValue);
        DynamicContextualProperty<String> rawProp = ConfigManagerFactory.getRawContextualProperty("prop.customclass", firstRawValue);
        DynamicContextualProperty<String> callBackProp = ConfigManagerFactory.getContextualProperty("callback.prop.customclass", firstValue, new Runnable() {
            @Override
            public void run() {
                callbackCalled.set(true);
            }
        });
        DynamicContextualProperty<String> callBackRawProp = ConfigManagerFactory.getRawContextualProperty("callback.prop.customclass", firstRawValue, new Runnable() {
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

    }
}