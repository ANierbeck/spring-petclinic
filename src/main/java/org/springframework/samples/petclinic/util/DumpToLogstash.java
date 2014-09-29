package org.springframework.samples.petclinic.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface DumpToLogstash {

}
