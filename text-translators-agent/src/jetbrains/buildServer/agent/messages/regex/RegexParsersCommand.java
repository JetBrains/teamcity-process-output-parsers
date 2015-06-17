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

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;

/**
 * Service Messages controlling Regex Translators:
 * 'RegexMessageTranslator.Add'
 * 'RegexMessageTranslator.Remove'
 * 'RegexMessageTranslator.Reset'
 * <p/>
 * Attributes:
 * 'file' - path to parser config file
 * 'name' - name of predefined (bundled) config file
 * 'scope' - ('Add' command) scope of new parser
 * Also simple argument may be used as 'file' or 'name' (system will distinguish attribute name automatically)
 *
 * Scope is either 'Build' or 'Runner' (case ignored)
 */
public class RegexParsersCommand {
  private static final String PREFIX = "RegexMessageTranslator.";
  public static final String COMMAND_ADD = PREFIX + "Add";
  public static final String COMMAND_REMOVE = PREFIX + "Remove";
  public static final String COMMAND_RESET = PREFIX + "Reset";

  private final String myFile;
  private final String myScope;
  private final String myName;
  private final String myArgument;
  private final String myResourcePath;

  public String getResourcePath() {
    return myResourcePath;
  }


  public enum Scope {
    THIS_RUNNER,
    NEXT_RUNNER,
    ALL_RUNNERS, // Build

  }
//private enum Command{
//  Add,
//  Remove,
//  Reset;
//
//  public void run(@NotNull final ServiceMessage message) {};
//}


  private RegexParsersCommand(final String file, final String scope, final String name, final String argument, final String resourcePath) {
    myFile = file;
    myScope = scope;
    myName = name;
    myArgument = argument;
    myResourcePath = resourcePath;
  }

  public static RegexParsersCommand fromFile(@NotNull final String file) {
    return new RegexParsersCommand(file, null, null, null, null);
  }

  public static RegexParsersCommand fromName(@NotNull final String name) {
    return new RegexParsersCommand(null, name, null, null, null);
  }

  public static RegexParsersCommand fromResource(@NotNull final String resourcePath) {
    return new RegexParsersCommand(null, null, null, null, resourcePath);
  }

  public RegexParsersCommand(@NotNull final ServiceMessage message) {
    final Map<String, String> attributes = message.getAttributes();
    final String command = message.getMessageName();
    assert command.startsWith(PREFIX);
    myArgument = message.getArgument();
    myFile = attributes.get("file");
    myScope = attributes.get("scope");
    myName = attributes.get("name");
    myResourcePath = attributes.get("resource");

    if (COMMAND_ADD.equals(command)) {
      if (isEmptyOrSpaces(myArgument) && isEmptyOrSpaces(myFile) && isEmptyOrSpaces(myName) && isEmptyOrSpaces(myResourcePath)) {
        throw new IllegalArgumentException("Command '" + command + "' requires attribute 'name', 'file' or single argument. Actual message: " + message);
      }
    } else if (COMMAND_REMOVE.equals(command)) {
      if (isEmptyOrSpaces(myArgument) && isEmptyOrSpaces(myFile) && isEmptyOrSpaces(myName) && isEmptyOrSpaces(myResourcePath)) {
        throw new IllegalArgumentException("Command '" + command + "' requires attribute 'name', 'file' or single argument. Actual message: " + message);
      }
    } else if (COMMAND_RESET.equals(command)) {

    } else {
      throw new IllegalArgumentException("Unexpected message name " + command);
    }
  }

  public String getFile() {
    return myFile;
  }

  public String getScope() {
    return myScope;
  }

  public String getName() {
    return myName;
  }

  public String getArgument() {
    return myArgument;
  }
}
