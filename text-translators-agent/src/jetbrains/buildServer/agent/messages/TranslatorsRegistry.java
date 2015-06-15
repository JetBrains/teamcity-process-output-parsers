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

import java.util.List;

/**
 * Simple translators registry.
 * <br/>
 * Don't forget to unregister translator once step/build is finished.
 */
public interface TranslatorsRegistry {
  void register(SimpleMessagesTranslator translator);
  void unregister(SimpleMessagesTranslator translator);
  List<SimpleMessagesTranslator> getAllTranslators();
}
