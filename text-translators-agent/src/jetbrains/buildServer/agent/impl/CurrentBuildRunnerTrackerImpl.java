

package jetbrains.buildServer.agent.impl;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

public class CurrentBuildRunnerTrackerImpl extends AgentLifeCycleAdapter implements CurrentBuildRunnerTracker {
  private BuildRunnerContext myRunningBuildRunner;

  public CurrentBuildRunnerTrackerImpl(@NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher) {
    dispatcher.addListener(this);
  }

  @Override
  public boolean isBuildRunnerRunning() {
    return myRunningBuildRunner != null;
  }

  @NotNull
  @Override
  public BuildRunnerContext getCurrentBuildRunner() throws NoRunningBuildException {
    if (myRunningBuildRunner == null) {
      throw new NoRunningBuildException();
    }
    return myRunningBuildRunner;
  }

  @Override
  public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
    myRunningBuildRunner = runner;
  }

  @Override
  public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
    myRunningBuildRunner = null;
  }
}