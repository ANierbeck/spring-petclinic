/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import scala.annotation.meta.setter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 */
@Aspect
public class ApiMonitoringAspect {

	private Logger apiLogger = LoggerFactory.getLogger("API");

	private final ObjectMapper mapper; 
	
	public ApiMonitoringAspect(){
		mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerBean() {}
    
    @Pointcut("execution(* *(..))")
    public void methodPointcut() {}
    
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMapping() {}
    
    @Around("controllerBean() && methodPointcut() && requestMapping()")
    public Object afterMethodInControllerClass(ProceedingJoinPoint joinPoint) throws Throwable {
    	MDC.clear();
        Map<String, Object> context = new HashMap<String, Object>();
		Object[] args = joinPoint.getArgs();
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature(); 
		String[] parameterNames = methodSignature.getParameterNames();
		Object result = null;
		Map<String, Object> target = new HashMap<String, Object>();
		target.put("class", joinPoint.getTarget().getClass().getSimpleName());
		target.put("package", joinPoint.getTarget().getClass().getPackage().getName());
		target.put("method", methodSignature.getName());
		MDC.put("target", mapper.writeValueAsString(target));
		for (int i = 0; i < parameterNames.length; i++) {
			if(hasDumpAnnotation(methodSignature, i))
			context.put(parameterNames[i], args[i]);
		}
		MDC.put("params", mapper.writeValueAsString(context));
		try {
			result = joinPoint.proceed();
		} catch (Throwable t){
			MDC.put("exception", t.getMessage());
			apiLogger.info("failure: {}", joinPoint.getSignature());
			throw t;
		}
		MDC.put("result", mapper.writeValueAsString(result));
		apiLogger.info("called: {}", joinPoint.getSignature());
		return result;
    }
    
    public boolean hasDumpAnnotation(MethodSignature signature, int parameterIndex){
    	Annotation[] annotations = signature.getMethod().getParameterAnnotations()[parameterIndex];
    	for(Annotation annotation : annotations){
    		if(annotation instanceof DumpToLogstash){
    			return true;
    		}
    	}
    	return false;
    	
    }
}
