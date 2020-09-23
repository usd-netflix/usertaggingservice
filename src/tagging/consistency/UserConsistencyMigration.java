package tagging.consistency;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.*;

import tagging.User;
import tagging.UserTagDatabase;

/**
 * This is a command line script to run a consistency migration on the User database.
 * By default it runs in dry mode (validation only) unless the live (-l) option is added, 
 * in which case it will also run the rule's fixer.
 * 
 * Run with help (-h) option for more detailed usage information.
 * 
 * @author amerhesson
 *
 */
final class UserConsistencyMigration {

	private static Options generateOptions() {
		Options options = new Options();
		options.addOption("r", "rule", true, "Consistency rule class name to validate, e.g 'NANDConsistencyRule'. If unspecified, all rules will be validated.")
				.addOption("l", "do-it-live", false,
						"Run the consistency migration live on production data. If unspecified, the migration will run in 'validation-only mode'.")
				.addOption("m", true, "Max number of entities to process. If unspecified, all entities will be processed.")
				.addOption("h", false, "Print usage information.");
		return options;
	}

	private static CommandLine generateCommandLine(final Options options, final String[] commandLineArguments) {
		final CommandLineParser cmdLineParser = new DefaultParser();
		CommandLine commandLine = null;
		try {
			commandLine = cmdLineParser.parse(options, commandLineArguments);
		} catch (ParseException parseException) {
			System.out.println("ERROR: Unable to parse command-line arguments " + Arrays.toString(commandLineArguments)
					+ " due to: " + parseException);
		}
		return commandLine;
	}

	private static void printHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(UserConsistencyMigration.class.getSimpleName(), options);
	}

	public static void main(String[] args)
			throws ClassNotFoundException, ConsistencyFixerViolationException, ConsistencyRuleViolationException {
		Options options = generateOptions();
		CommandLine commandLine = generateCommandLine(options, args);
		if (commandLine.hasOption('h')) {
			printHelp(options);
			return;
		}

		boolean doItLive = commandLine.hasOption('l');
		Integer maxNumberToProcess = null;
		Class<?> targetRuleClass = null;
		if (commandLine.hasOption('m')) {
			maxNumberToProcess = Integer.parseInt(commandLine.getOptionValue('m'));
		}
		if (commandLine.hasOption('r')) {
			String targetRuleClassName = commandLine.getOptionValue('r');
			targetRuleClass = Class.forName(ConsistencyRule.class.getPackageName() + "." + targetRuleClassName);
		}

		UserTagDatabase udb = new UserTagDatabase();
		List<User> allUsers = udb.getAllUsers();
		udb.close();

		System.out.println(String.format("Checking %d users.", allUsers.size()));
		int i = 0;
		for (User user : allUsers) {
			if (maxNumberToProcess != null && i >= maxNumberToProcess) {
				break;
			}

			Set<ConsistencyRule> consistencyRules = user.getConsistencyRules();
			for (ConsistencyRule rule : consistencyRules) {
				if (targetRuleClass == null || targetRuleClass == rule.getClass()) {
					boolean evaluation = rule.evaluate(user);
					System.out.println(String.format(
							"Validating user %-10s (ID: %-24s) with tags %-15s for rule %-10s %-10s: %6s.",
							user.getName(), user.getID(), user.getTags(), rule.getClass().getSimpleName(),
							rule.getTags(), evaluation ? "OK" : "INVALID"));
					if (!evaluation && doItLive) {
						System.out.println("Attempting to fix...");
						rule.fix(user);
						boolean afterFixEvaluation = rule.evaluate(user);
						if (afterFixEvaluation) {
							System.out.println("OK.");
						}
						assert afterFixEvaluation : "Fixer failed to make entity consistent.";
					}
				} else if (targetRuleClass != null) {
					System.out.println(String.format(
							"User %-10s (ID: %-24s) has rule %s which is not targeted. Skipped.",
							user.getName(), user.getID(), rule.getClass().getSimpleName()));
				}
			}

			i++;
		}

	}
}
