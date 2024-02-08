

package jetbrains.buildServer.agent;

import org.jetbrains.annotations.NotNull;

public interface CurrentBuildRunnerTracker {
  boolean isBuildRunnerRunning();

  @NotNull
  BuildRunnerContext getCurrentBuildRunner() throws NoRunningBuildException;
}