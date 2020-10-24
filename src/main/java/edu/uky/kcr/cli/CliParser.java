package edu.uky.kcr.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class enhances the features of the <a href="https://commons.apache.org/proper/commons-cli/">Apache Commons CLI library</a>
 * by providing common-sense defaults and handlers for the following:
 * <ul>
 *     <li>timestamped console logging</li>
 *     <li>console output of help when no arguments are passed to your application</li>
 *     <li>console output of version information, read from Jar manifest attributes </li>
 *     <li>listener interface for options as they are parsed</li>
 * </ul>
 * <p>
 * Typical usage would be:
 * <ol>
 *     <li>Create CliParser instance</li>
 *     <li>Configure the CliParser using ".with...()" methods</li>
 *     <li>Call {@link #parse(String[])} from the application main() method</li>
 *     <li>Utilize command-line parameters by either implementing a {@link CliListener} or calling
 *     {@link #getParsedValue(String)}</li>
 * </ol>
 */
public class CliParser
{
	private static final Logger logger = Logger.getLogger(CliParser.class.getName());

	public static final String DEFAULT_VERSION_MANIFEST_KEY = "App-Version";
	public static final String SHORT_OPTION_HELP = "h";
	public static final String LONG_OPTION_HELP = "help";
	public static final String SHORT_OPTION_LOGGING = "l";
	public static final String SHORT_OPTION_VERSION = "v";
	public static final String LONG_OPTION_VERSION = "version";

	private String versionManifestKey = DEFAULT_VERSION_MANIFEST_KEY;

	private List<String> extraVersionInfoManifestKeys = new ArrayList<>()
	{{
		addAll(Arrays.asList(new String[]{"Build-Jdk", "Build-Time"}));
	}};

	private Map<String, String> extraVersionInfoMap = new HashMap<>();

	private boolean includeRuntimeVersionInExtraVersionInfo = true;
	private String commandLineSyntax = null;
	private String defaultLogLevel = null;
	private Option loggingOption = null;
	private boolean enableLoggingOption = true;
	private Option versionOption = null;
	private boolean enableVersionOption = true;
	private Option helpOption = null;
	private boolean enableHelpOption = true;
	private Options options = null;
	private CommandLine commandLine = null;
	private CliListener listener = null;
	private Class classForVersionManifest = null;
	private boolean enableHelpWhenNoArgumentsOrOptions = false;

	/**
	 * Constructor that takes a String to shows the syntax of how to call your application, such as:
	 * {@code new CliParser("java -jar myapp.jar <argument1> [OPTIONS]")}
	 * @param commandLineSyntax
	 */
	public CliParser(String commandLineSyntax)
	{
		setCommandLineSyntax(commandLineSyntax);
	}

	protected Class getClassForVersionManifest()
	{
		return classForVersionManifest;
	}

	public void withClassForVersionManifest(Class classForVersionManifest)
	{
		this.classForVersionManifest = classForVersionManifest;
	}

	/**
	 * @return The "naked", non-option arguments parsed from the command-line.
	 */
	public String[] getNonOptionArgs()
	{
		return getCommandLine().getArgs();
	}

	/**
	 * Accessor for {@link Option} objects on this CliParser instance, added using the ".with...()" methods
	 * @param opt The short name of an {@link Option}
	 * @return the {@link Option} object
	 */
	public Option getOption(String opt)
	{
		return getOptions().getOption(opt);
	}

	/**
	 * @param opt
	 * @return Array of parsed values from the command-line for the option with the short name, {@code opt}.
	 * Returns null if no parsed values were found.
	 */
	public String[] getParsedValues(String opt)
	{
		return getCommandLine().getOptionValues(opt);
	}

	/**
	 * @param opt
	 * @param separator
	 * @return List of parsed values from the command-line for the option with the short name, {@code opt}, split by
	 * the {@code separator} into a String array.
	 * Returns an empty List if no parsed values were found.
	 */
	public List<String[]> getParsedValuesSplit(
			String opt,
			String separator)
	{
		List<String[]> parsedValuesSplit = new ArrayList<>();

		String[] parsedValues = getParsedValues(opt);
		if (parsedValues != null)
		{
			for (int i = 0; i < parsedValues.length; i++)
			{
				parsedValuesSplit.add(StringUtils.split(parsedValues[i], separator));
			}
		}

		return parsedValuesSplit;
	}

	/**
	 * @param opt
	 * @return First parsed value from the command-line for the option with the short name, {@code opt}.
	 * Returns null if no parsed values were found.
	 */
	public String getParsedValue(String opt)
	{
		return getCommandLine().getOptionValue(opt);
	}

	/**
	 * @param opt
	 * @param separator
	 * @return First parsed value from the command-line for the option with the short name, {@code opt}, split into a
	 * String array by the {@code separator}.
	 * Returns null if no parsed values were found.
	 */
	public String[] getParsedValueSplit(
			String opt,
			String separator)
	{
		return StringUtils.split(getParsedValue(opt), separator);
	}

	/**
	 * @param opt
	 * @return The number of parsed values from the command-line for the option with the short name, {@code opt}.
	 */
	public int getParsedValueCount(String opt)
	{
		int count = 0;

		if (getCommandLine().hasOption(opt))
		{
			String[] values = getCommandLine().getOptionValues(opt);

			if (values != null)
			{
				count = values.length;
			}
		}

		return count;
	}

	/**
	 * Call this method after configuring the CliParser instance with {@link Option} objects and an optional
	 * {@link CliListener}. Pay attention to the return value and exit the application as soon as possible if it
	 * returns false, which means that parsing failed and an error message was output to the console already.
	 * @param args
	 * @return true if parsing was successful and the application should continue, false if the application should exit
	 */
	public boolean parse(String[] args)
	{
		boolean shouldContinue = true;

		if (isEnableVersionOption())
		{
			getOptions().addOption(getVersionOption());

			if (ArrayUtils.contains(args, String.format("-%s", getVersionOption().getOpt())) || ArrayUtils.contains(
					args, String.format("--%s", getVersionOption().getLongOpt())))
			{
				printVersion();
				shouldContinue = false;
			}
		}

		if (isEnableLoggingOption())
		{
			getOptions().addOption(getLoggingOption());
		}

		if (shouldContinue && isEnableHelpOption())
		{
			getOptions().addOption(getHelpOption());

			if (ArrayUtils.contains(args, String.format("-%s", getHelpOption().getOpt())) || ArrayUtils.contains(
					args,
					String.format(
							"--%s",
							getHelpOption()
									.getLongOpt())))
			{
				printHelp();
				shouldContinue = false;
			}
		}

		if (shouldContinue)
		{
			try
			{
				CommandLineParser parser = new DefaultParser();

				setCommandLine(parser.parse(getOptions(), args));

				Level level = Level.parse(
						getCommandLine().getOptionValue(getLoggingOption().getOpt(), getDefaultLogLevel()));
				ConsoleLogging.initializeConsoleLogging(level);

				logger.fine("Logging intialized to level: " + level.getName());

				if (getCommandLine().getArgList() == null || getCommandLine().getArgList().size() == 0)
				{
					getListener().handleNoArguments(this);
				}

				if (getCommandLine().getOptions() == null || getCommandLine().getOptions().length == 0)
				{
					getListener().handleNoOptions(this);
				}

				if ((getCommandLine().getOptions() == null || getCommandLine()
						.getOptions().length == 0) && (getCommandLine().getArgList() == null || getCommandLine()
						.getArgList().size() == 0) && isEnableHelpWhenNoArgumentsOrOptions())
				{
					printHelp();
					shouldContinue = false;
				}
				else
				{
					List<Option> missingOptions = new ArrayList<>();

					for (Option appOption : getOptions().getOptions())
					{
						if (getCommandLine().hasOption(appOption.getOpt()) == false)
						{
							missingOptions.add(appOption);
						}
					}

					if (missingOptions.size() > 0)
					{
						getListener().handleMissingOptions(missingOptions, this);
					}

					if ((getCommandLine().getArgList() != null) && (getCommandLine().getArgList().size() > 0))
					{
						getListener().handleParsedArgumentList(getCommandLine().getArgList(), this);
					}

					for (Iterator<Option> optionIterator = getCommandLine().iterator(); optionIterator.hasNext(); )
					{
						Option lineOption = optionIterator.next();

						getListener().handleParsedOption(lineOption.getOpt(), getParsedValues(lineOption.getOpt()),
														 this);
					}
				}
			}
			catch (ParseException exception)
			{
				getListener().handleParseException(exception, this);

				shouldContinue = false;
			}
		}

		return shouldContinue;
	}

	/**
	 * @param option A new command-line {@link Option} to parse
	 * @return
	 */
	public CliParser withOption(Option option)
	{
		getOptions().addOption(option);

		return this;
	}

	/**
	 * @param options Array of command-line options to parse
	 * @return
	 */
	public CliParser withOptions(Option... options)
	{
		for (Option option : options)
		{
			getOptions().addOption(option);
		}

		return this;
	}

	/**
	 * @param enabled Whether to enable Help output on the console with the short name of 'h'. Defaults to true.
	 * @return
	 */
	public CliParser withHelpOptionEnabled(boolean enabled)
	{
		setEnableHelpOption(enabled);

		return this;
	}

	/**
	 * @param enabled Whether to enable Version output on the console with the short name of 'v'. Defaults to true.
	 * @return
	 */
	public CliParser withVersionOptionEnabled(boolean enabled)
	{
		setEnableVersionOption(enabled);

		return this;
	}

	/**
	 * @param enabled Whether to enable control of the level of console Logging as a command-line option with the short
	 *                name of 'l'. Defaults to true.
	 * @return
	 */
	public CliParser withLoggingOptionEnabled(boolean enabled)
	{
		setEnableLoggingOption(enabled);

		return this;
	}

	/**
	 * @param listener Handler for parsing events as command-line arguments are parsed.
	 * @return
	 */
	public CliParser withListener(CliListener listener)
	{
		setListener(listener);

		return this;
	}

	/**
	 * @param key The attribute key to look for in the Jar manifest that will hold the version of this application,
	 *            defaults to "App-Version";
	 * @return
	 */
	public CliParser withVersionManifestKey(String key)
	{
		setVersionManifestKey(key);

		return this;
	}

	/**
	 * @param enabled Whether to enable output of the Java Runtime version with the application version info,
	 *                defaults to true.
	 * @return
	 */
	public CliParser withRuntimeVersionEnabled(boolean enabled)
	{
		setIncludeRuntimeVersionInExtraVersionInfo(enabled);

		return this;
	}

	/**
	 * @param level The default JUL Logging level for console output, defaults to INFO.
	 * @return
	 */
	public CliParser withDefaultLogLevel(String level)
	{
		setDefaultLogLevel(level);

		return this;
	}

	/**
	 * @param keys Array of manifest attribute keys to read and output with the application version info,
	 *             defaults to: "Build-Jdk", "Build-Time"
	 * @return
	 */
	public CliParser withExtraVersionInfoManifestKeys(String... keys)
	{
		getExtraVersionInfoManifestKeys().addAll(Arrays.asList(keys));

		return this;
	}

	/**
	 * Add extra version information to the full version string that will be reported to the user when the -v
	 * option is specified.
	 * @param key   The name of the version information provided when the app version is printed
	 * @param value The version number for the {@code key} that shows up when the app version is printed
	 * @return
	 */
	public CliParser withExtraVersionInfo(String key, String value)
	{
		getExtraVersionInfoMap().put(key, value);

		return this;
	}

	/**
	 * @param printHelp Boolean that determines whether a Help message will be printed whenever the application is run
	 *                  without any arguments or options, defaults to false. Setting this to true will cause
	 *                  {@link CliParser#parse(String[])} to return false if no arguments or options are specified.
	 * @return
	 */
	public CliParser withEnableHelpWhenNoArgumentsOrOptions(boolean printHelp)
	{
		setEnableHelpWhenNoArgumentsOrOptions(printHelp);

		return this;
	}

	/**
	 * Convenience method for adding a command-line option
	 * @param opt
	 * @param longOpt
	 * @param hasArg
	 * @param description
	 * @return
	 */
	public CliParser withOption(
			String opt,
			String longOpt,
			boolean hasArg,
			String description)
	{
		getOptions().addOption(new Option(opt, longOpt, hasArg, description));

		return this;
	}

	/**
	 * Convenience method for adding a required command-line option
	 * @param opt
	 * @param longOpt
	 * @param hasArg
	 * @param description
	 * @return
	 */
	public CliParser withRequiredOption(
			String opt,
			String longOpt,
			boolean hasArg,
			String description)
	{
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(true);

		getOptions().addOption(option);

		return this;
	}

	/**
	 * Default method for handling any parsing exception that happens during the {@link #parse(String[])} operation.
	 * By default, will print out full application help along with a one-line message about why parsing failed.
	 * @param parseException
	 */
	public void defaultHandleParseException(ParseException parseException)
	{
		printHelp();

		System.err.println("Command Line Parsing failed: " + parseException.getMessage());
	}

	protected String getFullAppVersion()
			throws IOException
	{
		Map<String, String> manifestValues = CliUtils.readManifestValues(getClassForVersionManifest());

		List<String> extraVersionInfoList = new ArrayList<>();
		String version = StringUtils.EMPTY;

		for (String manifestKey : manifestValues.keySet())
		{
			String manifestValue = manifestValues.get(manifestKey);

			if (manifestKey.equals(getVersionManifestKey()))
			{
				version = manifestValue;
			}
			else if (getExtraVersionInfoManifestKeys().contains(manifestKey))
			{
				extraVersionInfoList.add(String.format("%s=%s", manifestKey, manifestValue));
			}
		}

		if (getExtraVersionInfoMap().isEmpty() == false)
		{
			for (String key : getExtraVersionInfoMap().keySet())
			{
				String value = getExtraVersionInfoMap().get(key);

				extraVersionInfoList.add(String.format("%s=%s", key, value));
			}
		}

		if (isIncludeRuntimeVersionInExtraVersionInfo())
		{
			extraVersionInfoList.add(String.format("Runtime=%s", Runtime.version().toString()));
		}

		String versionString = String.format("%s %s", LONG_OPTION_VERSION, version);

		if (extraVersionInfoList.size() > 0)
		{
			versionString = String.format("%s -- (%s)", versionString, StringUtils.join(extraVersionInfoList, "; "));
		}

		return versionString;
	}

	protected void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();

		try
		{
			formatter.printHelp(getCommandLineSyntax(), null, getOptions(), getFullAppVersion());
		}
		catch (IOException ioException)
		{
			handlePrintVersionException(ioException);
		}
	}

	protected void printVersion()
	{
		try
		{
			System.out.println(getFullAppVersion());
		}
		catch (IOException ioException)
		{
			handlePrintVersionException(ioException);
		}
	}

	protected void handlePrintVersionException(Exception exception)
	{
		System.err.println(String.format("Exception trying to get Application Version: %s", exception.getMessage()));
	}

	protected Options getOptions()
	{
		if (this.options == null)
		{
			this.options = new Options();
		}

		return options;
	}

	protected void setOptions(Options options)
	{
		this.options = options;
	}

	protected Option getVersionOption()
	{
		if (this.versionOption == null)
		{
			this.versionOption = new Option(SHORT_OPTION_VERSION, LONG_OPTION_VERSION, false, "Show version.");
		}
		return versionOption;
	}

	protected void setVersionOption(Option versionOption)
	{
		this.versionOption = versionOption;
	}

	protected Option getHelpOption()
	{
		if (this.helpOption == null)
		{
			this.helpOption = new Option(SHORT_OPTION_HELP, LONG_OPTION_HELP, false, "Show help.");
		}

		return helpOption;
	}

	protected void setHelpOption(Option helpOption)
	{
		this.helpOption = helpOption;
	}

	protected Option getLoggingOption()
	{
		if (this.loggingOption == null)
		{
			this.loggingOption = new Option(SHORT_OPTION_LOGGING, "loglevel", true,
											String.format("Log level (default is %s): %s", getDefaultLogLevel(),
														  StringUtils.joinWith(", ", Level.ALL.getName(),
																			   Level.OFF.getName(),
																			   Level.CONFIG.getName(),
																			   Level.FINE.getName(),
																			   Level.FINER.getName(),
																			   Level.FINEST.getName(),
																			   Level.INFO.getName(),
																			   Level.SEVERE.getName(),
																			   Level.WARNING.getName())));
		}

		return loggingOption;
	}

	protected void setLoggingOption(Option loggingOption)
	{
		this.loggingOption = loggingOption;
	}

	protected String getDefaultLogLevel()
	{
		if (this.defaultLogLevel == null)
		{
			this.defaultLogLevel = Level.INFO.getName();
		}
		return defaultLogLevel;
	}

	protected void setDefaultLogLevel(String defaultLogLevel)
	{
		this.defaultLogLevel = defaultLogLevel;
	}

	protected String getCommandLineSyntax()
	{
		return this.commandLineSyntax;
	}

	protected void setCommandLineSyntax(String commandLineSyntax)
	{
		this.commandLineSyntax = commandLineSyntax;
	}

	protected boolean isEnableLoggingOption()
	{
		return enableLoggingOption;
	}

	protected void setEnableLoggingOption(boolean enableLoggingOption)
	{
		this.enableLoggingOption = enableLoggingOption;
	}

	protected boolean isEnableVersionOption()
	{
		return enableVersionOption;
	}

	protected void setEnableVersionOption(boolean enableVersionOption)
	{
		this.enableVersionOption = enableVersionOption;
	}

	protected boolean isEnableHelpOption()
	{
		return enableHelpOption;
	}

	protected void setEnableHelpOption(boolean enableHelpOption)
	{
		this.enableHelpOption = enableHelpOption;
	}

	protected boolean isIncludeRuntimeVersionInExtraVersionInfo()
	{
		return includeRuntimeVersionInExtraVersionInfo;
	}

	protected void setIncludeRuntimeVersionInExtraVersionInfo(boolean includeRuntimeVersionInExtraVersionInfo)
	{
		this.includeRuntimeVersionInExtraVersionInfo = includeRuntimeVersionInExtraVersionInfo;
	}

	protected String getVersionManifestKey()
	{
		return versionManifestKey;
	}

	protected void setVersionManifestKey(String versionManifestKey)
	{
		this.versionManifestKey = versionManifestKey;
	}

	protected List<String> getExtraVersionInfoManifestKeys()
	{
		return extraVersionInfoManifestKeys;
	}

	protected void setExtraVersionInfoManifestKeys(List<String> extraVersionInfoManifestKeys)
	{
		this.extraVersionInfoManifestKeys = extraVersionInfoManifestKeys;
	}

	public boolean isEnableHelpWhenNoArgumentsOrOptions()
	{
		return enableHelpWhenNoArgumentsOrOptions;
	}

	protected void setEnableHelpWhenNoArgumentsOrOptions(boolean enableHelpWhenNoArgumentsOrOptions)
	{
		this.enableHelpWhenNoArgumentsOrOptions = enableHelpWhenNoArgumentsOrOptions;
	}

	/**
	 * @return The parsed {@link CommandLine} object with access to a lot of details about the parsed options
	 */
	public CommandLine getCommandLine()
	{
		return commandLine;
	}

	protected void setCommandLine(CommandLine commandLine)
	{
		this.commandLine = commandLine;
	}

	protected CliListener getListener()
	{
		if (this.listener == null)
		{
			this.listener = new DefaultCliAdapter();
		}

		return listener;
	}

	protected void setListener(CliListener listener)
	{
		this.listener = listener;
	}

	protected Map<String, String> getExtraVersionInfoMap()
	{
		return extraVersionInfoMap;
	}
}

