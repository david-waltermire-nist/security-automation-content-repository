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
package org.scapdev.jaxb.reflection.model.visitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.scapdev.jaxb.reflection.model.JAXBClass;
import org.scapdev.jaxb.reflection.model.JAXBModel;
import org.scapdev.jaxb.reflection.model.JAXBProperty;

public class PropertyPathModelVisitor extends DefaultModelVisitor {
	private final LinkedList<JAXBProperty> propertyPath = new LinkedList<JAXBProperty>();

	public PropertyPathModelVisitor(JAXBModel model) {
		super(model);
	}

	public List<JAXBProperty> getPropertyPath() {
		return new ArrayList<JAXBProperty>(propertyPath);
	}

	public final JAXBProperty getCurrentJAXBProperty() {
		return propertyPath.peekLast();
	}

	public JAXBClass getCurrentTypeInfo() {
		return getCurrentJAXBProperty().getEnclosingJAXBClass();
	}

	@Override
	protected void processJaxbProperty(JAXBProperty property) {
		pushPropertyInfo(property);
		super.processJaxbProperty(property);
		popPropertyInfo(property);
	}

	private void popPropertyInfo(JAXBProperty info) {
		JAXBProperty poppedInfo = propertyPath.pollLast();
		assert(poppedInfo == info);
	}

	private void pushPropertyInfo(JAXBProperty info) {
		propertyPath.addLast(info);
	}
}
