package thewebsemantic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME) 
public @interface Graph {
	public static final String DEFAULT = "default";	
	String value() default DEFAULT;
	String index() default DEFAULT;
}
