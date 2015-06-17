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
