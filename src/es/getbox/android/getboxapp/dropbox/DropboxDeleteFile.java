package es.getbox.android.getboxapp.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;

import android.content.Context;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;


public class DropboxDeleteFile extends AsyncTask<Void, Void, Boolean> {

	
    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mApi;
    private String mFilename;

    
    public DropboxDeleteFile(Context context, DropboxAPI<AndroidAuthSession> api,
			String filename) {
    	mContext=context;
		mApi = api;
        mFilename = filename;
	}
    
    @Override
    protected Boolean doInBackground(Void... params) {
    	try {
			mApi.delete(mFilename);
		} catch (DropboxException e) {
			return false;
		}
    	return true;
    }
	
	@Override
    protected void onPostExecute(Boolean result) {
		String[] resul = mFilename.split("/");
		if (!result) {
           showToast("Ha ocurrido un error mientras se eliminaba el archivo");
        }else{
        	showToast(resul[resul.length-1]+" eliminado con éxito");
        }
    }
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}