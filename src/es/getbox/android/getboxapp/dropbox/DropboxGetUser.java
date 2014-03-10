package es.getbox.android.getboxapp.dropbox;

import android.os.AsyncTask;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class DropboxGetUser extends AsyncTask<Void, Void, String> {

    private DropboxAPI<AndroidAuthSession> mApi;
    private String account;
    
    public DropboxGetUser(DropboxAPI<AndroidAuthSession> api) {
    	mApi = api;
    }
    
    @Override
    protected void onPreExecute (){}
    
    @Override
    protected String doInBackground(Void... params) {
    	try{
			Account a=this.mApi.accountInfo();
			this.account= a.displayName;
		}catch(DropboxException e){
			this.account="";
		}
        return this.account;
    }
	
	@Override
    protected void onPostExecute(String result) {}
}