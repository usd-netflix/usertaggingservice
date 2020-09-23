package tagging.consistency;

/**
 * Thrown when a fixer is run on a consistent entity.
 */
public class ConsistencyFixerViolationException extends Exception {

	private ConsistencyRule rule;

	public ConsistencyFixerViolationException(ConsistencyRule rule) {
		this.rule = rule;
	}

	@Override
	public String getMessage() {
		return String.format(
				"[%s] %s fixer invariant violated: A fixer cannot operate on an entity that is already consistent.",
				this.getClass().getSimpleName(), this.rule.getClass().getSimpleName());
	}

	// Generated
	private static final long serialVersionUID = -2809353749157128850L;
}
