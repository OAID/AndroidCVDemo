package com.openailab.posetrack;

import org.opencv.android.BaseLoaderCallback;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import org.opencv.android.LoaderCallbackInterface;

import org.opencv.android.OpenCVLoader;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    private static final String TAG = "Opencv::Activity";
    private Mat                    mRgba;
    private Mat                    mGray;
    private CameraBridgeViewBase mOpenCvCameraView;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private boolean hasExtSDPermission;
    private boolean hasaudioPermission;
    private int[] ret;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override

        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        hasCameraPermission = permissionsDelegate.hasCameraPermission();
        if (hasCameraPermission) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        } else {
            permissionsDelegate.requestCameraPermission();
        }
        hasExtSDPermission = permissionsDelegate.hasExtSDPermission();
        if (hasExtSDPermission) {
            //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        } else {
            permissionsDelegate.requestExtSDPermission();
        }
        hasaudioPermission = permissionsDelegate.hasaudioPermission();
        if (hasaudioPermission) {
            //mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        } else {
            permissionsDelegate.requestaudioPermission();
        }
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(640,480);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override

    public void onPause()

    {
        super.onPause();
       // mView.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override

    public void onResume()
    {
        super.onResume();
       // mView.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }
    public void onDestroy() {

        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
       trackDsstInit();
       handDetectInit();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();

    }
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

       if(handDetectDet(mRgba.getNativeObjAddr(),true)){
           ret =trackDsstnativeUpdate(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),false);//track update
           Log.d("morrisdebug","x="+ret[0]+",y="+ret[1]+",height="+ret[2]+",width="+ret[3]);
           return mRgba;
       };


      /*  if(firstFrame){

            trackDsstnativeUpdate(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),true);//tracker init
            firstFrame=false;
            return mGray;

        }
       else{
           ret =trackDsstnativeUpdate(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(),false);//track update
          Log.d("morrisdebug","x="+ret[0]+",y="+ret[1]+",height="+ret[2]+",width="+ret[3]);
            return mRgba;
        }*/

         return mRgba;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        }
    }
    public native void trackDsstInit();
    public native int[] trackDsstnativeUpdate(long matAddrGr, long matAddrRgba,boolean firstFrm);
    public native void handDetectInit();
    public native boolean handDetectDet(long matAddrRgba,boolean up);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
}
