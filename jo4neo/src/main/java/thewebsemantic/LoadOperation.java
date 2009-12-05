package thewebsemantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;

public class LoadOperation<T> {

	IndexedNeo neo;
	Class<?> cls;
	Map<Long, Object> cache;

	public LoadOperation(Class<?> type, IndexedNeo ineo) {
		this.cls = type;
		this.neo = ineo;
		cache = new HashMap<Long, Object>();
	}

	public T load(long key) {
		Transaction t = neo.beginTx();
		try {
			Node n = neo.getNodeById(key);
			if (n == null)
				return null;
			Object o = loadFully(n);
			t.success();
			return (T) o;
		} finally {
			t.finish();
		}
	}
	
	public boolean isClosed() {
		return neo.isClosed();
	}

	private Object loadFully(Node n) {
		TypeWrapper type = nodesJavaType(n);
		Object o = loadDirect(n);
		for (FieldContext field : type.getValueContexts(o)) {
			if (field.isSingular())
				single(n, field);
			else if (field.isPluralPrimitive())
				field.applyFrom(n);
			else if (field.isPlural())
				field.setProperty(new LazyList(field, this));
		}
		return o;
	}

	public Collection<T> loadAll() {
		Node n = neo.getMetaNode(cls);
		return load2(n.getRelationships(Relationships.HAS_MEMBER));
	}
	
	public Collection<T> since(long since) {		
		Transaction t = neo.beginTx();
		try {
			if (!cls.isAnnotationPresent(Timeline.class))
				throw new UnsupportedOperationException();
			org.neo4j.util.timeline.Timeline tl = neo.getTimeLine(cls);		
			return load(tl.getAllNodesAfter(since));
		} finally {
			t.finish();
		}
	}
	

	public Collection<T> load(Iterable<Node> nodes) {
		Transaction t = neo.beginTx();
		try {
			ArrayList<T> results = new ArrayList<T>();
			for (Node node : nodes)
				results.add((T) loadFully(node));
			t.success();
			return results;
		} finally {
			t.finish();
		}
	}
	
	private Collection<T> load2(Iterable<Relationship> relations) {
		Transaction t = neo.beginTx();
		try {
			ArrayList<T> results = new ArrayList<T>();
			for (Relationship r : relations)
				results.add((T) loadFully(r.getEndNode()));
			t.success();
			return results;
		} finally {
			t.finish();
		}
	}

	private void single(Node n, FieldContext field) {
		for (Relationship r : field.relationships(n, neo.getRelationFactory())) {
			field.setProperty(loadDirect(r));
			return;
		}
	}

	public Collection<Object> load(FieldContext field) {
		Transaction t = neo.beginTx();
		try { 
			Set<Object> values = new TreeSet<Object>(new NeoComparator());
			Node n = field.subjectNode(neo);
			for (Relationship r : outgoingRelationships(field, n)) {
				if (!values.add(loadDirect(r)))
					System.err.println("duplicate relations in graph.");
			}
			t.success();
			return values;
		} finally {
			t.finish();
		}
	}
	
	public void removeRelationship(FieldContext field, Object o) {
		Transaction t = neo.beginTx();
		try {
			Node source = field.subjectNode(neo);
			Node target = neo.asNode(o);
			for (Relationship r : outgoingRelationships(field, source)) {
				if (r.getEndNode().equals(target))
					r.delete();
			}
			t.success();
		} finally {
			t.finish();
		}
	}

	private Iterable<Relationship> outgoingRelationships(FieldContext field,
			Node n) {
		return n.getRelationships(field.toRelationship(neo
				.getRelationFactory()), Direction.OUTGOING);
	}

	protected Object loadDirect(Relationship r) {
		return loadDirect(r.getEndNode());
	}

	protected Object loadDirect(Node n) {
		if (cache.containsKey(n.getId()))
			return cache.get(n.getId());
		TypeWrapper type = nodesJavaType(n);
		Object o = type.newInstance();
		type.setId(o, new Nodeid(n.getId(), type.getWrappedType()));
		cache.put(n.getId(), o);
		for (FieldContext field : type.getValueContexts(o))
			if (field.isSimpleType())
				field.applyFrom(n);
			else if (field.isSingular())
				single(n, field);
		return o;
	}

	/**
	 * Each node created is annotated with the javatype from
	 * whence it came.
	 * @param n neo4j node.
	 * @return
	 */
	private TypeWrapper nodesJavaType(Node n) {
		String typename = (String) n.getProperty(Nodeid.class.getName());
		TypeWrapper type = TypeWrapperFactory.wrap(typename);
		return type;
	}



}
