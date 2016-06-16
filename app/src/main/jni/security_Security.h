/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <stdio.h>
#include "md5.h"
/* Header for class security_Security */

#ifndef _Included_security_Security
#define _Included_security_Security
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     security_Security
 * Method:    getApkSignHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_security_Security_getApkSignHashCode
  (JNIEnv *, jclass, jobject);


/*
 * Class:     security_Security
 * Method:    generateSignCode
 * Signature: (Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_security_Security_generateSignCode
  (JNIEnv *, jclass, jstring, jobject);

#ifdef __cplusplus
}
#endif
#endif