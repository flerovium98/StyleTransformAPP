package com.example.lenovo.styletransform;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class AsyncTaskDemo extends AsyncTask<String, Integer, Bitmap> {
    private Context mContext;
    private View mView;
    private Dialog mDialog;
    private Bitmap bp;
    Handler mHandler;
    private AssetManager assetManager;
    private Activity act;
    StyleTranform style = new StyleTranform();
    public AsyncTaskDemo(Activity act,Context context, View view, Bitmap bp,AssetManager assetManager,Handler handler) {
        this.mContext = context;
        this.mView = view;
        this.bp=bp;
        this.assetManager=assetManager;
        this.mHandler = handler;
        this.act=act;
    }
//
    @Override
    protected void onPreExecute() {
        //在ui线程中执行，ps：先弹一个加载框 表示你正在处理
        super.onPreExecute();
        mDialog=new MyDialog(act).showDialog();
        //	Toast.makeText(MainActivity.this, "鎸夐挳鎸変笅", Toast.LENGTH_SHORT).show();
//         View view = act.getLayoutInflater().inflate(R.layout.layout, null);
//         View imgview=(ImageView)view.findViewById(R.id.gif);
//         Glide.with(mContext).load(R.drawable.loadinggif).into((ImageView)imgview);
//          mDialog =
//                 new ProgressDialog(mContext);
//          ((ProgressDialog) mDialog).setView(view);
//          mDialog.setTitle("AI计算中");
//          mDialog.setCancelable(false);
//          mDialog.show();
    }

    @Override
    protected void onPostExecute(Bitmap s) {
        //在ui线程中执行
        super.onPostExecute(s);
       mDialog.dismiss(); //关闭加载框
        ((ImageView)mView).setImageBitmap(s);
        Message msg = mHandler.obtainMessage();
        if(s!=null){
            msg.what = 1;
            msg.obj = s;
        }else{
            msg.what = 2;
        }
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //当进度发生改变时，在ui线程中执行
        super.onProgressUpdate(values);
        //模拟 进度
    //    ((TextView)mView).setText("下载的进度是" + values[0] + "/10");
    }

    @Override
    protected void onCancelled(Bitmap s) {
        //在ui中执行，当任务被取消时执行
        super.onCancelled(s);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        //在子线程中执行 返回result onPostExecute得到回调的结果
        return style.make(bp,assetManager,params[0]);
    }
}
//                AsyncTaskDemo asyncTaskDemo = new AsyncTaskDemo(MainActivity.this,myImageView,selectbp,version_msg,getAssets());
//                asyncTaskDemo.execute("wave");