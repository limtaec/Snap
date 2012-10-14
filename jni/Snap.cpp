#include <jni.h>
#include "../bin/kr_teamdeer_snap_GestureRecognizeService.h"
#include "../bin/kr_teamdeer_snap_GestureLearningActivity.h"

/*
 * Class:     kr_teamdeer_snap_GestureRecognizeService
 * Method:    NeuralNetRecognize
 * Signature: (Lkr/teamdeer/snap/GestureData;Lkr/teamdeer/snap/GestureElement;)J
 */
JNIEXPORT jlong JNICALL Java_kr_teamdeer_snap_GestureRecognizeService_NeuralNetRecognize
  (JNIEnv * env, jobject jobj, jobject gData, jobject recvData)
{
	return 0;
}

/*
 * Class:     kr_teamdeer_snap_GestureRecognizeService
 * Method:    BoxCollisionRecognize
 * Signature: (Lkr/teamdeer/snap/GestureData;Lkr/teamdeer/snap/GestureElement;)J
 */
JNIEXPORT jlong JNICALL Java_kr_teamdeer_snap_GestureRecognizeService_BoxCollisionRecognize
  (JNIEnv * env, jobject jobj, jobject gData, jobject recvData)
{
	return 0;
}

/*
 * Class:     kr_teamdeer_snap_GestureLearningActivity
 * Method:    NeuralNetLearning
 * Signature: (Lkr/teamdeer/snap/GestureData;Lkr/teamdeer/snap/GestureElement;)J
 */
JNIEXPORT jlong JNICALL Java_kr_teamdeer_snap_GestureLearningActivity_NeuralNetLearning
  (JNIEnv * env, jobject jobj, jobject gData, jobject newData)
{
	return 0;
}

/*
 * Class:     kr_teamdeer_snap_GestureLearningActivity
 * Method:    BoxCollisionLearning
 * Signature: (Lkr/teamdeer/snap/GestureData;Lkr/teamdeer/snap/GestureElement;)J
 */
JNIEXPORT jlong JNICALL Java_kr_teamdeer_snap_GestureLearningActivity_BoxCollisionLearning
  (JNIEnv * env, jobject jobj, jobject gData, jobject recvData)
{
	return 0;
}
