package com.example.lenovo.styletransform;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.Iterator;

public class StyleTranform {
    public static final String FEATHERS = "feathers";
    public static final String STARRY = "starry";
    public static final String WAVE = "wave";
    public static final String SUMIAO = "sumiao";
    public static final String FEATHERS_ASSET_PB = "file:///android_asset/feathers.pb";
    public static final String STARRY_ASSET_PB = "file:///android_asset/starry.pb";
    public static final String WAVE_ASSET_PB = "file:///android_asset/wave.pb";
    public static final String SUMIAO_ASSET_PB = "file:///android_asset/sumiao.pb";
    private static final String INPUT_NODE = "padsss:0";
    private static final String OUTPUT_NODE = "squeezesss:0";
    private TensorFlowInferenceInterface inferenceInterface;
    private float[] floatValues;
    private float[] floatValuess;
    private int[] intValues;
    private int[] intValuess;
    private int mInWidth = 800;
    private int mInHeight = 600;
    private int mOutWidth = 780;
    private int mOutHeight = 580;
    private Bitmap bp_result;
    public Bitmap make(Bitmap bitmap,AssetManager assetManager,String type) {
        // 创建一个TensorFlow在Java下的实例，这里只要将 神经网络文件 放到assets中，然后让Tensorflow自动读取就好了
        if(type.equals(FEATHERS))
                inferenceInterface = new TensorFlowInferenceInterface(assetManager, FEATHERS_ASSET_PB);
        else if(type.equals(STARRY))
                inferenceInterface = new TensorFlowInferenceInterface(assetManager, STARRY_ASSET_PB);
        else if(type.equals(SUMIAO))
                inferenceInterface = new TensorFlowInferenceInterface(assetManager, SUMIAO_ASSET_PB);
        else if(type.equals(WAVE))
                inferenceInterface = new TensorFlowInferenceInterface(assetManager, WAVE_ASSET_PB);
        int width=bitmap.getWidth();
        int height=bitmap.getHeight();
        Size size=new Size(mInWidth,mInHeight);
        Mat dst = new Mat();
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.resize(src,dst,size);
        Bitmap bp_temp = Bitmap.createBitmap(mInWidth,mInHeight, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, bp_temp);

        // 因为 神经网络需要传入float数组，所以需要将bitmap中所有的像素的真实值传入到 float数组中
        floatValues = new float[mInHeight * mInWidth * 3];
        floatValuess = new float[mOutHeight * mOutWidth * 3];
        intValues = new int[mInHeight * mInWidth];
        intValuess = new int[mOutHeight * mOutWidth];

        // 将 mInHeight * mInWidth 这么大的图片像素传入到intValues中
        bp_temp.getPixels(intValues, 0, bp_temp.getWidth(), 0, 0, mInWidth, mInHeight);

        // intValues中的每一个值都是一个 将 A R G B四个通道整合之后的值，所以这里需要将四个通道分离，然后将其中的R G B三个通道写入到floatValues中
        // 所以floatValues的大小是 mInHeight * mInWidth * 3
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF);
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF);
            floatValues[i * 3 + 2] = (val & 0xFF);
        }
         // 获取一下 神经网络的每个节点 然后输出一下名字，有助于确认输入和输出节点
//        Iterator<Operation> operationIterator = inferenceInterface.graph().operations();
//        while (operationIterator.hasNext()) {
//            Operation operation = operationIterator.next();
//            Log.d("MainActivitysss", operation.name());
//        }
        // 将刚刚获取到的floatValues数组当做输入节点输入到Tensorflow实例中，参数依次是 节点名字，数据，dims：表示将floatValues转换成 1 * mInHeight * mInWidth * 3的张量
        inferenceInterface.feed(INPUT_NODE, floatValues, 1, mInHeight, mInWidth, 3);
        // 输入输出节点的名字，并运行，这里是阻塞当前线程的，所以在正式项目中需要在其他线程运行，这里我就简单一点在主线程运行。
        inferenceInterface.run(new String[]{OUTPUT_NODE}, true);
        // 运行完毕之后，取出经过神经网络处理的数据
        inferenceInterface.fetch(OUTPUT_NODE, floatValuess);

        // 将floatValuess 整合成Bitmap中需要的像素值
        for (int i = 0; i < intValuess.length; ++i) {
            intValuess[i] =
                    0xFF000000
                            | (((int) (floatValuess[i * 3])) << 16)
                            | (((int) (floatValuess[i * 3 + 1])) << 8)
                            | ((int) (floatValuess[i * 3 + 2]));
        }

        // 将构建好的像素值 存回Bitmap中
        Bitmap bp_result = Bitmap.createBitmap(mOutWidth, mOutHeight, Bitmap.Config.ARGB_8888);
        bp_result.setPixels(intValuess, 0, bp_result.getWidth(), 0, 0, bp_result.getWidth(), bp_result.getHeight());

        Size size_output=new Size(width,height);
        Mat dst_output = new Mat();
        Mat src_ouput = new Mat();
        Utils.bitmapToMat(bp_result, src_ouput);
        Imgproc.resize(src_ouput,dst_output,size_output);


        bp_result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst_output, bp_result);
        return bp_result;

    }
}
