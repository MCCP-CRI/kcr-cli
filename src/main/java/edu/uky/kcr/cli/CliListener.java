package edu.uky.kcr.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.util.List;

/**
 * Listener interface for various callback events that happen during parsing of command-line arguments.
 */
public interface CliListener
{
	/**
	 * Called once during parsing when no "naked" arguments were found in the command-line arguments, that is,
	 * arguments that do not have an option name.
	 * @param cliParser
	 * @throws ParseException
	 */
	void handleNoArguments(CliParser cliParser)
			throws ParseException;

	/**
	 * Called once during parsing, when no options were found in the command-line arguments, in other words,
	 * all Options are missing. (An option is a command-line argument that has an option name.)
	 * @param cliParser
	 * @throws ParseException
	 */
	void handleNoOptions(CliParser cliParser)
			throws ParseException;

	/**
	 * Called once during parsing when some options were missing from the command-line arguments.
	 * {@code emptyOptions} contains a list of all Option objects that were not found as command-line arguments.
	 * @param emptyOptions
	 * @param cliParser
	 * @throws ParseException
	 */
	void handleMissingOptions(List<Option> emptyOptions,
							  CliParser cliParser)
			throws ParseException;

	/**
	 * Called once during parsing when "naked" arguments were passed on the command-line.
	 * @param argList
	 * @param cliParser
	 * @throws ParseException
	 */
	void handleParsedArgumentList(List<String> argList,
								  CliParser cliParser)
			throws ParseException;

	/**
	 * Called once for every option that was specified on the command-line.
	 * @param opt
	 * @param parsedValues
	 * @param cliParser
	 * @throws ParseException
	 */
	void handleParsedOption(String opt,
							String[] parsedValues,
							CliParser cliParser)
			throws ParseException;

	/**
	 * Called if a {@link ParseException} is thrown during parsing of command-line arguments. The default handler will
	 * print out an application help string and print the error in System.err. Override this method if you want to
	 * handle parsing exceptions some other way.
	 * @param exception
	 * @param cliParser
	 */
	void handleParseException(ParseException exception,
							  CliParser cliParser);
}
