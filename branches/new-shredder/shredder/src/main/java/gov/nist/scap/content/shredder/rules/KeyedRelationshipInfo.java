package gov.nist.scap.content.shredder.rules;

import gov.nist.scap.content.model.IMutableEntity;
import gov.nist.scap.content.shredder.model.DefaultKeyedRelationship;
import gov.nist.scap.content.shredder.model.IKey;
import gov.nist.scap.content.shredder.model.IKeyedEntity;

public class KeyedRelationshipInfo {
	private final IKeyedRelationshipDefinition definition;
	private final IMutableEntity<?> containingEntity;
	private final IKey key;

	public KeyedRelationshipInfo(IKeyedRelationshipDefinition definition, IMutableEntity<?> containingEntity, IKey key) {
		this.definition = definition;
		this.containingEntity = containingEntity;
		this.key = key;
	}

	public IKey getKey() {
		return key;
	}
	
	public void applyRelationship(IKeyedEntity<?> referencedEntity) {
		containingEntity.addRelationship(new DefaultKeyedRelationship(definition, containingEntity, referencedEntity));
	}
}
