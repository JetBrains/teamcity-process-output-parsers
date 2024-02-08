

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