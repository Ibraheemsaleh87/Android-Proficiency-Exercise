package com.example.ibraheem.androidproficiencyexercise;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Ibraheem on 07/03/2018.
 */

public class FragmentManager {

    private JSONArray mJArray;

    private MainActivity mContext;

    private List<ImageFragment> mFragList;

    private LinearLayout mMainScrollLayout;

    public FragmentManager(MainActivity context){
        mContext = context;
        mFragList = new ArrayList<ImageFragment>();
        mMainScrollLayout = mContext.findViewById(R.id.mainScrollLayout);
    }

    public void reset(){

        mMainScrollLayout.removeAllViews();
        mFragList.clear();

        JsonTask jTask = new JsonTask( new OnEventListener<String>() {
            @Override
            public void onSuccess(String result){
                try {
                    JSONObject jsonTxt = new JSONObject(result);
                    mContext.setToolbarTitle(jsonTxt.getString("title"));

                    mJArray =  jsonTxt.getJSONArray("rows");

                    for(int i = 0; i<mJArray.length(); i++) {
                        String tempTitle = mJArray.getJSONObject(i).getString("title");
                        String tempDisc = mJArray.getJSONObject(i).getString("description");

                        Fragment newFragment = ImageFragment.newInstance(tempTitle, tempDisc);
                        FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
                        ft.add(R.id.mainScrollLayout, newFragment).commit();
                        mFragList.add((ImageFragment)newFragment);
                    }

                    loadImage(0);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        jTask.execute();
    }

    public void loadImage(int i){
        final int index = i;
        LoadImageTask imageTask = new LoadImageTask( new OnEventListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap result){
                if(result!=null)
                    mFragList.get(index).setImageBitmap(result);
                if (index != mFragList.size()-1)
                    loadImage(index+1);

            }
        });

        try{
            imageTask.execute( mJArray.getJSONObject(index).getString("imageHref") );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        private ProgressDialog pd;
        private OnEventListener<String> mCallBack;

        public JsonTask(OnEventListener callback) {
            mCallBack = callback;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(mContext);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL("https://dl.dropboxusercontent.com/s/2iodh4vg0eortkl/facts.json");

                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            if (mCallBack != null)
                mCallBack.onSuccess(result);
        }
    }

    public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private OnEventListener<Bitmap> mCallBack;

        public LoadImageTask(OnEventListener callback) {
            mCallBack = callback;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(stream);

                connection.disconnect();
                return myBitmap;

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mCallBack != null)
                mCallBack.onSuccess(result);
        }

    }

    public interface OnEventListener<T> {
        public void onSuccess(T object);
    }
}
