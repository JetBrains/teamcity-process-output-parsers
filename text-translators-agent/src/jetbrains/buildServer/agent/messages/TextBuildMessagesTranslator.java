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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.ServiceNotFoundException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.impl.runContext.RunningBuildServiceLocator;
import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.messages.serviceMessages.AbstractTextMessageProcessor;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesProcessor;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Build messages translator for text messages.
 * <br/>
 * Together with {@linkplain TranslatorsRegistry} provides extensibility to define custom text messages translators(parsers)
 * <br/>
 * If you want to translate text only messages instead of everything, define {@linkplain SimpleMessagesTranslator} and register it.
 */
public class TextBuildMessagesTranslator implements BuildMessagesTranslator {
  private static final Logger LOG = Logger.getInstance(TextBuildMessagesTranslator.class.getName());

  private final AtomicReference<BuildRunnerContext> myActiveRunner;
  @NotNull
  private final CurrentBuildTrackerEx myCurrentBuildTracker;
  @NotNull
  private final TranslatorsRegistry myTranslatorsRegistry;
  private boolean mySuspendServiceMessages = false;

  public TextBuildMessagesTranslator(@NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher,
                                     @NotNull final CurrentBuildTrackerEx currentBuildTracker,
                                     @NotNull final TranslatorsRegistry registry) {
    myCurrentBuildTracker = currentBuildTracker;
    myTranslatorsRegistry = registry;
    myActiveRunner = new AtomicReference<BuildRunnerContext>();

    dispatcher.addListener(new AgentLifeCycleAdapter() {
      @Override
      public void beforeRunnerStart(@NotNull final BuildRunnerContext runner) {
        myActiveRunner.set(runner);
      }

      @Override
      public void buildStarted(@NotNull final AgentRunningBuild runningBuild) {
        mySuspendServiceMessages = false;
      }

      @Override
      public void runnerFinished(@NotNull final BuildRunnerContext runner, @NotNull final BuildFinishedStatus status) {
        myActiveRunner.set(null);
      }
    });
  }

  @NotNull
  public List<BuildMessage1> translate(@NotNull final BuildMessage1 message) {
    final List<BuildMessage1> origin = Collections.singletonList(message);
    if (!myCurrentBuildTracker.isRunningBuild()) {
      return origin;
    }
    final AgentRunningBuildEx build = myCurrentBuildTracker.getCurrentBuild();
    final BuildLogTail tail = build.getBuildLogTail();
    if (!String.valueOf(build.getBuildId()).equals(tail.getBuildId())) {
      return origin;
    }
    // Only text messages processed
    if (!DefaultMessagesInfo.MSG_TEXT.equals(message.getTypeId())) {
      return origin;
    }

    final List<SimpleMessagesTranslator> translators = getAllTranslators(build);

    final BuildMessage1[] translated = {message};
    final List<BuildMessage1> newMessages = new ArrayList<BuildMessage1>();

    ServiceMessagesProcessor.processTextMessage(message, new AbstractTextMessageProcessor() {
      public void processServiceMessage(final @NotNull ServiceMessage serviceMessage, final @NotNull BuildMessage1 originalMessage) {
        if (ServiceMessage.DISABLE.equals(serviceMessage.getMessageName())) {
          mySuspendServiceMessages = true;
          return;
        }
        if (ServiceMessage.ENABLE.equals(serviceMessage.getMessageName())) {
          mySuspendServiceMessages = false;
          return;
        }
        if (mySuspendServiceMessages) return;

        for (SimpleMessagesTranslator simpleMessagesTranslator : translators) {
          final SimpleMessagesTranslator.Result result = simpleMessagesTranslator.doProcessMessage(serviceMessage, tail);
          if (!result.isConsumed()) continue;
          newMessages.addAll(result.getMessages());
          if (!result.isKeepOrigin()) {
            translated[0] = null;
            break;
          }
        }
      }

      @Override
      public void processText(final @NotNull BuildMessage1 originalMessage) {
        for (SimpleMessagesTranslator simpleMessagesTranslator : translators) {
          final SimpleMessagesTranslator.Result result = simpleMessagesTranslator.doProcessText((String) originalMessage.getValue(), tail);
          if (!result.isConsumed()) continue;
          newMessages.addAll(result.getMessages());
          if (!result.isKeepOrigin()) {
            translated[0] = null;
            break;
          }
        }
      }

      @Override
      public void processParseException(final @NotNull ParseException e, final @NotNull BuildMessage1 originalMessage) {
        LOG.warn("Invalid service message: " + originalMessage.getValue() + ", error: " + e.toString());
      }
    });

    final ArrayList<BuildMessage1> result = new ArrayList<BuildMessage1>(newMessages.size() + 1);
    if (translated[0] != null) {
      result.add(translated[0]);
    }
    result.addAll(newMessages);
    return result;
  }

  @NotNull
  private List<SimpleMessagesTranslator> getAllTranslators(AgentRunningBuildEx build) {
    final BuildRunnerContext runnerContext = myActiveRunner.get();
    final List<SimpleMessagesTranslator> simpleMessagesTranslators = new ArrayList<SimpleMessagesTranslator>();
    if (runnerContext != null) {
      simpleMessagesTranslators.addAll(getPerRunnerTranslators(runnerContext));
    }
    simpleMessagesTranslators.addAll(getPerBuildTranslators(build));
    simpleMessagesTranslators.addAll(getGlobalTranslators());
    return simpleMessagesTranslators;
  }


  @NotNull
  private List<SimpleMessagesTranslator> getGlobalTranslators() {
    return myTranslatorsRegistry.getAllTranslators();
  }

  @NotNull
  private List<SimpleMessagesTranslator> getPerBuildTranslators(@NotNull final AgentRunningBuildEx build) {
    final RunningBuildServiceLocator perBuildServiceLocator = build.getPerBuildService(RunningBuildServiceLocator.class);
    assert perBuildServiceLocator != null;
    try {
      // TODO: Make it work: for now there no per-build service
      final TranslatorsRegistry service = perBuildServiceLocator.getPerBuildServiceLocator().getSingletonService(TranslatorsRegistry.class);
      if (myTranslatorsRegistry != service) {
        return service.getAllTranslators();
      } else {
        return Collections.emptyList();
      }
    } catch (ServiceNotFoundException e) {
      return Collections.emptyList();
    }
  }

  @SuppressWarnings("UnusedParameters")
  @NotNull
  private List<SimpleMessagesTranslator> getPerRunnerTranslators(@NotNull final BuildRunnerContext context) {
    // TODO: Implement
    return Collections.emptyList();
  }

}
