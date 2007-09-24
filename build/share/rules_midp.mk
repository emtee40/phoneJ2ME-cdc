#
# Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

ifeq ($(USE_MIDP),true)

# print our configuration
printconfig::
	@echo "MIDP_DIR           = $(MIDP_DIR)"
	@echo "PCSL_DIR           = $(PCSL_DIR)"

# Build PCSL before MIDP.
initbuild_profile::
	@echo "====> start pcsl build"
	$(AT)$(MAKE) $(MAKE_NO_PRINT_DIRECTORY) \
		     PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             NETWORK_MODULE=$(NETWORK_MODULE) \
	             PCSL_OUTPUT_DIR=$(PCSL_OUTPUT_DIR) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
                 USE_DEBUG=$(CVM_DEBUG) \
	             -C $(PCSL_DIR) $(PCSL_MAKE_OPTIONS)
	@echo "<==== end pcsl build"

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
$(CVM_ROMJAVA_LIST): $(MIDP_PUB_CLASSES_ZIP) $(MIDP_PRIV_CLASSES_ZIP)

$(MIDP_PUB_CLASSES_ZIP) $(MIDP_PRIV_CLASSES_ZIP): $(MIDP_CLASSES_ZIP)

$(MIDP_CLASSES_ZIP): $(MIDP_CLASSESZIP_DEPS) force_midp_build
	@echo "====> start building MIDP classes"
	$(AT)$(MAKE) $(MAKE_NO_PRINT_DIRECTORY) \
		     JDK_DIR=$(JDK_DIR) TARGET_VM=$(TARGET_VM) \
	             TARGET_CPU=$(TARGET_CPU) USE_DEBUG=$(USE_DEBUG) \
	             USE_SSL=$(USE_SSL) \
	             USE_RESTRICTED_CRYPTO=$(USE_RESTRICTED_CRYPTO) \
	             VERIFY_BUILD_ENV= \
	             CONFIGURATION_OVERRIDE=$(CONFIGURATION_OVERRIDE) \
	             USE_QT_FB=$(USE_QT_FB) USE_DIRECTFB=$(USE_DIRECTFB) \
	             USE_DIRECTDRAW=$(USE_DIRECTDRAW) \
	             USE_SSL=$(USE_SSL) USE_CONFIGURATOR=$(USE_CONFIGURATOR) \
	             USE_VERBOSE_MAKE=$(USE_VERBOSE_MAKE) \
	             PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
	             MIDP_CLASSES_ZIP=$(MIDP_CLASSES_ZIP) \
                     MIDP_PRIV_CLASSES_ZIP=$(MIDP_PRIV_CLASSES_ZIP) \
                     MIDP_PUB_CLASSES_ZIP=$(MIDP_PUB_CLASSES_ZIP) \
	             MIDP_SHARED_LIB=$(MIDP_SHARED_LIB) \
		     VM_BOOTCLASSPATH=$(VM_BOOTCLASSPATH) \
		     CVM_BUILDTIME_CLASSESZIP=$(CVM_BUILDTIME_CLASSESZIP) \
	             $(MIDP_JSROP_USE_FLAGS) \
	             USE_OEM_AMS=$(USE_OEM_AMS) \
	             OEM_AMS_DIR=$(OEM_AMS_DIR) \
	             USE_OEM_PUSH=$(USE_OEM_PUSH) \
	             OEM_PUSH_DIR=$(OEM_PUSH_DIR) \
	             JSR_MIDP_INITIALIZER_LIST="$(subst .Initializer,.MIDPInitializer,$(JSR_INITIALIZER_LIST))" \
	             COMPONENTS_DIR=$(COMPONENTS_DIR) \
	             rom -C $(MIDP_DIR)/$(MIDP_MAKEFILE_DIR)
	@echo "<==== end building MIDP classes"

#
# Generate MIDP_PKG_CHECKER using the RomConfProcessor tool
#
$(CVM_DERIVEDROOT)/classes/sun/misc/$(MIDP_PKG_CHECKER):
	@echo "... $@"
	$(AT)$(JAVAC_CMD) -d $(CVM_MISC_TOOLS_CLASSPATH) \
		$(CVM_MISC_TOOLS_SRCDIR)/RomConfProcessor/RomConfProcessor.java
	$(AT)$(CVM_JAVA) -classpath $(CVM_MISC_TOOLS_CLASSPATH) \
		RomConfProcessor -dirs $(ROMGEN_INCLUDE_PATHS) \
		-romfiles $(ROMGEN_CFG_FILES)
	$(AT)mv $(MIDP_PKG_CHECKER) $(CVM_DERIVEDROOT)/classes/sun/misc/

#
# Build the source bundle
#
source_bundle:: $(CVM_BUILD_DEFS_MK)
	$(AT)$(MAKE) $(MAKE_NO_PRINT_DIRECTORY) \
		     JDK_DIR=$(JDK_DIR) TARGET_VM=$(TARGET_VM) \
	             TARGET_CPU=$(TARGET_CPU) USE_DEBUG=$(USE_DEBUG) \
	             USE_SSL=$(USE_SSL) \
	             USE_RESTRICTED_CRYPTO=$(USE_RESTRICTED_CRYPTO) \
	             VERIFY_BUILD_ENV= \
	             CONFIGURATION_OVERRIDE=$(CONFIGURATION_OVERRIDE) \
	             USE_QT_FB=$(USE_QT_FB) USE_DIRECTFB=$(USE_DIRECTFB) \
	             USE_DIRECTDRAW=$(USE_DIRECTDRAW) \
	             USE_SSL=$(USE_SSL) USE_CONFIGURATOR=$(USE_CONFIGURATOR) \
	             USE_VERBOSE_MAKE=$(USE_VERBOSE_MAKE) \
	             PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
		     SOURCE_OUTPUT_DIR=$(SOURCE_OUTPUT_DIR) \
	             $(MIDP_JSROP_USE_FLAGS) \
	             MIDP_CLASSES_ZIP=$(MIDP_CLASSES_ZIP) \
                     MIDP_PRIV_CLASSES_ZIP=$(MIDP_PRIV_CLASSES_ZIP) \
                     MIDP_PUB_CLASSES_ZIP=$(MIDP_PUB_CLASSES_ZIP) \
	             MIDP_SHARED_LIB=$(MIDP_SHARED_LIB) \
	             COMPONENTS_DIR=$(COMPONENTS_DIR) \
	             source_bundle -C $(MIDP_DIR)/$(MIDP_MAKEFILE_DIR) 
	$(AT)$(MAKE) $(MAKE_NO_PRINT_DIRECTORY) \
		     PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             NETWORK_MODULE=$(NETWORK_MODULE) \
	             PCSL_OUTPUT_DIR=$(PCSL_OUTPUT_DIR) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
		     SOURCE_OUTPUT_DIR=$(SOURCE_OUTPUT_DIR) \
		     COMPONENTS_DIR=$(COMPONENTS_DIR) \
	             source_bundle -C $(PCSL_DIR) $(PCSL_MAKE_OPTIONS)

#
# Now build MIDP natives. MIDP natives are linked into CVM binary.
#
ifeq ($(CVM_PRELOAD_LIB), true)
$(MIDP_OBJECTS): $(RUNMIDLET)
else
$(CVM_BINDIR)/$(CVM):: $(RUNMIDLET)
endif

$(RUNMIDLET): force_midp_build
	@echo "====> start building MIDP natives"
	$(AT)$(MAKE) $(MAKE_NO_PRINT_DIRECTORY) \
		     JDK_DIR=$(JDK_DIR) TARGET_VM=$(TARGET_VM) \
	             TARGET_CPU=$(TARGET_CPU) USE_DEBUG=$(USE_DEBUG) \
	             USE_SSL=$(USE_SSL) \
	             USE_RESTRICTED_CRYPTO=$(USE_RESTRICTED_CRYPTO) \
	             VERIFY_BUILD_ENV= \
	             CONFIGURATION_OVERRIDE=$(CONFIGURATION_OVERRIDE) \
	             USE_QT_FB=$(USE_QT_FB) USE_DIRECTFB=$(USE_DIRECTFB) \
	             USE_DIRECTDRAW=$(USE_DIRECTDRAW) \
	             USE_SSL=$(USE_SSL) USE_CONFIGURATOR=$(USE_CONFIGURATOR) \
	             USE_VERBOSE_MAKE=$(USE_VERBOSE_MAKE) \
	             PCSL_PLATFORM=$(PCSL_PLATFORM) \
	             GNU_TOOLS_BINDIR=$(GNU_TOOLS_BINDIR) \
	             MIDP_CLASSES_ZIP=$(MIDP_CLASSES_ZIP) \
                     MIDP_PRIV_CLASSES_ZIP=$(MIDP_PRIV_CLASSES_ZIP) \
                     MIDP_PUB_CLASSES_ZIP=$(MIDP_PUB_CLASSES_ZIP) \
	             MIDP_SHARED_LIB=$(MIDP_SHARED_LIB) \
		     VM_BOOTCLASSPATH=$(VM_BOOTCLASSPATH) \
	             COMPONENTS_DIR=$(COMPONENTS_DIR) \
	             $(MIDP_JSROP_USE_FLAGS) \
	             -C $(MIDP_DIR)/$(MIDP_MAKEFILE_DIR)
ifneq ($(USE_JUMP), true)
  ifeq ($(INCLUDE_SHELL_SCRIPTS), true)
	$(AT)cp $@ $(CVM_BINDIR)
  endif
endif
	@echo "<==== end building MIDP natives"

force_midp_build:

clean::
	rm -rf $(CVM_MIDP_BUILDDIR)

endif
