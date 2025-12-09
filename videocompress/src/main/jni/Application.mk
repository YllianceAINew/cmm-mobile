APP_ABI := armeabi-v7a
APP_ABI += arm64-v8a
APP_ABI += x86
APP_OPTIM := release
APP_PLATFORM := android-14
# GCC 4.9 Toolchain - requires NDK r10
NDK_TOOLCHAIN_VERSION = 4.9
# GNU libc++ is the only Android STL which supports C++11 features
# APP_STL := gnustl_static
APP_BUILD_SCRIPT:=$(call my-dir)/Android.mk
APP_MODULES := compress