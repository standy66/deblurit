LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ImageFilters
LOCAL_SRC_FILES := ImageFilters.cpp
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -ffast-math -O3 -funroll-loops -DANDROID_NDK -fexceptions
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)