package es.getbox.android.getboxapp.dropbox;

import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class DropboxGetSpace extends AsyncTask<Void, Void, Long> {

    private DropboxAPI<AndroidAuthSession> mApi;
    private long space;
    
    public DropboxGetSpace(DropboxAPI<AndroidAuthSession> api) {
    	mApi = api;
    }
    
    @Override
    protected void onPreExecute (){}
    
    @Override
    protected Long doInBackground(Void... params) {
    	try{
			Account a=this.mApi.accountInfo();
			this.space= a.quota-a.quotaNormal-a.quotaShared;
		}catch(DropboxException e){
			this.space=0;
		}
        return this.space;
    }
	
	@Override
    protected void onPostExecute(Long result) {}
}