/*
 * @(#)time_md.c	1.8 06/10/10
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

#include "javavm/include/porting/time.h"

#ifdef WINCE
#include <winbase.h>
#else
#include <windows.h>
#endif

#define FT2INT64(ft) \
        ((CVMInt64)(ft).dwHighDateTime << 32 | (CVMInt64)(ft).dwLowDateTime)

CVMInt64
CVMtimeMillis(void)
{
#ifndef WINCE
    static CVMInt64 fileTime_1_1_70 = 0;
    SYSTEMTIME st0;
    FILETIME   ft0;

    if (fileTime_1_1_70 == 0) {
        /* Initialize fileTime_1_1_70 -- the Win32 file time of midnight
         * 1/1/70.
         */

        memset(&st0, 0, sizeof(st0));
        st0.wYear  = 1970;
        st0.wMonth = 1;
        st0.wDay   = 1;
        SystemTimeToFileTime(&st0, &ft0);
        fileTime_1_1_70 = FT2INT64(ft0);
    }

    GetSystemTime(&st0);
    SystemTimeToFileTime(&st0, &ft0);

    return (FT2INT64(ft0) - fileTime_1_1_70) / 10000;
#else 
    CVMInt64 fileTime_1_1_70 = 0;
    SYSTEMTIME st0;
    FILETIME   ft0;
    static CVMInt64 originTick = 0;
    CVMInt64 ttt;

    if (originTick == 0) {
        /* Initialize fileTime_1_1_70 -- the Win32 file time of midnight
         * 1/1/70.
         */

        memset(&st0, 0, sizeof(st0));
        st0.wYear  = 1970;
        st0.wMonth = 1;
        st0.wDay   = 1;
        SystemTimeToFileTime(&st0, &ft0);
        fileTime_1_1_70 = FT2INT64(ft0);

        GetSystemTime(&st0);
        SystemTimeToFileTime(&st0, &ft0);
        originTick = (FT2INT64(ft0) - fileTime_1_1_70) / 10000;
        originTick -= GetTickCount(); 
    }

    ttt = GetTickCount() + originTick;
    return ttt;
#endif
}

#ifdef WINCE
#include "javavm/include/ansi/time.h"

clock_t
clock(void)
{
    return 0;
}
#endif
