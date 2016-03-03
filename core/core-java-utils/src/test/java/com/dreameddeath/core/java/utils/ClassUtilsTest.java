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

package com.dreameddeath.core.java.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 03/03/2016.
 */
public class ClassUtilsTest {

    public interface TestRoot<TOTO,TUTU>{}
    public interface TestRoot2<A,B>{}
    public interface TestSubRoot<TITI,TATA> extends TestRoot<TATA,TITI>{}
    public class TestClass implements TestRoot<String,Integer>{}
    public class TestClassSubInterface implements TestSubRoot<String,Double>{}
    public class TestRootClass<TOTO extends Number> implements TestRoot<Integer,TOTO>,TestRoot2<TOTO,TOTO>{}
    public class TestSubClass extends TestRootClass<Double>{}


    @Test
    public void testGetEffectiveGenericType() throws Exception {
        try{
            ClassUtils.getEffectiveGenericType(TestClass.class,TestRoot2.class,1);
            fail("Should have raised an error");
        }
        catch(RuntimeException e){
            //Ok
        }
        //Tests around TestClass
        assertEquals(String.class,ClassUtils.getEffectiveGenericType(TestClass.class,TestRoot.class,0));
        assertEquals(Integer.class,ClassUtils.getEffectiveGenericType(TestClass.class,TestRoot.class,1));
        //Tests around TestClassSubInterface
        assertEquals(String.class,ClassUtils.getEffectiveGenericType(TestClassSubInterface.class,TestSubRoot.class,0));
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestClassSubInterface.class,TestSubRoot.class,1));
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestClassSubInterface.class,TestRoot.class,0));
        assertEquals(String.class,ClassUtils.getEffectiveGenericType(TestClassSubInterface.class,TestRoot.class,1));
        //Tests around TestRootClass
        assertEquals(Integer.class,ClassUtils.getEffectiveGenericType(TestRootClass.class,TestRoot.class,0));
        assertNull(ClassUtils.getEffectiveGenericType(TestRootClass.class,TestRoot.class,1));
        assertNull(ClassUtils.getEffectiveGenericType(TestRootClass.class,TestRoot2.class,0));
        assertNull(ClassUtils.getEffectiveGenericType(TestRootClass.class,TestRoot2.class,1));
        //Tests around TestSubClass
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestSubClass.class,TestRootClass.class,0));
        assertEquals(Integer.class,ClassUtils.getEffectiveGenericType(TestSubClass.class,TestRoot.class,0));
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestSubClass.class,TestRoot.class,1));
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestSubClass.class,TestRoot2.class,0));
        assertEquals(Double.class,ClassUtils.getEffectiveGenericType(TestSubClass.class,TestRoot2.class,1));
    }
}