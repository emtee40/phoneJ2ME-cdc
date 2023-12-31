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
# %W% %E%
#
# Compile ccmcodecachecopy_cpu.S as a library and copy to the Symbian release directory,
# so we can link the CVM executable against it.
#

CVMEXT_LIB_DEPS = $(CVM_TOP)/src/arm/javavm/runtime/invokeNative_arm.S
ifeq ($(CVM_JIT), true)
CVMEXT_LIB_DEPS += \
		$(CVM_TOP)/src/arm/javavm/runtime/jit/ccmcodecachecopy_cpu.S \
		$(CVM_TOP)/src/arm/javavm/runtime/jit/jit_cpu.S
endif

ifeq ($(SYMBIAN_PLATFORM), armv5)
#
# Preprocess the *.S files.
#
ASM_COMMAND = armcc $(CVM_DEFINES) $(CVM_INCLUDES) -E $(1) > $(CVM_DERIVEDROOT)/javavm/runtime/$(2)0.S; \
              echo SOURCEPATH $(CVM_DERIVEDROOT)/javavm/runtime >> $(CVM_MMP); \
	      echo SOURCE $(2)0.S >> $(CVM_MMP)
$(CVMEXT_LIB) : $(CVMEXT_LIB_DEPS)
	echo "Preprocess $^"
ifeq ($(CVM_JIT), true)
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/jit/ccmcodecachecopy_cpu.S,ccmcodecachecopy_cpu)
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/jit/jit_cpu.S,jit_cpu)
endif
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/invokeNative_arm.S,invokeNative_arm)
else
#
# Use Symbian gcc to compile the *.S files and link as a lib.
#
ASM_COMMAND = $(TARGET_AS) $(ASM_FLAGS) -D_ASM $(CPPFLAGS) -o $(CVM_OBJDIR)/$(2).o $(1)
ifeq ($(CVM_JIT), true)
AR_COMMAND  = $(TARGET_AR) rc $(CVMEXT_LIB) \
			$(CVM_OBJDIR)/ccmcodecachecopy_cpu.o \
			$(CVM_OBJDIR)/jit_cpu.o \
			$(CVM_OBJDIR)/invokeNative_arm.o;
else
AR_COMMAND  = $(TARGET_AR) rc $(CVMEXT_LIB) $(CVM_OBJDIR)/invokeNative_arm.o;
endif
AR_COMMAND  += \
	cp $(CVMEXT_LIB) $(EPOC_U)/release/$(SYMBIAN_PLATFORM)/udeb; \
	cp $(CVMEXT_LIB) $(EPOC_U)/release/$(SYMBIAN_PLATFORM)/urel; \
	cp $(CVMEXT_LIB) $(EPOC_U)/release/$(SYMBIAN_PLATFORM)/lib
$(CVMEXT_LIB) : $(CVMEXT_LIB_DEPS)
	echo "generating $@"; \
	export PATH; PATH=$(EPOC_U)/gcc/bin:'$(subst :,':',$(PATH))';\
	gcc -v; \
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/jit/ccmcodecachecopy_cpu.S,ccmcodecachecopy_cpu); \
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/jit/jit_cpu.S,jit_cpu); \
	$(call ASM_COMMAND,$(CVM_TOP)/src/arm/javavm/runtime/invokeNative_arm.S,invokeNative_arm); \
	$(AR_COMMAND)
endif
