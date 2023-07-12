/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.testbed.GenericConsumer;
import org.apache.commons.lang3.reflect.testbed.GenericParent;
import org.apache.commons.lang3.reflect.testbed.StringParameterizedChild;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.lang3.ClassUtils}.
 */
@SuppressWarnings("boxing") // JUnit4 does not support primitive equality testing apart from long
public class ClassUtilsTest  {

    private static class CX implements IB, IA, IE {
        // empty
    }

    private static class CY extends CX implements IB, IC {
        // empty
    }

    private interface IA {
        // empty
    }

    private interface IB {
        // empty
    }

    private interface IC extends ID, IE {
        // empty
    }

    private interface ID {
        // empty
    }

    private interface IE extends IF {
        // empty
    }

    private interface IF {
        // empty
    }

    private static class Inner {
        private class DeeplyNested {
            // empty
        }
    }

    private void assertGetClassReturnsClass( final Class<?> c ) throws Exception {
        assertEquals( c, ClassUtils.getClass( c.getName() ) );
    }

    private void assertGetClassThrowsClassNotFound( final String className ) throws Exception {
        assertGetClassThrowsException( className, ClassNotFoundException.class );
    }

    private void assertGetClassThrowsException( final String className, final Class<?> exceptionType ) throws Exception {
        try {
            ClassUtils.getClass( className );
            fail( "ClassUtils.getClass() should fail with an exception of type " + exceptionType.getName() + " when given class name \"" + className + "\"." );
        } catch( final Exception e ) {
            assertTrue( exceptionType.isAssignableFrom( e.getClass() ) );
        }
    }

    private void assertGetClassThrowsNullPointerException( final String className ) throws Exception {
        assertGetClassThrowsException( className, NullPointerException.class );
    }

    @Test
    public void test_convertClassesToClassNames_List() {
        final List<Class<?>> list = new ArrayList<>();
        List<String> result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(0, result.size());

        list.add(String.class);
        list.add(null);
        list.add(Object.class);
        result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(3, result.size());
        assertEquals("java.lang.String", result.get(0));
        assertNull(result.get(1));
        assertEquals("java.lang.Object", result.get(2));

        @SuppressWarnings("unchecked") // test what happens when non-generic code adds wrong type of element
        final List<Object> olist = (List<Object>) (List<?>) list;
        olist.add(new Object());
        try {
            ClassUtils.convertClassesToClassNames(list);
            fail("Should not have been able to convert list");
        } catch (final ClassCastException expected) {
            // empty
    }
        assertNull(ClassUtils.convertClassesToClassNames(null));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_convertClassNamesToClasses_List() {
        final List<String> list = new ArrayList<>();
        List<Class<?>> result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(0, result.size());

        list.add("java.lang.String");
        list.add("java.lang.xxx");
        list.add("java.lang.Object");
        result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(3, result.size());
        assertEquals(String.class, result.get(0));
        assertNull(result.get(1));
        assertEquals(Object.class, result.get(2));

        @SuppressWarnings("unchecked") // test what happens when non-generic code adds wrong type of element
        final List<Object> olist = (List<Object>) (List<?>) list;
        olist.add(new Object());
        try {
            ClassUtils.convertClassNamesToClasses(list);
            fail("Should not have been able to convert list");
        } catch (final ClassCastException expected) {
            // empty
        }
        assertNull(ClassUtils.convertClassNamesToClasses(null));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_getAbbreviatedName_Class() {
        assertEquals("", ClassUtils.getAbbreviatedName((Class<?>)null, 1));
        assertEquals("j.l.String", ClassUtils.getAbbreviatedName(String.class, 1));
        assertEquals("j.l.String", ClassUtils.getAbbreviatedName(String.class, 5));
        assertEquals("j.lang.String", ClassUtils.getAbbreviatedName(String.class, 13));
        assertEquals("j.lang.String", ClassUtils.getAbbreviatedName(String.class, 15));
        assertEquals("java.lang.String", ClassUtils.getAbbreviatedName(String.class, 20));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getAbbreviatedName_Class_NegativeLen() {
        ClassUtils.getAbbreviatedName(String.class, -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_getAbbreviatedName_Class_ZeroLen() {
        ClassUtils.getAbbreviatedName(String.class, 0);
    }

    @Test
    public void test_getAbbreviatedName_String() {
        assertEquals("", ClassUtils.getAbbreviatedName((String) null, 1));
        assertEquals("WithoutPackage", ClassUtils.getAbbreviatedName("WithoutPackage", 1));
        assertEquals("j.l.String", ClassUtils.getAbbreviatedName("java.lang.String", 1));
    }

    @Test
    public void test_getAllInterfaces_Class() {
        final List<?> list = ClassUtils.getAllInterfaces(CY.class);
        assertEquals(6, list.size());
        assertEquals(IB.class, list.get(0));
        assertEquals(IC.class, list.get(1));
        assertEquals(ID.class, list.get(2));
        assertEquals(IE.class, list.get(3));
        assertEquals(IF.class, list.get(4));
        assertEquals(IA.class, list.get(5));

        assertNull(ClassUtils.getAllInterfaces(null));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_getAllSuperclasses_Class() {
        final List<?> list = ClassUtils.getAllSuperclasses(CY.class);
        assertEquals(2, list.size());
        assertEquals(CX.class, list.get(0));
        assertEquals(Object.class, list.get(1));

        assertNull(ClassUtils.getAllSuperclasses(null));
    }

    @Test
    public void test_getCanonicalName_Class() {
        assertEquals("org.apache.commons.lang3.ClassUtils", ClassUtils.getCanonicalName(ClassUtils.class));
        assertEquals("java.util.Map.Entry", ClassUtils.getCanonicalName(Map.Entry.class));
        assertEquals("", ClassUtils.getCanonicalName((Class<?>) null));

        assertEquals("java.lang.String[]", ClassUtils.getCanonicalName(String[].class));
        assertEquals("java.util.Map.Entry[]", ClassUtils.getCanonicalName(Map.Entry[].class));

        // Primitives
        assertEquals("boolean", ClassUtils.getCanonicalName(boolean.class));
        assertEquals("byte", ClassUtils.getCanonicalName(byte.class));
        assertEquals("char", ClassUtils.getCanonicalName(char.class));
        assertEquals("short", ClassUtils.getCanonicalName(short.class));
        assertEquals("int", ClassUtils.getCanonicalName(int.class));
        assertEquals("long", ClassUtils.getCanonicalName(long.class));
        assertEquals("float", ClassUtils.getCanonicalName(float.class));
        assertEquals("double", ClassUtils.getCanonicalName(double.class));

        // Primitive Arrays
        assertEquals("boolean[]", ClassUtils.getCanonicalName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getCanonicalName(byte[].class));
        assertEquals("char[]", ClassUtils.getCanonicalName(char[].class));
        assertEquals("short[]", ClassUtils.getCanonicalName(short[].class));
        assertEquals("int[]", ClassUtils.getCanonicalName(int[].class));
        assertEquals("long[]", ClassUtils.getCanonicalName(long[].class));
        assertEquals("float[]", ClassUtils.getCanonicalName(float[].class));
        assertEquals("double[]", ClassUtils.getCanonicalName(double[].class));

        // Arrays of arrays of ...
        assertEquals("java.lang.String[][]", ClassUtils.getCanonicalName(String[][].class));
        assertEquals("java.lang.String[][][]", ClassUtils.getCanonicalName(String[][][].class));
        assertEquals("java.lang.String[][][][]", ClassUtils.getCanonicalName(String[][][][].class));

        // Inner types
        class Named {
            // empty
        }
        assertEquals(StringUtils.EMPTY, ClassUtils.getCanonicalName(new Object() {
            // empty
        }.getClass()));
        assertEquals(StringUtils.EMPTY, ClassUtils.getCanonicalName(Named.class));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest.Inner", ClassUtils.getCanonicalName(Inner.class));
    }

    @Test
    public void test_getCanonicalName_Class_String() {
        assertEquals("org.apache.commons.lang3.ClassUtils", ClassUtils.getCanonicalName(ClassUtils.class, "X"));
        assertEquals("java.util.Map.Entry", ClassUtils.getCanonicalName(Map.Entry.class, "X"));
        assertEquals("X", ClassUtils.getCanonicalName((Class<?>) null, "X"));

        assertEquals("java.lang.String[]", ClassUtils.getCanonicalName(String[].class, "X"));
        assertEquals("java.util.Map.Entry[]", ClassUtils.getCanonicalName(Map.Entry[].class, "X"));

        // Primitives
        assertEquals("boolean", ClassUtils.getCanonicalName(boolean.class, "X"));
        assertEquals("byte", ClassUtils.getCanonicalName(byte.class, "X"));
        assertEquals("char", ClassUtils.getCanonicalName(char.class, "X"));
        assertEquals("short", ClassUtils.getCanonicalName(short.class, "X"));
        assertEquals("int", ClassUtils.getCanonicalName(int.class, "X"));
        assertEquals("long", ClassUtils.getCanonicalName(long.class, "X"));
        assertEquals("float", ClassUtils.getCanonicalName(float.class, "X"));
        assertEquals("double", ClassUtils.getCanonicalName(double.class, "X"));

        // Primitive Arrays
        assertEquals("boolean[]", ClassUtils.getCanonicalName(boolean[].class, "X"));
        assertEquals("byte[]", ClassUtils.getCanonicalName(byte[].class, "X"));
        assertEquals("char[]", ClassUtils.getCanonicalName(char[].class, "X"));
        assertEquals("short[]", ClassUtils.getCanonicalName(short[].class, "X"));
        assertEquals("int[]", ClassUtils.getCanonicalName(int[].class, "X"));
        assertEquals("long[]", ClassUtils.getCanonicalName(long[].class, "X"));
        assertEquals("float[]", ClassUtils.getCanonicalName(float[].class, "X"));
        assertEquals("double[]", ClassUtils.getCanonicalName(double[].class, "X"));

        // Arrays of arrays of ...
        assertEquals("java.lang.String[][]", ClassUtils.getCanonicalName(String[][].class, "X"));
        assertEquals("java.lang.String[][][]", ClassUtils.getCanonicalName(String[][][].class, "X"));
        assertEquals("java.lang.String[][][][]", ClassUtils.getCanonicalName(String[][][][].class, "X"));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("X", ClassUtils.getCanonicalName(new Object() {
            // empty
        }.getClass(), "X"));
        assertEquals("X", ClassUtils.getCanonicalName(Named.class, "X"));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest.Inner", ClassUtils.getCanonicalName(Inner.class, "X"));
    }

    @Test
    public void test_getName_Class() {
        assertEquals("org.apache.commons.lang3.ClassUtils", ClassUtils.getName(ClassUtils.class));
        assertEquals("java.util.Map$Entry", ClassUtils.getName(Map.Entry.class));
        assertEquals("", ClassUtils.getName((Class<?>) null));

        assertEquals("[Ljava.lang.String;", ClassUtils.getName(String[].class));
        assertEquals("[Ljava.util.Map$Entry;", ClassUtils.getName(Map.Entry[].class));

        // Primitives
        assertEquals("boolean", ClassUtils.getName(boolean.class));
        assertEquals("byte", ClassUtils.getName(byte.class));
        assertEquals("char", ClassUtils.getName(char.class));
        assertEquals("short", ClassUtils.getName(short.class));
        assertEquals("int", ClassUtils.getName(int.class));
        assertEquals("long", ClassUtils.getName(long.class));
        assertEquals("float", ClassUtils.getName(float.class));
        assertEquals("double", ClassUtils.getName(double.class));

        // Primitive Arrays
        assertEquals("[Z", ClassUtils.getName(boolean[].class));
        assertEquals("[B", ClassUtils.getName(byte[].class));
        assertEquals("[C", ClassUtils.getName(char[].class));
        assertEquals("[S", ClassUtils.getName(short[].class));
        assertEquals("[I", ClassUtils.getName(int[].class));
        assertEquals("[J", ClassUtils.getName(long[].class));
        assertEquals("[F", ClassUtils.getName(float[].class));
        assertEquals("[D", ClassUtils.getName(double[].class));

        // Arrays of arrays of ...
        assertEquals("[[Ljava.lang.String;", ClassUtils.getName(String[][].class));
        assertEquals("[[[Ljava.lang.String;", ClassUtils.getName(String[][][].class));
        assertEquals("[[[[Ljava.lang.String;", ClassUtils.getName(String[][][][].class));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$3", ClassUtils.getName(new Object() {
            // empty
        }.getClass()));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$3Named", ClassUtils.getName(Named.class));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$Inner", ClassUtils.getName(Inner.class));
    }

    @Test
    public void test_getName_Object() {
        assertEquals("org.apache.commons.lang3.ClassUtils", ClassUtils.getName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$Inner", ClassUtils.getName(new Inner(), "<null>"));
        assertEquals("java.lang.String", ClassUtils.getName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getName(null, "<null>"));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$4", ClassUtils.getName(new Object() {
            // empty
        }, "<null>"));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$4Named", ClassUtils.getName(new Named(), "<null>"));
        assertEquals("org.apache.commons.lang3.ClassUtilsTest$Inner", ClassUtils.getName(new Inner(), "<null>"));
    }

    @Test
    public void test_getPackageCanonicalName_Class() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[].class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils[][].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[][].class));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Object() {
            // empty
        }.getClass()));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(Named.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(Inner.class));
    }

    @Test
    public void test_getPackageCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getPackageCanonicalName(null, "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0][0], "<null>"));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Object() {
            // empty
        }, "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Named(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(new Inner(), "<null>"));
    }

    @Test
    public void test_getPackageCanonicalName_String() {
        assertEquals("org.apache.commons.lang3",
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils"));
        assertEquals("org.apache.commons.lang3",
            ClassUtils.getPackageCanonicalName("[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("org.apache.commons.lang3",
            ClassUtils.getPackageCanonicalName("[[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("org.apache.commons.lang3",
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils[]"));
        assertEquals("org.apache.commons.lang3",
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtils[][]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[][]"));

        // Inner types
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$6"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$5Named"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName("org.apache.commons.lang3.ClassUtilsTest$Inner"));
    }

    @Test
    public void test_getPackageName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));

        // LANG-535
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));

        // Primitive Arrays
        assertEquals("", ClassUtils.getPackageName(boolean[].class));
        assertEquals("", ClassUtils.getPackageName(byte[].class));
        assertEquals("", ClassUtils.getPackageName(char[].class));
        assertEquals("", ClassUtils.getPackageName(short[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
        assertEquals("", ClassUtils.getPackageName(long[].class));
        assertEquals("", ClassUtils.getPackageName(float[].class));
        assertEquals("", ClassUtils.getPackageName(double[].class));

        // Arrays of arrays of ...
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][][].class));

        // On-the-fly types
        class Named {
            // empty
        }
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new Object() {
            // empty
        }.getClass()));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(Named.class));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_getPackageName_Object() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(new Inner(), "<null>"));
        assertEquals("<null>", ClassUtils.getPackageName(null, "<null>"));
    }

    @Test
    public void test_getPackageName_String() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(ClassUtils.class.getName()));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
    }

    @Test
    public void test_getShortCanonicalName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(ClassUtils[].class));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(ClassUtils[][].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(int[][].class));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("ClassUtilsTest.8", ClassUtils.getShortCanonicalName(new Object() {
            // empty
        }.getClass()));
        assertEquals("ClassUtilsTest.8Named", ClassUtils.getShortCanonicalName(Named.class));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName(Inner.class));
    }

    @Test
    public void test_getShortCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getShortCanonicalName(null, "<null>"));
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], "<null>"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(new int[0][0], "<null>"));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("ClassUtilsTest.9", ClassUtils.getShortCanonicalName(new Object() {
            // empty
        }, "<null>"));
        assertEquals("ClassUtilsTest.9Named", ClassUtils.getShortCanonicalName(new Named(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName(new Inner(), "<null>"));
    }

    @Test
    public void test_getShortCanonicalName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("[[Lorg.apache.commons.lang3.ClassUtils;"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils[]"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtils[][]"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("int[]"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("int[][]"));

        // Inner types
        assertEquals("ClassUtilsTest.6", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$6"));
        assertEquals("ClassUtilsTest.5Named", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$5Named"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortCanonicalName("org.apache.commons.lang3.ClassUtilsTest$Inner"));
    }

    @Test
    public void test_getShortClassName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));

        // LANG-535
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("Map.Entry[]", ClassUtils.getShortClassName(Map.Entry[].class));

        // Primitives
        assertEquals("boolean", ClassUtils.getShortClassName(boolean.class));
        assertEquals("byte", ClassUtils.getShortClassName(byte.class));
        assertEquals("char", ClassUtils.getShortClassName(char.class));
        assertEquals("short", ClassUtils.getShortClassName(short.class));
        assertEquals("int", ClassUtils.getShortClassName(int.class));
        assertEquals("long", ClassUtils.getShortClassName(long.class));
        assertEquals("float", ClassUtils.getShortClassName(float.class));
        assertEquals("double", ClassUtils.getShortClassName(double.class));

        // Primitive Arrays
        assertEquals("boolean[]", ClassUtils.getShortClassName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getShortClassName(byte[].class));
        assertEquals("char[]", ClassUtils.getShortClassName(char[].class));
        assertEquals("short[]", ClassUtils.getShortClassName(short[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("long[]", ClassUtils.getShortClassName(long[].class));
        assertEquals("float[]", ClassUtils.getShortClassName(float[].class));
        assertEquals("double[]", ClassUtils.getShortClassName(double[].class));

        // Arrays of arrays of ...
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("String[][][]", ClassUtils.getShortClassName(String[][][].class));
        assertEquals("String[][][][]", ClassUtils.getShortClassName(String[][][][].class));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("ClassUtilsTest.10", ClassUtils.getShortClassName(new Object() {
            // empty
        }.getClass()));
        assertEquals("ClassUtilsTest.10Named", ClassUtils.getShortClassName(Named.class));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(Inner.class));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_getShortClassName_Object() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(new Inner(), "<null>"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getShortClassName(null, "<null>"));

        // Inner types
        class Named {
            // empty
        }
        assertEquals("ClassUtilsTest.11", ClassUtils.getShortClassName(new Object() {
            // empty
        }, "<null>"));
        assertEquals("ClassUtilsTest.11Named", ClassUtils.getShortClassName(new Named(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(new Inner(), "<null>"));
    }

    @Test
    public void test_getShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
    }

    @Test
    public void test_getSimpleName_Class() {
        assertEquals("ClassUtils", ClassUtils.getSimpleName(ClassUtils.class));
        assertEquals("Entry", ClassUtils.getSimpleName(Map.Entry.class));
        assertEquals("", ClassUtils.getSimpleName(null));

        // LANG-535
        assertEquals("String[]", ClassUtils.getSimpleName(String[].class));
        assertEquals("Entry[]", ClassUtils.getSimpleName(Map.Entry[].class));

        // Primitives
        assertEquals("boolean", ClassUtils.getSimpleName(boolean.class));
        assertEquals("byte", ClassUtils.getSimpleName(byte.class));
        assertEquals("char", ClassUtils.getSimpleName(char.class));
        assertEquals("short", ClassUtils.getSimpleName(short.class));
        assertEquals("int", ClassUtils.getSimpleName(int.class));
        assertEquals("long", ClassUtils.getSimpleName(long.class));
        assertEquals("float", ClassUtils.getSimpleName(float.class));
        assertEquals("double", ClassUtils.getSimpleName(double.class));

        // Primitive Arrays
        assertEquals("boolean[]", ClassUtils.getSimpleName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getSimpleName(byte[].class));
        assertEquals("char[]", ClassUtils.getSimpleName(char[].class));
        assertEquals("short[]", ClassUtils.getSimpleName(short[].class));
        assertEquals("int[]", ClassUtils.getSimpleName(int[].class));
        assertEquals("long[]", ClassUtils.getSimpleName(long[].class));
        assertEquals("float[]", ClassUtils.getSimpleName(float[].class));
        assertEquals("double[]", ClassUtils.getSimpleName(double[].class));

        // Arrays of arrays of ...
        assertEquals("String[][]", ClassUtils.getSimpleName(String[][].class));
        assertEquals("String[][][]", ClassUtils.getSimpleName(String[][][].class));
        assertEquals("String[][][][]", ClassUtils.getSimpleName(String[][][][].class));

        // On-the-fly types
        class Named {
            // empty
        }
        assertEquals("", ClassUtils.getSimpleName(new Object() {
            // empty
        }.getClass()));
        assertEquals("Named", ClassUtils.getSimpleName(Named.class));
    }

    @Test
    public void test_getSimpleName_Object() {
        assertEquals("ClassUtils", ClassUtils.getSimpleName(new ClassUtils()));
        assertEquals("Inner", ClassUtils.getSimpleName(new Inner()));
        assertEquals("String", ClassUtils.getSimpleName("hello"));
        assertEquals(StringUtils.EMPTY, ClassUtils.getSimpleName(null));
        assertEquals(StringUtils.EMPTY, ClassUtils.getSimpleName(null));
    }

    @Test
    public void test_getSimpleName_Object_String() {
        assertEquals("ClassUtils", ClassUtils.getSimpleName(new ClassUtils(), "<null>"));
        assertEquals("Inner", ClassUtils.getSimpleName(new Inner(), "<null>"));
        assertEquals("String", ClassUtils.getSimpleName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getSimpleName(null, "<null>"));
        assertEquals(null, ClassUtils.getSimpleName(null, null));
    }

    @Test
    public void test_isAssignable() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));

        assertTrue(ClassUtils.isAssignable(null, Object.class));
        assertTrue(ClassUtils.isAssignable(null, Integer.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertTrue(ClassUtils.isAssignable(String.class, String.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));

        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Object.class));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Object.class));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Object.class));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Object.class));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class));
    }

    @Test
    public void test_isAssignable_Autoboxing() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null, true));
        assertFalse(ClassUtils.isAssignable(String.class, null, true));

        assertTrue(ClassUtils.isAssignable(null, Object.class, true));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, true));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(String.class, String.class, true));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, true));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_isAssignable_ClassArray_ClassArray() throws Exception {
        final Class<?>[] array2 = new Class[] {Object.class, Object.class};
        final Class<?>[] array1 = new Class[] {Object.class};
        final Class<?>[] array1s = new Class[] {String.class};
        final Class<?>[] array0 = new Class[] {};
        final Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        final Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(null, array2));
        assertTrue(ClassUtils.isAssignable(null, array0));
        assertTrue(ClassUtils.isAssignable(array0, array0));
        assertTrue(ClassUtils.isAssignable(array0, (Class<?>[]) null)); // explicit cast to avoid warning
        assertTrue(ClassUtils.isAssignable(null, (Class<?>[]) null)); // explicit cast to avoid warning

        assertFalse(ClassUtils.isAssignable(array1, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1));

        assertTrue(ClassUtils.isAssignable(arrayPrimitives, arrayWrappers));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, arrayPrimitives));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1));
        assertTrue(ClassUtils.isAssignable(arrayPrimitives, array2));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2));
    }

    @Test
    public void test_isAssignable_ClassArray_ClassArray_Autoboxing() throws Exception {
        final Class<?>[] array2 = new Class[] {Object.class, Object.class};
        final Class<?>[] array1 = new Class[] {Object.class};
        final Class<?>[] array1s = new Class[] {String.class};
        final Class<?>[] array0 = new Class[] {};
        final Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        final Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2, true));
        assertFalse(ClassUtils.isAssignable(null, array2, true));
        assertTrue(ClassUtils.isAssignable(null, array0, true));
        assertTrue(ClassUtils.isAssignable(array0, array0, true));
        assertTrue(ClassUtils.isAssignable(array0, null, true));
        assertTrue(ClassUtils.isAssignable((Class[]) null, null, true));

        assertFalse(ClassUtils.isAssignable(array1, array1s, true));
        assertTrue(ClassUtils.isAssignable(array1s, array1s, true));
        assertTrue(ClassUtils.isAssignable(array1s, array1, true));

        assertTrue(ClassUtils.isAssignable(arrayPrimitives, arrayWrappers, true));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, arrayPrimitives, true));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1, true));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1, true));
        assertTrue(ClassUtils.isAssignable(arrayPrimitives, array2, true));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2, true));
    }

    @Test
    public void test_isAssignable_ClassArray_ClassArray_NoAutoboxing() throws Exception {
        final Class<?>[] array2 = new Class[] {Object.class, Object.class};
        final Class<?>[] array1 = new Class[] {Object.class};
        final Class<?>[] array1s = new Class[] {String.class};
        final Class<?>[] array0 = new Class[] {};
        final Class<?>[] arrayPrimitives = { Integer.TYPE, Boolean.TYPE };
        final Class<?>[] arrayWrappers = { Integer.class, Boolean.class };

        assertFalse(ClassUtils.isAssignable(array1, array2, false));
        assertFalse(ClassUtils.isAssignable(null, array2, false));
        assertTrue(ClassUtils.isAssignable(null, array0, false));
        assertTrue(ClassUtils.isAssignable(array0, array0, false));
        assertTrue(ClassUtils.isAssignable(array0, null, false));
        assertTrue(ClassUtils.isAssignable((Class[]) null, null, false));

        assertFalse(ClassUtils.isAssignable(array1, array1s, false));
        assertTrue(ClassUtils.isAssignable(array1s, array1s, false));
        assertTrue(ClassUtils.isAssignable(array1s, array1, false));

        assertFalse(ClassUtils.isAssignable(arrayPrimitives, arrayWrappers, false));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, arrayPrimitives, false));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array1, false));
        assertFalse(ClassUtils.isAssignable(arrayWrappers, array1, false));
        assertTrue(ClassUtils.isAssignable(arrayWrappers, array2, false));
        assertFalse(ClassUtils.isAssignable(arrayPrimitives, array2, false));
    }

    @Test
    public void test_isAssignable_DefaultUnboxing_Widening() throws Exception {
        // test byte conversions
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.class, Character.TYPE));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.class, Byte.TYPE));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.class, Short.TYPE));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.class, Integer.TYPE));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.class, Long.TYPE));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.class, Float.TYPE));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.class, Double.TYPE));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.class, Boolean.TYPE));

        // test short conversions
        assertFalse("short -> char", ClassUtils.isAssignable(Short.class, Character.TYPE));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.class, Byte.TYPE));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.class, Short.TYPE));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.class, Integer.TYPE));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.class, Long.TYPE));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.class, Float.TYPE));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.class, Double.TYPE));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.class, Boolean.TYPE));

        // test char conversions
        assertTrue("char -> char", ClassUtils.isAssignable(Character.class, Character.TYPE));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.class, Byte.TYPE));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.class, Short.TYPE));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.class, Integer.TYPE));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.class, Long.TYPE));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.class, Float.TYPE));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.class, Double.TYPE));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.class, Boolean.TYPE));

        // test int conversions
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.class, Character.TYPE));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.class, Byte.TYPE));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.class, Short.TYPE));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.class, Long.TYPE));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.class, Float.TYPE));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.class, Double.TYPE));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.class, Boolean.TYPE));

        // test long conversions
        assertFalse("long -> char", ClassUtils.isAssignable(Long.class, Character.TYPE));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.class, Byte.TYPE));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.class, Short.TYPE));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.class, Integer.TYPE));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.class, Long.TYPE));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.class, Float.TYPE));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.class, Double.TYPE));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.class, Boolean.TYPE));

        // test float conversions
        assertFalse("float -> char", ClassUtils.isAssignable(Float.class, Character.TYPE));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.class, Byte.TYPE));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.class, Short.TYPE));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.class, Integer.TYPE));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.class, Long.TYPE));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.class, Float.TYPE));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.class, Double.TYPE));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.class, Boolean.TYPE));

        // test double conversions
        assertFalse("double -> char", ClassUtils.isAssignable(Double.class, Character.TYPE));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.class, Byte.TYPE));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.class, Short.TYPE));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.class, Integer.TYPE));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.class, Long.TYPE));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.class, Float.TYPE));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.class, Double.TYPE));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.class, Boolean.TYPE));

        // test boolean conversions
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.class, Character.TYPE));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.class, Byte.TYPE));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.class, Short.TYPE));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.class, Integer.TYPE));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.class, Long.TYPE));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.class, Float.TYPE));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.class, Double.TYPE));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
    }

    @Test
    public void test_isAssignable_NoAutoboxing() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null, false));
        assertFalse(ClassUtils.isAssignable(String.class, null, false));

        assertTrue(ClassUtils.isAssignable(null, Object.class, false));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class, false));
        assertTrue(ClassUtils.isAssignable(String.class, String.class, false));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Object.class, false));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, false));
    }

    @Test
    public void test_isAssignable_Unboxing_Widening() throws Exception {
        // test byte conversions
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.class, Character.TYPE, true));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.class, Byte.TYPE, true));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.class, Short.TYPE, true));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.class, Integer.TYPE, true));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.class, Long.TYPE, true));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.class, Float.TYPE, true));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.class, Double.TYPE, true));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.class, Boolean.TYPE, true));

        // test short conversions
        assertFalse("short -> char", ClassUtils.isAssignable(Short.class, Character.TYPE, true));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.class, Byte.TYPE, true));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.class, Short.TYPE, true));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.class, Integer.TYPE, true));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.class, Long.TYPE, true));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.class, Float.TYPE, true));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.class, Double.TYPE, true));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.class, Boolean.TYPE, true));

        // test char conversions
        assertTrue("char -> char", ClassUtils.isAssignable(Character.class, Character.TYPE, true));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.class, Byte.TYPE, true));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.class, Short.TYPE, true));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.class, Integer.TYPE, true));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.class, Long.TYPE, true));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.class, Float.TYPE, true));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.class, Double.TYPE, true));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.class, Boolean.TYPE, true));

        // test int conversions
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.class, Character.TYPE, true));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.class, Byte.TYPE, true));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.class, Short.TYPE, true));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.class, Long.TYPE, true));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.class, Float.TYPE, true));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.class, Double.TYPE, true));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.class, Boolean.TYPE, true));

        // test long conversions
        assertFalse("long -> char", ClassUtils.isAssignable(Long.class, Character.TYPE, true));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.class, Byte.TYPE, true));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.class, Short.TYPE, true));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.class, Integer.TYPE, true));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.class, Long.TYPE, true));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.class, Float.TYPE, true));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.class, Double.TYPE, true));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.class, Boolean.TYPE, true));

        // test float conversions
        assertFalse("float -> char", ClassUtils.isAssignable(Float.class, Character.TYPE, true));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.class, Byte.TYPE, true));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.class, Short.TYPE, true));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.class, Integer.TYPE, true));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.class, Long.TYPE, true));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.class, Float.TYPE, true));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.class, Double.TYPE, true));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.class, Boolean.TYPE, true));

        // test double conversions
        assertFalse("double -> char", ClassUtils.isAssignable(Double.class, Character.TYPE, true));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.class, Byte.TYPE, true));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.class, Short.TYPE, true));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.class, Integer.TYPE, true));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.class, Long.TYPE, true));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.class, Float.TYPE, true));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.class, Double.TYPE, true));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.class, Boolean.TYPE, true));

        // test boolean conversions
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.class, Character.TYPE, true));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.class, Byte.TYPE, true));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.class, Short.TYPE, true));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.class, Integer.TYPE, true));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.class, Long.TYPE, true));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.class, Float.TYPE, true));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.class, Double.TYPE, true));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
    }

    @Test
    public void test_isAssignable_Widening() throws Exception {
        // test byte conversions
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.TYPE, Byte.TYPE));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));

        // test short conversions
        assertFalse("short -> char", ClassUtils.isAssignable(Short.TYPE, Character.TYPE));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.TYPE, Short.TYPE));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.TYPE, Boolean.TYPE));

        // test char conversions
        assertTrue("char -> char", ClassUtils.isAssignable(Character.TYPE, Character.TYPE));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.TYPE, Byte.TYPE));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.TYPE, Short.TYPE));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.TYPE, Boolean.TYPE));

        // test int conversions
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.TYPE, Character.TYPE));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.TYPE, Byte.TYPE));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.TYPE, Boolean.TYPE));

        // test long conversions
        assertFalse("long -> char", ClassUtils.isAssignable(Long.TYPE, Character.TYPE));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.TYPE, Byte.TYPE));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.TYPE, Short.TYPE));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.TYPE, Long.TYPE));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.TYPE, Boolean.TYPE));

        // test float conversions
        assertFalse("float -> char", ClassUtils.isAssignable(Float.TYPE, Character.TYPE));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.TYPE, Byte.TYPE));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.TYPE, Short.TYPE));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.TYPE, Integer.TYPE));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.TYPE, Long.TYPE));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.TYPE, Float.TYPE));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.TYPE, Boolean.TYPE));

        // test double conversions
        assertFalse("double -> char", ClassUtils.isAssignable(Double.TYPE, Character.TYPE));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.TYPE, Byte.TYPE));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.TYPE, Short.TYPE));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.TYPE, Integer.TYPE));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.TYPE, Long.TYPE));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.TYPE, Double.TYPE));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.TYPE, Boolean.TYPE));

        // test boolean conversions
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.TYPE, Character.TYPE));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.TYPE, Byte.TYPE));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.TYPE, Short.TYPE));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.TYPE, Long.TYPE));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.TYPE, Float.TYPE));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.TYPE, Double.TYPE));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
    }

    // -------------------------------------------------------------------------
    @Test
    public void test_isInnerClass_Class() {
        assertTrue(ClassUtils.isInnerClass(Inner.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        assertTrue(ClassUtils.isInnerClass(new Cloneable() {
            // empty
        }.getClass()));
        assertFalse(ClassUtils.isInnerClass(this.getClass()));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertFalse(ClassUtils.isInnerClass(null));
    }

    //-----------------------------------------------------------------------
    @Test
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        final Constructor<?>[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(ClassUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(ClassUtils.class.getModifiers()));
    }

    @Test
    public void testGetClassByNormalNameArrays() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( "int[]" ) );
        assertEquals( long[].class, ClassUtils.getClass( "long[]" ) );
        assertEquals( short[].class, ClassUtils.getClass( "short[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[]" ) );
        assertEquals( char[].class, ClassUtils.getClass( "char[]" ) );
        assertEquals( float[].class, ClassUtils.getClass( "float[]" ) );
        assertEquals( double[].class, ClassUtils.getClass( "double[]" ) );
        assertEquals( boolean[].class, ClassUtils.getClass( "boolean[]" ) );
        assertEquals( String[].class, ClassUtils.getClass( "java.lang.String[]" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "java.util.Map.Entry[]" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "java.util.Map$Entry[]" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "[Ljava.util.Map.Entry;" ) );
        assertEquals( java.util.Map.Entry[].class, ClassUtils.getClass( "[Ljava.util.Map$Entry;" ) );
    }

    @Test
    public void testGetClassByNormalNameArrays2D() throws ClassNotFoundException {
        assertEquals( int[][].class, ClassUtils.getClass( "int[][]" ) );
        assertEquals( long[][].class, ClassUtils.getClass( "long[][]" ) );
        assertEquals( short[][].class, ClassUtils.getClass( "short[][]" ) );
        assertEquals( byte[][].class, ClassUtils.getClass( "byte[][]" ) );
        assertEquals( char[][].class, ClassUtils.getClass( "char[][]" ) );
        assertEquals( float[][].class, ClassUtils.getClass( "float[][]" ) );
        assertEquals( double[][].class, ClassUtils.getClass( "double[][]" ) );
        assertEquals( boolean[][].class, ClassUtils.getClass( "boolean[][]" ) );
        assertEquals( String[][].class, ClassUtils.getClass( "java.lang.String[][]" ) );
    }

    @Test
    public void testGetClassClassNotFound() throws Exception {
        assertGetClassThrowsClassNotFound( "bool" );
        assertGetClassThrowsClassNotFound( "bool[]" );
        assertGetClassThrowsClassNotFound( "integer[]" );
    }

    @Test
    public void testGetClassInvalidArguments() throws Exception {
        assertGetClassThrowsNullPointerException( null );
        assertGetClassThrowsClassNotFound( "[][][]" );
        assertGetClassThrowsClassNotFound( "[[]" );
        assertGetClassThrowsClassNotFound( "[" );
        assertGetClassThrowsClassNotFound( "java.lang.String][" );
        assertGetClassThrowsClassNotFound( ".hello.world" );
        assertGetClassThrowsClassNotFound( "hello..world" );
    }

    @Test
    public void testGetClassRawPrimitives() throws ClassNotFoundException {
        assertEquals( int.class, ClassUtils.getClass( "int" ) );
        assertEquals( long.class, ClassUtils.getClass( "long" ) );
        assertEquals( short.class, ClassUtils.getClass( "short" ) );
        assertEquals( byte.class, ClassUtils.getClass( "byte" ) );
        assertEquals( char.class, ClassUtils.getClass( "char" ) );
        assertEquals( float.class, ClassUtils.getClass( "float" ) );
        assertEquals( double.class, ClassUtils.getClass( "double" ) );
        assertEquals( boolean.class, ClassUtils.getClass( "boolean" ) );
        assertEquals( void.class, ClassUtils.getClass( "void" ) );
    }

    @Test
    public void testGetClassWithArrayClasses() throws Exception {
        assertGetClassReturnsClass( String[].class );
        assertGetClassReturnsClass( int[].class );
        assertGetClassReturnsClass( long[].class );
        assertGetClassReturnsClass( short[].class );
        assertGetClassReturnsClass( byte[].class );
        assertGetClassReturnsClass( char[].class );
        assertGetClassReturnsClass( float[].class );
        assertGetClassReturnsClass( double[].class );
        assertGetClassReturnsClass( boolean[].class );
    }

    @Test
    public void testGetClassWithArrayClasses2D() throws Exception {
        assertGetClassReturnsClass( String[][].class );
        assertGetClassReturnsClass( int[][].class );
        assertGetClassReturnsClass( long[][].class );
        assertGetClassReturnsClass( short[][].class );
        assertGetClassReturnsClass( byte[][].class );
        assertGetClassReturnsClass( char[][].class );
        assertGetClassReturnsClass( float[][].class );
        assertGetClassReturnsClass( double[][].class );
        assertGetClassReturnsClass( boolean[][].class );
    }

    @Test
    public void testGetInnerClass() throws ClassNotFoundException {
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest.Inner.DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest.Inner$DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest$Inner$DeeplyNested" ) );
        assertEquals( Inner.DeeplyNested.class, ClassUtils.getClass( "org.apache.commons.lang3.ClassUtilsTest$Inner.DeeplyNested" ) );
    }

    @Test
    public void testGetPublicMethod() throws Exception {
        // Tests with Collections$UnmodifiableSet
        final Set<?> set = Collections.unmodifiableSet(new HashSet<>());
        final Method isEmptyMethod = ClassUtils.getPublicMethod(set.getClass(), "isEmpty");
            assertTrue(Modifier.isPublic(isEmptyMethod.getDeclaringClass().getModifiers()));

        try {
            isEmptyMethod.invoke(set);
        } catch(final java.lang.IllegalAccessException iae) {
            fail("Should not have thrown IllegalAccessException");
        }

        // Tests with a public Class
        final Method toStringMethod = ClassUtils.getPublicMethod(Object.class, "toString");
            assertEquals(Object.class.getMethod("toString", new Class[0]), toStringMethod);
    }

    @Test
    public void testHierarchyExcludingInterfaces() {
        final Iterator<Class<?>> iter = ClassUtils.hierarchy(StringParameterizedChild.class).iterator();
        assertEquals(StringParameterizedChild.class, iter.next());
        assertEquals(GenericParent.class, iter.next());
        assertEquals(Object.class, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testHierarchyIncludingInterfaces() {
        final Iterator<Class<?>> iter =
            ClassUtils.hierarchy(StringParameterizedChild.class, Interfaces.INCLUDE).iterator();
        assertEquals(StringParameterizedChild.class, iter.next());
        assertEquals(GenericParent.class, iter.next());
        assertEquals(GenericConsumer.class, iter.next());
        assertEquals(Object.class, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testIsPrimitiveOrWrapper() {

        // test primitive wrapper classes
        assertTrue("Boolean.class", ClassUtils.isPrimitiveOrWrapper(Boolean.class));
        assertTrue("Byte.class", ClassUtils.isPrimitiveOrWrapper(Byte.class));
        assertTrue("Character.class", ClassUtils.isPrimitiveOrWrapper(Character.class));
        assertTrue("Short.class", ClassUtils.isPrimitiveOrWrapper(Short.class));
        assertTrue("Integer.class", ClassUtils.isPrimitiveOrWrapper(Integer.class));
        assertTrue("Long.class", ClassUtils.isPrimitiveOrWrapper(Long.class));
        assertTrue("Double.class", ClassUtils.isPrimitiveOrWrapper(Double.class));
        assertTrue("Float.class", ClassUtils.isPrimitiveOrWrapper(Float.class));

        // test primitive classes
        assertTrue("boolean", ClassUtils.isPrimitiveOrWrapper(Boolean.TYPE));
        assertTrue("byte", ClassUtils.isPrimitiveOrWrapper(Byte.TYPE));
        assertTrue("char", ClassUtils.isPrimitiveOrWrapper(Character.TYPE));
        assertTrue("short", ClassUtils.isPrimitiveOrWrapper(Short.TYPE));
        assertTrue("int", ClassUtils.isPrimitiveOrWrapper(Integer.TYPE));
        assertTrue("long", ClassUtils.isPrimitiveOrWrapper(Long.TYPE));
        assertTrue("double", ClassUtils.isPrimitiveOrWrapper(Double.TYPE));
        assertTrue("float", ClassUtils.isPrimitiveOrWrapper(Float.TYPE));
        assertTrue("Void.TYPE", ClassUtils.isPrimitiveOrWrapper(Void.TYPE));

        // others
        assertFalse("null", ClassUtils.isPrimitiveOrWrapper(null));
        assertFalse("Void.class", ClassUtils.isPrimitiveOrWrapper(Void.class));
        assertFalse("String.class", ClassUtils.isPrimitiveOrWrapper(String.class));
        assertFalse("this.getClass()", ClassUtils.isPrimitiveOrWrapper(this.getClass()));
    }

    @Test
    public void testIsPrimitiveWrapper() {

        // test primitive wrapper classes
        assertTrue("Boolean.class", ClassUtils.isPrimitiveWrapper(Boolean.class));
        assertTrue("Byte.class", ClassUtils.isPrimitiveWrapper(Byte.class));
        assertTrue("Character.class", ClassUtils.isPrimitiveWrapper(Character.class));
        assertTrue("Short.class", ClassUtils.isPrimitiveWrapper(Short.class));
        assertTrue("Integer.class", ClassUtils.isPrimitiveWrapper(Integer.class));
        assertTrue("Long.class", ClassUtils.isPrimitiveWrapper(Long.class));
        assertTrue("Double.class", ClassUtils.isPrimitiveWrapper(Double.class));
        assertTrue("Float.class", ClassUtils.isPrimitiveWrapper(Float.class));

        // test primitive classes
        assertFalse("boolean", ClassUtils.isPrimitiveWrapper(Boolean.TYPE));
        assertFalse("byte", ClassUtils.isPrimitiveWrapper(Byte.TYPE));
        assertFalse("char", ClassUtils.isPrimitiveWrapper(Character.TYPE));
        assertFalse("short", ClassUtils.isPrimitiveWrapper(Short.TYPE));
        assertFalse("int", ClassUtils.isPrimitiveWrapper(Integer.TYPE));
        assertFalse("long", ClassUtils.isPrimitiveWrapper(Long.TYPE));
        assertFalse("double", ClassUtils.isPrimitiveWrapper(Double.TYPE));
        assertFalse("float", ClassUtils.isPrimitiveWrapper(Float.TYPE));

        // others
        assertFalse("null", ClassUtils.isPrimitiveWrapper(null));
        assertFalse("Void.class", ClassUtils.isPrimitiveWrapper(Void.class));
        assertFalse("Void.TYPE", ClassUtils.isPrimitiveWrapper(Void.TYPE));
        assertFalse("String.class", ClassUtils.isPrimitiveWrapper(String.class));
        assertFalse("this.getClass()", ClassUtils.isPrimitiveWrapper(this.getClass()));
    }

    @Test
    public void testPrimitivesToWrappers() {
        // test null
//        assertNull("null -> null", ClassUtils.primitivesToWrappers(null)); // generates warning
        assertNull("null -> null", ClassUtils.primitivesToWrappers((Class<?>[]) null)); // equivalent cast to avoid warning
        // Other possible casts for null
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers()));
        final Class<?>[] castNull = ClassUtils.primitivesToWrappers((Class<?>)null); // == new Class<?>[]{null}
        assertTrue("(Class<?>)null -> [null]", Arrays.equals(new Class<?>[]{null}, castNull));
        // test empty array is returned unchanged
        assertArrayEquals("empty -> empty",
                ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers(ArrayUtils.EMPTY_CLASS_ARRAY));

        // test an array of various classes
        final Class<?>[] primitives = new Class[] {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE,
                String.class, ClassUtils.class
        };
        final Class<?>[] wrappers= ClassUtils.primitivesToWrappers(primitives);

        for (int i=0; i < primitives.length; i++) {
            // test each returned wrapper
            final Class<?> primitive = primitives[i];
            final Class<?> expectedWrapper = ClassUtils.primitiveToWrapper(primitive);

            assertEquals(primitive + " -> " + expectedWrapper, expectedWrapper, wrappers[i]);
        }

        // test an array of no primitive classes
        final Class<?>[] noPrimitives = new Class[] {
                String.class, ClassUtils.class, Void.TYPE
        };
        // This used to return the exact same array, but no longer does.
        assertNotSame("unmodified", noPrimitives, ClassUtils.primitivesToWrappers(noPrimitives));
    }

    @Test
    public void testPrimitiveToWrapper() {

        // test primitive classes
        assertEquals("boolean -> Boolean.class",
            Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals("byte -> Byte.class",
            Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals("char -> Character.class",
            Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals("short -> Short.class",
            Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals("int -> Integer.class",
            Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals("long -> Long.class",
            Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals("double -> Double.class",
            Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals("float -> Float.class",
            Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));

        // test a few other classes
        assertEquals("String.class -> String.class",
            String.class, ClassUtils.primitiveToWrapper(String.class));
        assertEquals("ClassUtils.class -> ClassUtils.class",
            org.apache.commons.lang3.ClassUtils.class,
            ClassUtils.primitiveToWrapper(org.apache.commons.lang3.ClassUtils.class));
        assertEquals("Void.TYPE -> Void.TYPE",
            Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));

        // test null
        assertNull("null -> null",
            ClassUtils.primitiveToWrapper(null));
    }

    // Show the Java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4071957
    // We may have to delete this if a JDK fixes the bug.
    @Test
    public void testShowJavaBug() throws Exception {
        // Tests with Collections$UnmodifiableSet
        final Set<?> set = Collections.unmodifiableSet(new HashSet<>());
        final Method isEmptyMethod = set.getClass().getMethod("isEmpty");
        try {
            isEmptyMethod.invoke(set);
            fail("Failed to throw IllegalAccessException as expected");
        } catch(final IllegalAccessException iae) {
            // expected
        }
    }

    @Test
    public void testToClass_object() {
//        assertNull(ClassUtils.toClass(null)); // generates warning
        assertNull(ClassUtils.toClass((Object[]) null)); // equivalent explicit cast

        // Additional varargs tests
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass()));
        final Class<?>[] castNull = ClassUtils.toClass((Object) null); // == new Object[]{null}
        assertTrue("(Object)null -> [null]", Arrays.equals(new Object[]{null}, castNull));

        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(ArrayUtils.EMPTY_OBJECT_ARRAY));

        assertTrue(Arrays.equals(new Class[] { String.class, Integer.class, Double.class },
                ClassUtils.toClass("Test", Integer.valueOf(1), Double.valueOf(99d))));

        assertTrue(Arrays.equals(new Class[] { String.class, null, Double.class },
                ClassUtils.toClass("Test", null, Double.valueOf(99d))));
    }

    @Test
    public void testWithInterleavingWhitespace() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( " int [ ] " ) );
        assertEquals( long[].class, ClassUtils.getClass( "\rlong\t[\n]\r" ) );
        assertEquals( short[].class, ClassUtils.getClass( "\tshort                \t\t[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[\t\t\n\r]   " ) );
    }

    @Test
    public void testWrappersToPrimitives() {
        // an array with classes to test
        final Class<?>[] classes = {
                Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class,
                String.class, ClassUtils.class, null
        };

        final Class<?>[] primitives = ClassUtils.wrappersToPrimitives(classes);
        // now test the result
        assertEquals("Wrong length of result array", classes.length, primitives.length);
        for (int i = 0; i < classes.length; i++) {
            final Class<?> expectedPrimitive = ClassUtils.wrapperToPrimitive(classes[i]);
            assertEquals(classes[i] + " -> " + expectedPrimitive, expectedPrimitive,
                    primitives[i]);
        }
    }

    @Test
    public void testWrappersToPrimitivesEmpty() {
        final Class<?>[] empty = new Class[0];
        assertArrayEquals("Wrong result for empty input", empty, ClassUtils.wrappersToPrimitives(empty));
    }

    @Test
    public void testWrappersToPrimitivesNull() {
//        assertNull("Wrong result for null input", ClassUtils.wrappersToPrimitives(null)); // generates warning
        assertNull("Wrong result for null input", ClassUtils.wrappersToPrimitives((Class<?>[]) null)); // equivalent cast
        // Other possible casts for null
        assertTrue("empty -> empty", Arrays.equals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.wrappersToPrimitives()));
        final Class<?>[] castNull = ClassUtils.wrappersToPrimitives((Class<?>)null); // == new Class<?>[]{null}
        assertTrue("(Class<?>)null -> [null]", Arrays.equals(new Class<?>[]{null}, castNull));
}

    @Test
    public void testWrapperToPrimitive() {
        // an array with classes to convert
        final Class<?>[] primitives = {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
        };
        for (final Class<?> primitive : primitives) {
            final Class<?> wrapperCls = ClassUtils.primitiveToWrapper(primitive);
            assertFalse("Still primitive", wrapperCls.isPrimitive());
            assertEquals(wrapperCls + " -> " + primitive, primitive,
                    ClassUtils.wrapperToPrimitive(wrapperCls));
        }
    }

    @Test
    public void testWrapperToPrimitiveNoWrapper() {
        assertNull("Wrong result for non wrapper class", ClassUtils.wrapperToPrimitive(String.class));
    }

    @Test
    public void testWrapperToPrimitiveNull() {
        assertNull("Wrong result for null class", ClassUtils.wrapperToPrimitive(null));
    }
}
