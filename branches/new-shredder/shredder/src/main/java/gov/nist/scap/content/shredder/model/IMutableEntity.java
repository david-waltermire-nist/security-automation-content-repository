package gov.nist.scap.content.shredder.model;

import gov.nist.scap.content.shredder.rules.IEntityDefinition;

public interface IMutableEntity<DEFINITION extends IEntityDefinition> extends IContainer<DEFINITION> {
	void appendRelationship(IRelationship<?> relationship);
	void appendRelationship(IKeyedRelationship relationship);
	void appendRelationship(IIndirectRelationship relationship);
//	/**
//	 * 
//	 * @return the key for the entity if it is indexed or <code>null</code> otherwise
//	 */
//	IKey getKey();
}