package es.getbox.android.getboxapp.box;

import java.util.ArrayList;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.dao.BoxAndroidCollection;
import com.box.boxjavalibv2.dao.BoxCollection;
import com.box.boxjavalibv2.dao.BoxItem;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.restclientv2.exceptions.BoxSDKException;

import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.mysql.MySQL;
import es.getbox.android.getboxapp.utils.Item;
import es.getbox.android.getboxapp.utils.SQL;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class BoxListDirectory extends AsyncTask<Void, Void, ArrayList<Item>> {

	private BoxAndroidClient mClient;
    private AsyncTaskCompleteListener<ArrayList<Item>>callback;
    private String mPath;
    private int boxAccount;
    private Context context;
    private String username;
    
	
	public BoxListDirectory(String path,Context c, String u, AsyncTaskCompleteListener<ArrayList<Item>> cb, BoxAndroidClient client, int boxAccount){
		this.mClient=client;
		this.callback = cb;
		this.mPath=path;
		this.boxAccount=boxAccount;
		this.context=c;
		this.username=u;
    }
	
    @Override
    protected void onPostExecute(ArrayList<Item> result) {
        super.onPostExecute(result);
        callback.onTaskComplete(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ArrayList<Item> doInBackground(Void... params) {
    	BoxCollection files=new BoxAndroidCollection();
    	ArrayList<Item> result=new ArrayList<Item>();
    	try {
    		files=mClient.getFoldersManager().getFolderItems(mPath, null);

        	ArrayList<BoxTypedObject> boxObjects = files.getEntries();
        	if(boxObjects.size()>0){
    	    	Item item=new Item();
    	    	for(int i=0; i<=boxObjects.size()-1;i++){
    	    		BoxTypedObject bto= boxObjects.get(i);
    	    		BoxItem bi=(BoxItem) bto;
    	    		item=new Item(bi.getName(),bi.getId(),"box",boxAccount);
    	    		result.add(item);
    	    	}    	
        	}
        	if(result.size()==0){
        		Item item=new Item("","","box",boxAccount);
	    		result.add(item);
        	}
        }
        catch (BoxSDKException e) {
        	Log.i("Box","boxexcep"+e.getMessage());
        	Item item=new Item("fail","refresh","box",boxAccount);
    		result.add(item);
        }catch(Exception e){
        	Log.i("a","ex");
        }
    	return result;
    }
}
