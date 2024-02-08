

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.teamcity.util.regex.ParserLoadingException;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.Map;

public interface ParserLoader {
  @NotNull
  Map<ParserCommand.ParserId, RegexParser> getLoadedParsers();

  @NotNull
  RegexParser load(@NotNull final ParserCommand.ParserId parserId) throws FileNotFoundException, ParserLoadingException;

  void unload(@NotNull final ParserCommand.ParserId parserId);
}