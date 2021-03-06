inherit relocatable

# Cross packages are built indirectly via dependency,
# no need for them to be a direct target of 'world'
EXCLUDE_FROM_WORLD = "1"

CLASSOVERRIDE = "class-cross"
PACKAGES = ""
PACKAGES_DYNAMIC = ""
PACKAGES_DYNAMIC_class-native = ""

HOST_ARCH = "${BUILD_ARCH}"
HOST_VENDOR = "${BUILD_VENDOR}"
HOST_OS = "${BUILD_OS}"
HOST_PREFIX = "${BUILD_PREFIX}"
HOST_CC_ARCH = "${BUILD_CC_ARCH}"
HOST_LD_ARCH = "${BUILD_LD_ARCH}"
HOST_AS_ARCH = "${BUILD_AS_ARCH}"

STAGING_DIR_HOST = "${STAGING_DIR}/${HOST_ARCH}${HOST_VENDOR}-${HOST_OS}"

export PKG_CONFIG_DIR = "${STAGING_DIR}/${TUNE_PKGARCH}${TARGET_VENDOR}-${TARGET_OS}${libdir}/pkgconfig"
export PKG_CONFIG_SYSROOT_DIR = "${STAGING_DIR}/${TUNE_PKGARCH}${TARGET_VENDOR}-${TARGET_OS}"

CPPFLAGS = "${BUILD_CPPFLAGS}"
CFLAGS = "${BUILD_CFLAGS}"
CXXFLAGS = "${BUILD_CFLAGS}"
LDFLAGS = "${BUILD_LDFLAGS}"
LDFLAGS_build-darwin = "-L${STAGING_LIBDIR_NATIVE}"

TOOLCHAIN_OPTIONS = ""

DEPENDS_GETTEXT = "gettext-native"

# Path mangling needed by the cross packaging
# Note that we use := here to ensure that libdir and includedir are
# target paths.
target_base_prefix := "${base_prefix}"
target_prefix := "${prefix}"
target_exec_prefix := "${exec_prefix}"
target_base_libdir = "${target_base_prefix}/${baselib}"
target_libdir = "${target_exec_prefix}/${baselib}"
target_includedir := "${includedir}"

# Overrides for paths
CROSS_TARGET_SYS_DIR = "${MULTIMACH_TARGET_SYS}"
prefix = "${STAGING_DIR_NATIVE}${prefix_native}"
base_prefix = "${STAGING_DIR_NATIVE}"
exec_prefix = "${STAGING_DIR_NATIVE}${prefix_native}"
bindir = "${exec_prefix}/bin/${CROSS_TARGET_SYS_DIR}"
sbindir = "${bindir}"
base_bindir = "${bindir}"
base_sbindir = "${bindir}"
libdir = "${exec_prefix}/lib/${CROSS_TARGET_SYS_DIR}"
libexecdir = "${exec_prefix}/libexec/${CROSS_TARGET_SYS_DIR}"

do_populate_sysroot[sstate-inputdirs] = "${SYSROOT_DESTDIR}/${STAGING_DIR_NATIVE}/"

python cross_virtclass_handler () {
    if not isinstance(e, bb.event.RecipePreFinalise):
        return

    classextend = e.data.getVar('BBCLASSEXTEND', True) or ""
    if "cross" not in classextend:
        return

    pn = e.data.getVar("PN", True)
    if not pn.endswith("-cross"):
        return

    bb.data.setVar("OVERRIDES", e.data.getVar("OVERRIDES", False) + ":virtclass-cross", e.data)
}

addhandler cross_virtclass_handler

do_install () {
	oe_runmake 'DESTDIR=${D}' install
}
