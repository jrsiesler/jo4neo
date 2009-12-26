package jo4neo;

import java.lang.reflect.Field;

import jo4neo.util.RelationFactory;

import org.neo4j.api.core.DynamicRelationshipType;
import org.neo4j.api.core.RelationshipType;

public class RelationFactoryImpl implements RelationFactory {

	public RelationshipType relationshipType(Field f) {		
		if ( f.isAnnotationPresent(neo.class)) {
			neo p = f.getAnnotation(neo.class);
			String name = p.value();
			if (!neo.DEFAULT.equals(name))
				return relationshipType(name);
			
		}
		String n = f.getName();
		return relationshipType(n);
			
	}

	public RelationshipType relationshipType(String name) {
		return DynamicRelationshipType.withName(name);
	}
}

/**
 * jo4neo is a java object binding library for neo4j
 * Copyright (C) 2009  Taylor Cowan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */