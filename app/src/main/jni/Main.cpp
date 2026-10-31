#include <jni.h>
#include <pthread.h>
#include <unistd.h>
#include <cstdio>
#include <cstring>

#include "Includes/RESOURCES.h"

#define TARGET_LIB "libil2cpp.so"

static bool IsGhost = false;

bool (*Old_Ghost)(void *instance);
bool Ghost(void *instance)
{
    if (IsGhost)
    {
        return false;
    }
    
    if (!Old_Ghost)
    {
        return true;
    }
    
    return Old_Ghost(instance);
}

void (*Old_FaceTarget)(void *lTarget, void *lImmediate);
void FaceTarget(void *lTarget, void *lImmediate)
{
    return;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_hp_Menu_getFeatureList(JNIEnv *env, jobject)
{
    static const char *features[] = {
        "Category_FUN",
        "1_ButtonOnOff_GHOST",
    };

    const jsize count = sizeof(features) / sizeof(features[0]);
    jobjectArray array = env->NewObjectArray(count, env->FindClass("java/lang/String"), nullptr);

    for (jsize i = 0; i < count; i++)
    {
        jstring item = env->NewStringUTF(features[i]);
        env->SetObjectArrayElement(array, i, item);
        env->DeleteLocalRef(item);
    }

    return array;
}

extern "C" JNIEXPORT void JNICALL
Java_com_hp_Menu_onPreferenceChanged(JNIEnv *, jclass, jint featNum, jboolean enabled)
{
    switch (featNum)
    {
        case 1:
            IsGhost = enabled;
        break;
    }
}

static void *hackThread(void *)
{
    while (!isLibraryLoaded(TARGET_LIB))
        sleep(1);
        
        A64HookFunction ( (void *) getAbsoluteAddress ("libil2cpp.so", 0x3FC4CE8), (void *) Ghost, (void **) Old_Ghost);
        A64HookFunction ( (void *) getAbsoluteAddress ("libil2cpp.so", 0x42FBAE4), (void *) FaceTarget, (void **) Old_FaceTarget);
        
    return nullptr;
}

__attribute__((constructor))
static void libMain()
{
    pthread_t thread;

    if (pthread_create(&thread, nullptr, hackThread, nullptr) == 0)
        pthread_detach(thread);
}
