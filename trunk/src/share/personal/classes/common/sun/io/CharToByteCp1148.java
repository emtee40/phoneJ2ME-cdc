/*
 *
 * @(#)CharToByteCp1148.java	1.11 06/10/03
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
 *
 * (C) Copyright IBM Corp. 1998 - All Rights Reserved
 *
 */

package sun.io;

/**
 * Tables and data to convert Unicode to Cp1148
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.7
 */

public class CharToByteCp1148 extends CharToByteSingleByte {
    public String getCharacterEncoding() {
        return "Cp1148";
    }

    public CharToByteCp1148() {
        super.mask1 = 0xFF00;
        super.mask2 = 0x00FF;
        super.shift = 8;
        super.index1 = index1;
        super.index2 = index2;
    }
    private final static String index2 =

        "\u0000\u0001\u0002\u0003\u0037\u002D\u002E\u002F" + 
        "\u0016\u0005\u0025\u000B\f\r\u000E\u000F" + 
        "\u0010\u0011\u0012\u0013\u003C\u003D\u0032\u0026" + 
        "\u0018\u0019\u003F\'\u001C\u001D\u001E\u001F" + 
        "\u0040\u004F\u007F\u007B\u005B\u006C\u0050\u007D" + 
        "\u004D\u005D\\\u004E\u006B\u0060\u004B\u0061" + 
        "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7" + 
        "\u00F8\u00F9\u007A\u005E\u004C\u007E\u006E\u006F" + 
        "\u007C\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7" + 
        "\u00C8\u00C9\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6" + 
        "\u00D7\u00D8\u00D9\u00E2\u00E3\u00E4\u00E5\u00E6" + 
        "\u00E7\u00E8\u00E9\u004A\u00E0\u005A\u005F\u006D" + 
        "\u0079\u0081\u0082\u0083\u0084\u0085\u0086\u0087" + 
        "\u0088\u0089\u0091\u0092\u0093\u0094\u0095\u0096" + 
        "\u0097\u0098\u0099\u00A2\u00A3\u00A4\u00A5\u00A6" + 
        "\u00A7\u00A8\u00A9\u00C0\u00BB\u00D0\u00A1\u0007" + 
        "\u0020\u0021\"\u0023\u0024\u0015\u0006\u0017" + 
        "\u0028\u0029\u002A\u002B\u002C\t\n\u001B" + 
        "\u0030\u0031\u001A\u0033\u0034\u0035\u0036\b" + 
        "\u0038\u0039\u003A\u003B\u0004\u0014\u003E\u00FF" + 
        "\u0041\u00AA\u00B0\u00B1\u0000\u00B2\u006A\u00B5" + 
        "\u00BD\u00B4\u009A\u008A\u00BA\u00CA\u00AF\u00BC" + 
        "\u0090\u008F\u00EA\u00FA\u00BE\u00A0\u00B6\u00B3" + 
        "\u009D\u00DA\u009B\u008B\u00B7\u00B8\u00B9\u00AB" + 
        "\u0064\u0065\u0062\u0066\u0063\u0067\u009E\u0068" + 
        "\u0074\u0071\u0072\u0073\u0078\u0075\u0076\u0077" + 
        "\u00AC\u0069\u00ED\u00EE\u00EB\u00EF\u00EC\u00BF" + 
        "\u0080\u00FD\u00FE\u00FB\u00FC\u00AD\u00AE\u0059" + 
        "\u0044\u0045\u0042\u0046\u0043\u0047\u009C\u0048" + 
        "\u0054\u0051\u0052\u0053\u0058\u0055\u0056\u0057" + 
        "\u008C\u0049\u00CD\u00CE\u00CB\u00CF\u00CC\u00E1" + 
        "\u0070\u00DD\u00DE\u00DB\u00DC\u008D\u008E\u00DF" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u009F\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000" + 
        "\u0000\u0000\u0000\u0000"; 
    private final static short index1[] = {
            0, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            340, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
            256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 
        };
}
