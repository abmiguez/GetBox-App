package es.getbox.android.getboxapp.dropbox;

import java.util.ArrayList;
import java.util.List;
import android.os.AsyncTask;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import android.app.ProgressDialog;
//import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.utils.Item;


public class DropboxListDirectory extends AsyncTask<Void, Void, ArrayList<Item>> {

	private final ProgressDialog dialog;

    private Context mContext;
    private DropboxAPI<AndroidAuthSession> mApi;
    private String mPath;
    private ArrayList<Item> folderName;
    private String mErrorMsg;
    private boolean errores;
    private AsyncTaskCompleteListener<ArrayList<Item>>callback;
    private boolean isDialog;
    private int dropboxAccount;
    private DropboxStorageProvider dsp;
    
    public DropboxListDirectory(Context context, DropboxAPI<AndroidAuthSession> api,
			String dropboxPath, AsyncTaskCompleteListener<ArrayList<Item>> cb, boolean isD, int dropboxAccount,DropboxStorageProvider dsp) {
    	mContext=context;
		mApi = api;
        mPath = dropboxPath;
        folderName=new ArrayList<Item>();
        this.errores=true;
        isDialog=isD;
        dialog=new ProgressDialog(mContext);
        this.callback = cb;
        this.dropboxAccount=dropboxAccount;
        this.dsp=dsp;
    }
    
    @Override
    protected void onPreExecute (){
    	if(isDialog){
    		dialog.setMessage("Actualizando..."); 
    		dialog.show();
    	}
    }
    
    @Override
    protected ArrayList<Item> doInBackground(Void... params) {
    	ArrayList<Item> archives= new ArrayList<Item>();
    	try {

            Entry dirent = mApi.metadata(mPath, 0, null, true, null);    
    		
            if (!dirent.isDir || dirent.contents == null) {
                //mErrorMsg = "Directorio vacío";
                this.errores=false;
            }
            List<Entry> contents1 = dirent.contents;
		    if (contents1 != null) {
		    	folderName.clear();
		    	for (int i = 0; i < contents1.size(); i++) {
		    		Entry e = contents1.get(i);
		    		String a = e.fileName();  
		    		if(String.valueOf(e.isDir).equalsIgnoreCase("true")){
		           		folderName.add(new Item(a,mPath+a,"dropbox",dropboxAccount));
		           	}else{
		           		archives.add(new Item(a,mPath+a,"dropbox",dropboxAccount));
		           	}		    		
		    	}
		    }            
		    for(int i=0; i<archives.size(); i++){
		    	folderName.add(archives.get(i));
		    }
            this.errores = true;
            this.dsp.setNoLocation(true);
            return folderName;

        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            	this.dsp.setNoLocation(false);
            	this.errores=true;
            	return folderName;
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Error de red. Vuelva a intentarlo.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Error de Dropbox. Vuelva a intentarlo.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Error desconocido. Vuelva a intentarlo.";
        }
        this.errores = false;
        return folderName;
    }
	
	@Override
    protected void onPostExecute(ArrayList<Item> result) {
		if (!this.errores) {
            showToast(mErrorMsg);
			//Log.e("Errores dspList", mErrorMsg);
        }
        
        if (dialog.isShowing()&&isDialog) { 
        	dialog.dismiss(); 
        }  
        callback.onTaskComplete(result);
    }
	
	private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}