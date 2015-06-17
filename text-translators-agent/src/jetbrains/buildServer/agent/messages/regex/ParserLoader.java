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
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class ParserLoader {
  private static final Logger LOG = Logger.getInstance(ParserLoader.class.getName());
  @NotNull
  private final CurrentBuildTracker myBuildTracker;

  public ParserLoader(@NotNull final CurrentBuildTracker buildTracker) {
    myBuildTracker = buildTracker;
  }

  @Nullable
  public RegexParser load(@NotNull final ParserCommand.ParserId parserId) {
    RegexParser parser = null;
    if (!StringUtil.isEmptyOrSpaces(parserId.getResourcePath())) {
      final String path = parserId.getResourcePath();
      LOG.info("Loading parser config from resource " + path);
      parser = RegexParsersHelper.loadParserFromResource(path);
      if (parser == null) {
        LOG.error("Cannot find parser for resource path '" + path + "'");
        return null;
      }
    } else if (!StringUtil.isEmptyOrSpaces(parserId.getFile())) {
      final String path = parserId.getFile();
      final File file;
      if (FileUtil.isAbsolute(path)) {
        file = new File(path);
      } else {
        // CheckoutDir relative path
        if (!myBuildTracker.isRunningBuild()) {
          LOG.warn("Cannot register parser from file: no running build found and not absolute path specified: " + path);
          return null;
        }
        final File wd = myBuildTracker.getCurrentBuild().getCheckoutDirectory();
        file = new File(wd, path);
      }
      if (file.exists()) {
        final File cf = FileUtil.getCanonicalFile(file);
        if (cf.exists()) {
          LOG.info("Loading parser config from file " + cf.getAbsolutePath());
          parser = RegexParsersHelper.loadParserFromFile(cf);
        }
      } else {
        LOG.warn("Cannot register parser from file: file not found: " + file.getAbsolutePath());
      }
    }
    return parser;
  }
}
