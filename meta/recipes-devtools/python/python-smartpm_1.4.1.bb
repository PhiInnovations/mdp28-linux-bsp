SUMMARY = "The Smart Package Manager"

DESCRIPTION = "The Smart Package Manager project has the ambitious objective of creating \
smart and portable algorithms for solving adequately the problem of managing software \
upgrades and installation. This tool works in all major distributions and will bring \
notable advantages over native tools currently in use (APT, APT-RPM, YUM, URPMI, etc)."

HOMEPAGE = "http://smartpm.org/"
SECTION = "devel/python"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://LICENSE;md5=393a5ca445f6965873eca0259a17f833"

DEPENDS = "python rpm gettext-native"
PR = "r9"
SRCNAME = "smart"

SRC_URI = "\
          http://launchpad.net/smart/trunk/${PV}/+download/${SRCNAME}-${PV}.tar.bz2 \
          file://smartpm-rpm5-nodig.patch \
          file://smart-rpm-root.patch \
          file://smart-recommends.patch \
          file://smart-rpm-extra-macros.patch \
          file://smart-dflags.patch \
          file://smart-rpm-md-parse.patch \
          file://smart-tmpdir.patch \
          file://smart-metadata-match.patch \
          file://smart-improve-error-reporting.patch \
          file://smart-multilib-fixes.patch \
          file://smart-yaml-error.patch \
          file://smart-channelsdir.patch \
          "

SRC_URI[md5sum] = "573ef32ba177a6b3c4bf7ef04873fcb6"
SRC_URI[sha256sum] = "b1d519ddb43d60f293b065c28870a5d9e8b591cd49e8c68caea48ace91085eba"
S = "${WORKDIR}/${SRCNAME}-${PV}"

# Options - rpm, qt4, gtk
PACKAGECONFIG ??= "rpm"

RPM_RDEP = "${PN}-backend-rpm"
QT_RDEP = "${PN}-interface-qt4"
GTK_RDEP = "${PN}-interface-gtk"

RPM_RDEP_class-native = ""
QT_RDEP_class-native = ""
GTK_RDEP_class-native = ""

PACKAGECONFIG[rpm] = ",,rpm,${RPM_RDEP}"
PACKAGECONFIG[qt4] = ",,qt4-x11,${QT_RDEP}"
PACKAGECONFIG[gtk] = ",,gtk+,${GTK_RDEP}"

inherit distutils

do_install_append() {
   # We don't support the following items
   rm -rf ${D}${libdir}/python*/site-packages/smart/backends/slack
   rm -rf ${D}${libdir}/python*/site-packages/smart/backends/arch
   rm -rf ${D}${libdir}/python*/site-packages/smart/interfaces/qt

   # Temporary, debian support in OE is missing the python module
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/aptchannelsync.py*
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/debdir.py*
   rm -rf ${D}${libdir}/python*/site-packages/smart/backends/deb

   # Disable automatic channel detection
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/detectsys.py*

   # Disable landscape support
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/landscape.py*

   # Disable urpmi channel support
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/urpmichannelsync.py*

   # Disable yum channel support
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/yumchannelsync.py*

   # Disable zypper channel support
   rm -f ${D}${libdir}/python*/site-packages/smart/plugins/zyppchannelsync.py*

   if [ -z "${@base_contains('PACKAGECONFIG', 'rpm', 'rpm', '', d)}" ]; then
      rm -f ${D}${libdir}/python*/site-packages/smart/plugins/rpmdir.py*
      rm -rf ${D}${libdir}/python*/site-packages/smart/backends/rpm
   fi

   if [ -z "${@base_contains('PACKAGECONFIG', 'qt4', 'qt4', '', d)}" ]; then
      rm -rf ${D}${libdir}/python*/site-packages/smart/interfaces/qt4
   fi

   if [ -z "${@base_contains('PACKAGECONFIG', 'gtk+', 'gtk', '', d)}" ]; then
      rm -rf ${D}${libdir}/python*/site-packages/smart/interfaces/gtk
   fi
}

add_native_wrapper() {
        create_wrapper ${D}/${bindir}/smart \
		RPM_USRLIBRPM='`dirname $''realpath`'/${@os.path.relpath(d.getVar('libdir', True), d.getVar('bindir', True))}/rpm \
		RPM_ETCRPM='$'{RPM_ETCRPM-'`dirname $''realpath`'/${@os.path.relpath(d.getVar('sysconfdir', True), d.getVar('bindir', True))}/rpm} \
		RPM_LOCALEDIRRPM='`dirname $''realpath`'/${@os.path.relpath(d.getVar('datadir', True), d.getVar('bindir', True))}/locale
}

do_install_append_class-native() {
        add_native_wrapper
}

do_install_append_class-nativesdk() {
        add_native_wrapper
}

PACKAGES = "${PN}-dev ${PN}-dbg ${PN}-doc smartpm \
            ${@base_contains('PACKAGECONFIG', 'rpm', '${PN}-backend-rpm', '', d)} \
            ${@base_contains('PACKAGECONFIG', 'qt4', '${PN}-interface-qt4', '', d)} \
            ${@base_contains('PACKAGECONFIG', 'gtk', '${PN}-interface-gtk', '', d)} \
            ${PN}-interface-images ${PN}"

RDEPENDS_smartpm = "${PN}"

RDEPENDS_${PN} += "${PN}-backend-rpm python-codecs python-textutils python-xml python-fcntl \
                   python-pickle python-crypt python-compression python-shell \
                   python-resource python-netclient python-threading python-unixadmin python-pprint"
RDEPENDS_${PN}_class-native = ""

RDEPENDS_${PN}-backend-rpm = "python-rpm"

RDEPENDS_${PN}-interface-qt4 = "qt4-x11 ${PN}-interface-images"
RDEPENDS_${PN}-interface-gtk = "gtk+ ${PN}-interface-images"

FILES_smartpm = "${bindir}/smart"

FILES_${PN}-dbg += "${libdir}/python*/site-packages/smart/backends/rpm/.debug"

FILES_${PN}-backend-rpm = "${libdir}/python*/site-packages/smart/backends/rpm"

FILES_${PN}-interface-qt4 = "${libdir}/python*/site-packages/smart/interfaces/qt4"
FILES_${PN}-interface-gtk = "${libdir}/python*/site-packages/smart/interfaces/gtk"
FILES_${PN}-interface-images = "${datadir}/${baselib}/python*/site-packages/smart/interfaces/images"

BBCLASSEXTEND = "native"

