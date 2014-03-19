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
    private String mErrorMsg;

    
    public DropboxDeleteFile(Context context, DropboxAPI<AndroidAuthSession> api,
			String filename) {
    	mContext=context;
		mApi = api;
        mFilename = filename;
	}
    
    @Override
    protected Boolean doInBackground(Void... params) {
    	try {

			// get delete file
			mApi.delete(mFilename);
			return true;
		} catch (DropboxException e) {
			mErrorMsg="Something went wrong while getting metadata.";
		}
    	return false;
    }
	
	@Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
           showToast(mErrorMsg);
        }
    }
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}