/*
 * @(#)MethodConstant.java	1.9 06/10/10
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.  
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
 *
 */

package components;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import consts.Const;

// This class represents a CONSTANT_Methodref

public
class MethodConstant extends FMIrefConstant {
    boolean	didLookup;
    MethodInfo	theMethod;

    MethodConstant( int t ){
	tag = t;
    }

    public MethodConstant( ClassConstant c, NameAndTypeConstant sig ){
	super( Const.CONSTANT_METHOD, c, sig );
    }

    public static ConstantObject
    read( int t, DataInput in ) throws IOException {
	FMIrefConstant mc = new MethodConstant( t );
	mc.read( in );
	return mc;
    }

    public MethodInfo find(){
	if ( ! didLookup ){
	    theMethod = (MethodInfo)super.find(true);
	    didLookup = true;
	}
	return theMethod;
    }

}
