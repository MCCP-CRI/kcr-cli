import edu.uky.kcr.cli.CliParser;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class MainTest
{
	public static final String[] NULL_ARGS = null;
	public static final String HELP_SHORT_ARG = String.format("-%s", CliParser.SHORT_OPTION_HELP);
	public static final String HELP_LONG_ARG = String.format("--%s", CliParser.LONG_OPTION_HELP);
	public static final String VERSION_SHORT_ARG = String.format("-%s", CliParser.SHORT_OPTION_VERSION);
	public static final String VERSION_LONG_ARG = String.format("--%s", CliParser.LONG_OPTION_VERSION);
	public static final String LOGGING_SHORT_ARG = String.format("-%s", CliParser.SHORT_OPTION_LOGGING);
	public static final String[] HELP_SHORT_ARGS = new String[]
			{
					HELP_SHORT_ARG
			};

	public static final String[] HELP_LONG_ARGS = new String[]
			{
					HELP_LONG_ARG
			};

	public static final String CMD_LINE_SYNTAX = "java-jar myjar-bin.jar [OPTIONS]";

	@Test
	public void testNullArgsNoOptions()
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteArrayOutputStream, true);
		System.setOut(printStream);

		CliParser cliParser = new CliParser(CMD_LINE_SYNTAX);

		if (cliParser.parse(NULL_ARGS))
		{

		}

		String output = byteArrayOutputStream.toString().trim();
		Assert.assertTrue(StringUtils.isEmpty(output));
	}

	@Test
	public void testShortHelpArgsNoOptions()
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteArrayOutputStream, true);
		System.setOut(printStream);

		CliParser cliParser = new CliParser(CMD_LINE_SYNTAX);

		if (cliParser.parse(HELP_SHORT_ARGS))
		{

		}

		String output = byteArrayOutputStream.toString();
		Assert.assertTrue(StringUtils.contains(output, VERSION_LONG_ARG));
		Assert.assertTrue(StringUtils.contains(output, HELP_SHORT_ARG));
		Assert.assertTrue(StringUtils.contains(output, HELP_LONG_ARG));
		Assert.assertTrue(StringUtils.contains(output, VERSION_SHORT_ARG));
		Assert.assertTrue(StringUtils.contains(output, LOGGING_SHORT_ARG));
	}

	@Test
	public void testLongHelpArgsNoOptions()
	{
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(byteArrayOutputStream, true);
		System.setOut(printStream);

		CliParser cliParser = new CliParser(CMD_LINE_SYNTAX);

		if (cliParser.parse(HELP_LONG_ARGS))
		{

		}

		String output = byteArrayOutputStream.toString();
		Assert.assertTrue(StringUtils.contains(output, VERSION_LONG_ARG));
		Assert.assertTrue(StringUtils.contains(output, HELP_SHORT_ARG));
		Assert.assertTrue(StringUtils.contains(output, HELP_LONG_ARG));
		Assert.assertTrue(StringUtils.contains(output, VERSION_SHORT_ARG));
		Assert.assertTrue(StringUtils.contains(output, LOGGING_SHORT_ARG));
	}

}
