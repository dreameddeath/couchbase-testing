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

package com.test;


import com.dreameddeath.compile.tools.annotation.processor.testing.TestingAnnotation;

public class GenericsClassInfoTest{
    public class TestExemple{}
    public interface TestRoot<TOTO,TUTU>{}
    public interface TestRoot2<A,B>{}
    public interface TestSubRoot<TITI,TATA> extends TestRoot<TATA,TITI>{}
    public class TestClass implements TestRoot<String,Integer>{}
    public class TestClassSubInterface implements TestSubRoot<String,Double>{}
    public class TestRootClass<TOTO extends Number> implements TestRoot<Integer,TOTO>,TestRoot2<TOTO,TOTO>{}
    public class TestSubClass extends TestRootClass<Double>{}
    public class TestSubClassIntermediateComplex<TEST> implements TestSubRoot<Integer,TEST>{}
    public class TestSubClassComplex extends TestSubClassIntermediateComplex<TestExemple>{}
}