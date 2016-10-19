package com.yongda.site.app.aop.advice;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.netfinworks.site.domain.exception.CommonDefinedException;
import com.yongda.site.app.util.ResponseUtil;
import com.yongda.site.app.validator.CommonValidator;


@Aspect
public class FormValidAdvice{
	
	@Resource(name = "commonValidator")
    protected CommonValidator validator;
	
	@Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void formValid() {
    }
	
	
	@SuppressWarnings({ "rawtypes", "unused" })
	@Around("formValid()")
    public Object aroundFormValid(ProceedingJoinPoint pjp) throws Throwable {
		Object ret = null;
		Object target = pjp.getTarget(); 
		Object[] args = pjp.getArgs();
		String methodName = pjp.getSignature().getName(); 
		Method method = getMethodByClassAndName(target.getClass(), methodName);
		Class respClass = (Class) method.getGenericReturnType();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		int i=0;
		for(Annotation[] annotations : parameterAnnotations){
			 Object param = args[i++];  
			 for(Annotation annotation : annotations){  
				 if(annotation instanceof FormValid){
					 String error = validator.validate(param);
					 if(StringUtils.isNotBlank(error))
						 return ResponseUtil.buildExpResponse(CommonDefinedException.ILLEGAL_ARGUMENT, error);
				 }  
			 }  
		}
		ret = pjp.proceed();  
        return ret;
    }
	
	public Method getMethodByClassAndName(Class c , String methodName){  
        Method[] methods = c.getDeclaredMethods();  
        for (Method method : methods) {  
            if(method.getName().equals(methodName)){  
                return method ;  
            }  
        }  
        return null; 
    }
}