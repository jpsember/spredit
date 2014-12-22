package base;

import java.util.*;

import com.js.geometry.MyMath;

//import static base.MyMath.*;

@Deprecated
public final class MyTools {
	// placed here for convenience
	public static final boolean PSFONT = false;

	public static String stackTrace() {
		return stackTraceFmt(1);
	}

	public static String stackTrace(Throwable t) {
		return stackTrace(1, 10, t);
	}

	public static String stackTrace(int max) {
		StringBuilder sb = new StringBuilder();
		sb.append(stackTrace(1, max));
		sb.append(" : ");
		tab(sb, 24);
		return sb.toString();
	}

	private static String stackTraceFmt(int skip) {
		StringBuilder sb = new StringBuilder();
		sb.append(stackTrace(1 + skip, 1));
		sb.append(" : ");
		tab(sb, 24);
		return sb.toString();
	}

	public static void sleepFor(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			report(e, "sleep interrupted");
		}
	}

	/**
	 * Construct a string describing a stack trace
	 * 
	 * @param skipCount
	 *            : # stack frames to skip (actually skips 1 + skipCount, to
	 *            skip the call to this method)
	 * @param displayCount
	 *            : maximum # stack frames to display
	 * @return String; iff displayCount > 1, cr's inserted after every item
	 */
	public static String stackTrace(int skipCount, int displayCount) {
		// skip 1 for call to this method...
		return stackTrace(1 + skipCount, displayCount, new Throwable());
	}

	/**
	 * Construct string describing stack trace
	 * 
	 * @param skipCount
	 *            : # stack frames to skip (actually skips 1 + skipCount, to
	 *            skip the call to this method)
	 * @param displayCount
	 *            : maximum # stack frames to display
	 * @param t
	 *            : Throwable containing stack trace
	 * @return String; iff displayCount > 1, cr's inserted after every item
	 */
	private static String stackTrace(int skipCount, int displayCount,
			Throwable t) {
		final boolean db = false;

		StringBuilder sb = new StringBuilder();

		StackTraceElement[] elist = t.getStackTrace();

		if (db) {
			for (int j = 0; j < elist.length; j++) {
				StackTraceElement e = elist[j];
				sb.append(j >= skipCount && j < skipCount + displayCount ? "  "
						: "x ");
				String cn = e.getClassName();
				cn = cn.substring(cn.lastIndexOf('.') + 1);
				sb.append(cn);
				sb.append(".");
				sb.append(e.getMethodName());
				sb.append(":");
				sb.append(e.getLineNumber());
				sb.append("\n");

			}
			return sb.toString();
		}

		int s0 = skipCount;
		int s1 = s0 + displayCount;

		for (int i = s0; i < s1; i++) {
			if (i >= elist.length) {
				break;
			}
			StackTraceElement e = elist[i];
			String cn = e.getClassName();
			cn = cn.substring(cn.lastIndexOf('.') + 1);
			sb.append(cn);
			sb.append(".");
			sb.append(e.getMethodName());
			sb.append(":");
			sb.append(e.getLineNumber());
			if (displayCount > 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Simple assertion mechanism, throws RuntimeException if flag is false
	 * 
	 * @param flag
	 *            : flag to test
	 * @param message
	 *            : if flag is false, throws RuntimeException including this
	 *            message
	 */
	public static void ASSERT(boolean flag, String message) {
		if (!flag) {
			toss("ASSERTION FAILED (" + message + ")");

		}
	}

	private static void toss(String msg) {
		RuntimeException e = new RuntimeException(msg + " "
				+ MyTools.stackTrace());
		pr("Throwing: " + e + "\n" + stackTrace(8));
		throw e;
	}

	/**
	 * Simple assertion mechanism, throws RuntimeException if flag is false
	 * 
	 * @param flag
	 *            flag to test
	 */
	public static void ASSERT(boolean flag) {
		if (!flag) {
			toss("ASSERTION FAILED");
		}
	}

	// public static boolean rndBool(Random r) {
	// if (r == null)
	// r = random();
	// return r.nextInt() > 0;
	// }

	public static void unimp() {
		warn("TODO", null, 1);
	}

	public static void unimp(String msg) {
		warn("TODO", msg, 1);
	}

	private static void warn(String type, String s, int skipCount) {
		String st = MyTools.stackTrace(1 + skipCount, 1);
		StringBuilder sb = new StringBuilder();
		sb.append("*** ");
		if (type == null) {
			type = "WARNING";
		}
		sb.append(type);
		if (s != null && s.length() > 0) {
			sb.append(": ");
			sb.append(s);
		}
		sb.append(" (");
		sb.append(st);
		sb.append(")");
		String keyString = sb.toString();

		{
			Object wr = warningStrings.get(keyString);
			if (wr == null) {
				warningStrings.put(keyString, Boolean.TRUE);
				pr(keyString);
			}
		}
		// return true;
	}

	public static void warn(String s) {
		warn(null, s, 1);
	}

	public static String f(boolean b) {
		return b ? " T" : " F";
	}

	public static String fBits(int word, int nBits) {
		StringBuilder sb = new StringBuilder();

		for (int j = nBits - 1; j >= 0; j--) {
			if ((word & (1 << j)) != 0)
				sb.append('1');
			else
				sb.append('0');
		}
		sb.append(' ');
		return sb.toString();
	}

	/**
	 * Format a string to be at least a certain size
	 * 
	 * @param s
	 *            string to format
	 * @param length
	 *            minimum size to pad to; negative to insert leading spaces
	 * @return blank-padded string
	 */
	public static String f(String s, int length) {
		return f(s, length, null).toString();
		//
		// StringBuilder sb = new StringBuilder();
		// if (length >= 0) {
		// sb.append(s);
		// return tab(sb, length).toString();
		// } else {
		// tab(sb, (-length) - s.length());
		// sb.append(s);
		// return sb.toString();
		// }
	}

	public static StringBuilder f(String s, int length, StringBuilder sb) {
		if (sb == null)
			sb = new StringBuilder();
		int origLen = sb.length();
		if (length >= 0) {
			sb.append(s);
			if (length > s.length())
				tab(sb, length + origLen);
		} else {
			length = -length;
			if (s.length() < length)
				tab(sb, length - s.length());
			sb.append(s);
		}
		return sb;
	}

	/**
	 * Format a string for debug purposes
	 * 
	 * @param s
	 *            String, may be null
	 * @return String
	 */
	public static String d(CharSequence s) {
		return d(s, 80, false);
	}

	public static String d(Throwable t) {
		return t.getMessage() + "\n" + stackTrace(0, 15, t);
	}

	public static String d(Object obj) {
		String s = null;
		if (obj != null)
			s = obj.toString();
		return d(s);
	}

	public static String describe(Object obj) {
		if (obj == null)
			return "<null>";
		return "<type=" + obj.getClass().getName() + " value="
				+ d(obj.toString()) + ">";
	}

	public static String d(Map m) {
		if (m == null)
			return describe(m);
		StringBuilder sb = new StringBuilder();
		Iterator it = m.keySet().iterator();
		while (it.hasNext()) {
			Object k = it.next();
			sb.append(MyTools.f(k.toString(), 50));
			sb.append(" -> ");
			Object v = m.get(k);
			String s = "";
			if (v != null)
				s = chomp(v.toString());

			sb.append(MyTools.d(s));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String d(Collection c) {
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		Iterator it = c.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// sb.append(' ');
			sb.append(chomp(obj.toString()));
			sb.append('\n');
		}
		sb.append("]\n");
		return sb.toString();
	}

	public static String d(char c) {
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		convert(c, sb);
		sb.append('\'');
		return sb.toString();
	}

	/**
	 * Convert string to debug display
	 * 
	 * @param orig
	 *            String
	 * @param maxLen
	 *            : maximum length of resulting string
	 * @param pad
	 *            : if true, pads with spaces after conversion
	 * @return String in form [xxxxxx...xxx], with nonprintables converted to
	 *         unicode or escape sequences, and ... inserted if length is
	 *         greater than about the width of a line
	 */
	public static String d(CharSequence orig, int maxLen, boolean pad) {
		if (maxLen < 8) {
			maxLen = 8;
		}

		StringBuilder sb = new StringBuilder();
		if (orig == null) {
			sb.append("<null>");
		} else {
			sb.append("[");
			convert(orig, sb);
			sb.append("]");
			if (sb.length() > maxLen) {
				sb.replace(maxLen - 7, sb.length() - 4, "...");
			}
		}
		if (pad) {
			MyTools.tab(sb, maxLen);
		}

		return sb.toString();
	}

	private static void convert(char c, StringBuilder dest) {
		switch (c) {
		case '\n':
			dest.append("\\n");
			break;
		default:
			if (c >= ' ' && c < (char) 0x80) {
				dest.append(c);
			} else {
				dest.append("\\#");
				dest.append((int) c);
			}
			break;
		}
	}

	private static void convert(CharSequence orig, StringBuilder sb) {
		for (int i = 0; i < orig.length(); i++) {
			convert(orig.charAt(i), sb);
		}
	}

	// private static StringBuilder sbw = new StringBuilder();
	private static final String SPACES = "                             ";

	public static String sp(int len) {
		len = Math.max(len, 0);
		if (len < SPACES.length())
			return SPACES.substring(0, len);
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(' ');
		return sb.toString();
	}

	// /**
	// * Add spaces to a StringBuilder until its length is at some value. Sort
	// of a
	// * 'tab' feature, useful for aligning output.
	// *
	// * @param sb :
	// * StringBuilder to pad out
	// * @param len :
	// * desired length of StringBuilder; if it is already past this point,
	// * nothing is added to it
	// */
	// public static StringBuilder tab(StringBuilder sb, int len) {
	// sb.append(sp(len - sb.length()));
	// return sb;
	// }

	/**
	 * Add a space to buffer if it doesn't already end with whitespace
	 * 
	 * @param sb
	 * @return sb
	 */
	public static StringBuilder addSp(StringBuilder sb) {
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) > ' ')
			sb.append(' ');
		return sb;
	}

	public static void addCr(StringBuilder sb) {
		if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '\n')
			sb.append('\n');
	}

	public static void mySleep(long tm) {
		try {
			Thread.sleep(tm);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Format an int into a string
	 * 
	 * @param v
	 *            value
	 * @param width
	 *            max number of digits to display
	 * @param spaceLeadZeros
	 *            if true, right-justifies string
	 * @return String, with format siiii where s = sign (' ' or '-'), if
	 *         overflow, returns s********* of same size
	 */
	public static String f(int v, int width, boolean spaceLeadZeros) {

		// get string representation of absolute value
		String s = Integer.toString(Math.abs(v));

		// get number of spaces to pad
		int pad = width - s.length();

		StringBuilder sb = new StringBuilder();

		// if it won't fit, print stars
		if (pad < 0) {
			sb.append(v < 0 ? '-' : ' ');
			while (sb.length() < width + 1)
				sb.append('*');
		} else {
			// print padding spaces before or after number
			if (spaceLeadZeros) {
				while (pad-- > 0)
					sb.append(' ');
			}
			sb.append(v < 0 ? '-' : ' ');
			sb.append(s);
			// print trailing padding, if any required
			while (pad-- > 0)
				sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * Format a float into a string, without scientific notation
	 * 
	 * @param v
	 *            : value
	 * @param iDig
	 *            : number of integer digits to display
	 * @param fDig
	 *            : number of fractional digits to display
	 * @return String, with format siiii.fff where s = sign (' ' or '-'), . is
	 *         present only if fDig > 0 if overflow, returns s********* of same
	 *         size
	 */
	public static String f(float v, int iDig, int fDig) {

		StringBuilder sb = new StringBuilder();

		boolean neg = false;
		if (v < 0) {
			neg = true;
			v = -v;
		}

		int[] dig = new int[iDig + fDig];

		boolean overflow = false;

		// Determine which digits will be displayed.
		// Round last digit and propagate leftward.
		{
			float n = (float) Math.pow(10, iDig);
			if (v >= n) {
				overflow = true;
			} else {
				float v2 = v;
				for (int i = 0; i < iDig + fDig; i++) {
					n /= 10.0;
					float d = (float) Math.floor(v2 / n);
					dig[i] = (int) d;
					v2 -= d * n;
				}
				float d2 = (float) Math.floor(v2 * 10 / n);
				if (d2 >= 5) {
					for (int k = dig.length - 1;; k--) {
						if (k < 0) {
							overflow = true;
							break;
						}
						if (++dig[k] == 10) {
							dig[k] = 0;
						} else
							break;
					}
				}
			}
		}

		if (overflow) {
			int nDig = iDig + fDig + 1;
			if (fDig != 0)
				nDig++;
			for (int k = 0; k < nDig; k++)
				sb.append("*");
		} else {

			sb.append(' ');
			int signPos = 0;
			boolean leadZero = false;
			for (int i = 0; i < iDig + fDig; i++) {
				int digit = dig[i]; // (int) d;
				if (!leadZero) {
					if (digit != 0 || i == iDig || (i == iDig - 1 && fDig == 0)) {
						leadZero = true;
						signPos = sb.length() - 1;
					}
				}
				if (i == iDig) {
					sb.append('.');
				}

				if (digit == 0 && !leadZero) {
					sb.append(' ');
				} else {
					sb.append((char) ('0' + digit));
				}
			}
			if (neg)
				sb.setCharAt(signPos, '-');
		}
		return sb.toString();
	}

	public static String f(float f) {
		return f(f, 5, 3);
	}

	public static String fa(float radians) {
    return f(radians / MyMath.M_DEG, 3, 2);
	}

	public static String f(int f) {
		return f(f, 6, true);
	}

	public static String f(int[] ia) {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0; i < ia.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(ia[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	public static String f(int val, int width) {
		return f(val, width, true);
	}

	/**
	 * Add spaces to a StringBuilder until its length is at some value. Sort of
	 * a 'tab' feature, useful for aligning output.
	 * 
	 * @param sb
	 *            : StringBuilder to pad out
	 * @param len
	 *            : desired length of StringBuilder; if it is already past this
	 *            point, nothing is added to it
	 */
	public static StringBuilder tab(StringBuilder sb, int len) {
		sb.append(sp(len - sb.length()));
		return sb;
	}

	private static HashMap warningStrings = new HashMap();

	public static String fh(int n) {
		return "$" + toHex(n, 8);
	}

	public static String fh4(int n) {
		return "$" + toHex(n, 4);
	}

	public static Random rseed(int seed) {
		rnd = null;
		if (seed == 0)
			rnd = new Random();
		else
			rnd = new Random(seed);
		return rnd;
	}

	private static Random random() {
		if (rnd == null)
			rseed(0);
		return rnd;
	}

	public static int rnd(int i) {
		return random().nextInt(i);
	}

	private static Random rnd;

  // public static String fa2(float ang) {
  // return fa(angle2(ang));
  // }

	public static void pr(Object obj) {
		System.out.println(obj);
	}

	/**
	 * Trim trailing linefeeds from string
	 * 
	 * @param s
	 *            input
	 * @return trimmed string
	 */
	public static String chomp(String s) {
		while (s.endsWith("\n")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	/**
	 * Convert an integer to a hex string
	 * 
	 * @param val
	 *            : value
	 * @param digits
	 *            : number of digits to produce (number of nybbles, starting
	 *            from the lowest 4 bits, of the value to examine)
	 * @return String
	 */
	public static String toHex(int val, int digits) {
		return toHex(null, val, digits).toString();
	}

	/**
	 * Convert value to hex, store in StringBuilder
	 * 
	 * @param sb
	 *            where to store result, or null
	 * @param value0
	 *            value to convert
	 * @param digits
	 *            number of hex digits to output
	 * @return result
	 */
	private static StringBuilder toHex(StringBuilder sb, int value0, int digits) {
		if (sb == null)
			sb = new StringBuilder();

		long value = value0;

		int shift = (digits - 1) << 2;
		while (digits-- > 0) {
			shift = digits << 2;
			int v = (int) ((value >> shift)) & 0xf;
			char c;
			if (v < 10) {
				c = (char) ('0' + v);
			} else {
				c = (char) ('a' + (v - 10));
			}
			sb.append(c);
		}
		return sb;

	}

	public static void report(Throwable t, String msg) {
		if (t != null) {
			pr("*** Problem (" + msg + ")");
			t.printStackTrace();
		}
	}

	public static boolean loadLibrary(String libName, boolean exitIfFail) {
		final boolean db = false;

		boolean success = false;

		try {
			if (db)
				pr("MyTools.loadLibrary [" + libName + "]");
			System.loadLibrary(libName);
			success = true;
		} catch (Throwable e) {

			if (exitIfFail) {
				pr("*** Problem loading library: " + libName + "\n throwable: "
						+ e);
				String s = System.getProperty("java.library.path");
				pr("It must be available somewhere on the path:\n  " + s);

				System.exit(1);
			}
		}
		return success;
	}

}
