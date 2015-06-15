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

import jetbrains.buildServer.messages.BuildMessage1;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.TextMessageProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Simple text messages translator.
 * <br/>
 * Since text messages may contain service messages, there two methods in class.
 * <br/>
 * For processing result see {@linkplain Result}
 * <br/>
 * Inspired by {@linkplain TextMessageProcessor}
 */
public interface SimpleMessagesTranslator {
  Result doProcessMessage(@NotNull ServiceMessage message, @NotNull BuildLogTail tail);

  Result doProcessText(@NotNull String text, @NotNull BuildLogTail tail);

  /**
   * Result is either:
   * <ul>
   * <li/>SKIP (not matched by this translator),
   * <li/>KEEP_ORIGIN (matched, nothing should be changed) or
   * <li/>REPLACE (matched, message should be replaced)
   * </ul>
   */
  final class Result {
    public static final Result SKIP = new Result(false, true, Collections.<BuildMessage1>emptyList());
    public static final Result KEEP_ORIGIN = new Result(true, true, Collections.<BuildMessage1>emptyList());
    private final boolean consumed;
    private final boolean keep;
    private final List<BuildMessage1> messages;

    private Result(final boolean consumed, final boolean keepOrigin, final List<BuildMessage1> messages) {
      this.consumed = consumed;
      this.keep = keepOrigin;
      this.messages = messages;
    }

    public static Result REPLACE(List<BuildMessage1> messages) {
      return new Result(true, false, messages);
    }

    public boolean isConsumed() {
      return consumed;
    }

    public boolean isKeepOrigin() {
      return keep;
    }

    public List<BuildMessage1> getMessages() {
      return messages;
    }
  }
}
