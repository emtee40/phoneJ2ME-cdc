#
# @(#)id_personal.mk	1.22 06/10/10
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

include ../share/id_basis.mk

J2ME_PROFILE_NAME		= Personal Profile
J2ME_PROFILE_SPEC_VERSION	= 1.1

# NOTE: the build/<os>-<cpu>-<device>/id_personal.mk file can be used
# to override the following values, which you will want to do for
# any product that is shipped.
J2ME_PRODUCT_NAME       := $(subst PBP,PP,$(J2ME_PRODUCT_NAME))
J2ME_BUILD_VERSION	= 1.1.1-beta
J2ME_BUILD_STATUS	= beta
