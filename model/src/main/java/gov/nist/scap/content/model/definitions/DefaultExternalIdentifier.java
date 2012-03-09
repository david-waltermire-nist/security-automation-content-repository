package gov.nist.scap.content.model.definitions;

import java.util.regex.Pattern;

public class DefaultExternalIdentifier implements IExternalIdentifier {
	private final String id;
	private final Pattern pattern;

	public DefaultExternalIdentifier(String id, Pattern pattern) {
		this.id = id;
		this.pattern = pattern;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}
}