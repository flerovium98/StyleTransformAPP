package com.example.lenovo.styletransform;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;

import com.bumptech.glide.Glide;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;



public class MainActivity extends AppCompatActivity {

    private ImageView myImageView;
    private Bitmap selectbp;
    private long version_msg;
    private TextView textView;
    private View mLayout;
    private Button selectImageBtn;
 //   private Button takeImageBtn;
    private Button personMode;
    private Button viewMode;
    private Button processBtn1;
    private Button processBtn2;
    private Button processBtn3;
    private Button processBtn4;

    private static int  REQUST_ORIGINAL=2;//获取原图信号标识
    private static final int SELECT_FILE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 0;
    private String sdPath;
    private String picPath;
    public class StyleData{
        long version=(long)0;
        Bitmap bp;
        String name;
        public void setName(String name){
            this.name=name;
        }
    }

    StyleData waveData = new StyleData();
    StyleData starryData = new StyleData();
    StyleData feathersData = new StyleData();
    StyleData sumiaoData = new StyleData();

    FaceDetector face = new FaceDetector();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticLoadCVLibraries();
        mLayout = findViewById(R.id.container);
        myImageView = (ImageView) findViewById(R.id.photo);
        myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        textView=(TextView)findViewById(R.id.tv);
        selectImageBtn = (Button) findViewById(R.id.btnSelectPhoto);
  //      takeImageBtn = (Button) findViewById(R.id.btnTakePhoto);
        personMode = (Button) findViewById(R.id.btnPerson);
        viewMode = (Button) findViewById(R.id.btnView);
        sdPath= Environment.getExternalStorageDirectory().getPath();//获取sd卡的路径
        picPath=sdPath+"/"+"temp.png";//保存图片的路径
        waveData.setName("wave");
        starryData.setName("starry");
        feathersData.setName("feathers");
        sumiaoData.setName("sumiao");
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("请选择图片来源")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setItems(new String[]{"打开图库", "拍摄照片"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        boolean result = Utility.checkPermission(MainActivity.this);
                                        if (result)
                                            galleryIntent();
                                        break;
                                    case 1:
                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                                == PackageManager.PERMISSION_GRANTED) {
                                            cameraIntent();
                                        } else {
                                            requestCameraPermission();
                                        }
                                        break;
                                    default:;
                                }
                            }
                        })
                        .show();

            }
        });
//        takeImageBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Check if the Camera permission has been granted
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    // Permission is already available, start camera
//                    cameraIntent();
//                } else {
//                    // Permission is missing and must be requested.
//                    requestCameraPermission();
//                }
//            }
//        });
        personMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectbp==null) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
                }
                else {
                    Bitmap facebp=face.detector(selectbp);
                    if(facebp==null)
                        Toast.makeText(MainActivity.this.getApplicationContext(), "未检测到人脸", Toast.LENGTH_SHORT).show();
                    else
                        myImageView.setImageBitmap(facebp);
                }
            }
        });
        viewMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectbp==null) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
                }
                else {
                }
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myImageView.setImageBitmap(selectbp);
            }
        });
        processBtn1 = (Button) findViewById(R.id.process_btn_feathers);
        processBtn2 = (Button) findViewById(R.id.process_btn_wave);
        processBtn3 = (Button) findViewById(R.id.process_btn_starry);
        processBtn4 = (Button) findViewById(R.id.process_btn_sumiao);
        processBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnStyleTansformResult(feathersData);
            }
        });
        processBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnStyleTansformResult(waveData);
            }
        });
        processBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnStyleTansformResult(starryData);
            }
        });
        processBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnStyleTansformResult(sumiaoData);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy( builder.build() );
        }
    }
   public class MyThread implements Runnable{
        @Override
       public void run(){

        }
   }
    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
                Snackbar.make(mLayout, R.string.camera_access_required,BaseTransientBottomBar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void cameraIntent()
    {
        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//调用手机的相机
        Uri uri = Uri.fromFile(new File(picPath));//根据图片路径生成一个uri
        intent1.putExtra(MediaStore.EXTRA_OUTPUT,uri);//设置相机拍照图片保存的位置
        startActivityForResult(intent1,REQUST_ORIGINAL);//启动并设置返回请求码为原图的
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT); //
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
            else if (requestCode == REQUST_ORIGINAL) {
                onCaptureImageResult();
            }
        }
    }
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            try {
                selectbp = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                version_msg= System.currentTimeMillis();
                myImageView.setImageBitmap(selectbp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void onCaptureImageResult() {
        FileInputStream fileInputStream =null;
        try {
            fileInputStream =new FileInputStream(picPath);//从路径中读取拍照所得图片的原图
            Bitmap b = BitmapFactory.decodeStream(fileInputStream);//把所得文件输入流转为bitmap
            selectbp = ResizeBitmap(b, 800);
            version_msg=System.currentTimeMillis();
            myImageView.setImageBitmap(selectbp);
            b.recycle();//太大记得回收
        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        face.initializeOpenCVDependencies(getResources(),getDir("cascade", Context.MODE_PRIVATE));
    }

    public static Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {//拍照的图片太大，设置格式大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float temp = ((float) height) / ((float) width);
        int newHeight = (int) ((newWidth) * temp);
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }
    public void OnStyleTansformResult(final StyleData styleData)
    {
        if(styleData.version==version_msg)
            myImageView.setImageBitmap(styleData.bp);
        else {
            styleData.version=version_msg;
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            styleData.bp = (Bitmap) msg.obj;
                            myImageView.setImageBitmap(styleData.bp);
                            break;
                        default:
                            break;
                    }
                }
            };
            AsyncTaskDemo asyncTaskDemo= new AsyncTaskDemo(MainActivity.this,MainActivity.this,myImageView,selectbp,getAssets(),handler);
            asyncTaskDemo.execute(styleData.name);
        }
    }
}


