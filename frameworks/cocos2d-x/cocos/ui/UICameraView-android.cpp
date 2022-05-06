//
// Created by chentc on 2022/5/6.
//

#include "UICameraView.h"

#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID)
#include <jni.h>
#include <string>
#include "platform/android/jni/JniHelper.h"
#include "ui/UIHelper.h"

static const std::string JAVACLASSNAME ="org.cocos2dx.lib.Cocos2dxCameraHelper";

USING_NS_CC;
//-----------------------------------------------------------------------------------

using namespace cocos2d::experimental::ui;

CameraView::CameraView()
        :_onTop(false)
{
}

CameraView::~CameraView() {
}

void CameraView::openCamera() {
    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, JAVACLASSNAME.c_str(), "openCamera", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);

        t.env->DeleteLocalRef(t.classID);
    }
}

void CameraView::releaseCamera() {
    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, JAVACLASSNAME.c_str(), "releaseCamera", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);

        t.env->DeleteLocalRef(t.classID);
    }
}

void CameraView::switchCamera() {
    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, JAVACLASSNAME.c_str(), "switchCamera", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);

        t.env->DeleteLocalRef(t.classID);
    }
}

void CameraView::setTopLayer(bool top) {
    _onTop = top;
    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, JAVACLASSNAME.c_str(), "setOnTop", "(Z)V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID, top);

        t.env->DeleteLocalRef(t.classID);
    }
}

#endif
