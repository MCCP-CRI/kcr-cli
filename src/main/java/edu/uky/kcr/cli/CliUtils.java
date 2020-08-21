package edu.uky.kcr.cli;

import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Utility methods useful for handling parsed command-line options.
 */
public class CliUtils
{

	/**
	 * Will attempt to instantiate a new Object of Class "type" using a constructor that takes a single String as input.
	 * @param type
	 * @param value
	 * @param <T>
	 * @return
	 * @throws ParseException
	 */
	public static <T> T convertParsedValue(Class<T> type,
										   String value)
			throws ParseException
	{
		return createFromStringConstructor(type, value);
	}

	private static <T> T createFromStringConstructor(Class<T> type,
													 String value)
			throws ParseException
	{
		T returnValue = null;

		if (value != null)
		{
			try
			{
				returnValue = type.getConstructor(String.class).newInstance(value);
			}
			catch (Exception exception)
			{
				throw new ParseException(String.format("Could not parse [%s] into %s", value, type.getName()));
			}
		}

		return returnValue;
	}

	/**
	 * 	 Will attempt to instantiate a List of new Objects of Class "type" for each value in the stringValues Array,
	 * 	 using a constructor that takes a single String as input.
	 * @param type
	 * @param stringValues
	 * @param <T>
	 * @return
	 * @throws ParseException
	 */
	public static <T> List<T> convertParsedValues(Class<T> type,
												  String... stringValues)
			throws ParseException
	{
		List<T> returnValue = new ArrayList<T>();

		if (stringValues != null)
		{
			for (String stringValue : stringValues)
			{
				returnValue.add(createFromStringConstructor(type, stringValue));
			}
		}

		return returnValue;
	}

	/**
	 *
	 * @return HashMap of all attributes from the JAR manifest of {@link CliUtils}. If {@link CliUtils} was not loaded
	 * from a Jar file, returns an empty HashMap.
	 * @throws IOException
	 */
	public static Map<String, String> readManifestValues()
			throws IOException
	{
		return readManifestValues(CliUtils.class);
	}

	/**
	 *
	 * @param clazz
	 * @return HashMap of all attributes from the clazz JAR manifest. If clazz was not loaded from a Jar file,
	 * returns an empty HashMap.
	 * @throws IOException
	 */
	public static Map<String, String> readManifestValues(Class clazz)
			throws IOException
	{
		Map<String, String> valueMap = new LinkedHashMap<>();

		URL classUrl = clazz.getResource(clazz.getSimpleName() + ".class");
		URLConnection classUrlConnection = classUrl.openConnection();

		if (classUrlConnection instanceof JarURLConnection)
		{
			JarURLConnection classJarUrlConnection = (JarURLConnection) classUrlConnection;
			Manifest manifest = classJarUrlConnection.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			for (Object key : attributes.keySet())
			{
				Attributes.Name name = (Attributes.Name) key;
				valueMap.put(name.toString(), attributes.getValue(name));
			}
		}

		return valueMap;
	}


}