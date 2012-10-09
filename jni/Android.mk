LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := Snap
LOCAL_SRC_FILES := Snap.cpp

include $(BUILD_SHARED_LIBRARY)
