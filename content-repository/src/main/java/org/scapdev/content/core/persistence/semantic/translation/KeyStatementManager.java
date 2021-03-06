/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2011 Paul Cichonski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
/**
 * 
 */
package org.scapdev.content.core.persistence.semantic.translation;

import java.util.LinkedHashMap;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.scapdev.content.core.persistence.semantic.MetaDataOntology;
import org.scapdev.content.model.Key;
import org.scapdev.content.model.KeyBuilder;
import org.scapdev.content.model.KeyException;
import org.scapdev.content.model.RelationshipInfo;

/**
 * <p>A manager to coordinate the building of Key from triples originating from a specific
 * entity.</p>
 * 
 * @see Key
 */
class KeyStatementManager implements RegenerationStatementManager {
	private MetaDataOntology ontology;
	
	private KeyBuilder builder = new KeyBuilder();
	
	private LinkedHashMap<String, Field> fieldUriToFieldMap = new LinkedHashMap<String, Field>();
	
	KeyStatementManager(MetaDataOntology ontology) {
		this.ontology = ontology;
	}
	
	public boolean scan(Statement statement){
		URI predicate = statement.getPredicate();
		if (predicate.equals(ontology.HAS_KEY.URI)){
			return true;  // return true to claim ownership of statement, nothing to do though
		}
		if (predicate.equals(ontology.HAS_KEY_TYPE.URI)){
			populateKeyType(statement);
			return true;
		}
		if (predicate.equals(ontology.HAS_FIELD_DATA.URI)){
			populateFieldEntry(statement);
			return true;
		}
		if (predicate.equals(ontology.HAS_FIELD_TYPE.URI)){
			populateFieldType(statement);
			return true;
		}
		if (predicate.equals(ontology.HAS_FIELD_VALUE.URI)){
			populateFieldValue(statement);
			return true;
		}
		return false;
	}
	
	/**
	 * <p>
	 * Called after all triples are processed to populate re-constituted entity
	 * with the Key found in the graph.
	 * </p>
	 * 
	 * @param entity
	 *            - to populate.
	 */
	public void populateEntity(RebuiltEntity entity){
		populateFields();
		Key key = builder.toKey();
		entity.setKey(key);
	}

	/**
	 * Call to produce the key without populating any entity. TOOD: if this is a
	 * normal behavior pattern it should be added to interface.
	 * 
	 * @return
	 */
	Key produceKey(){
		populateFields();
		try {
			return builder.toKey();
		} catch (KeyException e) {
			throw new IncompleteBuildStateException(e);
		}
	}
	
	
	private void populateFields(){
		for (Field field : fieldUriToFieldMap.values()){
			builder.addKeyField(field.getFieldId(), field.getFieldValue());
		}
	}

	
	/**
	 * <p>
	 * Helper method to populate KeyType (or ID) based on the predicate found
	 * within the triple. Assumes Statement passed in contains a predicate that
	 * is HAS_KEY_TYPE.
	 * </p>
	 * 
	 * @param statement
	 * 
	 * @see RelationshipInfo
	 */
	private void populateKeyType(Statement statement){
		String keyType = statement.getObject().stringValue();
		builder.setId(keyType);
	}
	
	/**
	 * <p>
	 * Helper method to populate a single fieldEntry based on the predicate found
	 * within the triple. Assumes Statement passed in contains a predicate that
	 * is HAS_FIELD_DATA.
	 * </p>
	 * 
	 * @param statement
	 * 
	 * @see RelationshipInfo
	 */
	private void populateFieldEntry(Statement statement){
		String fieldURI = statement.getObject().stringValue();
		Field fieldEntry = fieldUriToFieldMap.get(fieldURI);
		if (fieldEntry == null){
			fieldUriToFieldMap.put(fieldURI, new Field());
		}
	}
	
	/**
	 * <p>
	 * Helper method to populate a field type based on the predicate found
	 * within the triple. Assumes Statement passed in contains a predicate that
	 * is a subProperty of HAS_FIELD_TYPE.
	 * </p>
	 * 
	 * @param statement
	 * 
	 * @see RelationshipInfo
	 */
	private void populateFieldType(Statement statement){
		String fieldURI = statement.getSubject().stringValue();
		String fieldType = statement.getObject().stringValue();
		Field fieldEntry = fieldUriToFieldMap.get(fieldURI);
		if (fieldEntry == null){
			fieldEntry = new Field();
			fieldEntry.setFieldId(fieldType);
			fieldUriToFieldMap.put(fieldURI, fieldEntry);
		} else {
			//can't assume it was set already
			fieldEntry.setFieldId(fieldType);
		}
	}
	
	/**
	 * <p>
	 * Helper method to populate a field value based on the predicate found
	 * within the triple. Assumes Statement passed in contains a predicate that
	 * is a subProperty of HAS_FIELD_VALUE.
	 * </p>
	 * 
	 * @param statement
	 * 
	 * @see RelationshipInfo
	 */
	private void populateFieldValue(Statement statement){
		String fieldURI = statement.getSubject().stringValue();
		String fieldValue = statement.getObject().stringValue();
		Field fieldEntry = fieldUriToFieldMap.get(fieldURI);
		if (fieldEntry == null){
			fieldEntry = new Field();
			fieldEntry.setFieldValue(fieldValue);
			fieldUriToFieldMap.put(fieldURI, fieldEntry);
		} else {
			//can't assume it was set already
			fieldEntry.setFieldValue(fieldValue);
		}
	}
	
	private static class Field {
		private String fieldId;
		private String fieldValue;
		
		Field() {
			// TODO Auto-generated constructor stub
		}
		void setFieldId(String fieldId) {
			this.fieldId = fieldId;
		}
		
		String getFieldId() {
			return fieldId;
		}
		
		void setFieldValue(String fieldValue) {
			this.fieldValue = fieldValue;
		}
		
		String getFieldValue() {
			return fieldValue;
		}
		
	}
	
}
