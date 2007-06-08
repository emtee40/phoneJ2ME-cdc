/*
 * @(#)jvmtiDumper.h	1.0 07/01/17
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.  
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


#ifndef _JAVA_JVMTIDUMP_H_
#define _JAVA_JVMTIDUMP_H_

void CVMjvmtiTagRehash();
CVMBool CVMjvmtiDumpObject(CVMObject *obj, jvmtiDumpContext *dc);
jvmtiError CVMjvmtiTagGetTagGC(CVMObject *obj, jlong *tag_ptr);
jvmtiError CVMjvmtiTagGetTag(jvmtiEnv* jvmtienv, jobject object, jlong* tag_ptr);
jvmtiError CVMjvmtiTagSetTag(jvmtiEnv *jvmtienv, jobject object, jlong tag);
CVMBool CVMjvmtiScanRoots(CVMExecEnv *ee, jvmtiDumpContext *dc);
void CVMjvmtiPostCallbackUpdateTag(CVMObject *obj, TagNode *node, jlong tag);
TagNode * CVMjvmtiTagGetNode(CVMObject *obj);

CVMBool CVMjvmtiIterateDoObject(CVMObject* obj, CVMClassBlock* cb, 
                                         CVMUint32  objSize, void* data);

int CVMjvmtiGetObjectsWithTag(JNIEnv *env, const jlong *tags, jint tag_count,
			      jobject *obj_ptr, jlong *tag_ptr);

#endif