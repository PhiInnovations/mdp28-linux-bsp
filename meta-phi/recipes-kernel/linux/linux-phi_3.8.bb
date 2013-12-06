# Copyright (C) 2012-2013 O.S. Systems Software LTDA.
# Released under the MIT license (see COPYING.MIT for the terms)

include linux-phi.inc

PV = "3.8+git${SRCPV}"
PR = "r1"

# patches-3.8
SRCREV = "8bb495e3f02401ee6f76d1b1d77f3ac9f079e376"

COMPATIBLE_MACHINE = "(mxs|mx3|mx5|mx6)"
