

package jetbrains.buildServer.agent.messages;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Simple translators registry.
 * <br/>
 * Don't forget to unregister translator once step/build is finished.
 */
public interface TranslatorsRegistry {
  void register(@NotNull SimpleMessagesTranslator translator);

  void unregister(@NotNull SimpleMessagesTranslator translator);

  @NotNull
  List<SimpleMessagesTranslator> getAllTranslators();
}