/*
 *简单的安全签名实现，签名算法：拼接传入字符串+md5 key,计算md5的值，作为签名，服务端根据参数用同样的算法计算，对比签名
 *使用方法：
 *1、根据签名的keystore修改KEY_HASHCODE_RELEASE和KEY_HASHCODE_DEBUG，可以通过调用Security.getApkSignHashCode(Context context)获取
 *2、修改MD5_KEY的值，与服务端
 */
#include "security_Security.h"
//release key的hashcode
#define KEY_HASHCODE_RELEASE 1256827912
//debug key的hascode
#define KEY_HASHCODE_DEBUG -1628732400
//参与md5签名的字符串，与服务端保持一致，必须要改
#define MD5_KEY "fqadfews"

jint Java_security_Security_getApkSignHashCode(JNIEnv *env, jclass clazz, jobject context) {
  int signHashCode = getSignHashCode(env, context);
  	(*env)->DeleteLocalRef(env, context);
  	return signHashCode;
 }

//读取应用签名的hashCode
int getSignHashCode(JNIEnv *env, jobject context) {
  	//Context的类
  	jclass context_clazz = (*env)->GetObjectClass(env, context);
  	// 得到 getPackageManager 方法的 ID
  	jmethodID methodID_getPackageManager = (*env)->GetMethodID(env,
  			context_clazz, "getPackageManager",
  			"()Landroid/content/pm/PackageManager;");

  	// 获得PackageManager对象
  	jobject packageManager = (*env)->CallObjectMethod(env, context,
  			methodID_getPackageManager);
  //	// 获得 PackageManager 类
  	jclass pm_clazz = (*env)->GetObjectClass(env, packageManager);
  	// 得到 getPackageInfo 方法的 ID
  	jmethodID methodID_pm = (*env)->GetMethodID(env, pm_clazz, "getPackageInfo",
  			"(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
  //
  //	// 得到 getPackageName 方法的 ID
  	jmethodID methodID_pack = (*env)->GetMethodID(env, context_clazz,
  			"getPackageName", "()Ljava/lang/String;");

  	// 获得当前应用的包名
  	jstring application_package = (*env)->CallObjectMethod(env, context,
  			methodID_pack);
  	const char *str = (*env)->GetStringUTFChars(env, application_package, 0);
  //	__android_log_print(ANDROID_LOG_DEBUG, "JNI", "packageName: %s\n", str);

  	// 获得PackageInfo
  	jobject packageInfo = (*env)->CallObjectMethod(env, packageManager,
  			methodID_pm, application_package, 64);

  	jclass packageinfo_clazz = (*env)->GetObjectClass(env, packageInfo);
  	jfieldID fieldID_signatures = (*env)->GetFieldID(env, packageinfo_clazz,
  			"signatures", "[Landroid/content/pm/Signature;");
  	jobjectArray signature_arr = (jobjectArray)(*env)->GetObjectField(env,
  			packageInfo, fieldID_signatures);
  	//Signature数组中取出第一个元素
  	jobject signature = (*env)->GetObjectArrayElement(env, signature_arr, 0);
  	//读signature的hashcode
  	jclass signature_clazz = (*env)->GetObjectClass(env, signature);
  	jmethodID methodID_hashcode = (*env)->GetMethodID(env, signature_clazz,
  			"hashCode", "()I");
  	jint hashCode = (*env)->CallIntMethod(env, signature, methodID_hashcode);

  	return hashCode;
}
/**
 * 判断当前的应用签名是否合法，防止二次打包
 */
int isLegal(JNIEnv *env, jobject context) {
	int singHashCode = getSignHashCode(env, context);
	return KEY_HASHCODE_RELEASE == singHashCode
			|| KEY_HASHCODE_DEBUG == singHashCode;
}


jstring JNICALL Java_security_Security_generateSignCode(JNIEnv *env, jclass clazz, jstring str, jobject context) {
    //keystore不对
    	if(!isLegal(env,context)){
    		__android_log_print(ANDROID_LOG_DEBUG, "JNI", "app keystore error");
    		return (*env)->NewStringUTF(env, "app keystore error");
    	}

    	const char *cstr = (*env)->GetStringUTFChars(env, str, 0);
    	(*env)->DeleteLocalRef(env, str);
    	unsigned char cstr1[strlen(cstr)];
    	strcpy(cstr1, cstr);

    	//拼接字符串，先算长度
    	int totalLen = strlen(cstr1)+strlen(MD5_KEY);
    	char * finalStr = (char*) malloc(sizeof(char) * (totalLen + 1));

    	memset((void *) finalStr, 0, totalLen + 1);
    	strcat(finalStr, cstr1);
    	strcat(finalStr, MD5_KEY);

    	MD5_CTX md5;
    	MD5Init(&md5);
    	unsigned char decrypt[16];
    	MD5Update(&md5, finalStr, strlen((char *) finalStr));
    	MD5Final(&md5, decrypt);
    	char resultstr[33] = "", temp[3] = "";
    	int i = 0;
    	for (; i < 16; i++) {
    		sprintf(temp, "%02x", decrypt[i]);
    		strcat(resultstr, temp);
    	}
//    	__android_log_print(ANDROID_LOG_DEBUG, "JNI", "input: %s\n output: %s\n",
//    			finalStr, resultstr);
    	free(finalStr);
    	return (*env)->NewStringUTF(env, resultstr);
}