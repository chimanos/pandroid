package com.leroymerlin.pandroid.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface RxWrapper {
    boolean single() default true;
}

