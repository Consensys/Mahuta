package net.consensys.mahuta.springdata.annotation;


import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface IPFSDocument {
    
    public String index() default "";
    public String indexConfiguration() default "";
    public boolean indexContent() default false;
}