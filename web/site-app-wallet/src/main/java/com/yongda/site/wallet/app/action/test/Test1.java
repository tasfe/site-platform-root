package com.yongda.site.wallet.app.action.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *@auther Shilong Che
 *@version 创建时间：2016年11月8日上午9:21:31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Test1 {
	String desc() default "";
	String methed() ;
	String issuccess();
}
