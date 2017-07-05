package com.example.dvqkrdlswhd.unzipexample;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    private final Context mContext = MainActivity.this;
    private final String mTAG = "MainActivity";
    private final String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String FilePath = SDPath + File.separator + "/example.zip";
    private final String UnzipPath = SDPath + "/UnzipExample/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage(getString(R.string.RationaleMessage))
                .setDeniedMessage(getString(R.string.DeniedMessage))
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(mContext, getString(R.string.AllowedPermissions)  , Toast.LENGTH_SHORT).show();
            new UnZipAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(mContext, getString(R.string.DeniedPermissions) + "\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private class UnZipAsyncTask extends AsyncTask<Void,String,Boolean> {
        private ProgressDialog mProgressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(getString(R.string.Unzipping));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            mProgressDialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isDownloaded = false;
            File dir = new File(UnzipPath);
            // create output directory if it doesn't exist
            if(!dir.exists()) dir.mkdirs();
            FileInputStream fis;
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            try {
                ZipFile zip = new ZipFile(FilePath);
                fis = new FileInputStream(FilePath);
                ZipInputStream zis = new ZipInputStream(fis);
                ZipEntry ze = null;
                long total = 0;
                int progress = 0;
                while((ze = zis.getNextEntry()) != null){
                    String fileName = ze.getName();
                    File newFile = new File(UnzipPath + File.separator + fileName);
                    Log.e(mTAG,"Unzipping to "+newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    if(!fileName.contains(".")){
                        newFile.mkdirs();
                    }else{
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    //close this ZipEntry
                    zis.closeEntry();
                    total ++;
                    int progress_temp = (int) total * 100 / zip.size();
                    publishProgress(""+ progress_temp);
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp;
                    }
                }
                //close last ZipEntry
                zis.closeEntry();
                zis.close();
                fis.close();
                isDownloaded = true;
            } catch (Exception e) {
                Log.e(mTAG, "Exception : " + e.toString());
            }
            return isDownloaded;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(Integer.parseInt(values[0]));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //file delete
        File file = new File(FilePath);
        if(file.exists()){
            file.delete();
        }
        file = new File(UnzipPath);
        if(file.exists()){
            file.delete();
        }
    }
}
