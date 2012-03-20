package gov.nist.scap.content.model.definitions;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;

public class DefaultBoundaryIdentifierRelationshipDefinition extends
		AbstractRelationshipDefinition implements
		IBoundaryIdentifierRelationshipDefinition {
	private final XPathRetriever valueRetriever;
	private final IExternalIdentifierMapping qualifierMapping;

	public DefaultBoundaryIdentifierRelationshipDefinition(ISchemaDefinition schema, String id,
			String xpath, String valueXpath, IExternalIdentifierMapping qualifierMapping) throws XmlException {
		super(schema, id, xpath);

		if (valueXpath == null) {
			throw new NullPointerException("valueXpath");
		}

		if (qualifierMapping == null) {
			throw new NullPointerException("qualifierMapping");
		}
		this.valueRetriever = new XPathRetriever(XmlBeans.compilePath(valueXpath));
		this.qualifierMapping = qualifierMapping;
	}

	public XPathRetriever getValueRetriever() {
		return valueRetriever;
	}

	public IExternalIdentifierMapping getQualifierMapping() {
		return qualifierMapping;
	}

	public void accept(IRelationshipDefinitionVisitor visitor) throws ProcessingException {
		visitor.visit(this);
	}

}
