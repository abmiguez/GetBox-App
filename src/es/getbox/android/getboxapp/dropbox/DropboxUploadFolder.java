package es.getbox.android.getboxapp.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;

import android.content.Context;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;

import es.getbox.android.getboxapp.GetBoxActivity;

public class DropboxUploadFolder extends AsyncTask<Void, Void, Boolean> {

	
    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mApi;
    private String mPath;
    private GetBoxActivity gba;

    
    public DropboxUploadFolder(Context context, DropboxAPI<AndroidAuthSession> api,
			String dropboxPath,GetBoxActivity g) {
    	mContext=context;
		mApi = api;
        mPath = dropboxPath;
        gba=g;
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
        	 gba.actualizarDirectorio();
        	 showToast(resul[resul.length-1]+" creada con �xito");
        }
    }
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}