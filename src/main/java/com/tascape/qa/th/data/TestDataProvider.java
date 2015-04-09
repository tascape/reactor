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
    String DEFAULT_METHOD_NAME = "getData";

    Class<? extends TestData> klass();

    String method() default DEFAULT_METHOD_NAME;

    String parameter() default "";
}
