package tagging.parsing;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import tagging.Tag;
import tagging.User;
import tagging.UserTagDatabase;
import tagging.UserTaggingSystemClient;
import tagging.consistency.ConsistencyRuleViolationException;

enum Command {
	CREATE, ADD, REMOVE, SHOW, HELP,
}

enum CreateParameter {
	USER,
}

enum AddParameter {
	TAGS,
}

enum RemoveParameter {
	TAGS,
}

enum ShowParameter {
	USER, USERS, TAGS,
}

public final class CommandParser {
	public static String parseLine(String line) throws CommandParserException {
		line = line.trim();
		StringTokenizer tokenizer = new StringTokenizer(line);
		if (tokenizer.countTokens() == 0 || line.equals(UserTaggingSystemClient.EXIT_COMMAND)) {
			return "";
		}
		Command command = getCommand(tokenizer.nextToken());
		return switch (command) {
		case CREATE -> {
			yield create(tokenizer);
		}
		case ADD -> {
			yield add(tokenizer);
		}
		case REMOVE -> {
			yield remove(tokenizer);
		}
		case SHOW -> {
			yield show(tokenizer);
		}
		case HELP -> {
			yield help();
		}
		};
	}

	private static Command getCommand(String command) throws CommandParserException {
		try {
			return Command.valueOf(command.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Unexpected command \"%s\".", command));
		}
	}

	private static String create(StringTokenizer tokenizer) throws CommandParserException {
		if (tokenizer.countTokens() == 0) {
			throw new CommandParserException("Expected more parameters for this command.");
		}
		String parameter = tokenizer.nextToken();
		CreateParameter createParameter;
		try {
			createParameter = CreateParameter.valueOf(parameter.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Unexpected parameter \"%s\".", parameter));
		}

		return switch (createParameter) {
		case USER -> {
			Set<String> tagNames = new HashSet<String>();
			if (tokenizer.countTokens() == 0) {
				throw new CommandParserException("Expected more parameters for this command.");
			}
			String name = tokenizer.nextToken();
			while (tokenizer.hasMoreTokens()) {
				tagNames.add(tokenizer.nextToken());
			}
			yield createUser(name, tagNames);
		}
		};
	}

	private static String createUser(String name, Set<String> tagNames) throws CommandParserException {
		Set<Tag> tags = getTagsFromTagNames(tagNames);
		User newUser;
		try {
			newUser = User.create(name, tags);
		} catch (ConsistencyRuleViolationException e) {
			return String.format("[Error] %s", e.getMessage());
		}
		return String.format("[Success] Created new user \"%s\" with ID: %s and tags %s.", newUser.getName(),
				newUser.getID(), newUser.getTags());
	}

	private static String add(StringTokenizer tokenizer) throws CommandParserException {
		if (tokenizer.countTokens() == 0) {
			throw new CommandParserException("Expected more parameters for this command.");
		}
		String parameter = tokenizer.nextToken();
		AddParameter addParameter;
		try {
			addParameter = AddParameter.valueOf(parameter.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Unexpected parameter \"%s\".", parameter));
		}

		return switch (addParameter) {
		case TAGS -> {
			if (tokenizer.countTokens() < 2) {
				throw new CommandParserException("Expected more parameters for this command.");
			}
			Set<String> tagNames = new HashSet<>();
			while (tokenizer.hasMoreTokens() && tokenizer.countTokens() > 1) {
				tagNames.add(tokenizer.nextToken());
			}
			yield addTags(tagNames, tokenizer.nextToken());
		}
		};
	}

	private static String addTags(Set<String> tagNames, String userID) throws CommandParserException {
		User user = getUser(userID);
		Set<Tag> tags = getTagsFromTagNames(tagNames);
		boolean successful = false;
		try {
			successful = user.addTags(tags);
		} catch (ConsistencyRuleViolationException e) {
			return String.format("[Error] %s", e.getMessage());
		}

		if (successful) {
			return String.format("[Success] Added tags %s to user \"%s\" (ID: %s).", tags, user.getName(),
					user.getID());
		}
		return String.format(
				"[Failure] Tags %s could not be added to user \"%s\" (ID: %s). Most likely they all already exist.",
				tags, user.getName(), user.getID());

	}

	private static String remove(StringTokenizer tokenizer) throws CommandParserException {
		if (tokenizer.countTokens() == 0) {
			throw new CommandParserException("Expected more parameters for this command.");
		}
		String parameter = tokenizer.nextToken();
		RemoveParameter removeParameter;
		try {
			removeParameter = RemoveParameter.valueOf(parameter.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Unexpected parameter \"%s\".", parameter));
		}

		return switch (removeParameter) {
		case TAGS -> {
			if (tokenizer.countTokens() < 2) {
				throw new CommandParserException("Expected more parameters for this command.");
			}
			Set<String> tagNames = new HashSet<>();
			while (tokenizer.hasMoreTokens() && tokenizer.countTokens() > 1) {
				tagNames.add(tokenizer.nextToken());
			}
			yield removeTags(tagNames, tokenizer.nextToken());
		}
		};
	}

	private static String removeTags(Set<String> tagNames, String userID) throws CommandParserException {
		User user = getUser(userID);
		Set<Tag> tags = getTagsFromTagNames(tagNames);
		boolean successful = false;
		try {
			successful = user.removeTags(tags);
		} catch (ConsistencyRuleViolationException e) {
			return String.format("[Error] %s", e.getMessage());
		}

		if (successful) {
			return String.format("[Success] Removed tags %s from user \"%s\" (ID: %s).", tags, user.getName(),
					user.getID());
		}
		return String.format(
				"[Failure] Tags %s were not removed from user \"%s\" (ID: %s). Most likely they didn't exist.", tags,
				user.getName(), user.getID());

	}

	private static String show(StringTokenizer tokenizer) throws CommandParserException {
		if (tokenizer.countTokens() == 0) {
			throw new CommandParserException("Expected more parameters for this command.");
		}
		String parameter = tokenizer.nextToken();
		ShowParameter showParameter;
		try {
			showParameter = ShowParameter.valueOf(parameter.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Unexpected parameter \"%s\".", parameter));
		}
		int tokensRemaining = tokenizer.countTokens();

		return switch (showParameter) {
		case USER -> {
			if (tokensRemaining == 0) {
				throw new CommandParserException("Expected additional userID parameter for this command.");
			} else if (tokensRemaining > 1) {
				throw new CommandParserException("Too many parameters passed for this command.");
			}
			yield showUser(tokenizer.nextToken());
		}
		case USERS -> {
			if (tokenizer.countTokens() != 0) {
				throw new CommandParserException("Too many parameters passed for this command.");
			}
			yield showAllUsers();
		}
		case TAGS -> {
			if (tokenizer.countTokens() != 0) {
				throw new CommandParserException("Too many parameters passed for this command.");
			}
			yield showAllTags();
		}
		};
	}

	private static String showUser(String userID) throws CommandParserException {
		User user = getUser(userID);
		return String.format("[Success] ID: %s | Name: %s | Tags: %s", user.getID(), user.getName(), user.getTags());
	}

	private static String showAllUsers() throws CommandParserException {
		UserTagDatabase udb = new UserTagDatabase();
		String allUsers = udb.getAllUsersAsString();
		udb.close();
		return allUsers;
	}

	private static String showAllTags() throws CommandParserException {
		UserTagDatabase udb = new UserTagDatabase();
		String allTags = udb.getAllTagsAsString();
		udb.close();
		return allTags;
	}

	private static String help() throws CommandParserException {
		Set<String> enumVals = EnumSet.allOf(Command.class).stream().map(enumVal -> enumVal.toString().toLowerCase())
				.collect(Collectors.toSet());
		String helpText = String.format("Possible commands are: %s\n", enumVals);

		for (String enumVal : enumVals) {
			Command command = Command.valueOf(enumVal.toUpperCase());
			switch (command) {
			case CREATE:
				helpText = helpText.concat("create user <name> <tag1> <tag2> <...> <tagN>\n");
				break;
			case ADD:
				helpText = helpText.concat("add tags <tag1> <tag2> <...> <tagN> <userID>\n");
				break;
			case REMOVE:
				helpText = helpText.concat("remove tags <tag1> <tag2> <...> <tagN> <userID>\n");
				break;
			case SHOW:
				helpText = helpText.concat("show user <userID>\n");
				helpText = helpText.concat("show users\n");
				helpText = helpText.concat("show tags\n");
				break;
			case HELP:
				helpText = helpText.concat("help\n");
				break;
			}
		}

		return helpText;
	}

	private static User getUser(String userID) throws CommandParserException {
		User user;
		try {
			user = User.fromID(userID);
		} catch (IllegalArgumentException e) {
			throw new CommandParserException(String.format("Invalid userID \"%s\".", userID));
		}
		return user;
	}

	private static Set<Tag> getTagsFromTagNames(Set<String> tagNames) throws CommandParserException {
		Set<Tag> tags = tagNames.stream().map(tagName -> Tag.fromName(tagName)).collect(Collectors.toSet());
		if (tags.contains(null)) {
			throw new CommandParserException("Invalid tag name passed.");
		}
		return tags;
	}
}
