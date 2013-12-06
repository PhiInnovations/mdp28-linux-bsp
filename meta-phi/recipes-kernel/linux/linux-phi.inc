# Copyright (C) 2012-2013 Phi Innovations Sistemas Eletronicos Ltda
# Released under the MIT license (see COPYING.MIT for the terms)

DESCRIPTION = "Mainline kernel to be used for Freescale"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"
DEPENDS += "lzop-native"
PROVIDES = "virtual/kernel linux-mainline"

inherit kernel

require recipes-kernel/linux/linux-imx.inc
require recipes-kernel/linux/linux-dtb.inc

# Avoid imx-test installation hacks
IMX_TEST_SUPPORT = "n"

SRC_URI = "git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux-stable.git \
           file://defconfig"
LOCALVERSION = "-phi"
