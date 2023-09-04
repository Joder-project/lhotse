package org.lhotse.config.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({LhotseConfiguration.class})
public @interface EnableLhotse {
}
