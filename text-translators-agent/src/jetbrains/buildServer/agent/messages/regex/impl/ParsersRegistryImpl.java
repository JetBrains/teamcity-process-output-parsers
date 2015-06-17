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

package jetbrains.buildServer.agent.messages.regex.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.hash.HashMap;
import jetbrains.buildServer.agent.CurrentBuildRunnerTracker;
import jetbrains.buildServer.agent.messages.KeepMessagesLogger;
import jetbrains.buildServer.agent.messages.TranslatorsRegistry;
import jetbrains.buildServer.agent.messages.regex.*;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.teamcity.util.regex.ParserManager;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class ParsersRegistryImpl implements ParsersRegistry {
  private static final Logger LOG = Logger.getInstance(ParsersRegistryImpl.class.getName());

  private final TranslatorsRegistry myTranslatorsRegistry;
  private final CurrentBuildRunnerTracker myCurrentBuildRunnerTracker;
  private final KeepMessagesLogger myKeepMessagesLogger;
  private final ParserManager myDefaultParsersManager;
  private final Map<String, RegexParserToSimpleMessagesTranslatorAdapter> myRegisteredTranslators;

  public ParsersRegistryImpl(@NotNull final TranslatorsRegistry translatorsRegistry,
                             @NotNull final CurrentBuildRunnerTracker currentBuildRunnerTracker) {
    myTranslatorsRegistry = translatorsRegistry;
    myCurrentBuildRunnerTracker = currentBuildRunnerTracker;
    myKeepMessagesLogger = new KeepMessagesLogger();
    final SimpleLogger logger = new SimpleLogger(myKeepMessagesLogger);
    myDefaultParsersManager = new ParserManager(logger);
    myRegisteredTranslators = new HashMap<String, RegexParserToSimpleMessagesTranslatorAdapter>();
  }

  public void unregister(@NotNull final ParserCommand.CommandWithParserIdentifier command) {
    unregister(command.getParserId(), command.getScope());
  }

  public void unregister(@NotNull final ParserCommand.ParserId parser, @NotNull final ParserCommand.Scope scope) {
    final String name = parser.getName();
    // Unregister from translators
    final RegexParserToSimpleMessagesTranslatorAdapter translator = myRegisteredTranslators.get(name);
    if (translator != null) {
      myTranslatorsRegistry.unregister(translator);
    }
  }

  public void register(@NotNull final ParserCommand.CommandWithParserIdentifier command) {
    register(command.getParserId(), command.getScope());
  }

  public void register(@NotNull final ParserCommand.ParserId parserId, @NotNull final ParserCommand.Scope scope) {
    RegexParser parser = null;
    if (parserId.getResourcePath() != null) {
      final String path = parserId.getResourcePath();
      LOG.info("Using parser config from resource " + path);
      parser = RegexParsersHelper.loadParserFromResource(path);
      if (parser == null) {
        LOG.error("Cannot find parser for resource path '" + path + "'");
      }
    } else if (parserId.getFile() != null) {
      if (!myCurrentBuildRunnerTracker.isBuildRunnerRunning()) {
        LOG.warn("Cannot register parser from file: no running build runner (step) found");
        return;
      }
      final String file = parserId.getFile();
      File wd = myCurrentBuildRunnerTracker.getCurrentBuildRunner().getWorkingDirectory();
      final File f = new File(wd, file);
      if (!StringUtil.isEmptyOrSpaces(file) && f.exists()) {
        final File cf = FileUtil.getCanonicalFile(f);
        if (cf.exists()) {
          LOG.info("Using parser config from file " + cf.getAbsolutePath());
          parser = RegexParsersHelper.loadParserFromFile(cf);
        }
      }
    }

    if (parser != null) {
      register(parser);
    }
  }

  @Override
  public void register(@NotNull final ParserCommand.ParserId parser) {
    register(parser, ParserCommand.Scope.BUILD);
  }

  @Override
  public void unregister(@NotNull final ParserCommand.ParserId parser) {
    unregister(parser, ParserCommand.Scope.BUILD);
  }

  public void register(@NotNull final RegexParser parser) {
    final RegexParserToSimpleMessagesTranslatorAdapter adapter = new RegexParserToSimpleMessagesTranslatorAdapter(parser, myDefaultParsersManager, myKeepMessagesLogger);
    register(adapter);
  }

  public void register(@NotNull final RegexParserToSimpleMessagesTranslatorAdapter adapter) {
    final String name = adapter.getName();
    LOG.info("Registering parser '" + name + "' as text translator");
    final RegexParserToSimpleMessagesTranslatorAdapter old = myRegisteredTranslators.put(name, adapter);
    if (old != null) {
      myTranslatorsRegistry.unregister(old);
    }
    myTranslatorsRegistry.register(adapter);
  }

  public void unregister(@NotNull final RegexParserToSimpleMessagesTranslatorAdapter adapter) {
    myTranslatorsRegistry.unregister(adapter);
  }
}
