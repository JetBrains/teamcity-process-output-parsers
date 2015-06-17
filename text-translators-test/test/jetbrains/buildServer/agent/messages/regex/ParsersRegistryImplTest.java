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

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.buildServer.RunnerTest2Base;
import jetbrains.buildServer.agent.messages.SimpleMessagesTranslator;
import jetbrains.buildServer.agent.messages.TranslatorsRegistry;
import jetbrains.buildServer.runner.SimpleRunnerConstants;
import jetbrains.buildServer.serverSide.BasePropertiesModel;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.CollectionsUtil;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Test
public class ParsersRegistryImplTest extends RunnerTest2Base {

  @Override
  @NotNull
  protected String getRunnerType() {
    return SimpleRunnerConstants.TYPE;
  }

  @Override
  protected String getTestDataPrefixPath() {
    return "text-translators-test/testData";
  }

  @Override
  protected String getTestDataSuffixPath() {
    return "";
  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    setPartialMessagesChecker();
    new TeamCityProperties() {{
      setModel(new BasePropertiesModel() {
        @NotNull
        @Override
        public Map<String, String> getUserDefinedProperties() {
          return CollectionsUtil.asMap("teamcity.agent.build.messages.translators.enabled", "true");
        }
      });
    }};
  }

  @Test
  public void testRegisterCommandProcessed() throws Throwable {
    @Language("Bash") final String script = "echo \"##teamcity[RegexMessageParser.Enable resource='simple-parser.xml']\"\n" +
        "sleep 10s\n" +
        "echo \"Should be matched by simple parser\" 1>&2\n" +
        "echo \"END\"";

    addRunParameter(SimpleRunnerConstants.USE_CUSTOM_SCRIPT, "true");
    addRunParameter(SimpleRunnerConstants.SCRIPT_CONTENT, script);
    new TeamCityProperties() {{}};

    final SFinishedBuild build = doTest(null);

    final ConfigurableApplicationContext context = getApplicationContext();
    final TranslatorsRegistry registry = context.getBean(TranslatorsRegistry.class);
    final List<SimpleMessagesTranslator> translators = registry.getAllTranslators();
    assertEquals(1, translators.size());

    checkMessages(build.getBuildLog().getMessagesIterator(), "simple-parser-enabled-by-command", getCurrentDir(), getCurrentOwnPort());
  }
}