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

import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ParsersRegistry {
  // TODO: Remove extra methods

//  void enable(@NotNull ParserCommand.CommandWithParserIdentifier parser);
//
//  void disable(@NotNull ParserCommand.CommandWithParserIdentifier parser);
//
//  void enable(@NotNull ParserCommand.ParserId parser);
//
//  void disable(@NotNull ParserCommand.ParserId parser);
  void enable(@NotNull ParserCommand.ParserId parser, @Nullable ParserCommand.Scope scope);
  void disable(@NotNull ParserCommand.ParserId parser, @Nullable ParserCommand.Scope scope);

  void enable(@NotNull String name, @Nullable ParserCommand.Scope scope);

  void disable(@NotNull String name, @Nullable ParserCommand.Scope scope);


  void register(@NotNull String name, @NotNull RegexParser parser);

  void unregister(@NotNull String name);

  @NotNull
  Map<String, RegexParser> getRegisteredParsers();

  @NotNull ParserLoader getLoader();
}
