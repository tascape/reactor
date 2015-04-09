package com.tascape.qa.th.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Level: 0, 1, 2, 3, whereas 0 is the top priority.
 *
 * @author linsong wang
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    int NONE = -1;

    int P0 = 0;

    int P1 = 1;

    int P2 = 2;

    int P3 = 3;

    int level() default 3;
}
