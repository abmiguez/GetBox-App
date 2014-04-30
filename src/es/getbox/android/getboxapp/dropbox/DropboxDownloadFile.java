package es.getbox.android.getboxapp.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;

public class DropboxDownloadFile extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private String mPath;
    //private long mFileLength;
    private Context mContext;
    private String nFile;


    public DropboxDownloadFile(Context context, DropboxAPI<?> api, String dropboxPath,String newfile) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();
        nFile=newfile;
        mApi = api;
        mPath = dropboxPath;
    }
    
    @Override
    protected void onPreExecute() {
    	Toast.makeText(mContext, "Descargando archivo...", Toast.LENGTH_LONG).show();
    	super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
    	FileOutputStream outputStream = null;
    	try {
    		File file = new File(nFile);
    		outputStream = new FileOutputStream(file);
    		DropboxFileInfo info = mApi.getFile(mPath, null,
    							outputStream,null);
    		Log.i("DropboxSP","The file's rev is: " + info.getMetadata().rev);
        } catch (Exception e) {
        	return false;
        } finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					return false;
				}
			}
		}

		return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    	String[] resul = mPath.split("/");
    	if (result) {
            showToast(resul[resul.length-1]+" descargado con éxito");
        } else {
            showToast("Ha ocurrido un error mientras se descargaba el archivo");
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}