package com.yongda.site.wallet.app.action.test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 *@auther Shilong Che
 *@version 创建时间：2016年11月8日上午9:45:38
 */

@Component
@Aspect
public class Test1Advice {
	
	@Pointcut("@annotation(com.yongda.site.wallet.app.action.test.Test1)")
	public void test(){
	}
	
	@Before("test()")
	public void before(){
		System.out.println("这是before");
	}
	
	@After("test()")
	public void after(){
		System.out.println("这是after");
	}

	@Around("test()")
    public void around(ProceedingJoinPoint pjp) throws Throwable{
		System.out.println("around1");
        pjp.proceed();
        System.out.println("around2");
    }
}
