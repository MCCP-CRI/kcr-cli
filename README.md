# Command-Line Interface Parser and Utilities

This class enhances the features of the <a href="https://commons.apache.org/proper/commons-cli/">Apache Commons CLI library</a>
for parsing command-line parameters in a Java application by providing common-sense defaults and handlers for the following:
 * timestamped console logging
 * console output of help when no arguments are passed to your application
 * console output of version information, read from Jar manifest attributes
 * listener interface for options as they are parsed
 
 ###Usage:
1. Create a [`CliParser`](src/main/java/edu/uky/kcr/cli/CliParser.java) at the top of the main() method, with a syntax String and options configured using `.with()` methods:
    ```
    public static void main(String[] args)
        throws Exception
    {
        CliParser cliParser = new CliParser("java -jar myapp.jar [OPTIONS] <Input File>")
            .withOption("r", "remove", true, "Remove something from somewhere")
            .withRequiredOption("a", "author", true, "Set an author (required)")
            ...
    ```  
   See all of the `.with()` methods in [`CliParser`](src/main/java/edu/uky/kcr/cli/CliParser.java) to know what to configure.
2. Call `.parse(args)` with application arguments and continue with the application only if it returns true:
    ```
    if (cliParser.parse(args))
    {
        ...
    ```
   If `parse(args)` returns false, then the CliParser has already output an error message to the console along with the 
   help message for the application. 
   
   If you configured a [`CliListener`](src/main/java/edu/uky/kcr/cli/CliListener.java) object when creating the CliParser, 
   it will get called with every command-line option during the `parse(args)` call so that the application can configure 
   itself with user-supplied options.
   If you did not configure a [`CliListener`](src/main/java/edu/uky/kcr/cli/CliListener.java) object, then you can read 
   parsed user options with the `getParsedValue(...)` and `getParsedValues(...)` methods.
