package edu.uky.kcr.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import java.util.List;

/**
 * Default implementation of the {@link CliListener} interface. Extend this class and implement only the methods you
 * need, the rest of the methods do nothing except for {@link #handleParseException(ParseException, CliParser)} which
 * calls {@link CliParser#defaultHandleParseException}
 */
public class DefaultCliAdapter
		implements CliListener
{

	@Override
	public void handleNoArguments(CliParser cliParser)
			throws ParseException
	{

	}

	@Override
	public void handleNoOptions(CliParser cliParser)
			throws ParseException
	{

	}

	@Override
	public void handleMissingOptions(List<Option> emptyOptions,
									 CliParser cliParser)
			throws ParseException
	{

	}

	@Override
	public void handleParsedArgumentList(List<String> argList,
										 CliParser cliParser)
			throws ParseException
	{

	}

	@Override
	public void handleParsedOption(String opt,
								   String[] parsedValues,
								   CliParser cliParser)
			throws ParseException
	{

	}

	@Override
	public void handleParseException(ParseException exception,
									 CliParser cliParser)
	{
		cliParser.defaultHandleParseException(exception);
	}
}
