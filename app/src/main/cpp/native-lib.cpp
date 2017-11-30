#include <jni.h>
#include <opencv2/opencv.hpp>
#include <string>

#include <algorithm>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/imgproc/imgproc.hpp"
#include <GLES/gl.h>
#include <GLES/glext.h>
#include <android/log.h>
#include <pthread.h>
#include <time.h>
#include <Math.h>
#include <opencv/cv.h>
#include "dssttrack/kcftracker.hpp"
#include "handdetect/gesture.hpp"
#include <android/log.h>
#define  LOG_TAG    "JNI_PART"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG, __VA_ARGS__)
using namespace cv;
using namespace std;
extern "C" {
gesture* gs;
KCFTracker mTracker;
Rect res;
bool hdStop=false;
JNIEXPORT void JNICALL
Java_com_openailab_posetrack_MainActivity_trackDsstInit(JNIEnv *env, jobject instance) {

    // TODO
    LOGI("init here trackdsst");
    mTracker= new KCFTracker();

    //init_tracker();
}


JNIEXPORT jintArray JNICALL
Java_com_openailab_posetrack_MainActivity_trackDsstnativeUpdate(JNIEnv *env, jobject instance,
                                                            jlong matAddrGr, jlong matAddrRgba,
                                                            jboolean firstFrm) {

    // TODO
    Mat &mGr = *(Mat *) matAddrGr;
    Mat &mRgb = *(Mat *) matAddrRgba;
    LOGD("rgba color format %d, channel %d",mRgb.depth(),mRgb.channels());
    cvtColor(mRgb,mRgb,COLOR_RGBA2BGR);
    flip(mRgb,mRgb,1);
    //mRgb.convertTo(mRgb,CV_8UC3);
    //resize(mRgb,mRgb,Size(640,480));
    jintArray iarry=env->NewIntArray(4);
    jint buffer[4] ={0,0,0,0};

   // LOGD("native update height %d,width %d",src.rows,src.cols);
    if(firstFrm){

        //mTracker.init(Rect(100,100,50,50),mRgb);

    }else{
       res=mTracker.update(mRgb);
        buffer[0]=res.x;
        buffer[1]=res.y;
        buffer[2]=res.width;
        buffer[3]=res.height;
        rectangle( mRgb, Point( res.x, res.y ), Point( res.x+res.width, res.y+res.height), Scalar( 0, 255, 255 ), 1, 8 );
        LOGD("update x== %d  y==%d",res.x,res.y);
    }
    cvtColor(mRgb,mRgb,COLOR_BGR2RGBA);
    env->SetIntArrayRegion(iarry, 0, 4, buffer);
    return iarry;
}

JNIEXPORT void JNICALL
Java_com_openailab_posetrack_MainActivity_handDetectInit(JNIEnv *env, jobject instance) {

    // TODO
    gs= new gesture();
}
void DrawText(cv::Mat& img,std::string text,int x, int y,cv::Scalar color)
{
    cv::putText(img,text.c_str(),cv::Point(x,y),cv::FONT_HERSHEY_SIMPLEX,0.8,color,2,1);
}
JNIEXPORT jboolean JNICALL
Java_com_openailab_posetrack_MainActivity_handDetectDet(JNIEnv *env, jobject instance,
                                                    jlong matAddrRgba, jboolean up) {

    // TODO
    Mat &mRgb = *(Mat *) matAddrRgba;
    LOGD("rgba color format %d, channel %d",mRgb.depth(),mRgb.channels());
    if(hdStop){
        return (jboolean) true;
    }
    cvtColor(mRgb,mRgb,COLOR_RGBA2BGR);//opencv default work on BGR order without alpha
    flip(mRgb,mRgb,1);//miro mode
    //mRgb.convertTo(mRgb,CV_8UC3);
    //resize(mRgb,mRgb,Size(640,480));
    gs->detect(mRgb);
    if(gs->is_fist())
    {
        Rect Fists = gs->get_fist();
        putText(mRgb,"Fist",cv::Point(Fists.x,Fists.y),FONT_HERSHEY_SIMPLEX,0.8,Scalar(0,0,255),2,1);
    }
    if(gs->is_palm())
    {
        Rect palm = gs->get_palm();
        putText(mRgb,"Palm",Point(palm.x,palm.y),FONT_HERSHEY_SIMPLEX,0.8,Scalar(0,0,255),2,1);
        Point* p = gs->get_palm_center();
        if(p)
        {
            circle(mRgb, *p, 5, Scalar(0,255,0), -1, 8, 0);
            if(gs->is_select_start())
                rectangle(mRgb, *gs->get_select_lt(), *p, cv::Scalar(0,255,0), 1, 8, 0);
        }

    }
    if(gs->is_select_confirmed())
    {
        rectangle(mRgb, *gs->get_select_lt(), *gs->get_select_rb(), cv::Scalar(255,0,0), 5, 8, 0);
        //the second one need to be at right and lower area of first point, or it will crash

        if(gs->e_gesture.rect_select.height>0 && gs->e_gesture.rect_select.width>0){
            mTracker.init(gs->e_gesture.rect_select,mRgb);//begin track here
            hdStop=true;
        }else{
            gs->clear_select();
        }

    }

    char fps_str[256];
    sprintf(fps_str,"%s %d","FPS : ",(int)gs->get_avg_fps());
    DrawText(mRgb,fps_str,10,50,cv::Scalar(0,255,0));
    cvtColor(mRgb,mRgb,COLOR_BGR2RGBA);//change back to android mode
    return (jboolean) false;
   // imshow("Gesture Recognition",mRgb);
}

}
