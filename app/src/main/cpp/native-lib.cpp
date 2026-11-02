#include <jni.h>
#include <string>
#include "core.h"
static ax3d::Core core;
static std::string fromJava(JNIEnv*e,jstring s){const char*p=e->GetStringUTFChars(s,nullptr);std::string v=p?p:"";if(p)e->ReleaseStringUTFChars(s,p);return v;}
static jstring toJava(JNIEnv*e,const std::string&s){return e->NewStringUTF(s.c_str());}
extern "C" JNIEXPORT jstring JNICALL Java_com_ax3d_engine_MainActivity_nativeOpenGame(JNIEnv*e,jobject,jstring p){return toJava(e,core.openGame(fromJava(e,p)));}
extern "C" JNIEXPORT jstring JNICALL Java_com_ax3d_engine_MainActivity_nativeRunFrame(JNIEnv*e,jobject){return toJava(e,core.runFrame());}
extern "C" JNIEXPORT void JNICALL Java_com_ax3d_engine_MainActivity_nativeStop(JNIEnv*,jobject){core.stop();}
extern "C" JNIEXPORT jstring JNICALL Java_com_ax3d_engine_MainActivity_nativeGetBuildInfo(JNIEnv*e,jobject){return toJava(e,"C++17 • ABX v1 • AX3D bytecode VM • CRC32 • sandbox");}
