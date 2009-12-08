package thewebsemantic;

import java.util.HashMap;
import java.util.Map;

public class FieldValueMap<A> implements Where<A> {
	
	Class<A> c;
	Map<Object, FieldContext> map;
	PersistenceManager pm;
	
	public FieldValueMap(A a, PersistenceManager pm) {
		c = (Class<A>) a.getClass();
		this.pm = pm;
		map = new HashMap<Object, FieldContext>();
		TypeWrapper type = TypeWrapperFactory.$(a);	
		for (FieldContext f : type.getFields(a)) {
			Object val = f.initWithNewObject();
			map.put(val, f);
		} 
	}

	public FieldContext getField(Object value) {
		return map.get(value);
	}
	
	public Is<A> where(Object o) {
		return new IndexQuery<A>(map.get(o), pm, c);
	}

}