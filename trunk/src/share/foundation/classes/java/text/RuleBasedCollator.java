/*
 *
 * @(#)RuleBasedCollator.java	1.36 06/10/03
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
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
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

import java.util.Vector;
import java.util.Locale;
import sun.text.Normalizer;
import sun.text.NormalizerUtilities;

/**
 * The <code>RuleBasedCollator</code> class is a concrete subclass of
 * <code>Collator</code> that provides a simple, data-driven, table
 * collator.  With this class you can create a customized table-based
 * <code>Collator</code>.  <code>RuleBasedCollator</code> maps
 * characters to sort keys.
 *
 * <p>
 * <code>RuleBasedCollator</code> has the following restrictions
 * for efficiency (other subclasses may be used for more complex languages) :
 * <ol>
 * <li>If a special collation rule controlled by a &lt;modifier&gt; is
      specified it applies to the whole collator object.
 * <li>All non-mentioned characters are at the end of the
 *     collation order.
 * </ol>
 *
 * <p>
 * The collation table is composed of a list of collation rules, where each
 * rule is of one of three forms:
 * <pre>
 *    &lt;modifier&gt;
 *    &lt;relation&gt; &lt;text-argument&gt;
 *    &lt;reset&gt; &lt;text-argument&gt;
 * </pre>
 * The definitions of the rule elements is as follows:
 * <UL Type=disc>
 *    <LI><strong>Text-Argument</strong>: A text-argument is any sequence of
 *        characters, excluding special characters (that is, common
 *        whitespace characters [0009-000D, 0020] and rule syntax characters
 *        [0021-002F, 003A-0040, 005B-0060, 007B-007E]). If those
 *        characters are desired, you can put them in single quotes
 *        (e.g. ampersand => '&'). Note that unquoted white space characters
 *        are ignored; e.g. <code>b c</code> is treated as <code>bc</code>.
 *    <LI><strong>Modifier</strong>: There are currently two modifiers that 
 *        turn on special collation rules.
 *        <UL Type=square>
 *            <LI>'@' : Turns on backwards sorting of accents (secondary
 *                      differences), as in French.
 *            <LI>'!' : Turns on Thai/Lao vowel-consonant swapping.  If this
 *                      rule is in force when a Thai vowel of the range
 *                      &#92;U0E40-&#92;U0E44 precedes a Thai consonant of the range
 *                      &#92;U0E01-&#92;U0E2E OR a Lao vowel of the range &#92;U0EC0-&#92;U0EC4
 *                      precedes a Lao consonant of the range &#92;U0E81-&#92;U0EAE then
 *                      the vowel is placed after the consonant for collation
 *                      purposes.
 *        </UL>
 *        <p>'@' : Indicates that accents are sorted backwards, as in French.
 *    <LI><strong>Relation</strong>: The relations are the following:
 *        <UL Type=square>
 *            <LI>'&lt;' : Greater, as a letter difference (primary)
 *            <LI>';' : Greater, as an accent difference (secondary)
 *            <LI>',' : Greater, as a case difference (tertiary)
 *            <LI>'=' : Equal
 *        </UL>
 *    <LI><strong>Reset</strong>: There is a single reset
 *        which is used primarily for contractions and expansions, but which
 *        can also be used to add a modification at the end of a set of rules.
 *        <p>'&' : Indicates that the next rule follows the position to where
 *            the reset text-argument would be sorted.
 * </UL>
 *
 * <p>
 * This sounds more complicated than it is in practice. For example, the
 * following are equivalent ways of expressing the same thing:
 * <blockquote>
 * <pre>
 * a &lt; b &lt; c
 * a &lt; b &amp; b &lt; c
 * a &lt; c &amp; a &lt; b
 * </pre>
 * </blockquote>
 * Notice that the order is important, as the subsequent item goes immediately
 * after the text-argument. The following are not equivalent:
 * <blockquote>
 * <pre>
 * a &lt; b &amp; a &lt; c
 * a &lt; c &amp; a &lt; b
 * </pre>
 * </blockquote>
 * Either the text-argument must already be present in the sequence, or some
 * initial substring of the text-argument must be present. (e.g. "a &lt; b &amp; ae &lt; 
 * e" is valid since "a" is present in the sequence before "ae" is reset). In
 * this latter case, "ae" is not entered and treated as a single character;
 * instead, "e" is sorted as if it were expanded to two characters: "a"
 * followed by an "e". This difference appears in natural languages: in
 * traditional Spanish "ch" is treated as though it contracts to a single
 * character (expressed as "c &lt; ch &lt; d"), while in traditional German
 * a-umlaut is treated as though it expanded to two characters
 * (expressed as "a,A &lt; b,B ... &amp;ae;&#92;u00e3&amp;AE;&#92;u00c3").
 * [&#92;u00e3 and &#92;u00c3 are, of course, the escape sequences for a-umlaut.]
 * <p>
 * <strong>Ignorable Characters</strong>
 * <p>
 * For ignorable characters, the first rule must start with a relation (the
 * examples we have used above are really fragments; "a &lt; b" really should be
 * "&lt; a &lt; b"). If, however, the first relation is not "&lt;", then all the all
 * text-arguments up to the first "&lt;" are ignorable. For example, ", - &lt; a &lt; b"
 * makes "-" an ignorable character, as we saw earlier in the word
 * "black-birds". In the samples for different languages, you see that most
 * accents are ignorable.
 *
 * <p><strong>Normalization and Accents</strong>
 * <p>
 * <code>RuleBasedCollator</code> automatically processes its rule table to
 * include both pre-composed and combining-character versions of
 * accented characters.  Even if the provided rule string contains only
 * base characters and separate combining accent characters, the pre-composed
 * accented characters matching all canonical combinations of characters from
 * the rule string will be entered in the table.
 * <p>
 * This allows you to use a RuleBasedCollator to compare accented strings
 * even when the collator is set to NO_DECOMPOSITION.  There are two caveats,
 * however.  First, if the strings to be collated contain combining
 * sequences that may not be in canonical order, you should set the collator to
 * CANONICAL_DECOMPOSITION or FULL_DECOMPOSITION to enable sorting of
 * combining sequences.  Second, if the strings contain characters with
 * compatibility decompositions (such as full-width and half-width forms),
 * you must use FULL_DECOMPOSITION, since the rule tables only include
 * canonical mappings.
 *
 * <p><strong>Errors</strong>
 * <p>
 * The following are errors:
 * <UL Type=disc>
 *     <LI>A text-argument contains unquoted punctuation symbols
 *        (e.g. "a &lt; b-c &lt; d").
 *     <LI>A relation or reset character not followed by a text-argument
 *        (e.g. "a &lt; ,b").
 *     <LI>A reset where the text-argument (or an initial substring of the
 *         text-argument) is not already in the sequence.
 *         (e.g. "a &lt; b &amp; e &lt; f")
 * </UL>
 * If you produce one of these errors, a <code>RuleBasedCollator</code> throws
 * a <code>ParseException</code>.
 * 
 * <p><strong>Examples</strong>
 * <p>Simple:     "&lt; a &lt; b &lt; c &lt; d"
 * <p>Norwegian:  "&lt; a,A&lt; b,B&lt; c,C&lt; d,D&lt; e,E&lt; f,F&lt; g,G&lt; h,H&lt; i,I&lt; j,J
 *                 &lt; k,K&lt; l,L&lt; m,M&lt; n,N&lt; o,O&lt; p,P&lt; q,Q&lt; r,R&lt; s,S&lt; t,T
 *                 &lt; u,U&lt; v,V&lt; w,W&lt; x,X&lt; y,Y&lt; z,Z
 *                 &lt; &#92;u00E5=a&#92;u030A,&#92;u00C5=A&#92;u030A
 *                 ;aa,AA&lt; &#92;u00E6,&#92;u00C6&lt; &#92;u00F8,&#92;u00D8"
 *
 * <p>
 * Normally, to create a rule-based Collator object, you will use
 * <code>Collator</code>'s factory method <code>getInstance</code>.
 * However, to create a rule-based Collator object with specialized
 * rules tailored to your needs, you construct the <code>RuleBasedCollator</code>
 * with the rules contained in a <code>String</code> object. For example:
 * <blockquote>
 * <pre>
 * String Simple = "&lt; a&lt; b&lt; c&lt; d";
 * RuleBasedCollator mySimple = new RuleBasedCollator(Simple);
 * </pre>
 * </blockquote>
 * Or:
 * <blockquote>
 * <pre>
 * String Norwegian = "&lt; a,A&lt; b,B&lt; c,C&lt; d,D&lt; e,E&lt; f,F&lt; g,G&lt; h,H&lt; i,I&lt; j,J" +
 *                 "&lt; k,K&lt; l,L&lt; m,M&lt; n,N&lt; o,O&lt; p,P&lt; q,Q&lt; r,R&lt; s,S&lt; t,T" +
 *                 "&lt; u,U&lt; v,V&lt; w,W&lt; x,X&lt; y,Y&lt; z,Z" +
 *                 "&lt; &#92;u00E5=a&#92;u030A,&#92;u00C5=A&#92;u030A" +
 *                 ";aa,AA&lt; &#92;u00E6,&#92;u00C6&lt; &#92;u00F8,&#92;u00D8";
 * RuleBasedCollator myNorwegian = new RuleBasedCollator(Norwegian);
 * </pre>
 * </blockquote>
 *
 * <p>
 * Combining <code>Collator</code>s is as simple as concatenating strings.
 * Here's an example that combines two <code>Collator</code>s from two
 * different locales:
 * <blockquote>
 * <pre>
 * // Create an en_US Collator object
 * RuleBasedCollator en_USCollator = (RuleBasedCollator)
 *     Collator.getInstance(new Locale("en", "US", ""));
 * // Create a da_DK Collator object
 * RuleBasedCollator da_DKCollator = (RuleBasedCollator)
 *     Collator.getInstance(new Locale("da", "DK", ""));
 * // Combine the two
 * // First, get the collation rules from en_USCollator
 * String en_USRules = en_USCollator.getRules();
 * // Second, get the collation rules from da_DKCollator
 * String da_DKRules = da_DKCollator.getRules();
 * RuleBasedCollator newCollator =
 *     new RuleBasedCollator(en_USRules + da_DKRules);
 * // newCollator has the combined rules
 * </pre>
 * </blockquote>
 *
 * <p>
 * Another more interesting example would be to make changes on an existing
 * table to create a new <code>Collator</code> object.  For example, add
 * "&amp;C&lt; ch, cH, Ch, CH" to the <code>en_USCollator</code> object to create
 * your own:
 * <blockquote>
 * <pre>
 * // Create a new Collator object with additional rules
 * String addRules = "&amp;C&lt; ch, cH, Ch, CH";
 * RuleBasedCollator myCollator =
 *     new RuleBasedCollator(en_USCollator + addRules);
 * // myCollator contains the new rules
 * </pre>
 * </blockquote>
 *
 * <p>
 * The following example demonstrates how to change the order of
 * non-spacing accents,
 * <blockquote>
 * <pre>
 * // old rule
 * String oldRules = "=&#92;u0301;&#92;u0300;&#92;u0302;&#92;u0308"    // main accents
 *                 + ";&#92;u0327;&#92;u0303;&#92;u0304;&#92;u0305"    // main accents
 *                 + ";&#92;u0306;&#92;u0307;&#92;u0309;&#92;u030A"    // main accents
 *                 + ";&#92;u030B;&#92;u030C;&#92;u030D;&#92;u030E"    // main accents
 *                 + ";&#92;u030F;&#92;u0310;&#92;u0311;&#92;u0312"    // main accents
 *                 + "&lt; a , A ; ae, AE ; &#92;u00e6 , &#92;u00c6"
 *                 + "&lt; b , B &lt; c, C &lt; e, E & C &lt; d, D";
 * // change the order of accent characters
 * String addOn = "& &#92;u0300 ; &#92;u0308 ; &#92;u0302";
 * RuleBasedCollator myCollator = new RuleBasedCollator(oldRules + addOn);
 * </pre>
 * </blockquote>
 *
 * <p>
 * The last example shows how to put new primary ordering in before the
 * default setting. For example, in Japanese <code>Collator</code>, you
 * can either sort English characters before or after Japanese characters,
 * <blockquote>
 * <pre>
 * // get en_US Collator rules
 * RuleBasedCollator en_USCollator = (RuleBasedCollator)Collator.getInstance(Locale.US);
 * // add a few Japanese character to sort before English characters
 * // suppose the last character before the first base letter 'a' in
 * // the English collation rule is &#92;u2212
 * String jaString = "& &#92;u2212 &lt; &#92;u3041, &#92;u3042 &lt; &#92;u3043, &#92;u3044";
 * RuleBasedCollator myJapaneseCollator = new
 *     RuleBasedCollator(en_USCollator.getRules() + jaString);
 * </pre>
 * </blockquote>
 *
 * @see        Collator
 * @see        CollationElementIterator
 * @version    1.25 07/24/98
 * @author     Helena Shih, Laura Werner, Richard Gillam
 */
public class RuleBasedCollator extends Collator{
    // IMPLEMENTATION NOTES:  The implementation of the collation algorithm is
    // divided across three classes: RuleBasedCollator, RBCollationTables, and
    // CollationElementIterator.  RuleBasedCollator contains the collator's
    // transient state and includes the code that uses the other classes to
    // implement comparison and sort-key building.  RuleBasedCollator also
    // contains the logic to handle French secondary accent sorting.
    // A RuleBasedCollator has two CollationElementIterators.  State doesn't
    // need to be preserved in these objects between calls to compare() or
    // getCollationKey(), but the objects persist anyway to avoid wasting extra
    // creation time.  compare() and getCollationKey() are synchronized to ensure
    // thread safety with this scheme.  The CollationElementIterator is responsible
    // for generating collation elements from strings and returning one element at
    // a time (sometimes there's a one-to-many or many-to-one mapping between
    // characters and collation elements-- this class handles that).
    // CollationElementIterator depends on RBCollationTables, which contains the
    // collator's static state.  RBCollationTables contains the actual data
    // tables specifying the collation order of characters for a particular locale
    // or use.  It also contains the base logic that CollationElementIterator
    // uses to map from characters to collation elements.  A single RBCollationTables
    // object is shared among all RuleBasedCollators for the same locale, and
    // thus by all the CollationElementIterators they create.

    /**
     * RuleBasedCollator constructor.  This takes the table rules and builds
     * a collation table out of them.  Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @see java.util.Locale
     * @param rules the collation rules to build the collation table from.
     * @exception ParseException A format exception
     * will be thrown if the build process of the rules fails. For
     * example, build rule "a < ? < d" will cause the constructor to
     * throw the ParseException because the '?' is not quoted.
     */
    public RuleBasedCollator(String rules) throws ParseException {
        this(rules, Collator.CANONICAL_DECOMPOSITION);
    }

    /**
     * RuleBasedCollator constructor.  This takes the table rules and builds
     * a collation table out of them.  Please see RuleBasedCollator class
     * description for more details on the collation rule syntax.
     * @see java.util.Locale
     * @param rules the collation rules to build the collation table from.
     * @param decomp the decomposition strength used to build the
     * collation table and to perform comparisons.
     * @exception ParseException A format exception
     * will be thrown if the build process of the rules fails. For
     * example, build rule "a < ? < d" will cause the constructor to
     * throw the ParseException because the '?' is not quoted.
     */
    RuleBasedCollator(String rules, int decomp) throws ParseException {
        setStrength(Collator.TERTIARY);
        setDecomposition(decomp);
        tables = new RBCollationTables(rules, decomp);
    }

    /**
     * "Copy constructor."  Used in clone() for performance.
     */
    private RuleBasedCollator(RuleBasedCollator that) {
        setStrength(that.getStrength());
        setDecomposition(that.getDecomposition());
        tables = that.tables;
    }

    /**
     * Gets the table-based rules for the collation object.
     * @return returns the collation rules that the table collation object
     * was created from.
     */
    public String getRules()
    {
        return tables.getRules();
    }

    /**
     * Return a CollationElementIterator for the given String.
     * @see java.text.CollationElementIterator
     */
    public CollationElementIterator getCollationElementIterator(String source) {
        return new CollationElementIterator( source, this );
    }

    /**
     * Return a CollationElementIterator for the given String.
     * @see java.text.CollationElementIterator
     * @since 1.2
     */
    public CollationElementIterator getCollationElementIterator(
                                                CharacterIterator source) {
        return new CollationElementIterator( source, this );
    }

    /**
     * Compares the character data stored in two different strings based on the
     * collation rules.  Returns information about whether a string is less
     * than, greater than or equal to another string in a language.
     * This can be overriden in a subclass.
     */
    public synchronized int compare(String source, String target)
    {
        // The basic algorithm here is that we use CollationElementIterators
        // to step through both the source and target strings.  We compare each
        // collation element in the source string against the corresponding one
        // in the target, checking for differences.
        //
        // If a difference is found, we set <result> to LESS or GREATER to
        // indicate whether the source string is less or greater than the target.
        //
        // However, it's not that simple.  If we find a tertiary difference
        // (e.g. 'A' vs. 'a') near the beginning of a string, it can be
        // overridden by a primary difference (e.g. "A" vs. "B") later in
        // the string.  For example, "AA" < "aB", even though 'A' > 'a'.
        //
        // To keep track of this, we use strengthResult to keep track of the
        // strength of the most significant difference that has been found
        // so far.  When we find a difference whose strength is greater than
        // strengthResult, it overrides the last difference (if any) that
        // was found.

        int result = Collator.EQUAL;

        if (sourceCursor == null) {
            sourceCursor = getCollationElementIterator(source);
        } else {
            sourceCursor.setText(source);
        }
        if (targetCursor == null) {
            targetCursor = getCollationElementIterator(target);
        } else {
            targetCursor.setText(target);
        }

        int sOrder = 0, tOrder = 0;

        boolean initialCheckSecTer = getStrength() >= Collator.SECONDARY;
        boolean checkSecTer = initialCheckSecTer;
        boolean checkTertiary = getStrength() >= Collator.TERTIARY;

        boolean gets = true, gett = true;

        while(true) {
            // Get the next collation element in each of the strings, unless
            // we've been requested to skip it.
            if (gets) sOrder = sourceCursor.next(); else gets = true;
            if (gett) tOrder = targetCursor.next(); else gett = true;

            // If we've hit the end of one of the strings, jump out of the loop
            if ((sOrder == CollationElementIterator.NULLORDER)||
                (tOrder == CollationElementIterator.NULLORDER))
                break;

            int pSOrder = CollationElementIterator.primaryOrder(sOrder);
            int pTOrder = CollationElementIterator.primaryOrder(tOrder);

            // If there's no difference at this position, we can skip it
            if (sOrder == tOrder) {
                if (tables.isFrenchSec() && pSOrder != 0) {
                    if (!checkSecTer) {
                        // in french, a secondary difference more to the right is stronger,
                        // so accents have to be checked with each base element
                        checkSecTer = initialCheckSecTer;
                        // but tertiary differences are less important than the first
                        // secondary difference, so checking tertiary remains disabled
                        checkTertiary = false;
                    }
                }
                continue;
            }

            // Compare primary differences first.
            if ( pSOrder != pTOrder )
            {
                if (sOrder == 0) {
                    // The entire source element is ignorable.
                    // Skip to the next source element, but don't fetch another target element.
                    gett = false;
                    continue;
                }
                if (tOrder == 0) {
                    gets = false;
                    continue;
                }

                // The source and target elements aren't ignorable, but it's still possible
                // for the primary component of one of the elements to be ignorable....

                if (pSOrder == 0)  // primary order in source is ignorable
                {
                    // The source's primary is ignorable, but the target's isn't.  We treat ignorables
                    // as a secondary difference, so remember that we found one.
                    if (checkSecTer) {
                        result = Collator.GREATER;  // (strength is SECONDARY)
                        checkSecTer = false;
                    }
                    // Skip to the next source element, but don't fetch another target element.
                    gett = false;
                }
                else if (pTOrder == 0)
                {
                    // record differences - see the comment above.
                    if (checkSecTer) {
                        result = Collator.LESS;  // (strength is SECONDARY)
                        checkSecTer = false;
                    }
                    // Skip to the next source element, but don't fetch another target element.
                    gets = false;
                } else {
                    // Neither of the orders is ignorable, and we already know that the primary
                    // orders are different because of the (pSOrder != pTOrder) test above.
                    // Record the difference and stop the comparison.
                    if (pSOrder < pTOrder) {
                        return Collator.LESS;  // (strength is PRIMARY)
                    } else {
                        return Collator.GREATER;  // (strength is PRIMARY)
                    }
                }
            } else { // else of if ( pSOrder != pTOrder )
                // primary order is the same, but complete order is different. So there
                // are no base elements at this point, only ignorables (Since the strings are
                // normalized)

                if (checkSecTer) {
                    // a secondary or tertiary difference may still matter
                    short secSOrder = CollationElementIterator.secondaryOrder(sOrder);
                    short secTOrder = CollationElementIterator.secondaryOrder(tOrder);
                    if (secSOrder != secTOrder) {
                        // there is a secondary difference
                        result = (secSOrder < secTOrder) ? Collator.LESS : Collator.GREATER;
                                                // (strength is SECONDARY)
                        checkSecTer = false;
                        // (even in french, only the first secondary difference within
                        //  a base character matters)
                    } else {
                        if (checkTertiary) {
                            // a tertiary difference may still matter
                            short terSOrder = CollationElementIterator.tertiaryOrder(sOrder);
                            short terTOrder = CollationElementIterator.tertiaryOrder(tOrder);
                            if (terSOrder != terTOrder) {
                                // there is a tertiary difference
                                result = (terSOrder < terTOrder) ? Collator.LESS : Collator.GREATER;
                                                // (strength is TERTIARY)
                                checkTertiary = false;
                            }
                        }
                    }
                } // if (checkSecTer)

            }  // if ( pSOrder != pTOrder )
        } // while()

        if (sOrder != CollationElementIterator.NULLORDER) {
            // (tOrder must be CollationElementIterator::NULLORDER,
            //  since this point is only reached when sOrder or tOrder is NULLORDER.)
            // The source string has more elements, but the target string hasn't.
            do {
                if (CollationElementIterator.primaryOrder(sOrder) != 0) {
                    // We found an additional non-ignorable base character in the source string.
                    // This is a primary difference, so the source is greater
                    return Collator.GREATER; // (strength is PRIMARY)
                }
                else if (CollationElementIterator.secondaryOrder(sOrder) != 0) {
                    // Additional secondary elements mean the source string is greater
                    if (checkSecTer) {
                        result = Collator.GREATER;  // (strength is SECONDARY)
                        checkSecTer = false;
                    }
                }
            } while ((sOrder = sourceCursor.next()) != CollationElementIterator.NULLORDER);
        }
        else if (tOrder != CollationElementIterator.NULLORDER) {
            // The target string has more elements, but the source string hasn't.
            do {
                if (CollationElementIterator.primaryOrder(tOrder) != 0)
                    // We found an additional non-ignorable base character in the target string.
                    // This is a primary difference, so the source is less
                    return Collator.LESS; // (strength is PRIMARY)
                else if (CollationElementIterator.secondaryOrder(tOrder) != 0) {
                    // Additional secondary elements in the target mean the source string is less
                    if (checkSecTer) {
                        result = Collator.LESS;  // (strength is SECONDARY)
                        checkSecTer = false;
                    }
                }
            } while ((tOrder = targetCursor.next()) != CollationElementIterator.NULLORDER);
        }

        // For IDENTICAL comparisons, we use a bitwise character comparison
        // as a tiebreaker if all else is equal
        if (result == 0 && getStrength() == IDENTICAL) {
            Normalizer.Mode mode = NormalizerUtilities.toNormalizerMode(getDecomposition());
            String sourceDecomposition = Normalizer.normalize(source, mode, 0);
            String targetDecomposition = Normalizer.normalize(target, mode, 0);
            result = sourceDecomposition.compareTo(targetDecomposition);
        }
        return result;
    }

    /**
     * Transforms the string into a series of characters that can be compared
     * with CollationKey.compareTo. This overrides java.text.Collator.getCollationKey.
     * It can be overriden in a subclass.
     */
    public synchronized CollationKey getCollationKey(String source)
    {
        //
        // The basic algorithm here is to find all of the collation elements for each
        // character in the source string, convert them to a char representation,
        // and put them into the collation key.  
        // Each collation element in a string has three components: primary (A vs B),
        // secondary (A vs A-acute), and tertiary (A' vs a); and a primary difference
        // at the end of a string takes precedence over a secondary or tertiary
        // difference earlier in the string.
        //
        // To account for this, we put all of the primary orders at the beginning of the
        // string, followed by the secondary and tertiary orders, separated by nulls.
        //
        // Here's a hypothetical example, with the collation element represented as
        // a three-digit number, one digit for primary, one for secondary, etc.
        //
        // String:              A     a     B   \u00e9 <--(e-acute)
        // Collation Elements: 101   100   201  510
        //
        // Collation Key:      1125<null>0001<null>1010
        //
        // Secondary differences (accent marks) are compared
        // starting at the *end* of the string in languages with French secondary ordering.
        // But when comparing the accent marks on a single base character, they are compared
        // from the beginning.  To handle this, we reverse all of the accents that belong
        // to each base character, then we reverse the entire string of secondary orderings
        // at the end.  Taking the same example above, a French collator might return
        // this instead:
        //
        // Collation Key:      1125<null>1000<null>1010
        //
        if (source == null)
            return null;

        if (primResult == null) {
            primResult = new StringBuffer();
            secResult = new StringBuffer();
            terResult = new StringBuffer();
        } else {
            primResult.setLength(0);
            secResult.setLength(0);
            terResult.setLength(0);
        }
        int order = 0;
        boolean compareSec = (getStrength() >= Collator.SECONDARY);
        boolean compareTer = (getStrength() >= Collator.TERTIARY);
        int secOrder = CollationElementIterator.NULLORDER;
        int terOrder = CollationElementIterator.NULLORDER;
        int preSecIgnore = 0;

        if (sourceCursor == null) {
            sourceCursor = getCollationElementIterator(source);
        } else {
            sourceCursor.setText(source);
        }

        // walk through each character
        while ((order = sourceCursor.next()) !=
               CollationElementIterator.NULLORDER)
        {
            secOrder = CollationElementIterator.secondaryOrder(order);
            terOrder = CollationElementIterator.tertiaryOrder(order);
            if (!CollationElementIterator.isIgnorable(order))
            {
                primResult.append((char) (CollationElementIterator.primaryOrder(order)
                                    + COLLATIONKEYOFFSET));

                if (compareSec) {
                    //
                    // accumulate all of the ignorable/secondary characters attached
                    // to a given base character
                    //
                    if (tables.isFrenchSec() && preSecIgnore < secResult.length()) {
                        //
                        // We're doing reversed secondary ordering and we've hit a base
                        // (non-ignorable) character.  Reverse any secondary orderings
                        // that applied to the last base character.  (see block comment above.)
                        //
                        RBCollationTables.reverse(secResult, preSecIgnore, secResult.length());
                    }
                    // Remember where we are in the secondary orderings - this is how far
                    // back to go if we need to reverse them later.
                    secResult.append((char)(secOrder+ COLLATIONKEYOFFSET));
                    preSecIgnore = secResult.length();
                }
                if (compareTer) {
                    terResult.append((char)(terOrder+ COLLATIONKEYOFFSET));
                }
            }
            else
            {
                if (compareSec && secOrder != 0)
                    secResult.append((char)
                        (secOrder + tables.getMaxSecOrder() + COLLATIONKEYOFFSET));
                if (compareTer && terOrder != 0)
                    terResult.append((char)
                        (terOrder + tables.getMaxTerOrder() + COLLATIONKEYOFFSET));
            }
        }
        if (tables.isFrenchSec())
        {
            if (preSecIgnore < secResult.length()) {
                // If we've accumlated any secondary characters after the last base character,
                // reverse them.
                RBCollationTables.reverse(secResult, preSecIgnore, secResult.length());
            }
            // And now reverse the entire secResult to get French secondary ordering.
            RBCollationTables.reverse(secResult, 0, secResult.length());
        }
        primResult.append((char)0);
        secResult.append((char)0);
        secResult.append(terResult.toString());
        primResult.append(secResult.toString());

        if (getStrength() == IDENTICAL) {
            primResult.append((char)0);
            Normalizer.Mode mode = NormalizerUtilities.toNormalizerMode(getDecomposition());
            primResult.append(Normalizer.normalize(source, mode, 0));
        }
        return new CollationKey(source, primResult.toString());
    }

    /**
     * Standard override; no change in semantics.
     */
    public Object clone() {
        // if we know we're not actually a subclass of RuleBasedCollator
        // (this class really should have been made final), bypass
        // Object.clone() and use our "copy constructor".  This is faster.
        if (getClass() == RuleBasedCollator.class) {
            return new RuleBasedCollator(this);
        }
        else {
            RuleBasedCollator result = (RuleBasedCollator) super.clone();
            result.primResult = null;
            result.secResult = null;
            result.terResult = null;
            result.sourceCursor = null;
            result.targetCursor = null;
            return result;
        }
    }

    /**
     * Compares the equality of two collation objects.
     * @param obj the table-based collation object to be compared with this.
     * @return true if the current table-based collation object is the same
     * as the table-based collation object obj; false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!super.equals(obj)) return false;  // super does class check
        RuleBasedCollator other = (RuleBasedCollator) obj;
        // all other non-transient information is also contained in rules.
        return (getRules().equals(other.getRules()));
    }

    /**
     * Generates the hash code for the table-based collation object
     */
    public int hashCode() {
        return getRules().hashCode();
    }

    /**
     * Allows CollationElementIterator access to the tables object
     */
    RBCollationTables getTables() {
        return tables;
    }

    // ==============================================================
    // private
    // ==============================================================

    final static int CHARINDEX = 0x70000000;  // need look up in .commit()
    final static int EXPANDCHARINDEX = 0x7E000000; // Expand index follows
    final static int CONTRACTCHARINDEX = 0x7F000000;  // contract indexes follow
    final static int UNMAPPED = 0xFFFFFFFF;

    private final static int COLLATIONKEYOFFSET = 1;

    private RBCollationTables tables = null;

    // Internal objects that are cached across calls so that they don't have to
    // be created/destroyed on every call to compare() and getCollationKey()
    private StringBuffer primResult = null;
    private StringBuffer secResult = null;
    private StringBuffer terResult = null;
    private CollationElementIterator sourceCursor = null;
    private CollationElementIterator targetCursor = null;
}

