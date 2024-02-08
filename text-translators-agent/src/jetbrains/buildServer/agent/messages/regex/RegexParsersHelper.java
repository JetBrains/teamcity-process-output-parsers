

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.buildServer.util.FileUtil;
import jetbrains.teamcity.util.regex.ParserLoader;
import jetbrains.teamcity.util.regex.ParserLoadingException;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class RegexParsersHelper {
  @NotNull
  public static RegexParser loadParserFromFile(@NotNull final File file) throws FileNotFoundException, ParserLoadingException {
    if (file.exists() && file.isFile()) {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        return ParserLoader.loadParser(fis);
      } finally {
        FileUtil.close(fis);
      }
    }
    throw new FileNotFoundException(file.getAbsolutePath());
  }

  @NotNull
  public static RegexParser loadParserFromResource(@NotNull final String path) throws FileNotFoundException, ParserLoadingException {
    return ParserLoader.loadParser(path, Thread.currentThread().getContextClassLoader());
  }
}