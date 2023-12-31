#
# %W% %E%
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

ifeq ($(CVM_INCLUDE_MIDP),true)

# print our configuration
printconfig::
	@echo "MIDP_DIR           = $(MIDP_DIR)"
	@echo "PCSL_DIR           = $(PCSL_DIR)"

# Build PCSL before MIDP.
initbuild_profile::
	$(AT)echo "Building pcsl ..."
	$(AT)$(MAKE) PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             NETWORK_MODULE=$(NETWORK_MODULE) \
	             PCSL_OUTPUT_DIR=$(PCSL_OUTPUT_DIR) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
	             -C $(PCSL_DIR) $(PCSL_MAKE_OPTIONS)

#
# Invoke MIDP build process. Build MIDP classes first. If 
# CVM_PRELOAD_LIB is true, MIDP classes are added to  JCC 
# input list so the we can romize MIDP classes.
#
# We can't build MIDP natives together with classes, because
# MIDP natives requires generated header files, such as
# generated/cni/sun_misc_CVM.h and
# generated/offsets/java_lang_ref_Reference.h. The header files 
# are generated by the ROMizer. MIDP natives are compiled
# after ROMization is done.
#
$(CVM_ROMJAVA_LIST): $(MIDP_CLASSESZIP)

$(MIDP_CLASSESZIP): $(MIDP_CLASSESZIP_DEPS) force_midp_build
	$(AT)echo "Building MIDP classes ..."
	$(AT)$(MAKE) JDK_DIR=$(JDK_DIR) TARGET_VM=$(TARGET_VM) \
	             TARGET_CPU=$(TARGET_CPU) USE_DEBUG=$(USE_DEBUG) \
	             USE_SSL=$(USE_SSL) \
	             USE_RESTRICTED_CRYPTO=$(USE_RESTRICTED_CRYPTO) \
	             VERIFY_BUILD_ENV= \
	             CONFIGURATION_OVERRIDE=$(CONFIGURATION_OVERRIDE) \
	             USE_QT_FB=$(USE_QT_FB) USE_DIRECTFB=$(USE_DIRECTFB) \
	             USE_SSL=$(USE_SSL) USE_CONFIGURATOR=$(USE_CONFIGURATOR) \
	             USE_VERBOSE_MAKE=$(USE_VERBOSE_MAKE) \
	             PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
	             rom -C $(MIDP_DIR)/$(MIDP_MAKEFILE_DIR)

#
# Now build MIDP natives. MIDP natives are linked into CVM binary.
#
ifeq ($(CVM_PRELOAD_LIB), true)
$(MIDP_OBJECTS): $(RUNMIDLET)
else
$(CVM_BINDIR)/$(CVM):: $(RUNMIDLET)
endif

$(RUNMIDLET): force_midp_build
	$(AT)echo "Building MIDP native ..."
	$(AT)$(MAKE) JDK_DIR=$(JDK_DIR) TARGET_VM=$(TARGET_VM) \
	             TARGET_CPU=$(TARGET_CPU) USE_DEBUG=$(USE_DEBUG) \
	             USE_SSL=$(USE_SSL) \
	             USE_RESTRICTED_CRYPTO=$(USE_RESTRICTED_CRYPTO) \
	             VERIFY_BUILD_ENV= \
	             CONFIGURATION_OVERRIDE=$(CONFIGURATION_OVERRIDE) \
	             USE_QT_FB=$(USE_QT_FB) USE_DIRECTFB=$(USE_DIRECTFB) \
	             USE_SSL=$(USE_SSL) USE_CONFIGURATOR=$(USE_CONFIGURATOR) \
	             USE_VERBOSE_MAKE=$(USE_VERBOSE_MAKE) \
	             PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
	             -C $(MIDP_DIR)/$(MIDP_MAKEFILE_DIR)
	$(AT)cp $@ $(CVM_BINDIR)
ifneq ($(CVM_PRELOAD_LIB), true)
	$(AT)cp $(MIDP_OUTPUT_DIR)/bin/$(TARGET_CPU)/libmidp$(LIB_POSTFIX) $(CVM_LIBDIR)
endif

force_midp_build:

endif
