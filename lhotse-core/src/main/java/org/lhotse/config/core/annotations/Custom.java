package org.lhotse.config.core.annotations;

import org.lhotse.config.core.FieldConvertor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Custom {

    Class<? extends FieldConvertor> convertor();
}
