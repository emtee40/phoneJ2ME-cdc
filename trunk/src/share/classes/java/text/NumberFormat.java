/*
 * 
 * @(#)NumberFormat.java	1.55 06/10/10
 * 
 * Portions Copyright  2000-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Currency;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import sun.text.resources.LocaleData;

/**
 * <code>NumberFormat</code> is the abstract base class for all number
 * formats. This class provides the interface for formatting and parsing
 * numbers. <code>NumberFormat</code> also provides methods for determining
 * which locales have number formats, and what their names are.
 *
 * <p>
 * <code>NumberFormat</code> helps you to format and parse numbers for any locale.
 * Your code can be completely independent of the locale conventions for
 * decimal points, thousands-separators, or even the particular decimal
 * digits used, or whether the number format is even decimal.
 *
 * <p>
 * To format a number for the current Locale, use one of the factory
 * class methods:
 * <blockquote>
 * <pre>
 *  myString = NumberFormat.getInstance().format(myNumber);
 * </pre>
 * </blockquote>
 * If you are formatting multiple numbers, it is
 * more efficient to get the format and use it multiple times so that
 * the system doesn't have to fetch the information about the local
 * language and country conventions multiple times.
 * <blockquote>
 * <pre>
 * NumberFormat nf = NumberFormat.getInstance();
 * for (int i = 0; i < a.length; ++i) {
 *     output.println(nf.format(myNumber[i]) + "; ");
 * }
 * </pre>
 * </blockquote>
 * To format a number for a different Locale, specify it in the
 * call to <code>getInstance</code>.
 * <blockquote>
 * <pre>
 * NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
 * </pre>
 * </blockquote>
 * You can also use a <code>NumberFormat</code> to parse numbers:
 * <blockquote>
 * <pre>
 * myNumber = nf.parse(myString);
 * </pre>
 * </blockquote>
 * Use <code>getInstance</code> or <code>getNumberInstance</code> to get the
 * normal number format. Use <code>getIntegerInstance</code> to get an
 * integer number format. Use <code>getCurrencyInstance</code> to get the
 * currency number format. And use <code>getPercentInstance</code> to get a
 * format for displaying percentages. With this format, a fraction like
 * 0.53 is displayed as 53%.
 *
 * <p>
 * You can also control the display of numbers with such methods as
 * <code>setMinimumFractionDigits</code>.
 * If you want even more control over the format or parsing,
 * or want to give your users more control,
 * you can try casting the <code>NumberFormat</code> you get from the factory methods
 * to a <code>DecimalFormat</code>. This will work for the vast majority
 * of locales; just remember to put it in a <code>try</code> block in case you
 * encounter an unusual one.
 *
 * <p>
 * NumberFormat and DecimalFormat are designed such that some controls
 * work for formatting and others work for parsing.  The following is
 * the detailed description for each these control methods,
 * <p>
 * setParseIntegerOnly : only affects parsing, e.g.
 * if true,  "3456.78" -> 3456 (and leaves the parse position just after index 6)
 * if false, "3456.78" -> 3456.78 (and leaves the parse position just after index 8)
 * This is independent of formatting.  If you want to not show a decimal point
 * where there might be no digits after the decimal point, use
 * setDecimalSeparatorAlwaysShown.
 * <p>
 * setDecimalSeparatorAlwaysShown : only affects formatting, and only where
 * there might be no digits after the decimal point, such as with a pattern
 * like "#,##0.##", e.g.,
 * if true,  3456.00 -> "3,456."
 * if false, 3456.00 -> "3456"
 * This is independent of parsing.  If you want parsing to stop at the decimal
 * point, use setParseIntegerOnly.
 *
 * <p>
 * You can also use forms of the <code>parse</code> and <code>format</code>
 * methods with <code>ParsePosition</code> and <code>FieldPosition</code> to
 * allow you to:
 * <ul>
 * <li> progressively parse through pieces of a string
 * <li> align the decimal point and other areas
 * </ul>
 * For example, you can align numbers in two ways:
 * <ol>
 * <li> If you are using a monospaced font with spacing for alignment,
 *      you can pass the <code>FieldPosition</code> in your format call, with
 *      <code>field</code> = <code>INTEGER_FIELD</code>. On output,
 *      <code>getEndIndex</code> will be set to the offset between the
 *      last character of the integer and the decimal. Add
 *      (desiredSpaceCount - getEndIndex) spaces at the front of the string.
 *
 * <li> If you are using proportional fonts,
 *      instead of padding with spaces, measure the width
 *      of the string in pixels from the start to <code>getEndIndex</code>.
 *      Then move the pen by
 *      (desiredPixelWidth - widthToAlignmentPoint) before drawing the text.
 *      It also works where there is no decimal, but possibly additional
 *      characters at the end, e.g., with parentheses in negative
 *      numbers: "(12)" for -12.
 * </ol>
 *
 * <h4><a name="synchronization">Synchronization</a></h4>
 *
 * <p>
 * Number formats are generally not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 * @see          DecimalFormat
 * @see          ChoiceFormat
 * @version      1.47, 01/19/00
 * @author       Mark Davis
 * @author       Helena Shih
 */
public abstract class NumberFormat extends Format  {

    /**
     * Field constant used to construct a FieldPosition object. Signifies that
     * the position of the integer part of a formatted number should be returned.
     * @see java.text.FieldPosition
     */
    public static final int INTEGER_FIELD = 0;

    /**
     * Field constant used to construct a FieldPosition object. Signifies that
     * the position of the fraction part of a formatted number should be returned.
     * @see java.text.FieldPosition
     */
    public static final int FRACTION_FIELD = 1;

    /**
     * Formats an object to produce a string.
     * This general routines allows polymorphic parsing and
     * formatting for objects.
     * @param number      the object to format
     * @param toAppendTo  where the text is to be appended
     * @param pos         On input: an alignment field, if desired.
     *                    On output: the offsets of the alignment field.
     * @return       the value passed in as toAppendTo (this allows chaining,
     * as with StringBuffer.append())
     * @exception IllegalArgumentException when the Format cannot format the
     * given object.
     * @see java.text.FieldPosition
     */
    public final StringBuffer format(Object number,
                                     StringBuffer toAppendTo,
                                     FieldPosition pos)
    {
        if (number instanceof Long ||
            (number instanceof BigInteger && ((BigInteger)number).bitLength() < 64)) {
            return format(((Number)number).longValue(), toAppendTo, pos);
        }
        /* Here is the code that's required to get all the bits we can out of
         * BigDecimal into a long or double.  In the interests of simplicity, we
         * don't use this code; we just convert BigDecimal values into doubles.
         * (Actually, to really do things right, you'd compare against both
         * Long.MIN_VALUE and Long.MAX_VALUE, since they differ in magnitude.)
         * NOTE: <B>java.math.BigDecimal</B> is found in J2ME CDC profiles such as 
         * J2ME Foundation Profile.
         * Liu 6/98
         */
        //  else if (number instanceof BigDecimal) {
        //      BigDecimal bd = (BigDecimal)number;
        //      try {
        //          if (bd.setScale(0, BigDecimal.ROUND_UNNECESSARY).
        //              abs().compareTo(new BigDecimal("9223372036854775807")) <= 0) {
        //              return format(((Number)number).longValue(), toAppendTo, pos);
        //          }
        //      }
        //      catch (ArithmeticException e) {}
        //      return format(((Number)number).doubleValue(), toAppendTo, pos);        
        //  }
        else if (number instanceof Number) {
            return format(((Number)number).doubleValue(), toAppendTo, pos);
        }
        else {
            throw new IllegalArgumentException("Cannot format given Object as a Number");
        }
    }

    /**
     * Parses text from a string to produce a <code>Number</code>.
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>.
     * If parsing succeeds, then the index of <code>pos</code> is updated
     * to the index after the last character used (parsing does not necessarily
     * use all characters up to the end of the string), and the parsed
     * number is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method.
     * If an error occurs, then the index of <code>pos</code> is not
     * changed, the error index of <code>pos</code> is set to the index of
     * the character where the error occurred, and null is returned.
     * <p>
     * See the {@link #parse(String, ParsePosition)} method for more information
     * on number parsing.
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos A <code>ParsePosition</code> object with index and error
     *            index information as described above.
     * @return A <code>Number</code> parsed from the string. In case of
     *         error, returns null.
     * @exception NullPointerException if <code>pos</code> is null.
     */
    public final Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

   /**
     * Specialization of format.
     * @see java.text.Format#format
     */
    public final String format(double number) {
        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }

   /**
     * Specialization of format.
     * @see java.text.Format#format
     */
    public final String format(long number) {
        return format(number, new StringBuffer(),
                      DontCareFieldPosition.INSTANCE).toString();
    }

   /**
     * Specialization of format.
     * @see java.text.Format#format
     */
    public abstract StringBuffer format(double number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);

   /**
     * Specialization of format.
     * @see java.text.Format#format
     */
    public abstract StringBuffer format(long number,
                                        StringBuffer toAppendTo,
                                        FieldPosition pos);

   /**
     * Returns a Long if possible (e.g., within the range [Long.MIN_VALUE,
     * Long.MAX_VALUE] and with no decimals), otherwise a Double.
     * If IntegerOnly is set, will stop at a decimal
     * point (or equivalent; e.g., for rational numbers "1 2/3", will stop
     * after the 1).
     * Does not throw an exception; if no object can be parsed, index is
     * unchanged!
     * @see java.text.NumberFormat#isParseIntegerOnly
     * @see java.text.Format#parseObject
     */
    public abstract Number parse(String source, ParsePosition parsePosition);

    /**
     * Parses text from the beginning of the given string to produce a number.
     * The method may not use the entire text of the given string.
     * <p>
     * See the {@link #parse(String, ParsePosition)} method for more information
     * on number parsing.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return A <code>Number</code> parsed from the string.
     * @exception ParseException if the beginning of the specified string
     *            cannot be parsed.
     */
    public Number parse(String source) throws ParseException {
        ParsePosition parsePosition = new ParsePosition(0);
        Number result = parse(source, parsePosition);
        if (parsePosition.index == 0) {
            throw new ParseException("Unparseable number: \"" + source + "\"",
                parsePosition.errorIndex);
        }
        return result;
    }

    /**
     * Returns true if this format will parse numbers as integers only.
     * For example in the English locale, with ParseIntegerOnly true, the
     * string "1234." would be parsed as the integer value 1234 and parsing
     * would stop at the "." character.  Of course, the exact format accepted
     * by the parse operation is locale dependant and determined by sub-classes
     * of NumberFormat.
     */
    public boolean isParseIntegerOnly() {
        return parseIntegerOnly;
    }

    /**
     * Sets whether or not numbers should be parsed as integers only.
     * @see #isParseIntegerOnly
     */
    public void setParseIntegerOnly(boolean value) {
        parseIntegerOnly = value;
    }

    //============== Locale Stuff =====================

    /**
     * Returns the default number format for the current default locale.
     * The default format is one of the styles provided by the other
     * factory methods: getNumberInstance, getIntegerInstance,
     * getCurrencyInstance or getPercentInstance.
     * Exactly which one is locale dependant.
     */
    public final static NumberFormat getInstance() {
        return getInstance(Locale.getDefault(), NUMBERSTYLE);
    }

    /**
     * Returns the default number format for the specified locale.
     * The default format is one of the styles provided by the other
     * factory methods: getNumberInstance, getIntegerInstance,
     * getCurrencyInstance or getPercentInstance.
     * Exactly which one is locale dependant.
     */
    public static NumberFormat getInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    /**
     * Returns a general-purpose number format for the current default locale.
     */
    public final static NumberFormat getNumberInstance() {
        return getInstance(Locale.getDefault(), NUMBERSTYLE);
    }

    /**
     * Returns a general-purpose number format for the specified locale.
     */
    public static NumberFormat getNumberInstance(Locale inLocale) {
        return getInstance(inLocale, NUMBERSTYLE);
    }

    /**
     * Returns an integer number format for the current default locale. The
     * returned number format is configured to round floating point numbers
     * to the nearest integer using IEEE half-even rounding (see {@link 
     * java.math.BigDecimal#ROUND_HALF_EVEN ROUND_HALF_EVEN}) for formatting,
     * and to parse only the integer part of an input string (see {@link
     * #isParseIntegerOnly isParseIntegerOnly}).
     * NOTE: <B>java.math.BigDecimal</B> is found in J2ME CDC profiles such as 
     * J2ME Foundation Profile.
     * @return a number format for integer values
     * @since 1.4
     */
    public final static NumberFormat getIntegerInstance() {
        return getInstance(Locale.getDefault(), INTEGERSTYLE);
    }

    /**
     * Returns an integer number format for the specified locale. The
     * returned number format is configured to round floating point numbers
     * to the nearest integer using IEEE half-even rounding (see {@link 
     * java.math.BigDecimal#ROUND_HALF_EVEN ROUND_HALF_EVEN}) for formatting,
     * and to parse only the integer part of an input string (see {@link
     * #isParseIntegerOnly isParseIntegerOnly}).
     * NOTE: <B>java.math.BigDecimal</B> is found in J2ME CDC profiles such as 
     * J2ME Foundation Profile.
     *
     * @param inLocale the locale for which a number format is needed
     * @return a number format for integer values
     * @since 1.4
     */
    public static NumberFormat getIntegerInstance(Locale inLocale) {
        return getInstance(inLocale, INTEGERSTYLE);
    }

    /**
     * Returns a currency format for the current default locale.
     */
    public final static NumberFormat getCurrencyInstance() {
        return getInstance(Locale.getDefault(), CURRENCYSTYLE);
    }

    /**
     * Returns a currency format for the specified locale.
     */
    public static NumberFormat getCurrencyInstance(Locale inLocale) {
        return getInstance(inLocale, CURRENCYSTYLE);
    }

    /**
     * Returns a percentage format for the current default locale.
     */
    public final static NumberFormat getPercentInstance() {
        return getInstance(Locale.getDefault(), PERCENTSTYLE);
    }

    /**
     * Returns a percentage format for the specified locale.
     */
    public static NumberFormat getPercentInstance(Locale inLocale) {
        return getInstance(inLocale, PERCENTSTYLE);
    }

    /**
     * Returns a scientific format for the current default locale.
     */
    /*public*/ final static NumberFormat getScientificInstance() {
        return getInstance(Locale.getDefault(), SCIENTIFICSTYLE);
    }

    /**
     * Returns a scientific format for the specified locale.
     */
    /*public*/ static NumberFormat getScientificInstance(Locale inLocale) {
        return getInstance(inLocale, SCIENTIFICSTYLE);
    }


    /**
     * Get the set of Locales for which NumberFormats are installed
     * @return available locales
     */
    public static Locale[] getAvailableLocales() {
        return LocaleData.getAvailableLocales("NumberPatterns");
    }

    /**
     * Overrides hashCode
     */
    public int hashCode() {
        return maximumIntegerDigits * 37 + maxFractionDigits;
        // just enough fields for a reasonable distribution
    }

    /**
     * Overrides equals
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        NumberFormat other = (NumberFormat) obj;
        return (maximumIntegerDigits == other.maximumIntegerDigits
            && minimumIntegerDigits == other.minimumIntegerDigits
            && maximumFractionDigits == other.maximumFractionDigits
            && minimumFractionDigits == other.minimumFractionDigits
            && groupingUsed == other.groupingUsed
            && parseIntegerOnly == other.parseIntegerOnly);
    }

    /**
     * Overrides Cloneable
     */
    public Object clone()
    {
        NumberFormat other = (NumberFormat) super.clone();
        return other;
    }

    /**
     * Returns true if grouping is used in this format. For example, in the
     * English locale, with grouping on, the number 1234567 might be formatted
     * as "1,234,567". The grouping separator as well as the size of each group
     * is locale dependant and is determined by sub-classes of NumberFormat.
     * @see #setGroupingUsed
     */
    public boolean isGroupingUsed() {
        return groupingUsed;
    }

    /**
     * Set whether or not grouping will be used in this format.
     * @see #isGroupingUsed
     */
    public void setGroupingUsed(boolean newValue) {
        groupingUsed = newValue;
    }

    /**
     * Returns the maximum number of digits allowed in the integer portion of a
     * number.
     * @see #setMaximumIntegerDigits
     */
    public int getMaximumIntegerDigits() {
        return maximumIntegerDigits;
    }

    /**
     * Sets the maximum number of digits allowed in the integer portion of a
     * number. maximumIntegerDigits must be >= minimumIntegerDigits.  If the
     * new value for maximumIntegerDigits is less than the current value
     * of minimumIntegerDigits, then minimumIntegerDigits will also be set to
     * the new value.
     * @param newValue the maximum number of integer digits to be shown; if
     * less than zero, then zero is used. The concrete subclass may enforce an
     * upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMaximumIntegerDigits
     */
    public void setMaximumIntegerDigits(int newValue) {
        maximumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits)
            minimumIntegerDigits = maximumIntegerDigits;
    }

    /**
     * Returns the minimum number of digits allowed in the integer portion of a
     * number.
     * @see #setMinimumIntegerDigits
     */
    public int getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    /**
     * Sets the minimum number of digits allowed in the integer portion of a
     * number. minimumIntegerDigits must be <= maximumIntegerDigits.  If the
     * new value for minimumIntegerDigits exceeds the current value
     * of maximumIntegerDigits, then maximumIntegerDigits will also be set to
     * the new value
     * @param newValue the minimum number of integer digits to be shown; if
     * less than zero, then zero is used. The concrete subclass may enforce an
     * upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMinimumIntegerDigits
     */
    public void setMinimumIntegerDigits(int newValue) {
        minimumIntegerDigits = Math.max(0,newValue);
        if (minimumIntegerDigits > maximumIntegerDigits)
            maximumIntegerDigits = minimumIntegerDigits;
    }

    /**
     * Returns the maximum number of digits allowed in the fraction portion of a
     * number.
     * @see #setMaximumFractionDigits
     */
    public int getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    /**
     * Sets the maximum number of digits allowed in the fraction portion of a
     * number. maximumFractionDigits must be >= minimumFractionDigits.  If the
     * new value for maximumFractionDigits is less than the current value
     * of minimumFractionDigits, then minimumFractionDigits will also be set to
     * the new value.
     * @param newValue the maximum number of fraction digits to be shown; if
     * less than zero, then zero is used. The concrete subclass may enforce an
     * upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMaximumFractionDigits
     */
    public void setMaximumFractionDigits(int newValue) {
        maximumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits)
            minimumFractionDigits = maximumFractionDigits;
    }

    /**
     * Returns the minimum number of digits allowed in the fraction portion of a
     * number.
     * @see #setMinimumFractionDigits
     */
    public int getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    /**
     * Sets the minimum number of digits allowed in the fraction portion of a
     * number. minimumFractionDigits must be <= maximumFractionDigits.  If the
     * new value for minimumFractionDigits exceeds the current value
     * of maximumFractionDigits, then maximumIntegerDigits will also be set to
     * the new value
     * @param newValue the minimum number of fraction digits to be shown; if
     * less than zero, then zero is used. The concrete subclass may enforce an
     * upper limit to this value appropriate to the numeric type being formatted.
     * @see #getMinimumFractionDigits
     */
    public void setMinimumFractionDigits(int newValue) {
        minimumFractionDigits = Math.max(0,newValue);
        if (maximumFractionDigits < minimumFractionDigits)
            maximumFractionDigits = minimumFractionDigits;
    }
    
    /**
     * Gets the currency used by this number format when formatting
     * currency values. The initial value is derived in a locale dependent
     * way. The returned value may be null if no valid
     * currency could be determined and no currency has been set using
     * {@link #setCurrency(java.util.Currency) setCurrency}.
     * <p>
     * The default implementation throws
     * <code>UnsupportedOperationException</code>.
     *
     * @return the currency used by this number format, or <code>null</code>
     * @exception UnsupportedOperationException if the number format class
     * doesn't implement currency formatting
     * @since 1.4
     */
    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Sets the currency used by this number format when formatting
     * currency values. This does not update the minimum or maximum
     * number of fraction digits used by the number format.
     * <p>
     * The default implementation throws
     * <code>UnsupportedOperationException</code>.
     *
     * @param currency the new currency to be used by this number format
     * @exception UnsupportedOperationException if the number format class
     * doesn't implement currency formatting
     * @exception NullPointerException if <code>currency</code> is null
     * @since 1.4
     */
    public void setCurrency(Currency currency) {
        throw new UnsupportedOperationException();
    }

    // =======================privates===============================

    private static NumberFormat getInstance(Locale desiredLocale,
                                           int choice) {
        /* try the cache first */
        String[] numberPatterns = (String[])cachedLocaleData.get(desiredLocale);
        if (numberPatterns == null) { /* cache miss */
            ResourceBundle resource = LocaleData.getLocaleElements(desiredLocale);
            numberPatterns = resource.getStringArray("NumberPatterns");
            /* update cache */
            cachedLocaleData.put(desiredLocale, numberPatterns);
        }
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(desiredLocale);
        int entry = (choice == INTEGERSTYLE) ? NUMBERSTYLE : choice;
        DecimalFormat format = new DecimalFormat(numberPatterns[entry], symbols);
        
        if (choice == INTEGERSTYLE) {
            format.setMaximumFractionDigits(0);
            format.setDecimalSeparatorAlwaysShown(false);
            format.setParseIntegerOnly(true);
        } else if (choice == CURRENCYSTYLE) {
            format.adjustForCurrencyDefaultFractionDigits();
        }

        return format;
    }

    /**
     * First, read in the default serializable data.
     *
     * Then, if <code>serialVersionOnStream</code> is less than 1, indicating that
     * the stream was written by JDK 1.1,
     * set the <code>int</code> fields such as <code>maximumIntegerDigits</code>
     * to be equal to the <code>byte</code> fields such as <code>maxIntegerDigits</code>,
     * since the <code>int</code> fields were not present in JDK 1.1.
     * Finally, set serialVersionOnStream back to the maximum allowed value so that
     * default serialization will work properly if this object is streamed out again.
     *
     * <p>If <code>minimumIntegerDigits</code> is greater than
     * <code>maximumIntegerDigits</code> or <code>minimumFractionDigits</code>
     * is greater than <code>maximumFractionDigits</code>, then the stream data
     * is invalid and this method throws an <code>InvalidObjectException</code>.
     * In addition, if any of these values is negative, then this method throws
     * an <code>InvalidObjectException</code>.
     *
     * @since 1.2
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        if (serialVersionOnStream < 1) {
            // Didn't have additional int fields, reassign to use them.
            maximumIntegerDigits = maxIntegerDigits;
            minimumIntegerDigits = minIntegerDigits;
            maximumFractionDigits = maxFractionDigits;
            minimumFractionDigits = minFractionDigits;
        }
        if (minimumIntegerDigits > maximumIntegerDigits ||
            minimumFractionDigits > maximumFractionDigits ||
            minimumIntegerDigits < 0 || minimumFractionDigits < 0) {
            throw new InvalidObjectException("Digit count range invalid");
        }
        serialVersionOnStream = currentSerialVersion;
    }

    /**
     * Write out the default serializable data, after first setting
     * the <code>byte</code> fields such as <code>maxIntegerDigits</code> to be
     * equal to the <code>int</code> fields such as <code>maximumIntegerDigits</code>
     * (or to <code>Byte.MAX_VALUE</code>, whichever is smaller), for compatibility
     * with the JDK 1.1 version of the stream format.
     *
     * @since 1.2
     */
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        maxIntegerDigits = (maximumIntegerDigits > Byte.MAX_VALUE) ? Byte.MAX_VALUE :
            (byte)maximumIntegerDigits;
        minIntegerDigits = (minimumIntegerDigits > Byte.MAX_VALUE) ? Byte.MAX_VALUE :
            (byte)minimumIntegerDigits;
        maxFractionDigits = (maximumFractionDigits > Byte.MAX_VALUE) ? Byte.MAX_VALUE :
            (byte)maximumFractionDigits;
        minFractionDigits = (minimumFractionDigits > Byte.MAX_VALUE) ? Byte.MAX_VALUE :
            (byte)minimumFractionDigits;
        stream.defaultWriteObject();
    }
    /**
     * Cache to hold the NumberPatterns of a Locale.
     */
    private static final Hashtable cachedLocaleData = new Hashtable(3);

    // Constants used by factory methods to specify a style of format.
    private static final int NUMBERSTYLE = 0;
    private static final int CURRENCYSTYLE = 1;
    private static final int PERCENTSTYLE = 2;
    private static final int SCIENTIFICSTYLE = 3;
    private static final int INTEGERSTYLE = 4;

    /**
     * True if the the grouping (i.e. thousands) separator is used when
     * formatting and parsing numbers.
     *
     * @serial
     * @see #isGroupingUsed
     */
    private boolean groupingUsed = true;

    /**
     * The maximum number of digits allowed in the integer portion of a
     * number.  <code>maxIntegerDigits</code> must be greater than or equal to
     * <code>minIntegerDigits</code>.
     * <p>
     * <strong>Note:</strong> This field exists only for serialization
     * compatibility with JDK 1.1.  In Java platform 2 v1.2 and higher, the new
     * <code>int</code> field <code>maximumIntegerDigits</code> is used instead.
     * When writing to a stream, <code>maxIntegerDigits</code> is set to
     * <code>maximumIntegerDigits</code> or <code>Byte.MAX_VALUE</code>,
     * whichever is smaller.  When reading from a stream, this field is used
     * only if <code>serialVersionOnStream</code> is less than 1. 
     *
     * @serial
     * @see #getMaximumIntegerDigits
     */
    private byte    maxIntegerDigits = 40;

    /**
     * The minimum number of digits allowed in the integer portion of a
     * number.  <code>minimumIntegerDigits</code> must be less than or equal to
     * <code>maximumIntegerDigits</code>.
     * <p>
     * <strong>Note:</strong> This field exists only for serialization
     * compatibility with JDK 1.1.  In Java platform 2 v1.2 and higher, the new
     * <code>int</code> field <code>minimumIntegerDigits</code> is used instead.
     * When writing to a stream, <code>minIntegerDigits</code> is set to
     * <code>minimumIntegerDigits</code> or <code>Byte.MAX_VALUE</code>,
     * whichever is smaller.  When reading from a stream, this field is used
     * only if <code>serialVersionOnStream</code> is less than 1. 
     *
     * @serial
     * @see #getMinimumIntegerDigits
     */
    private byte    minIntegerDigits = 1;

    /**
     * The maximum number of digits allowed in the fractional portion of a
     * number.  <code>maximumFractionDigits</code> must be greater than or equal to
     * <code>minimumFractionDigits</code>.
     * <p>
     * <strong>Note:</strong> This field exists only for serialization
     * compatibility with JDK 1.1.  In Java platform 2 v1.2 and higher, the new
     * <code>int</code> field <code>maximumFractionDigits</code> is used instead.
     * When writing to a stream, <code>maxFractionDigits</code> is set to
     * <code>maximumFractionDigits</code> or <code>Byte.MAX_VALUE</code>,
     * whichever is smaller.  When reading from a stream, this field is used
     * only if <code>serialVersionOnStream</code> is less than 1. 
     *
     * @serial
     * @see #getMaximumFractionDigits
     */
    private byte    maxFractionDigits = 3;    // invariant, >= minFractionDigits

    /**
     * The minimum number of digits allowed in the fractional portion of a
     * number.  <code>minimumFractionDigits</code> must be less than or equal to
     * <code>maximumFractionDigits</code>.
     * <p>
     * <strong>Note:</strong> This field exists only for serialization
     * compatibility with JDK 1.1.  In Java platform 2 v1.2 and higher, the new
     * <code>int</code> field <code>minimumFractionDigits</code> is used instead.
     * When writing to a stream, <code>minFractionDigits</code> is set to
     * <code>minimumFractionDigits</code> or <code>Byte.MAX_VALUE</code>,
     * whichever is smaller.  When reading from a stream, this field is used
     * only if <code>serialVersionOnStream</code> is less than 1. 
     *
     * @serial
     * @see #getMinimumFractionDigits
     */
    private byte    minFractionDigits = 0;

    /**
     * True if this format will parse numbers as integers only.
     *
     * @serial
     * @see #isParseIntegerOnly
     */
    private boolean parseIntegerOnly = false;

    // new fields for 1.2.  byte is too small for integer digits.

    /**
     * The maximum number of digits allowed in the integer portion of a
     * number.  <code>maximumIntegerDigits</code> must be greater than or equal to
     * <code>minimumIntegerDigits</code>.
     *
     * @serial
     * @since 1.2
     * @see #getMaximumIntegerDigits
     */
    private int    maximumIntegerDigits = 40;

    /**
     * The minimum number of digits allowed in the integer portion of a
     * number.  <code>minimumIntegerDigits</code> must be less than or equal to
     * <code>maximumIntegerDigits</code>.
     *
     * @serial
     * @since 1.2
     * @see #getMinimumIntegerDigits
     */
    private int    minimumIntegerDigits = 1;

    /**
     * The maximum number of digits allowed in the fractional portion of a
     * number.  <code>maximumFractionDigits</code> must be greater than or equal to
     * <code>minimumFractionDigits</code>.
     *
     * @serial
     * @since 1.2
     * @see #getMaximumFractionDigits
     */
    private int    maximumFractionDigits = 3;    // invariant, >= minFractionDigits

    /**
     * The minimum number of digits allowed in the fractional portion of a
     * number.  <code>minimumFractionDigits</code> must be less than or equal to
     * <code>maximumFractionDigits</code>.
     *
     * @serial
     * @since 1.2
     * @see #getMinimumFractionDigits
     */
    private int    minimumFractionDigits = 0;

    static final int currentSerialVersion = 1;

    /**
     * Describes the version of <code>NumberFormat</code> present on the stream.
     * Possible values are:
     * <ul>
     * <li><b>0</b> (or uninitialized): the JDK 1.1 version of the stream format.
     *     In this version, the <code>int</code> fields such as
     *     <code>maximumIntegerDigits</code> were not present, and the <code>byte</code>
     *     fields such as <code>maxIntegerDigits</code> are used instead.
     *
     * <li><b>1</b>: the 1.2 version of the stream format.  The values of the
     *     <code>byte</code> fields such as <code>maxIntegerDigits</code> are ignored,
     *     and the <code>int</code> fields such as <code>maximumIntegerDigits</code>
     *     are used instead.
     * </ul>
     * When streaming out a <code>NumberFormat</code>, the most recent format
     * (corresponding to the highest allowable <code>serialVersionOnStream</code>)
     * is always written.
     *
     * @serial
     * @since 1.2
     */
    private int serialVersionOnStream = currentSerialVersion;

    // Removed "implements Cloneable" clause.  Needs to update serialization
    // ID for backward compatibility.
    static final long serialVersionUID = -2308460125733713944L;


    //
    // class for AttributedCharacterIterator attributes
    //
    /**
     * Defines constants that are used as attribute keys in the
     * <code>AttributedCharacterIterator</code> returned
     * from <code>NumberFormat.formatToCharacterIterator</code> and as
     * field identifiers in <code>FieldPosition</code>.
     *
     * @since 1.4
     */
    public static class Field extends Format.Field {
        // table of all instances in this class, used by readResolve
        private static final Map instanceMap = new HashMap(11);

        /**
         * Creates a Field instance with the specified
         * name.
         *
         * @param name Name of the attribute
         */
        protected Field(String name) {
            super(name);
            if (this.getClass() == NumberFormat.Field.class) {
                instanceMap.put(name, this);
            }
        }

        /**
         * Resolves instances being deserialized to the predefined constants.
         *
	 * @throws InvalidObjectException if the constant could not be
         *         resolved.
         * @return resolved NumberFormat.Field constant
         */
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != NumberFormat.Field.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }

            Object instance = instanceMap.get(getName());
            if (instance != null) {
                return instance;
            } else {
                throw new InvalidObjectException("unknown attribute name");
            }
        }

        /**
         * Constant identifying the integer field.
         */
        public static final Field INTEGER = new Field("integer");

        /**
         * Constant identifying the fraction field.
         */
        public static final Field FRACTION = new Field("fraction");

        /**
         * Constant identifying the exponent field.
         */
        public static final Field EXPONENT = new Field("exponent");

        /**
         * Constant identifying the decimal separator field.
         */
        public static final Field DECIMAL_SEPARATOR =
                            new Field("decimal separator");

        /**
         * Constant identifying the sign field.
         */
        public static final Field SIGN = new Field("sign");

        /**
         * Constant identifying the grouping separator field.
         */
        public static final Field GROUPING_SEPARATOR =
                            new Field("grouping separator");

        /**
         * Constant identifying the exponent symbol field.
         */
        public static final Field EXPONENT_SYMBOL = new
                            Field("exponent symbol");

        /**
         * Constant identifying the percent field.
         */
        public static final Field PERCENT = new Field("percent");

        /**
         * Constant identifying the permille field.
         */
        public static final Field PERMILLE = new Field("per mille");

        /**
         * Constant identifying the currency field.
         */
        public static final Field CURRENCY = new Field("currency");

        /**
         * Constant identifying the exponent sign field.
         */
        public static final Field EXPONENT_SIGN = new Field("exponent sign");
    }
}
