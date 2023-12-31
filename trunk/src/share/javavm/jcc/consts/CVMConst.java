/*
 * @(#)CVMConst.java	1.19 06/10/10
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
package consts;
public interface CVMConst {

    /* 
     * Class, field, and method access and modifier flags. Some of these
     * values are different from the red book so they'll fit in one byte
     */

    public static final int CVM_CLASS_ACC_PUBLIC        = 0x01; /* visible to everyone */
    public static final int CVM_CLASS_ACC_PRIMITIVE     = 0x02; /* primitive type class */
    public static final int CVM_CLASS_ACC_FINALIZABLE   = 0x04; /* has non-trivial finalizer */
    public static final int CVM_CLASS_ACC_REFERENCE     = 0x08; /* is a flavor of weak reference */
    public static final int CVM_CLASS_ACC_FINAL         = 0x10; /* no further subclassing */
    public static final int CVM_CLASS_ACC_SUPER         = 0x20; /* funky handling of invokespecial */
    public static final int CVM_CLASS_ACC_INTERFACE     = 0x40; /* class is an interface */
    public static final int CVM_CLASS_ACC_ABSTRACT      = 0x80; /* may not be instantiated */

    public static final int CVM_FIELD_ACC_PUBLIC        = 0x01; /* visible to everyone */
    public static final int CVM_FIELD_ACC_PRIVATE       = 0x02; /* visible only to defining class */
    public static final int CVM_FIELD_ACC_PROTECTED     = 0x04; /* visible to subclasses */
    public static final int CVM_FIELD_ACC_STATIC        = 0x08; /* instance variable is static */
    public static final int CVM_FIELD_ACC_FINAL         = 0x10; /* no subclassing/overriding */
    public static final int CVM_FIELD_ACC_VOLATILE      = 0x40; /* cannot cache in registers */
    public static final int CVM_FIELD_ACC_TRANSIENT     = 0x80; /* not persistant */

    public static final int CVM_METHOD_ACC_PUBLIC       = 0x01; /* visible to everyone */
    public static final int CVM_METHOD_ACC_PRIVATE      = 0x02; /* visible only to defining class */
    public static final int CVM_METHOD_ACC_PROTECTED    = 0x04; /* visible to subclasses */
    public static final int CVM_METHOD_ACC_STATIC       = 0x08; /* method is static */
    public static final int CVM_METHOD_ACC_FINAL        = 0x10; /* no further overriding */
    public static final int CVM_METHOD_ACC_SYNCHRONIZED = 0x20; /* wrap method call in monitor lock */
    public static final int CVM_METHOD_ACC_NATIVE       = 0x40; /* implemented in C */
    public static final int CVM_METHOD_ACC_ABSTRACT     = 0x80; /* no definition provided */


    /* 
     * CVMConstantPoolType
     * See the enum CVMConstantPoolEntryTypeEnum in
     * javavm/include/constantpool.h
     *
     * The first set is right out of the class file.
     */
    public static final int CVM_CONSTANT_Utf8		= 1;
    public static final int CVM_CONSTANT_Unicode 	= 2;
    public static final int CVM_CONSTANT_Integer	= 3;
    public static final int CVM_CONSTANT_Float		= 4;
    public static final int CVM_CONSTANT_Long		= 5;      
    public static final int CVM_CONSTANT_Double		= 6;
    public static final int CVM_CONSTANT_Class		= 7;
    public static final int CVM_CONSTANT_String		= 8;
    public static final int CVM_CONSTANT_Fieldref	= 9;
    public static final int CVM_CONSTANT_Methodref	= 10;
    public static final int CVM_CONSTANT_InterfaceMethodref= 11;
    public static final int CVM_CONSTANT_NameAndType	= 12;

    /*
     * These are the unresolved types (already processed into
     * typeID's, but not into pointers).
     */
    public static final int CVM_CONSTANT_ClassTypeID	= 13;
    public static final int CVM_CONSTANT_MethodTypeID	= 14;
    public static final int CVM_CONSTANT_FieldTypeID	= 15;

    /*
     * These are fully resolved into pointers to the
     * appropriate data structures. You should always see these
     * with the CVM_CONSTANT_POOL_ENTRY_RESOLVED bit set.
     * The scalar numbers, such as Integer, Float, Long, are also
     * considered as resolved, and should have that bit set.
     */
    public static final int CVM_CONSTANT_ClassBlock	= 19;
    public static final int CVM_CONSTANT_FieldBlock	= 20;
    public static final int CVM_CONSTANT_MethodBlock	= 21;
    public static final int CVM_CONSTANT_StringObj	= 22; /* ROM string */
    public static final int CVM_CONSTANT_StringICell	= 23; /* classloaded string */
    public static final int CVM_CONSTANT_Invalid	= 24;

    public static final int CVM_CONSTANT_POOL_ENTRY_RESOLVED = 0x80;
    public static final int CVM_CONSTANT_POOL_ENTRY_RESOLVING = 0x40;
    public static final int CVM_CONSTANT_POOL_ENTRY_TYPEMASK = 0x3F;

    /*
     * Implementation details we must know to compute
     * field offsets.
     * Note that header size is the minimum. Alternative GC's
     * might require more, so will require a change here!!!!!
     */
    public static final int ObjHeaderWords		= 2;
    public static final int BytesPerWord		= 4;

}
