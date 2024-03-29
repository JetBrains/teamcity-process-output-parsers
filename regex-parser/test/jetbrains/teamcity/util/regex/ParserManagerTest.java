

package jetbrains.teamcity.util.regex;

import jetbrains.buildServer.BaseTestCase;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vladislav.Rassokhin
 */
public class ParserManagerTest extends BaseTestCase {

  @Test
  public void testBasic() throws Exception {
    final CounterLogger logger = new CounterLogger();
    final ParserManager pm = new ParserManager(logger);
    Assert.assertEquals(pm.getLogger(), logger);
    pm.parsingError("test");
    Assert.assertEquals(logger.warning, 1);
  }

  @Test
  public void testLoggingCount() throws Exception {
    final CounterLogger logger = new CounterLogger();
    final ParserManager pm = new ParserManager(logger);

    final Map<Severity, Integer> countMap = new HashMap<Severity, Integer>();
    countMap.put(Severity.INFO, 5);
    countMap.put(Severity.ERROR, 2);
    countMap.put(Severity.WARN, 3);
    countMap.put(Severity.SPECIAL, 2);

    for (final Map.Entry<Severity, Integer> entry : countMap.entrySet()) {
      for (int i = 0; i < entry.getValue(); ++i) {
        pm.log("text", entry.getKey());
      }
    }

    Assert.assertEquals(logger.message, 5 + 2);
    Assert.assertEquals(logger.error, 2);
    Assert.assertEquals(logger.warning, 3);
    Assert.assertEquals(logger.special, 0);
  }

  @Test
  public void testLoggingSpecialCount() throws Exception {
    final CounterLogger logger = new CounterLogger();
    final ParserManager pm = new ParserManager(logger) {
      @Override
      protected boolean specialParse(@NotNull final String line) {
        ((CounterLogger) getLogger()).special();
        return true;
      }
    };

    final Map<Severity, Integer> countMap = new HashMap<Severity, Integer>();
    countMap.put(Severity.INFO, 5);
    countMap.put(Severity.ERROR, 2);
    countMap.put(Severity.WARN, 3);
    countMap.put(Severity.SPECIAL, 2);

    for (final Map.Entry<Severity, Integer> entry : countMap.entrySet()) {
      for (int i = 0; i < entry.getValue(); ++i) {
        pm.log("text", entry.getKey());
      }
    }

    Assert.assertEquals(logger.message, 5);
    Assert.assertEquals(logger.error, 2);
    Assert.assertEquals(logger.warning, 3);
    Assert.assertEquals(logger.special, 2);
  }
}