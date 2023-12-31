/*
 * @(#)endianness_arch.h	1.6 04/12/23
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

#ifndef _LINUX_ENDIANNESS_ARCH_H
#define _LINUX_ENDIANNESS_ARCH_H

#ifdef __ARMEB__
#define CVM_ENDIANNESS CVM_BIG_ENDIAN
#else
#define CVM_ENDIANNESS CVM_LITTLE_ENDIAN
#endif

#ifdef __VFP_FP__
#define CVM_DOUBLE_ENDIANNESS CVM_ENDIANNESS
#else
#define CVM_DOUBLE_ENDIANNESS CVM_BIG_ENDIAN
#endif

#endif /* _LINUX_ENDIANNESS_ARCH_H */
