#include <jni.h>
#include <android/log.h>

/**
 * Throws OutOfMemoryError with a given message.
 */
static void throwOOME(JNIEnv* env, const char* message){
	jclass lClass = env->FindClass("java/lang/OutOfMemoryError");

	if (lClass != NULL)
		env->ThrowNew(lClass, message);

	env->DeleteLocalRef(lClass);
}

/**
 * Throws NullPointerException with a given message.
 */
static void throwNPE(JNIEnv* env, const char* message){
	jclass lClass = env->FindClass("java/lang/NullPointerException");

	if (lClass != NULL)
		env->ThrowNew(lClass, message);

	env->DeleteLocalRef(lClass);
}

/**
 * Throws ArrayIndexOutOfBoundsException with a given message.
 */
static void throwAIOBE(JNIEnv* env, const char* message){
	jclass lClass = env->FindClass("java/lang/ArrayIndexOutOfBoundsException");

	if (lClass != NULL)
		env->ThrowNew(lClass, message);

	env->DeleteLocalRef(lClass);
}
