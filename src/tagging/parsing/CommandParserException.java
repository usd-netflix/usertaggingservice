package tagging.parsing;

public class CommandParserException extends Exception {

	public CommandParserException(String message) {
		super(message);
	}

	public String getMessage() {
		return "[Command Error]: " + super.getMessage();
	}

	// Generated
	private static final long serialVersionUID = 4756825017823185245L;
}
