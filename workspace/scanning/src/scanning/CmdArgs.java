package scanning;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import streams.*;

//import static com.js.basic.Tools.*;

/**
 * Class to manipulate command line arguments
 * 
 * <pre>
 * 
 * Arguments are of these types: 
 * 
 * single-character options: -t -I -ab 
 *   (they must consist of one or more letters, digits, or _) (-ab is treated as -a -b)
 * string options: --debug 
 * values: file.txt 42 "string with \n embedded and \\n escaped characters" 
 *   'also can be single \n quotes' 
 * - (single dash) -2 (digit interpreted as value)
 * 
 * </pre>
 */
public class CmdArgs {

  public void help() {
    throw new CmdArgsException();
  }

  public void exception(String msg) {
    throw new CmdArgsException(msg);
  }

  /**
   * Constructor
   * 
   * @param args
   *          String[] passed to main(); if the first argument is --debugargs,
   *          strips it off and sets verbose mode for this object
   * @param defaults
   *          if not null, string to parse and insert in front of args as
   *          defaults
   * 
   *          It has the following format: == : set mode to equivalencies !! :
   *          set mode to defaults (the initial mode)
   * 
   *          In defaults mode, tokens are parsed as arguments In equivalencies
   *          mode, pairs are read, for --long -short equivalences
   * 
   * @param helpMsg
   *          if not null, help message to display if exception occurs
   */
  public CmdArgs(String[] args, String defaults, String helpMsg) {

    if (helpMsg != null)
      mHelpMsg = helpMsg;

    if (defaults != null) {
      boolean modeDef = true;

      TextScanner s = new TextScanner(defaults);
      while (true) {
        s.readWS();
        if (s.eof()) {
          break;
        }

        String arg = s.readWordOrStr(false);
        if (arg.equals("!!"))
          modeDef = true;
        else if (arg.equals("=="))
          modeDef = false;
        else {
          if (modeDef) {
            addArguments(arg, false);
          } else {
            mEquivalentsMap.put(arg, s.readWordOrStr(true));
          }
        }
      }
    }
    addArguments(args, 0, -1, false);
  }

  /**
   * Throw an 'unsupported' CmdArg exception with last option parsed
   */
  public void unsupported() {
    throw new CmdArgsException("Unsupported option: " + mLastOption);
  }

  // determine if there are more arguments to process
  public boolean hasNext() {
    return !mArguments.isEmpty();
  }

  // determine if there is a next argument which is a value
  public boolean nextIsValue() {
    return hasNext() && !nextIsOption();
  }

  public String peek() {
    String out = null;
    if (hasNext()) {
      out = (String) mArguments.peek();
    }
    return out;
  }

  public boolean nextIsInt() {
    String s = peek();
    boolean out = false;
    if (s != null) {
      try {
        Integer.parseInt(s);
        out = true;
      } catch (NumberFormatException e) {
      }
    }
    return out;
  }

  /**
   * Determine if there is a next argument that is a single-character or
   * multiple-character option
   * 
   * @return boolean
   */
  public boolean nextIsOption() {
    boolean out = false;
    do {
      if (!hasNext()) {
        break;
      }
      String s = (String) mArguments.peek();
      if (!isOption(s)) {
        break;
      }
      out = true;
    } while (false);
    return out;
  }

  /**
   * Determine if there is a next argument that is a single-character option
   * 
   * @return boolean
   */
  public boolean nextIsChar() {
    boolean out = false;
    do {
      if (!hasNext()) {
        break;
      }
      String s = peek();
      if (!isOption(s)) {
        break;
      }
      if (s.length() > 2) {
        break;
      }
      out = true;
    } while (false);
    return out;
  }

  /**
   * Read next argument as an option. Throw exception if missing or not an
   * option.
   * 
   * @return String
   */
  public String nextOption() {
    if (nextIsValue()) {
      throw new CmdArgsException("Unexpected value in arguments: "
          + mArguments.peek());
    }

    mLastOption = canonicalArgument(mArguments.remove());
    return mLastOption;
  }

  /**
   * Read next argument as a single-character option. Throw exception if missing
   * or not of this type
   * 
   * @return char
   */
  public char nextChar() {
    String s = nextOption();
    if (s.length() > 2) {
      throw new CmdArgsException("Unexpected argument: " + s);
    }
    return s.charAt(1);
  }

  // read the next argument if it matches a particular option;
  // if there are no more, or it's not a match, return false
  public boolean peekOption(String c) {
    boolean out = true;
    do {
      if (hasNext()) {
        String s = peek();
        if (isOption(s) && optionBody(s).equals(c)) {
          nextOption();
          break;
        }
      }
      out = false;
    } while (false);
    return out;
  }

  // parse next argument as integer
  public int nextInt() {
    return Integer.parseInt(nextValue());
  }

  // parse next argument as double
  public double nextDouble() {
    return Double.parseDouble(nextValue());
  }

  // read next argument as a value; throw exception if it's
  // not a value, or is missing
  public String nextValue() {
    if (!nextIsValue()) {
      StringBuilder sb = new StringBuilder("Missing value in arguments");
      if (mLastOption != null) {
        sb.append(" for option " + mLastOption);
      }
      throw new CmdArgsException(sb.toString());
    }
    String s = mArguments.remove();
    return TextScanner.removeQuotes(s);
  }

  // indicate that processing is done; generate exception if more
  // arguments remain unprocessed
  public void done() {
    if (!hasNext()) {
      StringBuilder sb = new StringBuilder("Unexpected arguments: ");
      while (!mArguments.isEmpty()) {
        sb.append(mArguments.remove());
        sb.append(" ");
      }
      throw new CmdArgsException(sb.toString());
    }
  }

  /**
   * Get next value as a path, convert to abstract form
   * 
   * @param defaultExtension
   *          : if not null, and path hasn't got an extension, adds this one
   * @return path
   */
  public File nextPath(String defaultExtension) {
    String str = nextValue();
    if (defaultExtension != null)
      str = Streams.addExtension(str, defaultExtension);
    return new File(str);

  }

  /**
   * Get next value as a path, and convert to abstract form
   * 
   * @return path
   */
  public File nextPath() {
    return nextPath(null);
  }

  private void addArgument(String s, boolean toFront) {
    String arg = null;
    // If it starts with a space, surround it with quotes to make it a value.
    if (s.startsWith(" ")) {
      s = "\"" + s.trim() + "\"";
    }

    // is it a character option?
    if (isStringOption(s)) {
      arg = s;
    } else if (isOption(s)) {
      for (int j = 1; j < s.length(); j++) {
        char oc = s.charAt(j);
        arg = "-" + oc;
      }
    } else {
      arg = s;
    }
    if (toFront)
      mArguments.addFirst(arg);
    else
      mArguments.add(arg);
  }

  /**
   * Parse arguments
   * 
   * @param args
   *          String[]
   */
  private void addArguments(String[] args, int startOffset, int total,
      boolean toFront) {
    if (total < 0)
      total = args.length - startOffset;

    if (toFront) {
      for (int i = startOffset + total - 1; i >= startOffset; i--) {
        addArgument(args[i], true);
      }
    } else
      for (int i = startOffset; i < startOffset + total; i++) {
        addArgument(args[i], false);
      }
  }

  /**
   * Get canonical argument by replacing longer with shorter equivalent (if one
   * found)
   */
  private String canonicalArgument(String s) {
    String equiv = mEquivalentsMap.get(s);
    if (equiv == null)
      equiv = s;
    return s;
  }

  private static boolean isStringOption(String s) {
    return s.startsWith("--");
  }

  /**
   * Determine if string represents an option (starts with "-" or "--", and next
   * character is letter or _)
   * 
   * @param s
   *          String
   * @return boolean
   */
  private static boolean isOption(String s) {
    boolean out = false;
    do {
      if (!s.startsWith("-")) {
        break;
      }
      if (s.length() > 1
          && (s.charAt(1) != '-' && !TextScanner.isIdentifierStart(s.charAt(1)))) {
        break;
      }
      out = true;
    } while (false);
    return out;
  }

  private static void addWord(String str, List<String> sa, int pos, int len,
      boolean addZeroLen) {
    if (addZeroLen || len > 0) {
      String out = str.substring(pos, pos + len);
      sa.add(out);
    }
  }

  private static String optionBody(String s) {
    int i = 1;
    if (s.startsWith("--")) {
      i = 2;
    }
    return s.substring(i);
  }

  /**
   * Parse string into individual strings, add to command arguments
   * 
   * @param str
   *          : string to split into individual cmd line args
   */
  private void addArguments(String str, boolean toFront) {
    List<String> sa = new ArrayList();

    char strDelim = 0;
    int len = 0;
    int pos = 0;

    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);

      if (strDelim == 0 && c == '/' && i + 1 < str.length()
          && str.charAt(i + 1) == '/') {
        while (i < str.length() && str.charAt(i) != '\n')
          i++;
        continue;
      }

      if (strDelim != 0 && c == strDelim) {
        strDelim = 0;
        addWord(str, sa, pos, len, true);
        len = 0;
        continue;
      }

      if (strDelim == 0 && (c == '\'' || c == '\"')) {
        addWord(str, sa, pos, len, false);
        len = 0;
        strDelim = c;
        continue;
      }

      if (c <= ' ' && strDelim == 0) {
        addWord(str, sa, pos, len, false);
        len = 0;
        continue;
      }

      if (len == 0) {
        pos = i;
      }
      len++;

    }
    if (strDelim != 0) {
      throw new CmdArgsException("Missing quote in arguments");
    }
    addWord(str, sa, pos, len, false);
    addArguments(sa.toArray(new String[0]), 0, -1, toFront);
  }

  class CmdArgsException extends ScanException {
    public CmdArgsException() {
      super(mHelpMsg);
    }

    public CmdArgsException(String msg) {
      super(msg + "\n" + mHelpMsg);
    }
  }

  private ArrayDeque<String> mArguments = new ArrayDeque();
  private String mHelpMsg = "(No help provided)\n";
  private Map<String, String> mEquivalentsMap = new HashMap();
  private String mLastOption;
}
