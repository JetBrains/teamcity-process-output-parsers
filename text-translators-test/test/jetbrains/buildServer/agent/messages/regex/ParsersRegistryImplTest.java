

package jetbrains.buildServer.agent.messages.regex;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.CurrentBuildTracker;
import jetbrains.buildServer.agent.impl.CurrentBuildTrackerImpl;
import jetbrains.buildServer.agent.messages.impl.TranslatorsRegistryImpl;
import jetbrains.buildServer.agent.messages.regex.impl.ParserLoaderImpl;
import jetbrains.buildServer.agent.messages.regex.impl.ParsersRegistryImpl;
import jetbrains.buildServer.serverSide.BasePropertiesModel;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

@Test
public class ParsersRegistryImplTest extends BaseTestCase {
  private EventDispatcher<AgentLifeCycleListener> myEventDispatcher;
  private CurrentBuildTracker myCurrentBuildTracker;
  private TranslatorsRegistryImpl myTranslatorsRegistry;
  private ParserLoader myParserLoader;
  private ParsersRegistry myParsersRegistry;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
//    setPartialMessagesChecker();
    new TeamCityProperties() {{
      setModel(new BasePropertiesModel() {
        @NotNull
        @Override
        public Map<String, String> getUserDefinedProperties() {
          return CollectionsUtil.asMap("teamcity.agent.build.messages.translators.enabled", "true");
        }
      });
    }};
    myEventDispatcher = EventDispatcher.create(AgentLifeCycleListener.class);
    myCurrentBuildTracker = new CurrentBuildTrackerImpl(myEventDispatcher);
    myTranslatorsRegistry = new TranslatorsRegistryImpl();
    myParserLoader = new ParserLoaderImpl(myCurrentBuildTracker);
    myParsersRegistry = new ParsersRegistryImpl(myTranslatorsRegistry, myParserLoader);
  }

  @Test
  public void testCommandsAffectsTranslatorsPerBuild() throws Throwable {
    final ParserCommand.ParserId id = ParserCommand.ParserId.byResourcePath("simple-parser.xml");
    assertTranslatorsCount(0);
    myParsersRegistry.enable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(1);
    myParsersRegistry.disable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(0);
  }

  @Test
  public void testDisablePerBuildShouldDisablePerRunnerTranslator() throws Throwable {
    final ParserCommand.ParserId id = ParserCommand.ParserId.byResourcePath("simple-parser.xml");
    assertTranslatorsCount(0);
    myParsersRegistry.enable(id, ParserCommand.Scope.THIS_RUNNER);
    assertTranslatorsCount(1);
    myParsersRegistry.disable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(0);
  }

  @Test
  public void testSecondEnableDoesNothing() throws Throwable {
    final ParserCommand.ParserId id = ParserCommand.ParserId.byResourcePath("simple-parser.xml");
    assertTranslatorsCount(0);
    myParsersRegistry.enable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(1);
    myParsersRegistry.enable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(1);
    myParsersRegistry.enable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(1);
    myParsersRegistry.disable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(0);
    myParsersRegistry.disable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(0);
  }

  @Test
  public void testLesserScopeEnableDoesNothing() throws Throwable {
    final ParserCommand.ParserId id = ParserCommand.ParserId.byResourcePath("simple-parser.xml");
    assertTranslatorsCount(0);
    myParsersRegistry.enable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(1);
    myParsersRegistry.enable(id, ParserCommand.Scope.THIS_RUNNER);
    assertTranslatorsCount(1);
    myParsersRegistry.disable(id, ParserCommand.Scope.BUILD);
    assertTranslatorsCount(0);
  }

  @Test(enabled = false)
  public void testSwitchingToNextRunnerShouldDestroyPerRunnerParsers() throws Throwable {
    final ParserCommand.ParserId id = ParserCommand.ParserId.byResourcePath("simple-parser.xml");
    assertTranslatorsCount(0);
    myParsersRegistry.enable(id, ParserCommand.Scope.THIS_RUNNER);
    assertTranslatorsCount(1);
    // TODO: Trigger 'runner finished' event
    // TODO: Trigger 'runner started' event
    assertTranslatorsCount(0);
  }

  protected void assertTranslatorsCount(final int expected) {
    assertEquals(expected, myTranslatorsRegistry.getAllTranslators().size());
  }
}