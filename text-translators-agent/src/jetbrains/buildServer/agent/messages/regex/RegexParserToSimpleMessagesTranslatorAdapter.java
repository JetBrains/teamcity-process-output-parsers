

package jetbrains.buildServer.agent.messages.regex;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.messages.BuildLogTail;
import jetbrains.buildServer.agent.messages.KeepMessagesLogger;
import jetbrains.buildServer.agent.messages.SimpleMessagesTranslator;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.teamcity.util.regex.ParserManager;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RegexParserToSimpleMessagesTranslatorAdapter implements SimpleMessagesTranslator {
  private static final Logger LOG = Logger.getInstance(RegexParserToSimpleMessagesTranslatorAdapter.class.getName());
  private final RegexParser myParser;
  private final ParserManager myManager;
  private final KeepMessagesLogger myLogger;

  public RegexParserToSimpleMessagesTranslatorAdapter(@NotNull final RegexParser parser,
                                                      @NotNull final ParserManager manager,
                                                      @NotNull final KeepMessagesLogger logger) {
    myParser = parser;
    myManager = manager;
    myLogger = logger;
  }

  public String getName() {
    return myParser.getName();
  }

  @Override
  public Result doProcessMessage(@NotNull final ServiceMessage message, @NotNull final BuildLogTail tail) {
    return Result.SKIP;
  }

  @Override
  public Result doProcessText(@NotNull final String text, @NotNull final BuildLogTail tail) {
    final List<BuildMessage1> messages;
    final boolean consumed;
    synchronized (this) {
      consumed = myParser.processLine(text, myManager);
      messages = myLogger.getUnprocessedMessagesAndReset();
    }
    if (!consumed) {
      if (!messages.isEmpty()) {
        LOG.warn("Parser '" + myParser.getId() + "'not consumed message but there some pending messages produced: " + messages);
      }
      return Result.SKIP;
    }
    if (messages.isEmpty()) {
      return Result.EAT;
    }
    if (messages.size() == 1) {
      final BuildMessage1 msg = messages.iterator().next();
      if (msg.getValue() instanceof String && text.equals(msg.getValue())) {
        return Result.KEEP_ORIGIN;
      }
    }
    return Result.REPLACE(messages);
  }

}