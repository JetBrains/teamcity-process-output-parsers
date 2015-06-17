/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.messages;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.cmakerunner.regexparser.ParserManager;
import jetbrains.buildServer.cmakerunner.regexparser.RegexParser;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
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
