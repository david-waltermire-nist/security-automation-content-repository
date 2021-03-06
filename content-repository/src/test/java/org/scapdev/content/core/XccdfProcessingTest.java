/*******************************************************************************
 * The MIT License
 * 
 * Copyright (c) 2011 davidwal
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
package org.scapdev.content.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scapdev.content.core.persistence.ContentPersistenceManager;
import org.scapdev.content.core.persistence.hybrid.MemoryResidentHybridContentPersistenceManager;
import org.scapdev.content.core.query.SimpleQuery;
import org.scapdev.content.model.Key;
import org.scapdev.content.model.KeyBuilder;

public class XccdfProcessingTest extends ContentRepositoryTestBase {
	private static final Logger log = Logger.getLogger(XccdfProcessingTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	StopWatch watch = new StopWatch();
    	watch.start();
    	repository = new ContentRepository();
		ContentPersistenceManager persistenceManager = new MemoryResidentHybridContentPersistenceManager(); 
		repository.setContentPersistenceManager(persistenceManager);
    	watch.stop();
    	log.info("Repository startup: "+watch.toString());

    	importer = repository.getJaxbEntityProcessor().newImporter();
    	writer = repository.newInstanceWriter();
	}

	@Test
	public void importUSGCBWin7OVALContent() {
		File file = new File("src/test/xml/USGCB-Major-Version-1.1.0.0/Win7/USGCB-Windows-7-xccdf.xml");
		importFile(file);
	}
	
	@Test
	public void writeWin7Definition() throws IOException, XMLStreamException, FactoryConfigurationError {
		KeyBuilder keyBuilder = new KeyBuilder();
		keyBuilder.setId("urn:scap-content:key:gov.nist.checklists:xccdf-1.1-benchmark");
		keyBuilder.addKeyField("urn:scap-content:field:gov.nist.checklists:xccdf-1.1-benchmark", "USGCB-Windows-7");
    	Key key = keyBuilder.toKey();
		SimpleQuery query = new SimpleQuery(key);
		query.setResolveReferences(true);
		writeQuery(query);
	}

	@Test
	public void writeSpecificCCEs() throws IOException, XMLStreamException, FactoryConfigurationError {
    	List<String> cces = new LinkedList<String>();
    	cces.add("CCE-9345-0");
    	cces.add("CCE-8414-5");

		IndirectQuery query = new IndirectQuery("urn:scap-content:external-identifier:org.mitre:cce-5", cces, Collections.singleton("urn:scap-content:entity:org.mitre.oval:definition"), repository.getContentPersistenceManager());
		query.setResolveReferences(true);
		writeQuery(query);
	}

}
