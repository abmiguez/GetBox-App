package es.getbox.android.getboxapp.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;

import android.content.Context;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;

public class DropboxUploadFolder extends AsyncTask<Void, Void, Boolean> {

	
    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mApi;
    private String mPath;

    
    public DropboxUploadFolder(Context context, DropboxAPI<AndroidAuthSession> api,
			String dropboxPath) {
    	mContext=context;
		mApi = api;
        mPath = dropboxPath;
	}
    
    @Override
    protected Boolean doInBackground(Void... params) {
       try {
            mApi.createFolder(mPath); 

        } catch (Exception e) {
            return false;
        }
        return true;
    }
	
	@Override
    protected void onPostExecute(Boolean result) {
		String[] resul = mPath.split("/");
    	
		if (!result) {
            showToast("Ha ocurrido un error mientras se creaba la carpeta");
        }else{
        	 showToast(resul[resul.length-1]+" creada con éxito");
        }
    }
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}