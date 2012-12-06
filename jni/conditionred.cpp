/*
 * native.c
 *
 *  Created on: May 30, 2012
 *      Author: Matt
 */

#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <GLES/gl.h>
#include <cmath>
#include <me_mattsutter_conditionred_RadarRenderer.h>
#include <animation.h>

#ifdef __cplusplus
extern "C" {
#endif

static Animation animation;

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_initAnimation(JNIEnv* env, jclass thiz,
		jint radial_bin_num, jint num_frames){

	animation.init(radial_bin_num, num_frames);
}

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_addFrame(JNIEnv* env, jclass thiz,
		jobject paint_array, jobjectArray rle, jint index){
	animation.addFrame(env, paint_array, rle, index);
	__android_log_print(ANDROID_LOG_INFO, "Native", "Frame added: %d", index);
}

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_DeInit(JNIEnv* env, jclass thiz){
	animation.reset();
}

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_drawImage(JNIEnv* env, jclass thiz,
		jint index){
	if (index >= 0)
		animation.drawFrame(index);
}

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_bufferFrame(JNIEnv* env, jclass thiz,
		jint index){
	animation.bufferFrame(index);
}

JNIEXPORT void JNICALL Java_me_mattsutter_conditionred_RadarRenderer_unbufferFrame(JNIEnv* env, jclass thiz,
		jint index){
	animation.unbufferFrame(index);
}

JNIEXPORT void JNICALL me_mattsutter_conditionred_RadarRenderer_changeAlpha(JNIEnv* env, jclass thiz,
		jshort new_alpha){
	animation.changeAlpha(new_alpha);
}


#ifdef __cplusplus
}
#endif
