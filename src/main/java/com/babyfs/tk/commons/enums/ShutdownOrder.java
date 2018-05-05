package com.babyfs.tk.commons.enums;

import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * 停止的顺序
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface ShutdownOrder {

    /**
     * The order value.
     * <p>Default is {@link Ordered#HIGHEST_PRECEDENCE}.
     *
     * @see Ordered#getOrder()
     */
    int value() default Ordered.HIGHEST_PRECEDENCE;
}
