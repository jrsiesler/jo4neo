package jo4neo.impl;

import jo4neo.Nodeid;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;



class DeleteOpertation {

	IndexedNeo ineo;
	
	public DeleteOpertation(IndexedNeo ineo) {
		this.ineo = ineo;
	}

	public void delete(Object... o) {
		Transaction t = ineo.beginTx();
		try {
			for (Object item : o) {
				TypeWrapper type = TypeWrapperFactory.$(item);
				Nodeid neo = type.id(item);
				Node delNode = ineo.getNodeById(neo.id());
				if (neo == null)
					return;
				for (FieldContext field : type.getFields(item))
					if (field.isIndexed())
						indexRemove(delNode, field);

				for (Relationship r : delNode.getRelationships())
					r.delete();
				delNode.delete();
			}
			t.success();
		} finally {
			t.finish();
		}
	}
	
	private void indexRemove(Node delNode, FieldContext field) {
		ineo.getIndexService().removeIndex(delNode, field.getIndexName(),
				field.value());
	}
}
