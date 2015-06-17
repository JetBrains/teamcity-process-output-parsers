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

package jetbrains.buildServer.agent.messages;

import jetbrains.buildServer.agent.BaseServerLoggerFacade;
import jetbrains.buildServer.messages.BuildMessage1;

import java.util.ArrayList;
import java.util.List;

/**
 * Instead of sending messages somewhere, it stores them until {@linkplain #getUnprocessedMessagesAndReset()} called.
 */
public class KeepMessagesLogger extends BaseServerLoggerFacade {
  private final List<BuildMessage1> myUnprocessedMessages = new ArrayList<BuildMessage1>();

  @Override
  public void flush() {
  }

  @Override
  protected void log(final BuildMessage1 buildMessage1) {
    myUnprocessedMessages.add(buildMessage1);
  }

  public List<BuildMessage1> getUnprocessedMessagesAndReset() {
    final ArrayList<BuildMessage1> list = new ArrayList<BuildMessage1>(myUnprocessedMessages);
    myUnprocessedMessages.clear();
    return list;
  }
}
