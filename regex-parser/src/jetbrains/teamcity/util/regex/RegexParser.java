/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.teamcity.util.regex;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.io.StreamUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import jetbrains.buildServer.messages.XStreamHolder;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.xstream.XStreamWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code RegexParser} is an parser designed to use regular expressions in order
 * to parse build output to change output type for Errors, Warnings or some other using {@link RegexPattern}
 *
 * @author Vladislav.Rassokhin
 */
@XStreamAlias("parser")
public class RegexParser {
  @XStreamAlias("id")
  @XStreamAsAttribute
  private final String myId;

  @XStreamAlias("name")
  @XStreamAsAttribute
  private final String myName;

  @NotNull
  @XStreamImplicit(itemFieldName = "pattern")
  private final List<RegexPattern> myPatterns = new ArrayList<RegexPattern>();

  /**
   * Constructor to initialize ID and name of the error parser.
   *
   * @param id   - ID of the parser.
   * @param name - name of the parser.
   */
  public RegexParser(final String id, final String name) {
    myName = name;
    myId = id;
  }

  /**
   * @return id of parser
   */
  public String getId() {
    return myId;
  }

  /**
   * @return name of parser
   */
  public String getName() {
    return myName;
  }

  /**
   * @return list of patterns of this parser.
   */
  @NotNull
  public List<RegexPattern> getPatterns() {
    return myPatterns;
  }

  public void addPattern(@NotNull final RegexPattern pattern) {
    myPatterns.add(pattern);
  }


  /**
   * Parse a line of build output.
   *
   * @param line          - line of the input
   * @param parserManager - parsing manager
   * @return true if parser recognized and accepted line, false otherwise
   */
  public boolean processLine(@NotNull final String line, @NotNull final ParserManager parserManager) {
    for (final RegexPattern pattern : myPatterns) {
      try {
        if (pattern.processLine(line, parserManager)) {
          return true;
        }
      } catch (Exception e) {
        parserManager.parsingError("Error parsing line [" + line + "]" + StringUtil.stackTrace(e));  // TODO: using 'debug' param
      }
    }
    return false;
  }

  @NotNull
  public String serialize() {
    return XStreamWrapper.serializeObject(this, createXStreamHolder());
  }

  private static final NotNullLazyValue<XStreamHolder> ourXStreamHolder = (new NotNullLazyValue<XStreamHolder>() {
    @NotNull
    @Override
    protected XStreamHolder compute() {
      return createXStreamHolder();
    }
  });

  @Nullable
  public static RegexParser deserialize(@NotNull final InputStream serialized) throws IOException, ParserLoadingException {
    return deserialize(StreamUtil.readText(serialized, "UTF-8"));
  }


  @Nullable
  public static RegexParser deserialize(@NotNull final String xml) throws ParserLoadingException {
    if (xml.isEmpty()) {
      return null;
    } else {
      try {
        return XStreamWrapper.deserializeObject(RegexParser.class.getClassLoader(), xml, ourXStreamHolder.getValue());
      } catch (XStreamException e) {
        throw new ParserLoadingException("Cannot deserialize parser configuration: " + e.getMessage(), e);
      }
    }
  }

  @NotNull
  private static XStreamHolder createXStreamHolder() {
    return new XStreamHolder() {
      @Override
      protected void configureXStream(@NotNull final XStream xStream) {
        xStream.processAnnotations(RegexParser.class);
        xStream.processAnnotations(RegexPattern.class);
      }
    };
  }

}
