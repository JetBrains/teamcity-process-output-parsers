

package jetbrains.teamcity.util.regex;

public class ParserLoadingException extends Exception {
  public ParserLoadingException() {
  }

  public ParserLoadingException(final String message) {
    super(message);
  }

  public ParserLoadingException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ParserLoadingException(final Throwable cause) {
    super(cause);
  }
}