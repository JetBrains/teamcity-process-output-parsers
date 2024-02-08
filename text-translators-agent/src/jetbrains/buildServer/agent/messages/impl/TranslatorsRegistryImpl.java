

package jetbrains.buildServer.agent.messages.impl;

import jetbrains.buildServer.agent.messages.SimpleMessagesTranslator;
import jetbrains.buildServer.agent.messages.TranslatorsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TranslatorsRegistryImpl implements TranslatorsRegistry {
  private final List<SimpleMessagesTranslator> mySimpleMessagesTranslators = new CopyOnWriteArrayList<SimpleMessagesTranslator>();

  @Override
  public void register(@NotNull final SimpleMessagesTranslator translator) {
    mySimpleMessagesTranslators.add(translator);
  }

  @Override
  public void unregister(@NotNull final SimpleMessagesTranslator translator) {
    mySimpleMessagesTranslators.remove(translator);
  }

  @NotNull
  @Override
  public List<SimpleMessagesTranslator> getAllTranslators() {
    return Collections.unmodifiableList(mySimpleMessagesTranslators);
  }
}