LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := conditionred

LOCAL_CFLAGS := -DANDROID_NDK -DDISABLE_IMPORTGL -g

LOCAL_SRC_FILES := \
    conditionred.cpp \

LOCAL_LDLIBS := -lGLESv1_CM -llog -g

include $(BUILD_SHARED_LIBRARY)
