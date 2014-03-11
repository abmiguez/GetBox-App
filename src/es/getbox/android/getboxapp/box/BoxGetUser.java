package es.getbox.android.getboxapp.box;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.restclientv2.exceptions.BoxRestException;

import android.os.AsyncTask;

public class BoxGetUser extends AsyncTask<Void, Void, String> {

    private BoxAndroidClient mClient;
    private String account;
    
    public BoxGetUser(BoxAndroidClient client) {
    	mClient = client;
    }
    
    @Override
    protected void onPreExecute (){}
    
    @Override
    protected String doInBackground(Void... params) {
    	account="";
    	try {
			account=mClient.getUsersManager().getCurrentUser(null).getLogin();
		} catch (BoxRestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoxServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthFatalFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return account;
    }
	
	@Override
    protected void onPostExecute(String result) {}
}