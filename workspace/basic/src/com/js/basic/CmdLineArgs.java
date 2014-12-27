package com.js.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * <pre>
 * 
 * Command line argument processor
 * 
 * Inspired by trollop.rb.
 * I looked at 3rd party alternatives, and they all seem fairly heavyweight.
 * 
 * Terminology: 
 * 
 * Options (e.g., 'speed' of type: double) are provided by the client application.
 * Arguments (e.g., '--speed 42.5') are provided by the user from the command line.
 * 
 * Options are analogous to classes, whereas arguments are analogous to objects 
 * or instances.
 * 
 * Option types can be booleans, integers, doubles, or strings; they can be scalars,
 * or (except boolean) can be arrays of zero or more of these.
 * 
 * Usage:
 * 
 * 1) construct a CmdLineArgs instance
 * 
 * CmdLineArgs clArgs = new CmdLineArgs()
 * 
 * 2) define options 
 * 
 * clArgs.banner("Program for compiling graphs or whatnot");   // displayed in help
 * 
 * clArgs.add("speed").def(42.0);      // Double
 * clArgs.add("verbose");              // Boolean; default is false
 * clArgs.add("maxdepth").def(8);      // Integer
 * clArgs.add("name").setString();     // No default value, type set explicitly
 * clArgs.add("heights").setInt().setArray();   // array of (zero or more) ints
 * 
 * 3) parse command line arguments
 * 
 * try {
 *   clArgs.parse(args);                          // where clArgs is String[]
 * } catch (CmdLineArgs.Exception e) {
 *   System.out.println(e.getMessage());
 * }
 * 
 * 4) process arguments
 * 
 * if (clArgs.get("verbose")) { ... }   
 * 
 * if (clArgs.hasValue("name")) { 
 *    System.out.println("name is "+clArgs.getString("name"));
 * }
 * 
 * clArgs.getInts("heights"); // get array (or ints)
 * 
 * clArgs.getExtras();  // returns array of any arguments not parsed as options
 * 
 * 
 * </pre>
 */
public class CmdLineArgs {

  private static final String HELP = "help";

  /**
   * Specify a banner, which describes the app; it's displayed in help messages
   */
  public CmdLineArgs banner(String banner) {
    mBanner = banner;
    return this;
  }

  /**
   * Add an option; makes it the current option. It initially has type boolean,
   * with default value false
   * 
   * @param longName
   */
  public CmdLineArgs add(String longName) {
    if (mLocked)
      throw new IllegalStateException();
    if (mOptions.containsKey(longName))
      throw new IllegalArgumentException("option already exists: " + longName);
    mOpt = new Opt(longName);
    mLongestNameLength = Math.max(mLongestNameLength, longName.length());
    mOptions.put(mOpt.mLongName, mOpt);
    mOptionList.add(mOpt.mLongName);
    return this;
  }

  /**
   * Set type of current option to int
   */
  public CmdLineArgs setInt() {
    mOpt.setType(T_INT);
    return this;
  }

  /**
   * Set type of current option to double
   */
  public CmdLineArgs setDouble() {
    mOpt.setType(T_DOUBLE);
    return this;
  }

  /**
   * Set type of current option to string
   */
  public CmdLineArgs setString() {
    mOpt.setType(T_STRING);
    return this;
  }

  /**
   * Make current option an array type
   */
  public CmdLineArgs setArray() {
    mOpt.setArray();
    return this;
  }

  /**
   * Set default value of current option to a boolean
   */
  public CmdLineArgs def(boolean boolValue) {
    mOpt.setType(T_BOOL);
    mOpt.mDefaultValue = boolValue;
    return this;
  }

  /**
   * Set type of current option to string, with a default value
   */
  public CmdLineArgs def(String stringValue) {
    mOpt.setType(T_STRING);
    mOpt.mDefaultValue = stringValue;
    return this;
  }

  /**
   * Set type of current option to integer, with a default value
   */
  public CmdLineArgs def(int intValue) {
    mOpt.setType(T_INT);
    mOpt.mDefaultValue = intValue;
    return this;
  }

  /**
   * Set type of current option to double, with a default value
   */
  public CmdLineArgs def(double doubleValue) {
    mOpt.setType(T_DOUBLE);
    mOpt.mDefaultValue = doubleValue;
    return this;
  }

  /**
   * Set description for an option (it is displayed within help messages)
   */
  public CmdLineArgs desc(String description) {
    mOpt.mDescription = description;
    return this;
  }

  /**
   * Determine if user provided a value for a particular option
   * 
   * @param optionName
   *          name of option
   * @return true if user provided a value
   */
  public boolean hasValue(String optionName) {
    return !findOption(optionName).mValues.isEmpty();
  }

  /**
   * Get the boolean value supplied for an option, or its default if none was
   * given. If no default was specified, assume it was false.
   */
  public boolean get(String optionName) {
    Opt opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_BOOL, "type mismatch", optionName);
    Object value = opt.mDefaultValue;
    if (value == null)
      value = Boolean.FALSE;
    if (!opt.mValues.isEmpty())
      value = opt.mValues.get(0);
    return (Boolean) value;
  }

  /**
   * Get the single integer supplied for an option, or its default value if none
   * was given
   */
  public int getInt(String optionName) {
    Opt opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_INT, "type mismatch", optionName);
    Object value = opt.mDefaultValue;
    if (!opt.mValues.isEmpty())
      value = opt.mValues.get(0);
    validate(value != null, "missing value", optionName);
    return (Integer) value;
  }

  /**
   * Get the single double supplied for an option, or its default value if none
   * was given
   */
  public double getDouble(String optionName) {
    Opt opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_DOUBLE, "type mismatch", optionName);
    Object value = opt.mDefaultValue;
    if (!opt.mValues.isEmpty())
      value = opt.mValues.get(0);
    validate(value != null, "missing value", optionName);
    return (Double) value;
  }

  /**
   * Get the single string supplied for an option, or its default value if none
   * was given
   */
  public String getString(String optionName) {
    Opt opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_STRING, "type mismatch", optionName);
    Object stringVal = opt.mDefaultValue;
    if (!opt.mValues.isEmpty())
      stringVal = opt.mValues.get(0);
    validate(stringVal != null, "missing value", optionName);
    return stringVal.toString();
  }

  /**
   * Get the array of ints supplied for an option
   */
  public int[] getInts(String optionName) {
    Opt opt = findOption(optionName);
    validate(opt.mArray && opt.mType == T_INT, "type mismatch", optionName);
    int[] a = new int[opt.mValues.size()];
    for (int i = 0; i < a.length; i++)
      a[i] = ((Integer) opt.mValues.get(i));
    return a;
  }

  /**
   * Get the array of doubles supplied for an argument
   */
  public double[] getDoubles(String arg) {
    Opt opt = findOption(arg);
    validate(opt.mArray && opt.mType == T_DOUBLE, "type mismatch", arg);

    double[] a = new double[opt.mValues.size()];
    for (int i = 0; i < a.length; i++)
      a[i] = ((Double) opt.mValues.get(i));
    return a;
  }

  /**
   * Get the array of doubles supplied for an argument, converted to floats
   */
  public float[] getFloats(String arg) {
    double[] d = getDoubles(arg);
    float[] f = new float[d.length];
    for (int i = 0; i < d.length; i++)
      f[i] = (float) d[i];
    return f;
  }

  /**
   * Get the array of strings supplied for an option
   */
  public String[] getStrings(String optionName) {
    Opt opt = findOption(optionName);
    validate(opt.mArray && opt.mType == T_STRING, "type mismatch", optionName);

    String[] a = new String[opt.mValues.size()];
    for (int i = 0; i < a.length; i++)
      a[i] = opt.mValues.get(i).toString();
    return a;
  }

  public String[] getExtras() {
    return mExtraArguments.toArray(new String[0]);
  }

  /**
   * Parse command line arguments
   */
  public void parse(String[] args) {
    lock();
    ArrayList argList = unpackArguments(args);
    readArgumentValues(argList);
  }

  /**
   * Throw a CmdLineArgs.Exception
   * 
   * @param message
   */
  public void fail(String message) {
    throw new Exception(message);
  }

  public void help() {
    if (!mLocked)
      throw new IllegalStateException();
    StringBuilder sb = new StringBuilder();
    if (mBanner != null) {
      sb.append(mBanner);
      sb.append("\n");
    }
    for (String key : mOptionList) {
      Opt opt = mOptions.get(key);
      sb.append(Tools.spaces(mLongestNameLength - opt.mLongName.length()));
      sb.append("--" + opt.mLongName + ", -" + opt.mShortName + ":  ");
      sb.append(opt.description());
      sb.append("\n");
    }
    throw new Exception(sb.toString());
  }

  private Opt findOption(String optionName) {
    Opt opt = mOptions.get(optionName);
    validate(opt != null, "unrecognized option", optionName);
    return opt;
  }

  private ArrayList unpackArguments(String[] args) {
    ArrayList argList = new ArrayList();
    for (int cursor = 0; cursor < args.length; cursor++) {
      String arg = args[cursor];
      if (sArgumentsPattern.matcher(arg).matches()) {
        if (arg.startsWith("--")) {
          Opt opt = findOption(arg.substring(2));
          opt.mInvocation = arg;
          argList.add(opt);
        } else {
          for (int i = 1; i < arg.length(); i++) {
            Opt opt = findOption(arg.substring(i, i + 1));
            opt.mInvocation = arg;
            argList.add(opt);
          }
        }
        continue;
      }
      argList.add(arg);
    }
    return argList;
  }

  private void readArgumentValues(ArrayList args) {
    int cursor = 0;
    while (cursor < args.size()) {
      Object arg = args.get(cursor);
      cursor++;
      if (arg instanceof Opt) {
        Opt opt = (Opt) arg;
        if (opt.mType == T_BOOL) {
          opt.addValue(Boolean.TRUE);
          if (opt.mLongName == HELP) {
            help();
          }
          continue;
        }

        while (true) {
          if (cursor == args.size())
            break;
          if (!opt.mArray && !opt.mValues.isEmpty())
            break;
          arg = args.get(cursor);
          if (arg instanceof Opt)
            break;
          cursor++;
          String value = arg.toString();
          if (opt.mType == T_DOUBLE) {
            try {
              opt.addValue(Double.parseDouble(value));
            } catch (NumberFormatException e) {
              validate(false, "invalid argument " + value, opt.mInvocation);
            }
          } else if (opt.mType == T_INT) {
            try {
              opt.addValue(Integer.parseInt(value));
            } catch (NumberFormatException e) {
              validate(false, "invalid argument " + value, opt.mInvocation);
            }
          } else {
            opt.addValue(value);
          }
        }
        validate(!(!opt.mArray && opt.mValues.isEmpty()), "missing argument",
            opt.mInvocation);
      } else {
        mExtraArguments.add(arg.toString());
      }
    }
  }

  private static final String T_INT = "INT";
  private static final String T_DOUBLE = "DOUBLE";
  private static final String T_STRING = "STRING";
  private static final String T_BOOL = "BOOL";

  /**
   * Throw a CmdLineArgs.Exception if a condition is false
   * 
   * @param condition
   * @param message
   *          message to include
   * @param arg
   *          optional argument to append to message
   */
  private void validate(boolean condition, String message, String arg) {
    if (condition)
      return;
    if (arg != null)
      message += ": " + arg;
    throw new Exception(message);
  }

  private void lock() {
    if (mLocked)
      return;
    add(HELP).desc("Show this message");
    mOpt = null;
    mLocked = true;
    chooseShortNames();
  }

  private void chooseShortNames() {
    for (String key : mOptionList) {

      Opt opt = mOptions.get(key);

      for (int j = 0; opt.mShortName == null; j++) {
        if (j == key.length()) {
          // Choose first unused character
          String poss = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
          for (int k = 0; k < poss.length(); k++) {
            String candidate = poss.substring(k, k + 1);
            if (!mOptions.containsKey(candidate)) {
              opt.mShortName = candidate;
              break;
            }
          }
          break;
        }

        String candidate = key.substring(j, j + 1);
        if (!mOptions.containsKey(candidate)) {
          opt.mShortName = candidate;
          break;
        }
        candidate = candidate.toUpperCase();
        if (!mOptions.containsKey(candidate)) {
          opt.mShortName = candidate;
          break;
        }
      }
      validate(opt.mShortName != null, "can't find short name for", key);
      mOptions.put(opt.mShortName, opt);
    }
  }

  /**
   * Representation of a command line option
   */
  private static class Opt {
    public Opt(String longName) {
      mLongName = longName;
      // Until changed to something else, we will assume the type is boolean
      mType = T_BOOL;
    }

    public void setType(String type) {
      if (mTypeDefined)
        throw new IllegalStateException();
      mTypeDefined = true;
      mType = type;
    }

    public void setArray() {
      if (!(mTypeDefined && !mArray && mDefaultValue == null))
        throw new IllegalStateException();
      mArray = true;
    }

    public String description() {
      if (mDescription == null)
        return "(no description)";
      return mDescription;
    }

    public void addValue(Object value) {
      if (!mArray)
        mValues.clear();
      mValues.add(value);
    }

    public String mLongName;
    public String mShortName;
    public Object mDefaultValue;
    public String mDescription;
    public Object mType;
    public boolean mArray;
    public boolean mTypeDefined;
    public String mInvocation;
    public ArrayList<Object> mValues = new ArrayList();
  }

  public class Exception extends RuntimeException {
    public Exception(String msg) {
      super(msg);
    }
  }

  // Regular expression for arguments; e.g. '-a' '-abc' '--sound'
  private static Pattern sArgumentsPattern = Pattern.compile("^--?[a-zA-Z]+$");

  private int mLongestNameLength = 8;
  private boolean mLocked;
  private String mBanner;
  private Opt mOpt;
  private ArrayList<String> mExtraArguments = new ArrayList();
  private HashMap<String, Opt> mOptions = new HashMap();
  private ArrayList<String> mOptionList = new ArrayList();
}
