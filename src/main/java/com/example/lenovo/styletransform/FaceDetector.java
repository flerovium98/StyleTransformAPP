package com.example.lenovo.styletransform;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FaceDetector {
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize = (int) (780 * 0.2);
    private Bitmap bp_face;

    public void initializeOpenCVDependencies(Resources resources,File cascadeDir) {
        try {
            InputStream is = resources.openRawResource(R.raw.haarcascade_frontalface_alt2);
           // File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }
    }

        public Bitmap detector( Bitmap selectbp) {
        Mat aInputFrame = new Mat();
        Utils.bitmapToMat(selectbp, aInputFrame);
        grayscaleImage=new Mat(580,780,CvType.CV_8UC4);
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
        MatOfRect faces = new MatOfRect();
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();
        if(facesArray.length==0)
            return null;
            else {
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
                Log.d("rrr", facesArray[i].toString());
            }
            Log.d("rrr", aInputFrame.toString());
            Bitmap facebp = Bitmap.createBitmap(selectbp.getWidth(), selectbp.getHeight(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(aInputFrame, facebp);
            return facebp;
        }
    }
}