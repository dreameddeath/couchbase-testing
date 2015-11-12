/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.config;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.config.impl.*;
import com.dreameddeath.core.config.spring.ConfigPropertySource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ConfigPropertyFactoryTest {
    private static double delta=0.0000001d;


    @Before
    public void setup(){
        ConfigManagerFactory.cleanLocalProperty();
    }

    @Test
    public void testConfigPropertySource(){
        StringConfigProperty propertyWithDefault=ConfigPropertyFactory.getStringProperty("test.default.for.property.source","defaultValue for Property Source");

        PropertySource source = new ConfigPropertySource("core-config");
        assertTrue(source.containsProperty("test.default.for.property.source"));
        assertEquals("defaultValue for Property Source", source.getProperty("test.default.for.property.source"));
        ConfigManagerFactory.addPersistentConfigurationEntry("test.default.for.property.source", "overriden property");
        assertEquals("overriden property", source.getProperty("test.default.for.property.source"));
    }

    @Test
    public void testAddConfigurationEntry() throws Exception {
        IntConfigProperty prop = ConfigPropertyFactory.getIntProperty("prop.add", 10);

        assertEquals(10, prop.get());
        Assert.assertEquals("prop.add", prop.getName());

        ConfigManagerFactory.addPersistentConfigurationEntry("prop.add", 1);
        assertEquals(1, prop.get());
    }


    @Test
    public void testTemplateNameConfigurationEntry() throws Exception {
        ConfigPropertyWithTemplateName<Integer,IntConfigProperty> propTemplate = ConfigPropertyFactory.getTemplateNameConfigProperty(IntConfigProperty.class,"prop.template.int.base.{coreName}.value", 10);
        ConfigPropertyWithTemplateName<Integer,IntConfigProperty> propTemplateWithRef = ConfigPropertyFactory.getTemplateNameConfigProperty(IntConfigProperty.class,"prop.template.int.{coreName}.value", propTemplate);

        Map<String,String> paramHash = new HashMap<String,String>(){{
            put("coreName","hashParamName");
        }};
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.template.int.base.core-default.value", 1);
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.template.int.base.core-overriden.value", 2);

        assertEquals("prop.template.int.base.simple.value", propTemplate.getProperty("simple").getName());
        assertEquals("prop.template.int.simple.value", propTemplateWithRef.getProperty("simple").getName());
        assertEquals("prop.template.int.base.hashParamName.value", propTemplate.getProperty(paramHash).getName());
        assertEquals("prop.template.int.hashParamName.value", propTemplateWithRef.getProperty(paramHash).getName());

        assertEquals(10, propTemplate.getProperty("simple").get());
        assertEquals(10, propTemplateWithRef.getProperty("simple").get());
        assertEquals(1, propTemplate.getProperty("core-default").get());
        assertEquals(1, propTemplateWithRef.getProperty("core-default").get());
        IntConfigProperty overridenProp = propTemplateWithRef.getProperty("core-overriden");
        assertEquals(2, overridenProp.get());
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.template.int.core-overriden.value", 3);
        assertEquals(3, overridenProp.get());

    }



    @Test
    public void testAddRefDefaultConfigurationEntry() throws Exception {
        final AtomicInteger nbCallbackCalled=new AtomicInteger(0);

        IntConfigProperty propRef = ConfigPropertyFactory.getIntProperty("prop.ref", 10);
        IntConfigProperty prop = ConfigPropertyFactory.getIntProperty("prop.withRef", propRef);
        IntConfigProperty propCallback = ConfigPropertyFactory.getIntProperty("prop.withRefAndCallback", propRef,(modifiedProp,oldValue,newValue)->{
            nbCallbackCalled.incrementAndGet();
            switch (nbCallbackCalled.get()){
                case 1:
                    assertEquals(10,oldValue.intValue());
                    assertEquals(20,newValue.intValue());
                    break;
                case 2:
                    assertEquals(20,oldValue.intValue());
                    assertEquals(30,newValue.intValue());
                    break;
            }
        });

        assertEquals(10, prop.get());
        assertEquals(10, propCallback.get());

        //Set default Value of ref
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.ref", 20);
        assertEquals(20, prop.get());
        assertEquals(20, propCallback.get());
        assertEquals(1,  nbCallbackCalled.get());


        //Set default Value of ref
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.withRef", 30);
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.withRefAndCallback", 30);
        assertEquals(30, prop.get());
        assertEquals(30, propCallback.get());
        assertEquals(2, nbCallbackCalled.get());

        //Set default Value of ref but shouldn't change anything/call any callback
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.ref", 25);
        assertEquals(30, prop.get());
        assertEquals(30, propCallback.get());
        assertEquals(2,  nbCallbackCalled.get());
    }


    @Test(expected = ConfigPropertyValueNotFoundException.class)
    public void testNotFoundProperty() throws Exception {
        try {
            ConfigPropertyFactory.getStringProperty("toto.not_found", (String) null).getMandatoryValue("Normal error <{}>", "toto.not_found");
            fail("Should have raised exception");
        }
        catch(ConfigPropertyValueNotFoundException e){
            assertEquals("The property <toto.not_found> value hasn't been found. The error message :\n" +
                    "Normal error <toto.not_found>",e.getMessage());
        }

        ConfigPropertyFactory.getStringProperty("toto.not_found", (String)null).getMandatoryValue("Normal error");
    }

    @Test
    public void testGetBooleanProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final boolean firstValue=true;
        final boolean secondValue=false;

        BooleanConfigProperty prop = ConfigPropertyFactory.getBooleanProperty("prop.boolean", firstValue);
        BooleanConfigProperty callBackProp = ConfigPropertyFactory.getBooleanProperty("callback.prop.boolean", firstValue, new ConfigPropertyChangedCallback<Boolean>() {
            @Override
            public void onChange(IConfigProperty<Boolean> prop, Boolean oldValue, Boolean newValue) {
                assertEquals(oldValue, firstValue);
                assertEquals(newValue, secondValue);
                callbackCalled.set(true);
            }
        });

        //Check default value
        assertEquals(firstValue,prop.get());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertFalse(callbackCalled.get());
        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.boolean", secondValue);
        assertEquals(secondValue, prop.get());
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.boolean", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
    }

    @Test
    public void testGetIntProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final int firstValue=10;
        final int secondValue=1;

        IntConfigProperty prop = ConfigPropertyFactory.getIntProperty("prop.int", firstValue);
        IntConfigProperty callBackProp = ConfigPropertyFactory.getIntProperty("callback.prop.int", firstValue, new ConfigPropertyChangedCallback<Integer>() {
            @Override
            public void onChange(IConfigProperty<Integer> prop, Integer oldValue, Integer newValue) {
                assertEquals(oldValue.intValue(), firstValue);
                assertEquals(newValue.intValue(), secondValue);
                callbackCalled.set(true);
            }
        });

        //Check default value
        assertEquals(firstValue,prop.get());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertFalse(callbackCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.int", secondValue);
        assertEquals(secondValue, prop.get());
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.int", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
    }

    @Test
    public void testGetLongProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final long firstValue=10L;
        final long secondValue=1L;

        LongConfigProperty prop = ConfigPropertyFactory.getLongProperty("prop.long", firstValue);
        LongConfigProperty callBackProp = ConfigPropertyFactory.getLongProperty("callback.prop.long", firstValue, new ConfigPropertyChangedCallback<Long>() {
            @Override
            public void onChange(IConfigProperty<Long> prop, Long oldValue, Long newValue) {
                assertEquals(oldValue.longValue(), firstValue);
                assertEquals(newValue.longValue(), secondValue);
                callbackCalled.set(true);
            }
        });

        //Check default value
        assertEquals(firstValue,prop.get());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertFalse(callbackCalled.get());

        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.long", secondValue);
        assertEquals(secondValue, prop.get());
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.long", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
    }

    @Test
    public void testGetStringProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final String firstValue="1st Value";
        final String secondValue="2nd Value";

        StringConfigProperty prop = ConfigPropertyFactory.getStringProperty("prop.string", firstValue);
        StringConfigProperty callBackProp = ConfigPropertyFactory.getStringProperty("callback.prop.string", firstValue, new ConfigPropertyChangedCallback<String>() {
            @Override
            public void onChange(IConfigProperty<String> prop, String oldValue, String newValue) {
                assertEquals(oldValue, firstValue);
                assertEquals(newValue, secondValue);
                callbackCalled.set(true);
            }
        });


        //Check default value
        assertEquals(firstValue,prop.get());
        //With callback
        assertEquals(firstValue, callBackProp.get());
        assertFalse(callbackCalled.get());


        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.string", secondValue);
        assertEquals(secondValue, prop.get());
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.string", secondValue);
        assertEquals(secondValue, callBackProp.get());
        assertTrue(callbackCalled.get());
    }


    @Test
    public void testGetStringListProperty() throws Exception {
        final AtomicInteger callbackCalled=new AtomicInteger(0);
        final String firstValueString= "1st Value,1st Value";
        final List<String> firstValueList= Arrays.asList("1st Value","1st Value");
        final String secondValueString="2nd Value,2nd Value";
        final List<String> secondValue=Arrays.asList("2nd Value", "2nd Value");

        StringListConfigProperty prop = ConfigPropertyFactory.getStringListProperty("prop.stringlist", firstValueString);
        StringListConfigProperty callBackProp = ConfigPropertyFactory.getStringListProperty("callback.prop.stringlist", firstValueString, (prop1, oldValue, newValue) -> {
            assertEquals(oldValue, firstValueList);
            assertEquals(newValue, secondValue);
            callbackCalled.incrementAndGet();
        });


        //Check default value
        assertEquals(firstValueList, prop.get());
        //With callback
        assertEquals(firstValueList, callBackProp.get());
        assertEquals(0,callbackCalled.get());
        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.stringlist", secondValueString);
        assertEquals(secondValue, prop.get());
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.stringlist", secondValueString);
        assertEquals(secondValue, callBackProp.get());
        assertEquals(1, callbackCalled.get());
    }

    @Test
    public void testGetFloatProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final float firstValue=10.0f;
        final float secondValue=1.0f;

        FloatConfigProperty prop = ConfigPropertyFactory.getFloatProperty("prop.float", firstValue);
        FloatConfigProperty callBackProp = ConfigPropertyFactory.getFloatProperty("callback.prop.float", firstValue, new ConfigPropertyChangedCallback<Float>() {
            @Override
            public void onChange(IConfigProperty<Float> prop, Float oldValue, Float newValue) {
                assertEquals(oldValue, firstValue, 0.0001);
                assertEquals(newValue, secondValue, 0.0001);
                callbackCalled.set(true);
            }
        });

        //Check default value
        assertEquals(firstValue,prop.get(),delta);
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        assertFalse(callbackCalled.get());
        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.float", secondValue);
        assertEquals(secondValue, prop.get(), delta);
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.float", secondValue);
        assertEquals(secondValue, callBackProp.get(),delta);
        assertTrue(callbackCalled.get());
    }

    @Test
    public void testGetDoubleProperty() throws Exception {
        final AtomicBoolean callbackCalled=new AtomicBoolean(false);
        final double firstValue=10.0;
        final double secondValue=1.0;

        DoubleConfigProperty prop = ConfigPropertyFactory.getDoubleProperty("prop.double", firstValue);
        DoubleConfigProperty callBackProp = ConfigPropertyFactory.getDoubleProperty("callback.prop.double", firstValue, new ConfigPropertyChangedCallback<Double>() {
            @Override
            public void onChange(IConfigProperty<Double> prop, Double oldValue, Double newValue) {
                assertEquals(oldValue, firstValue, 0.0001);
                assertEquals(newValue, secondValue, 0.0001);
                callbackCalled.set(true);
            }
        });

        //Check default value
        assertEquals(firstValue, prop.get(), delta);
        //With callback
        assertEquals(firstValue, callBackProp.get(),delta);
        assertFalse(callbackCalled.get());

        //Set and Check Overridden Property
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.double", secondValue);
        assertEquals(secondValue, prop.get(), delta);
        //Set and Check Overridden Property with callbacks
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.double", secondValue);
        assertEquals(secondValue, callBackProp.get(),delta);
        assertTrue(callbackCalled.get());
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
                "   \"if\":{\"@domain\":[\"preprod\"]},\n"+
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
        ConfigManagerFactory.addPersistentConfigurationEntry("prop.customclass", buildJson(secondValue));
        ConfigManagerFactory.addRawConfigurationEntry("prop.customclass", buildJson(secondRawValue));
        assertEquals(secondValue, prop.getValue());
        assertEquals(secondRawValue, rawProp.getValue());
        //Set and Check Overridden Property with callbacks 
        ConfigManagerFactory.addPersistentConfigurationEntry("callback.prop.customclass", buildJson(secondValue));
        assertEquals(secondValue, callBackProp.getValue());
        assertTrue(callbackCalled.get());
        ConfigManagerFactory.addRawConfigurationEntry("callback.prop.customclass", buildJson(secondRawValue));
        assertEquals(secondRawValue, callBackRawProp.getValue());
        assertTrue(callbackRawCalled.get());

        //check context parameter with full
        ConfigManagerFactory.addPersistentConfigurationEntry("@domain", "preprod");
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