

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.teamcity.util.regex.LoggerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vladislav.Rassokhin
 */
public class SimpleLogger extends LoggerAdapter {
  @NotNull
  private final BuildProgressLogger myBuildLogger;

  public SimpleLogger(@NotNull final BuildProgressLogger buildLogger) {
    myBuildLogger = buildLogger;
  }

  @Override
  public void message(@NotNull final String message) {
    myBuildLogger.message(message);
  }

  @Override
  public void error(@NotNull final String message) {
    myBuildLogger.error(message);
  }

  @Override
  public void warning(@NotNull final String message) {
    myBuildLogger.warning(message);
  }

  @Override
  public void blockStart(@NotNull final String name) {
    myBuildLogger.targetStarted(name);
  }

  @Override
  public void blockFinish(@NotNull final String name) {
    myBuildLogger.targetFinished(name);
  }

  @Override
  public void compilationBlockStart(@NotNull final String name) {
    myBuildLogger.activityStarted(name, DefaultMessagesInfo.BLOCK_TYPE_COMPILATION);
  }

  @Override
  public void compilationBlockFinish(@NotNull final String name) {
    myBuildLogger.activityFinished(name, DefaultMessagesInfo.BLOCK_TYPE_COMPILATION);
  }
}