/*
 * @(#)ClassMemberInfo.java	1.19 06/10/10
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
import jcc.Util;
import consts.Const;
import jcc.Str2ID;

public abstract
class ClassMemberInfo extends ClassComponent {
    public int		 access;
    public int		 nameIndex;
    public int		 typeIndex;
    public UnicodeConstant name;
    public UnicodeConstant type;
    public ClassInfo	 parent;
    private int	 	 ID;
    private boolean 	 computedID = false;

    public int		 index;		// used by in-core output writers

    public int 		 flags;		// used by member loader
    public final static int INCLUDE	= 1; // a flag value.

    public ClassMemberInfo( int n, int t, int a, ClassInfo p ){
	nameIndex = n;
	typeIndex = t;
	access    = a;
	parent    = p;
	flags	  = INCLUDE; // by default, we want everything.
    }

    public boolean isStaticMember( ){
	return ( (access & Const.ACC_STATIC) != 0 );
    }

    public boolean isPrivateMember( ){
	return ( (access & Const.ACC_PRIVATE) != 0 );
    }

    public boolean isFinalMember( ){
	return ( (access & Const.ACC_FINAL) != 0 );
    }


    public void
    resolve( ConstantObject table[] ){
	if ( resolved ) return;
	name     = (UnicodeConstant)table[nameIndex];
	type     = (UnicodeConstant)table[typeIndex];
	resolved = true;
    }

    public int
    getID(){
	if ( ! computedID ){
	    ID       = Str2ID.sigHash.getID( name, type );
	    computedID = true;
	}
	return ID;
    }

    public void
    countConstantReferences(boolean isRelocatable){
	if (isRelocatable){
	    if ( name != null ) name.incReference();
	    if ( type != null ) type.incReference();
	}
    }

    /*
     * To be called just before writing output.
     * validate that our UnicodeConstants are not in any constant pool.
     * Subclasses can override to provide more checking.
     */
    public void
    validate(){
	name.validate();
	type.validate();
    }

    public String toString(){
	if ( resolved ){
	    return Util.accessToString(access)+" "+parent.className+"."+name.string+" : "+type.string;
	} else {
	    return Util.accessToString(access)+" [ "+nameIndex+" : "+typeIndex+" ]";
	}
    }
    public String qualifiedName(){
	if ( resolved ){
	    return name.string+":"+type.string;
	}else{
	    return Util.accessToString(access)+" "+parent.className+" [ "+nameIndex+" : "+typeIndex+" ]";
	}
    }
}

