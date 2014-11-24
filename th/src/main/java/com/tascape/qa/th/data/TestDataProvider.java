package com.tascape.qa.th.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author linsong wang
 */
@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDataProvider {
    Class<? extends TestData> klass();

    String method() default TestData.DEFAULT_METHOD;

    String parameter() default "";
}
