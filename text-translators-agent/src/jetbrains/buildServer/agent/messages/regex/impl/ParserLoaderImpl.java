

package jetbrains.buildServer.agent.messages.regex.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.messages.regex.ParserCommand;
import jetbrains.buildServer.agent.messages.regex.ParserLoader;
import jetbrains.buildServer.agent.messages.regex.RegexParsersHelper;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.teamcity.util.regex.ParserLoadingException;
import jetbrains.teamcity.util.regex.RegexParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParserLoaderImpl implements ParserLoader {
  private static final Logger LOG = Logger.getInstance(ParserLoaderImpl.class.getName());
  @NotNull
  private final CurrentBuildTracker myBuildTracker;
  private final Map<ParserCommand.ParserId, RegexParser> myLoadedParsers = new ConcurrentHashMap<ParserCommand.ParserId, RegexParser>();

  public ParserLoaderImpl(@NotNull final CurrentBuildTracker buildTracker) {
    myBuildTracker = buildTracker;
  }

  @NotNull
  @Override
  public Map<ParserCommand.ParserId, RegexParser> getLoadedParsers() {
    return Collections.unmodifiableMap(myLoadedParsers);
  }

  @NotNull
  public RegexParser load(@NotNull final ParserCommand.ParserId parserId) throws FileNotFoundException, ParserLoadingException {
    RegexParser parser = myLoadedParsers.get(parserId);
    if (parser != null) return parser;
    parser = doLoadPath(parserId);
    myLoadedParsers.put(parserId, parser);
    return parser;
  }

  @Override
  public void unload(@NotNull final ParserCommand.ParserId parserId) {
    myLoadedParsers.remove(parserId);
  }

  @NotNull
  private RegexParser doLoadPath(@NotNull final ParserCommand.ParserId parserId) throws FileNotFoundException, ParserLoadingException {
    RegexParser parser;
    if (!StringUtil.isEmptyOrSpaces(parserId.getResourcePath())) {
      final String path = parserId.getResourcePath();
      LOG.info("Loading parser config from resource " + path);
      parser = RegexParsersHelper.loadParserFromResource(path);
    } else if (!StringUtil.isEmptyOrSpaces(parserId.getFile())) {
      final String path = parserId.getFile();
      final File file;
      if (FileUtil.isAbsolute(path)) {
        file = new File(path);
      } else {
        // Path relative to checkout directory
        if (!myBuildTracker.isRunningBuild()) {
          String message = "Cannot register parser from file: no running build found and not absolute path specified: " + path;
          LOG.error(message);
          throw new IllegalStateException(message);
        }
        final File wd = myBuildTracker.getCurrentBuild().getCheckoutDirectory();
        file = new File(wd, path);
      }
      if (file.exists()) {
        final File cf = FileUtil.getCanonicalFile(file);
        LOG.info("Loading parser config from file " + cf.getAbsolutePath());
        parser = RegexParsersHelper.loadParserFromFile(cf);
      } else {
        String message = "Cannot register parser from file: file not found: " + file.getAbsolutePath();
        LOG.warn(message);
        throw new FileNotFoundException(message);
      }
    } else {
      throw new IllegalArgumentException("Expected non-empty resource path or file path");
    }
    return parser;
  }
}