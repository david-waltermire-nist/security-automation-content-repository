package gov.nist.scap.content.shredder.rules.xmlbeans;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.exist.xmldb.RemoteXMLResource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

public class TestContentHandler implements ContentHandler {

	private Collection col;
	private OutputStream os;
	private boolean skipEndElement = false;
	private Map<String, String> namespaceMap = new HashMap<String, String>();
	private Map<String, Map<String, String>> rootNamespaces;
	private boolean isFirstElement = true;
	private String resourceId;
    private final String encoding;

	public TestContentHandler(Collection col,
			Map<String, Map<String, String>> namespaces, String resourceId, OutputStream os, String encoding) {
		this.col = col;
		this.rootNamespaces = namespaces;
		this.os = os;
		this.resourceId = resourceId;
		this.encoding = encoding;
	}

	@Override
	public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
		try {
			os.write(new String(arg0, arg1, arg2).getBytes(encoding));
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {

	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		if (!skipEndElement) {
			try {
				os.write(new String("</" + arg2 + ">").getBytes(encoding));
			} catch (IOException e) {
				throw new SAXException(e);
			}
		} else {
			skipEndElement = false;
		}
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
			throws SAXException {
		characters(arg0, arg1, arg2);
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator arg0) {
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		try {
			if ("gov:nist:scap:content-repo".equals(arg0)
					&& "xinclude".equals(arg1)) {
				String resId = arg3.getValue("resource-id");
				RemoteXMLResource res = (RemoteXMLResource) col.getResource(resId);
		        res.setLexicalHandler(new TestContentLexicalHandler(os, encoding));
				res.getContentAsSAX(new TestContentHandler(col, rootNamespaces, res.getId(),
						os, encoding));
				skipEndElement = true;
				namespaceMap.clear();
			} else {
				os.write(new String("<" + arg2 + " ").getBytes(encoding));
				for (int i = 0, size = arg3.getLength(); i < size; i++) {
					os.write((arg3.getQName(i) + "=\"" + arg3.getValue(i) + "\" ")
							.getBytes(encoding));
				}
				Map<String,String> namespaces = isFirstElement ? rootNamespaces.get(resourceId) : namespaceMap; 
				for (String prefix : namespaces.keySet()) {
					String separator = "";
					if (prefix.length() > 0)
						separator = ":";
					os.write(("xmlns" + separator + prefix + "=\""
							+ namespaceMap.get(prefix) + "\" ")
							.getBytes(encoding));
				}
				namespaceMap.clear();
				isFirstElement = false;
				os.write(">".getBytes(encoding));
			}
		} catch (IOException e) {
			throw new SAXException(e);
		} catch (XMLDBException e) {
			throw new SAXException(e);
		}

	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		namespaceMap.put(arg0, arg1);
	}

}