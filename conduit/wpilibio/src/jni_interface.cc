#include <jni.h>

#include "conduit/api/org_littletonrobotics_conduit_ConduitJni.h"
#include "conduit/wpilibio/include/wpilibio.h"

JNIEXPORT jobject JNICALL
Java_org_littletonrobotics_conduit_ConduitJni_getBuffer(JNIEnv* env,
                                                        jclass clazz) {
  if (akit::conduit::wpilibio::shared_buf == 0) {
    akit::conduit::wpilibio::make_buffer();
  }
  return env->NewDirectByteBuffer(akit::conduit::wpilibio::shared_buf,
                                  akit::conduit::wpilibio::BUF_SIZE);
}

JNIEXPORT void JNICALL Java_org_littletonrobotics_conduit_ConduitJni_capture(
    JNIEnv* env, jclass clazz) {
  akit::conduit::wpilibio::capture_data();
}