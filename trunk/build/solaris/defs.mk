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
# @(#)defs.mk	1.118 06/10/24
#
# defs for Solaris target
#

CVM_TARGETROOT	= $(CVM_TOP)/src/$(TARGET_OS)

#
# Platform specific defines
#
CVM_DEFINES	+= -D_REENTRANT
CVM_DEFINES     += -D__solaris__

#
# All the platform specific build flags we need to keep track of in
# case they are toggled. OK to leave the CVM_ prefix off.
#
HAVE_64_BIT_IO	= false

CVM_FLAGS += \
	HAVE_64_BIT_IO \

HAVE_64_BIT_IO_CLEANUP_ACTION	= $(CVM_DEFAULT_CLEANUP_ACTION)

#
# Platform specific defines
#
ifeq ($(HAVE_64_BIT_IO), true)
CVM_DEFINES	+= -DHAVE_64_BIT_IO
endif

#
# Platform specific source directories
#
CVM_SRCDIRS   += $(CVM_TOP)/src/portlibs/ansi_c
CVM_SRCDIRS   += $(CVM_TOP)/src/portlibs/posix
CVM_SRCDIRS   += $(CVM_TOP)/src/portlibs/dlfcn
CVM_SRCDIRS   += $(CVM_TOP)/src/portlibs/realpath
CVM_SRCDIRS   += \
	$(CVM_TARGETROOT)/javavm/runtime \
	$(CVM_TARGETROOT)/bin \
	$(CVM_TARGETROOT)/native/java/lang \
	$(CVM_TARGETROOT)/native/java/io \
	$(CVM_TARGETROOT)/native/java/net \

CVM_INCLUDES  += \
	-I$(CVM_TOP)/src \
	-I$(CVM_TARGETROOT) \
	-I$(CVM_TARGETROOT)/native/java/net \
	-I$(CVM_TARGETROOT)/native/common \

#
# Platform specific objects
#

CVM_TARGETOBJS_SPACE += \
	java_md.o \
	ansi_java_md.o \
	canonicalize_md.o \
	posix_sync_md.o \
	posix_threads_md.o \
	io_md.o \
	posix_io_md.o \
	posix_net_md.o \
	posix_time_md.o \
	io_util.o \
	sync_md.o \
	system_md.o \
	threads_md.o \
	globals_md.o \
	java_props_md.o \
	memory_md.o \

#
# On solaris, CVM_INCLUDE_JUMP=true if and only if CVM_MTASK=true
#
ifeq ($(CVM_INCLUDE_JUMP), true)
override CVM_MTASK	= true
endif
ifeq ($(CVM_MTASK), true)
override CVM_INCLUDE_JUMP = true
endif

ifeq ($(CVM_MTASK), true)
CVM_SRCDIRS += \
	$(CVM_SHAREROOT)/native/sun/mtask
CVM_DEFINES   += -DCVM_MTASK
CVM_SHAREOBJS_SPACE += \
	mtask.o \
	Listener.o
CLASSLIB_CLASSES += \
	sun.mtask.Warmup \
	sun.mtask.AppModelManager \
	sun.mtask.Listener
endif

ifeq ($(CVM_JIT), true)
CVM_SRCDIRS   += \
	$(CVM_TARGETROOT)/javavm/runtime/jit
CVM_TARGETOBJS_SPACE += \
	jit_md.o
endif

ifeq ($(CVM_DYNAMIC_LINKING), true)
	CVM_TARGETOBJS_SPACE += linker_md.o
endif

###########################################
# Overrides of stuff setup in share/defs.mk
###########################################

#
# Fixup SO_LINKFLAGS..
#
SO_LINKFLAGS	:= $(subst -shared,,$(SO_LINKFLAGS))
SO_LINKFLAGS	+= -dy -G

#
# The solaris linker automatically exports all symbols in the executable.
# However, the gnu linker does not. By default LINKFLAGS includes the
# -Wl,-export-dynamic option to export all symbols, but the solaris
# linker won't accept this option so we need to strip it off.
#
ifneq ($(CVM_USE_NATIVE_TOOLS), true)
is_gnu_ld := $(shell $(TARGET_CC) -Wl,-V 2>&1 | grep -c 'GNU ld')
else
is_gnu_ld := 0
endif
ifeq ($(is_gnu_ld), 0)
LINKFLAGS	:= $(subst -Wl$(comma)-export-dynamic,,$(LINKFLAGS))
SO_LINKFLAGS	:= $(subst -Wl$(comma)-export-dynamic,,$(SO_LINKFLAGS))
endif

#
#  Adjust compiler options if we are using suncc
#
ifeq ($(CVM_USE_NATIVE_TOOLS), true)
ASM_ARCH_FLAGS  := $(subst -Wa$(comma)-xarch,-xarch,$(ASM_ARCH_FLAGS))
CC_ARCH_FLAGS_LOOP  := $(subst -fno-inline,,$(CC_ARCH_FLAGS_LOOP))
CC_ARCH_FLAGS	:= $(subst -m,-xarch=,$(CC_ARCH_FLAGS))
ASM_FLAGS  	:= $(subst -fno-common,,$(ASM_FLAGS))
CCFLAGS  	:= $(subst -fno-strict-aliasing,,$(CCFLAGS))
CCFLAGS  	:= $(subst -fno-common,,$(CCFLAGS))
CCFLAGS     	:= $(subst -Wall,,$(CCFLAGS))
CCFLAGS     	:= $(subst -pg,-xpg,$(CCFLAGS))
CCFLAGS_SPEED	:= $(subst -O,-xO,$(CCFLAGS_SPEED))
CCFLAGS_SPACE	:= $(subst -O,-xO,$(CCFLAGS_SPACE))
CCFLAGS         += -mt -xCC
CCFLAGS_SPEED   += -mt -xCC
CCFLAGS_SPACE   += -mt -xCC
CCDEPEND    	= -xM
LINKFLAGS     	:= $(subst -pg,-xpg,$(LINKFLAGS))
# suncc does not support assembling files, so we need to use the sun "as"
# tool directly and also tweak the arguments.
TARGET_AS	= /usr/ccs/bin/as
ASM_FLAGS     	:= $(subst -c,-P -D__SUNPRO_C=1,$(ASM_FLAGS))
endif

ifneq ($(CVM_TOOLS_BUILD), true)
LINKLIBS 	= -lpthread -lm -lnsl -lsocket -ldl -lposix4 $(LINK_ARCH_LIBS)
endif

#
# suncc doesn't support C++. Use CC instead. Also, get rid of -fno-rtti option.
#
ifeq ($(CVM_USE_NATIVE_TOOLS), true)
HOST_CCC	:= CC
CCCFLAGS     	:= $(subst -fno-rtti,,$(CCCFLAGS))
LINKLIBS_JCS    += -lCrun
endif

#
# tcov only supported with suncc
#
ifeq ($(CVM_TCOV_PROF), true)
ifeq ($(CVM_USE_NATIVE_TOOLS), true)
CFLAGS += -xildoff -xprofile=tcov
LINKFLAGS += -xprofile=tcov
endif
endif
