package es.getbox.android.getboxapp.box;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.restclientv2.exceptions.BoxRestException;

import android.os.AsyncTask;

public class BoxGetSpace extends AsyncTask<Void, Void, Long> {

    private BoxAndroidClient mClient;
    private long space;
    
    public BoxGetSpace(BoxAndroidClient client) {
    	mClient = client;
    }
    
    @Override
    protected void onPreExecute (){}
    
    @Override
    protected Long doInBackground(Void... params) {
    	space=0;
    	try {
			Double a=mClient.getUsersManager().getCurrentUser(null).getSpaceUsed();
			Double b=mClient.getUsersManager().getCurrentUser(null).getSpaceAmount();
			Double c=b-a;
			space=Math.round(c);
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
    	return space;
    }
	
	@Override
    protected void onPostExecute(Long result) {}
}