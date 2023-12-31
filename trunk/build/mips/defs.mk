#
# Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation. 
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt). 
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA 
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions. 
#
# @(#)defs.mk	1.11 06/10/10
#

#
# defs for mips targets
#

CVM_SRCDIRS   += \
	$(CVM_TOP)/src/$(TARGET_CPU_FAMILY)/javavm/runtime

CVM_INCLUDES  += \
	-I$(CVM_TOP)/src/$(TARGET_CPU_FAMILY)

ifeq ($(CVM_AOT), true)
$(error AOT is not supported for MIPS)
endif

#
# JIT related settings
#
ifeq ($(CVM_JIT), true)

CVM_TARGETOBJS_SPEED +=	\
    ccmintrinsics_cpu.o

CVM_JCS_CPU_INCLUDES_FILE = \

CVM_JCS_CPU_DEFS_FILE     = \

CVM_JCS_CPU_RULES_FILE    = \
    $(CVM_TOP)/src/mips/javavm/runtime/jit/jitgrammarrules.jcs

ifeq ($(CVM_JIT_USE_FP_HARDWARE), true)
CVM_JCS_CPU_RULES_FILE    += \
    $(CVM_TOP)/src/mips/javavm/runtime/jit/jitfloatgrammarrules.jcs
endif

# Copy ccm assembler code to the codecache so it is reachable
# with a branch instruction. The branch range is +-32K, for methods
# out of the range, non-PC relative instruction jal is used instead
# of the PC relative branch instructon. If AOT is supported, we
# want the jal target at a fixed location.
ifneq ($(CVM_AOT), true)
CVM_JIT_COPY_CCMCODE_TO_CODECACHE ?= true
endif
include  ../portlibs/defs_jit_risc.mk

endif
