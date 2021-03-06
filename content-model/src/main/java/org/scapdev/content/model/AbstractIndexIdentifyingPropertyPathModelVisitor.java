/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2011 David Waltermire
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
package org.scapdev.content.model;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.scapdev.jaxb.reflection.model.JAXBClass;
import org.scapdev.jaxb.reflection.model.JAXBModel;
import org.scapdev.jaxb.reflection.model.JAXBProperty;
import org.scapdev.jaxb.reflection.model.visitor.PropertyPathModelVisitor;

abstract class AbstractIndexIdentifyingPropertyPathModelVisitor<ANNOTATION extends Annotation, FIELD extends Annotation> extends
		PropertyPathModelVisitor {
	private static final Logger log = Logger.getLogger(AbstractIndexIdentifyingPropertyPathModelVisitor.class);
	private final JAXBClass jaxbClass;
	private final ANNOTATION indexedAnnotation;
	private final Map<String, List<JAXBProperty>> propertyMap;
	private final Class<FIELD> fieldClass;

	public AbstractIndexIdentifyingPropertyPathModelVisitor(ANNOTATION indexedAnnotation, JAXBClass jaxbClass, JAXBModel model, Class<ANNOTATION> annotationClass, Class<FIELD> fieldClass) {
		super(model);
		if (jaxbClass.getAnnotation(annotationClass, false) == null) {
			ModelException e = new ModelException("Type '"+jaxbClass.getType().getName()+"' does not contain a "+indexedAnnotation.getClass());
			log.error("Unable to find indexed annotation", e);
			throw e;
		}
		this.jaxbClass = jaxbClass;
		propertyMap = new LinkedHashMap<String, List<JAXBProperty>>();
		this.indexedAnnotation = indexedAnnotation;
		this.fieldClass = fieldClass;
	}

	protected ANNOTATION getIndexedAnnotation() {
		return indexedAnnotation;
	}

	/**
	 * @return the propertyMap
	 */
	public Map<String, List<JAXBProperty>> getPropertyMap() {
		return propertyMap;
	}

	protected abstract List<String> getIndexedFields();
	protected abstract String getIndexedAnnotationId();
	protected abstract String getIndexedFieldId(FIELD field);

	public void visit() {
		visit(jaxbClass);

		Set<String> fields = new HashSet<String>(getIndexedFields());
		Set<String> locatedFields = propertyMap.keySet();
		if (!locatedFields.equals(fields)) {
			fields.removeAll(locatedFields);
			if (!fields.isEmpty()) {
				ModelException e = new ModelException("Unable to identify fields for "+indexedAnnotation.getClass().getName()+" '"+getIndexedAnnotationId()+"': "+fields.toString());
				log.error("unable to identify index field", e);
				throw e;
			}
		}
	}

	@Override
	public boolean beforeJAXBProperty(JAXBProperty property) {
		FIELD field = property.getAnnotation(fieldClass);
		if (field != null) {
			String id = getIndexedFieldId(field);
			if (propertyMap.containsKey(id)) {
				ModelException e = new ModelException("Duplicate field found for "+indexedAnnotation.getClass().getName()+" '"+getIndexedAnnotationId()+"': "+id);
				log.error("duplicate index field", e);
				throw e;
			}
			propertyMap.put(id, getPropertyPath());
		}
		return false;
	}
}
