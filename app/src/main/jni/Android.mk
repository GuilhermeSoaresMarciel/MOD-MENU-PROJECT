LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := HP
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -w -fvisibility=hidden
LOCAL_CPPFLAGS := -std=c++17

LOCAL_SRC_FILES := Main.cpp \
    Includes/AND64INLINEHOOK.cpp

include $(BUILD_SHARED_LIBRARY)
