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
import jetbrains.buildServer.agent.messages.KeepMessagesLogger;
import jetbrains.buildServer.agent.messages.TranslatorsRegistry;
import jetbrains.buildServer.agent.messages.regex.*;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.teamcity.util.regex.ParserManager;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO: Support scopes
public class ParsersRegistryImpl implements ParsersRegistry {
  private static final Logger LOG = Logger.getInstance(ParsersRegistryImpl.class.getName());

  private final TranslatorsRegistry myTranslatorsRegistry;
  @NotNull
  private final ParserLoader myLoader;
  private final KeepMessagesLogger myKeepMessagesLogger;
  private final ParserManager myDefaultParsersManager;
  private final Map<String, RegexParserToSimpleMessagesTranslatorAdapter> myRegisteredTranslators;
  private final Map<String, RegexParser> myKnownParsers = new HashMap<String, RegexParser>();
  private final Map<ParserCommand.ParserId, String> myParsersHistory = new HashMap<ParserCommand.ParserId, String>();


  public ParsersRegistryImpl(@NotNull final TranslatorsRegistry translatorsRegistry,
                             @NotNull final ParserLoader loader) {
    myTranslatorsRegistry = translatorsRegistry;
    myLoader = loader;
    myKeepMessagesLogger = new KeepMessagesLogger();
    final SimpleLogger logger = new SimpleLogger(myKeepMessagesLogger);
    myDefaultParsersManager = new ParserManager(logger);
    myRegisteredTranslators = new HashMap<String, RegexParserToSimpleMessagesTranslatorAdapter>();
  }

  public void disable(@NotNull final ParserCommand.ParserId parser, @Nullable final ParserCommand.Scope scope) {
    if (!StringUtil.isEmptyOrSpaces(parser.getName())) {
      String name = parser.getName();
      disable(name, scope);
      return;
    }
    final String name = myParsersHistory.get(parser);
    if (name == null) {
      LOG.warn("Cannot disable parser '" + parser + "': unknown parser identifier");
      return;
    }
    disable(name, scope);
  }

  public void enable(@NotNull final ParserCommand.ParserId parserId, @Nullable final ParserCommand.Scope scope) {
    if (!StringUtil.isEmptyOrSpaces(parserId.getName())) {
      String name = parserId.getName();
      final RegexParser parser = myKnownParsers.get(name);
      if (parser != null) {
        // Looks like already registered.
        LOG.debug("Parser '" + name + "' already registered");
        enable(parser, scope);
        return;
      }
    }
    final RegexParser parser = myLoader.load(parserId);
    if (parser != null) {
      myParsersHistory.put(parserId, parser.getName());
      register(parser.getName(), parser);
      enable(parser, null);
    }
  }

  public void enable(@NotNull final RegexParser parser, @Nullable final ParserCommand.Scope scope) {
    final RegexParserToSimpleMessagesTranslatorAdapter adapter = new RegexParserToSimpleMessagesTranslatorAdapter(parser, myDefaultParsersManager, myKeepMessagesLogger);
    enable(adapter);
  }

  public void enable(@NotNull final RegexParserToSimpleMessagesTranslatorAdapter adapter) {
    final String name = adapter.getName();
    LOG.info("Registering parser '" + name + "' as text translator");
    final RegexParserToSimpleMessagesTranslatorAdapter old = myRegisteredTranslators.put(name, adapter);
    if (old != null) {
      myTranslatorsRegistry.unregister(old);
    }
    myTranslatorsRegistry.register(adapter);
  }

  @Override
  public void enable(@NotNull final String name, @Nullable final ParserCommand.Scope scope) {
    final RegexParser parser = myKnownParsers.get(name);
    if (parser == null) {
      LOG.error("Cannot enable parser '" + name + "': not found. Register parser first");
      return;
    }
    enable(parser, scope);
  }

  @Override
  public void disable(@NotNull final String name, @Nullable final ParserCommand.Scope scope) {
    // Unregister from translators
    final RegexParserToSimpleMessagesTranslatorAdapter translator = myRegisteredTranslators.get(name);
    if (translator == null) {
      LOG.warn("Cannot disable parser with name '" + name + "': not found");
    } else {
      myTranslatorsRegistry.unregister(translator);
      myRegisteredTranslators.remove(name);
    }
  }

  @Override
  public void register(@NotNull final String name, @NotNull final RegexParser parser) {
    synchronized (myKnownParsers) {
      final RegexParser already = myKnownParsers.get(name);
      if (already != null) {
        if (already.equals(parser)) {
          LOG.warn("Exact same parser with name '" + name + "' already registered");
        } else {
          LOG.warn("Parser with name '" + name + "' already registered. Nothing is changed. Unregister parser first.");
        }
      } else {
        myKnownParsers.put(name, parser);
      }
    }
  }

  @Override
  public void unregister(@NotNull final String name) {
    synchronized (myKnownParsers) {
      myKnownParsers.remove(name);
    }
  }

  @NotNull
  @Override
  public Map<String, RegexParser> getRegisteredParsers() {
    return Collections.unmodifiableMap(myKnownParsers);
  }

  @NotNull
  @Override
  public ParserLoader getLoader() {
    return myLoader;
  }
}
