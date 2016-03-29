/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.reflection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.crypt.cert.CertificateDetails;
import com.foilen.smalltools.inputstream.ZerosInputStream;

public class ReflectionUtilsTest {

    private static interface Animal {
        public int getAge();

        public String getName();
    }

    private static class AnimalContainer {
        private Animal animal;

        public Animal getAnimal() {
            return animal;
        }

        public void setAnimal(Animal animal) {
            this.animal = animal;
        }
    }

    private static class Anything {
        private String name;
        private int age;
        @MyAnnotation
        private Dog animal;

        public int getAge() {
            return age;
        }

        public Dog getAnimal() {
            return animal;
        }

        @MyAnnotation
        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setAnimal(Dog animal) {
            this.animal = animal;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @SuppressWarnings("unused")
    private static class Anything2 extends Anything {
        private String nickname;
        @MyAnnotation
        private String something;

        @MyAnnotation
        private void init() {
        }
    }

    @SuppressWarnings("unused")
    private static class Cat implements Animal {
        private String name;
        private int age;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    private static class Dog implements Animal {
        private String name;
        private int age;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    private static @interface MyAnnotation {
    }

    private void assertContainField(List<Field> fields, String fieldName) {
        for (Field field : fields) {
            if (fieldName.equals(field.getName())) {
                return;
            }
        }
        Assert.fail("Field " + fieldName + " is not part of the list");
    }

    private void assertContainMethod(List<Method> methods, String methodName) {
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return;
            }
        }
        Assert.fail("Method " + methodName + " is not part of the list");
    }

    @Test
    public void testAllFields() {
        List<Field> fields = ReflectionUtils.allFields(Anything2.class);
        int expectedSize = 5;
        if (fields.size() == expectedSize + 2) {
            // Coverage test
            assertContainField(fields, "$jacocoData");
        } else {
            Assert.assertEquals(expectedSize, fields.size());
        }
        assertContainField(fields, "age");
        assertContainField(fields, "animal");
        assertContainField(fields, "name");
        assertContainField(fields, "nickname");
        assertContainField(fields, "something");
    }

    @Test
    public void testAllFieldsWithAnnotation() {
        List<Field> fields = ReflectionUtils.allFieldsWithAnnotation(Anything2.class, MyAnnotation.class);
        Assert.assertEquals(2, fields.size());
        assertContainField(fields, "animal");
        assertContainField(fields, "something");
    }

    @Test
    public void testAllMethodsWithAnnotations() {
        List<Method> methods = ReflectionUtils.allMethodsWithAnnotation(Anything2.class, MyAnnotation.class);
        Assert.assertEquals(2, methods.size());
        assertContainMethod(methods, "getName");
        assertContainMethod(methods, "init");
    }

    @Test
    public void testCopyAllProperties() {

        // Simple
        Dog dog = new Dog();
        Anything anything = new Anything();
        dog.setAge(10);
        dog.setName("Fido");
        ReflectionUtils.copyAllProperties(dog, anything);
        Assert.assertEquals(10, anything.getAge());
        Assert.assertEquals("Fido", anything.getName());
        Assert.assertNull(anything.getAnimal());

        // Simple reverse
        dog = new Dog();
        Dog setDog = new Dog();
        anything.setAge(20);
        anything.setName("Rick");
        anything.setAnimal(setDog);
        ReflectionUtils.copyAllProperties(anything, dog);
        Assert.assertEquals(20, dog.getAge());
        Assert.assertEquals("Rick", dog.getName());

        // With sub-class
        AnimalContainer animalContainer = new AnimalContainer();
        ReflectionUtils.copyAllProperties(anything, animalContainer);
        Assert.assertEquals(setDog, animalContainer.getAnimal());

        // With sub-class (wrong way)
        anything = new Anything();
        animalContainer.setAnimal(new Cat());
        ReflectionUtils.copyAllProperties(animalContainer, anything);
        Assert.assertEquals(0, anything.getAge());
        Assert.assertNull(anything.getName());
        Assert.assertNull(anything.getAnimal());
    }

    @Test
    public void testInstantiateEmptyContructor() {
        CertificateDetails actual = ReflectionUtils.instantiate(CertificateDetails.class);
        Assert.assertNotNull(actual);
    }

    @Test
    public void testInstantiateNonEmptyContructor() {
        ZerosInputStream actual = ReflectionUtils.instantiate(ZerosInputStream.class, 10L);
        Assert.assertNotNull(actual);
    }

}
