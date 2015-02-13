package com.example.prog8_1;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

/**
 * カメラプレビューを表示する {@link Activity} です。
 */
public class CameraActivity extends Activity{

	ProgressDialog mProgressDialog;
	MyAsyncHttpClient myAsyncHttpClient;

	private boolean mIsTake = false;

	MainActivity  maniactivity = new MainActivity();

	String s = maniactivity.getS();

	/** カメラのハードウェアを操作する {@link Camera} クラスです。 */
	private Camera mCamera;

	/** カメラのプvレビューを表示する {@link SurfaceView} です。 */
	private SurfaceView mView;

	/** カメラのプレビューに重ねる {@link View} です。 */
	private CameraOverlayView mCameraOverlayView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = new SurfaceView(this);
		setContentView(mView);
		mCameraOverlayView = new CameraOverlayView(this);
		addContentView(mCameraOverlayView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mIsTake) {
                        // 撮影中の2度押し禁止用フラグ
                        mIsTake = true;
                        // 画像取得
                        mCamera.takePicture(null, null, mPicJpgListener);
                    }
                }
                return true;
            }
        });
	}

	private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/test";

            // SD カードフォルダを取得
            File file = new File(saveDir);

            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.e("Debug", "Make Dir Error");
                }
            }

            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            // ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                registAndroidDB(imgPath);

            } catch (Exception e) {
                Log.e("Debug", e.getMessage());
            }

            fos = null;

            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCamera.startPreview();

            mIsTake = false;
        }
    };

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		SurfaceHolder holder = mView.getHolder();
		holder.addCallback(surfaceHolderCallback);
	}

	/** カメラのコールバックです。 */
	private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// 生成されたとき
			mCamera = Camera.open(0);
			mCamera.setDisplayOrientation(90);

			// リスナをセット
			mCamera.setFaceDetectionListener(faceDetectionListener);

			// 顔検出の開始
			mCamera.startFaceDetection();
			try {
				// プレビューを表示する
				mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height ) {
			// 変更されたとき

			Camera.Parameters parameters = mCamera.getParameters();
			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
			Camera.Size previewSize = previewSizes.get(0);
			parameters.setPreviewSize(previewSize.width, previewSize.height);
			// width, heightを変更する
			mCamera.setParameters(parameters);
			mCamera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// 破棄されたとき
			mCamera.release();
			mCamera = null;
		}
	};

	/** 顔検出のコールバックです。 */
	private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			Log.d("onFaceDetection", "顔検出数:" + faces.length);
			mCameraOverlayView.setFaces(faces);
		}
	};

	private void registAndroidDB(String path) {
        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = CameraActivity.this.getContentResolver();
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}