//
// Created by chentc on 2022/5/6.
//

#ifndef __COCOS2D_UI_UICAMERAVIEW_H
#define __COCOS2D_UI_UICAMERAVIEW_H

#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID || CC_TARGET_PLATFORM == CC_PLATFORM_IOS) && !defined(CC_PLATFORM_OS_TVOS)
#include "ui/UIWidget.h"
NS_CC_BEGIN
namespace experimental
{
    namespace ui
    {
        class CameraView : public cocos2d::ui::Widget
        {
        public:
            // Static create method for instancing a CameraView.
            CREATE_FUNC(CameraView);
            void openCamera();
            void releaseCamera();
            void switchCamera();
            void setTopLayer(bool top);
        protected:
            CC_CONSTRUCTOR_ACCESS:
            CameraView();
            virtual ~CameraView();
        private:
            bool _onTop;
        };
    }
}
NS_CC_END

#endif
#endif //__COCOS2D_UI_UICAMERAVIEW_H
