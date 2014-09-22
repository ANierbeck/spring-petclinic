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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 */
@Aspect
public class ApiMonitoringAspect {

    private Logger logger = LoggerFactory.getLogger("API");

	private ObjectMapper mapper = new ObjectMapper();

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerBean() {}
    
    @Pointcut("execution(* *(..))")
    public void methodPointcut() {}
    
    @Around("controllerBean() && methodPointcut() ")
    public Object afterMethodInControllerClass(ProceedingJoinPoint joinPoint) throws Throwable {
    	logger.info("called: {}", joinPoint.getSignature());
		StopWatch sw = new StopWatch(joinPoint.toShortString());
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		Object[] args = joinPoint.getArgs();
		String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
		Class[] parameterTypes = ((MethodSignature)joinPoint.getSignature()).getParameterTypes();
		sw.start("invoke");
		for (int i = 0; i < parameterNames.length; i++) {
			Class type = parameterTypes[i];
			if (type.getCanonicalName().startsWith(
					"org.springframework.web.bind.")
					|| type.getCanonicalName().startsWith(
							"org.springframework.validation."))
				continue;
			// MDC.put(parameterNames[i], mapper.writeValueAsString(args[i]));
			MDC.put(parameterNames[i], args[i].toString());
		}
		try {
    	return joinPoint.proceed();
		} finally {
			sw.stop();
			synchronized (this) {
				logger.info("{} : {}", joinPoint.toShortString(),
						sw.getTotalTimeMillis());
			}
		}
    }

	public ObjectMapper getMapper() {
		return mapper;
	}

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
}
