package com.yongda.site.core.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpField {
	public String name();
	
	public String dateFormat() default "yyyy-MM-dd HH:mm:ss";
}
