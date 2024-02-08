

package jetbrains.buildServer.agent.messages.regex;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageHandler;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister;
import org.jetbrains.annotations.NotNull;

public class ParserCommandServiceMessageHandler implements ServiceMessageHandler {
  private static final Logger LOG = Logger.getInstance(ParserCommandServiceMessageHandler.class.getName());
  @NotNull
  private final ParsersRegistry myManipulator;

  public ParserCommandServiceMessageHandler(@NotNull final ParsersRegistry manipulator,
                                            @NotNull final ServiceMessagesRegister register) {
    myManipulator = manipulator;
    for (String command : ParserCommand.COMMANDS) {
      register.registerHandler(command, this);
    }
  }

  @Override
  public void handle(@NotNull final ServiceMessage message) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(message.getMessageName() + " message found: " + message);
    }
    final ParserCommand command;
    try {
      command = ParserCommand.fromSM(message);
    } catch (IllegalArgumentException e) {
      LOG.warn("Cannot create parser command from service message '" + message + "':" + e.getMessage());
      return;
    }
    command.apply(myManipulator);
  }
}