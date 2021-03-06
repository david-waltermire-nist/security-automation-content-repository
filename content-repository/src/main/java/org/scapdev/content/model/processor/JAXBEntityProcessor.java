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
package org.scapdev.content.model.processor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBElement;

import org.scapdev.content.core.PersistenceContext;
import org.scapdev.content.core.persistence.ContentPersistenceManager;
import org.scapdev.content.model.Entity;
import org.scapdev.content.model.JAXBRelationshipIdentifyingImportVisitor;
import org.scapdev.content.model.MetadataModel;
import org.scapdev.content.model.MutableEntity;

public class JAXBEntityProcessor implements EntityProcessor {
	private final PersistenceContext persistenceContext;
	private final ExecutorService service;

	public JAXBEntityProcessor(PersistenceContext persistenceContext) {
		this.persistenceContext = persistenceContext;
		this.service = Executors.newFixedThreadPool(2);
	}

	public void shutdown() {
		service.shutdown();
	}

	public Importer newImporter() {
		return new JAXBImporter(this);
	}

	/**
	 * @return the persistenceManager
	 */
	public ContentPersistenceManager getPersistenceManager() {
		return persistenceContext.getContentPersistenceManager();
	}

	/**
	 * @return the model
	 */
	public MetadataModel getMetadataModel() {
		return persistenceContext.getMetadataModel();
	}

	public Future<Entity> processEntity(EntityImpl entity, JAXBElement<Object> obj) {
		RelationshipExtractingTask task = new RelationshipExtractingTask(entity, obj, getMetadataModel());
		Future<Entity> future = service.submit(task);
		return future;
	}

	private static class RelationshipExtractingTask implements Callable<Entity> {
		private final MutableEntity entity;
		private final JAXBRelationshipIdentifyingImportVisitor visitor;

		RelationshipExtractingTask(MutableEntity entity, JAXBElement<Object> node, MetadataModel model) {
			this.entity = entity;
			this.visitor = new JAXBRelationshipIdentifyingImportVisitor(entity, node, model);
		}

		@Override
		public Entity call() throws Exception {
			visitor.visit();
			return entity;
		}
		
	}
}
