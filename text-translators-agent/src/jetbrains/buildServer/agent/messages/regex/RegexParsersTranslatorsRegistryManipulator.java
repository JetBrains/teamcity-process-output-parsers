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

package jetbrains.buildServer.agent.messages.regex;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.hash.HashMap;
import jetbrains.buildServer.agent.CurrentBuildRunnerTracker;
import jetbrains.buildServer.agent.messages.KeepMessagesLogger;
import jetbrains.buildServer.agent.messages.RegexParserToSimpleMessagesTranslatorAdapter;
import jetbrains.buildServer.agent.messages.SimpleLogger;
import jetbrains.buildServer.agent.messages.TranslatorsRegistry;
import jetbrains.buildServer.cmakerunner.regexparser.ParserManager;
import jetbrains.buildServer.cmakerunner.regexparser.RegexParser;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageHandler;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class RegexParsersTranslatorsRegistryManipulator {
  private static final Logger LOG = Logger.getInstance(RegexParsersTranslatorsRegistryManipulator.class.getName());

  private final TranslatorsRegistry myTranslatorsRegistry;
  private final CurrentBuildRunnerTracker myCurrentBuildRunnerTracker;
  private final KeepMessagesLogger myKeepMessagesLogger;
  private final ParserManager myDefaultParsersManager;
  private final Map<String, RegexParserToSimpleMessagesTranslatorAdapter> myRegisteredTranslators;

  public RegexParsersTranslatorsRegistryManipulator(@NotNull TranslatorsRegistry translatorsRegistry,
                                                    @NotNull ServiceMessagesRegister serviceMessagesRegister,
                                                    @NotNull CurrentBuildRunnerTracker currentBuildRunnerTracker) {
    myTranslatorsRegistry = translatorsRegistry;
    myCurrentBuildRunnerTracker = currentBuildRunnerTracker;

    serviceMessagesRegister.registerHandler(RegexParsersCommand.COMMAND_ADD, new ServiceMessageHandler() {
      @Override
      public void handle(@NotNull final ServiceMessage message) {
        LOG.debug(message.getMessageName() + " message found: " + message);
        register(new RegexParsersCommand(message));
      }
    });
    serviceMessagesRegister.registerHandler(RegexParsersCommand.COMMAND_REMOVE, new ServiceMessageHandler() {
      @Override
      public void handle(@NotNull final ServiceMessage message) {
        LOG.debug(message.getMessageName() + " message found: " + message);
        doUnregisterParser(new RegexParsersCommand(message));
      }
    });

    myKeepMessagesLogger = new KeepMessagesLogger();
    final SimpleLogger logger = new SimpleLogger(myKeepMessagesLogger);
    myDefaultParsersManager = new ParserManager(logger);
    myRegisteredTranslators = new HashMap<String, RegexParserToSimpleMessagesTranslatorAdapter>();
  }

  private void doUnregisterParser(RegexParsersCommand command) {
    final String name = command.getName();
    // Unregister from translators
    final RegexParserToSimpleMessagesTranslatorAdapter translator = myRegisteredTranslators.get(name);
    if (translator != null) {
      myTranslatorsRegistry.unregister(translator);
    }
  }

  public void register(RegexParsersCommand command) {
    RegexParser parser = null;
    if (command.getResourcePath() != null) {
      final String path = command.getResourcePath();
      LOG.info("Using parser config from resource " + path);
      parser = RegexParsersHelper.loadParserFromResource(path);
      if (parser == null) {
        LOG.error("Cannot find parser for resource path '" + path + "'");
      }
    } else if (command.getFile() != null) {
      if (!myCurrentBuildRunnerTracker.isBuildRunnerRunning()) {
        LOG.warn("Cannot register parser from file: no running build runner (step) found");
        return;
      }
      final String file = command.getFile();
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

  public void unregister(@NotNull RegexParserToSimpleMessagesTranslatorAdapter adapter) {
    myTranslatorsRegistry.unregister(adapter);
  }
}
