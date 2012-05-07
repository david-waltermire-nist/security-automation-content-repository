package gov.nist.scap.content.server;

import static org.scapdev.content.core.query.Conditional.allOf;
import static org.scapdev.content.core.query.entity.EntityQuery.selectEntitiesWith;
import static org.scapdev.content.core.query.relationship.Relationship.relationship;
import static org.scapdev.content.core.query.relationship.ToBoundaryIdentifier.toBoundaryIdentifier;
import gov.nist.scap.content.model.IEntity;
import gov.nist.scap.content.model.definitions.ProcessingException;
import gov.nist.scap.content.semantic.TripleStoreFacadeMetaDataManager;
import gov.nist.scap.content.semantic.entity.EntityProxy;
import gov.nist.scap.content.shredder.parser.ContentHandler;
import gov.nist.scap.content.shredder.parser.ContentShredder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.xmlbeans.XmlException;
import org.openrdf.repository.RepositoryException;
import org.scapdev.content.core.ContentException;
import org.scapdev.content.core.persistence.ContentPersistenceManager;
import org.scapdev.content.core.query.entity.EntityQuery;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Path("/")
public class ContentRepoRest {

	public static String CONTENT_STORE_CLASS = "";
	public static String METADATA_STORE_CLASS = "";

	private ContentPersistenceManager contentRepo;
	private ContentShredder shredder;

	private static ConfigurableApplicationContext context;

	static {
		context = new ClassPathXmlApplicationContext(
				new String[] { "spring-beans.xml" });
		context.registerShutdownHook();
	}

	public ContentRepoRest() {
		contentRepo = (ContentPersistenceManager) context
				.getBean("defaultLocalRepository");
		shredder = (ContentShredder) context.getBean("defaultContentShredder");
	}

	@POST
	@Path("submit")
	@Consumes("text/xml")
	@Produces("text/xml")
	public Object submit(InputStream is) {

		try {
			ContentHandler handler = (ContentHandler) context
					.getBean("defaultContentHandler");
			shredder.shred(is, handler);
			List<String> storedEntities = contentRepo.storeEntities(handler
					.getEntities());

			return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<content-id>"
					+ storedEntities.get(storedEntities.size() - 1)
					+ "</content-id>";
		} catch (XmlException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		} catch (ProcessingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		} catch (ContentException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}
	}

	@POST
	@Path("sparql-query")
	@Produces("text/xml")
	public Object sparqlQuery(String sparqlQuery) {
		// TODO delete this once testing is complete
		try {
			TripleStoreFacadeMetaDataManager tsfmdm = (TripleStoreFacadeMetaDataManager) context
					.getBean("defaultLocalMetadataStore");
			List<SortedMap<String, String>> r;
			r = tsfmdm.runSPARQL(sparqlQuery);

			StringBuilder returnString = new StringBuilder();
			returnString.append("<results>\n");

			for (SortedMap<String, String> m : r) {
				returnString.append("  <result>\n");
				for (String key : m.keySet()) {
					returnString.append("    <key>" + key + "</key><value>"
							+ m.get(key) + "</value>\n");
				}
				returnString.append("  </result>\n");
			}

			returnString.append("</result>\n");
			return returnString.toString();
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}

	}

	@GET
	@Path("test")
	@Produces("text/xml")
	public String test() {
		// TODO delete this once testing is complete
		TripleStoreFacadeMetaDataManager tsfmdm = (TripleStoreFacadeMetaDataManager) context
				.getBean("defaultLocalMetadataStore");

		EntityQuery query = selectEntitiesWith(allOf(
		// anyOf(
		// entityType("http://oval.mitre.org/resource/content/definition/5#content-node-test"),
		// entityType("http://oval.mitre.org/resource/content/definition/5#content-node-definition")
		// ),
		relationship(
		// anyOf(
		// relationshipType("http://scap.nist.gov/resource/content/model#hasParentRelationshipTo"),
		toBoundaryIdentifier(
				"http://cce.mitre.org/resource/content/cce#external-identifier-cce-5",
				"CCE-13091-4")
		// )
		)));
		try {
			@SuppressWarnings("unchecked")
			Collection<? extends IEntity<?>> results = (Collection<? extends IEntity<?>>) tsfmdm
					.getEntities(query, EntityProxy.class);
			StringBuilder returnString = new StringBuilder();
			returnString.append("<results>\n");

			for (IEntity<?> entityObj : results) {
				returnString.append("  <class>");
				returnString.append(entityObj.getDefinition().getId());
				returnString.append("</class>\n");
				// returnString.append("  <uri>");
				// returnString.append(entityObj.getUri());
				// returnString.append("</uri>\n");
			}

			returnString.append("</result>\n");
			return returnString.toString();
		} catch (ProcessingException e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}

	}

	@GET
	@Path("retrieve")
	@Produces("text/xml")
	public Object retrieve(@QueryParam("content-id") String contentId) {
		try {
			IEntity<?> entity = contentRepo.getEntity(contentId);
			if (entity != null) {
				return entity.getContentHandle().getCursor().getObject()
						.newInputStream();
			} else {
				throw new WebApplicationException(204);
			}
		} catch (ProcessingException e) {
			throw new WebApplicationException(e);
		}
	}

}