/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
