

package jetbrains.teamcity.util.regex;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Vladislav.Rassokhin
 */
public class ParserLoader {
  @NotNull
  private static final Logger LOG = Logger.getInstance(ParserLoader.class.getName());

  @NotNull
  public static RegexParser loadParser(@NotNull final String configResourceName, @NotNull final Class clazz) throws FileNotFoundException, ParserLoadingException {
    final InputStream parserConfigStream = clazz.getResourceAsStream(configResourceName);
    if (parserConfigStream == null) {
      String message = "Specified parser configuration resource not found (" + configResourceName + ")";
      LOG.warn(message);
      throw new FileNotFoundException(message);
    }
    return loadParser(parserConfigStream);
  }

  @NotNull
  public static RegexParser loadParser(@NotNull final String configResourceName, @NotNull final ClassLoader classLoader) throws FileNotFoundException, ParserLoadingException {
    final InputStream parserConfigStream = classLoader.getResourceAsStream(configResourceName);
    if (parserConfigStream == null) {
      String message = "Specified parser configuration resource not found (" + configResourceName + ")";
      LOG.warn(message);
      throw new FileNotFoundException(message);
    }
    return loadParser(parserConfigStream);
  }

  @NotNull
  public static RegexParser loadParser(@NotNull final InputStream parserConfigStream) throws ParserLoadingException {
    try {
      return RegexParser.deserialize(parserConfigStream);
    } catch (final IOException e) {
      LOG.warnAndDebugDetails("Failed to read parser configuration", e);
      throw new ParserLoadingException("Failed to read parser configuration: " + e.getMessage());
    }
  }
}