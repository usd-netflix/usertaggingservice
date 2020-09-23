package tagging.consistency;

/**
 * Thrown on creations/mutations that would cause an entity to become inconsistent.
 */
public class ConsistencyRuleViolationException extends Exception {

	ConsistencyRule rule;

	public ConsistencyRuleViolationException(ConsistencyRule rule) {
		this.rule = rule;
	}
	
	@Override
	public String getMessage() {
		return String.format("[%s] %s violated: %s", this.getClass().getSimpleName(), this.rule.getClass().getSimpleName(), rule.getViolationMessage());
	}

	// Generated
	private static final long serialVersionUID = 3461533466244515750L;

}
