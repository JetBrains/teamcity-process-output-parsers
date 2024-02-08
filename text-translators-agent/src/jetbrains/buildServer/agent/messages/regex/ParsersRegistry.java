

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ParsersRegistry {
  void enable(@NotNull ParserCommand.ParserId parser, @Nullable ParserCommand.Scope scope);
  void disable(@NotNull ParserCommand.ParserId parser, @Nullable ParserCommand.Scope scope);

  void enable(@NotNull String name, @Nullable ParserCommand.Scope scope);
  void disable(@NotNull String name, @Nullable ParserCommand.Scope scope);

  void register(@NotNull String name, @NotNull RegexParser parser);
  void unregister(@NotNull String name);

  @NotNull
  Map<String, RegexParser> getRegisteredParsers();

  @NotNull
  ParserLoader getLoader();
}