package es.getbox.android.getboxapp.dropbox;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.utils.Item;
import es.getbox.android.getboxapp.utils.SQL;

public class DropboxStorageProvider {

	final static private String TAG = "DropboxSP";
	final static private String APP_KEY = "xu04wh848hkxva0";
	final static private String APP_SECRET = "yuybjmoxofqdgm4";
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

	/* Dropbox preferences name and access key */
	final static private String ACCOUNT_PREFS_NAME = "prefs";
   // final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    //final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private DropboxAPI<AndroidAuthSession> mDBApi = null;
	private Context mContext;
	private int dropboxAccount;
	private SQL sql;
	
	public DropboxAPI<AndroidAuthSession> getApi(){
		return this.mDBApi;
	}
	
	public DropboxStorageProvider(Context context,int dropboxAccount){
		this.mContext=context;
		this.dropboxAccount=dropboxAccount;
		sql=new SQL(context);		
	}
	
	public boolean startAuthentication() {
		AndroidAuthSession session = buildSession();
		if (session == null) {
			Log.e(TAG, "failed to build Dropbox authentication session");
			return false;
		}

		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		if (mDBApi == null) {
			Log.e(TAG,"Failed to construct Dropbox API Object");
			return false;
		}

		Log.i(TAG,"Dropbox API object constructed, starting authentication");

		// if we have already authenticated, we only need to set
		// the token pair
		//Esto esta comentado para permitir multiples cuentas
		String[] keys = getKeys();
		if (keys != null) {
			return true;
		}
		mDBApi.getSession().startAuthentication(mContext);
		return true;
	}
	
	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
					stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
					accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}

	public boolean finishAuthentication() {
		// if we have already authenticated, we only need to check
		// the token pair
		String[] keys = getKeys();
		if (keys != null) {

			// we are authenticated
			return true;
		}

		if (mDBApi == null)
			return false;

		// get session
		AndroidAuthSession session = mDBApi.getSession();
		if (session == null)
			return false;

		if (session.authenticationSuccessful()) {
			try {
				// MANDATORY call to complete auth.
				// Sets the access token on the session
				session.finishAuthentication();

				AccessTokenPair tokens = session.getAccessTokenPair();

				// store access keys
				storeKeys(tokens.key, tokens.secret);

				// done
				return true;
			} catch (IllegalStateException e) {
				Log.e(TAG,"Error authenticating IllegalStateException");
			}
		} else {
			Log.e(TAG,"Failed to authenticate");
		}
		return false;
	}
	
	private void storeKeys(String key, String secret) {	
		this.sql.openDatabase();
		sql.insertDropbox(dropboxAccount, key, secret,getUser());
		this.sql.closeDatabase();
	}

	private void clearKeys() {
		SharedPreferences prefs = mContext.getSharedPreferences(
				ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private String[] getKeys() {
		this.sql.openDatabase();
		ArrayList<String> tokens =sql.getDropboxTokens(dropboxAccount);
		try{
			String key= tokens.get(0);
			String secret= tokens.get(1);
			if (key != null && secret != null) {
				String[] ret = new String[2];
				ret[0] = key;
				ret[1] = secret;
				this.sql.closeDatabase();
				return ret;
			} else { 
				this.sql.closeDatabase();
				return null;
			}
		}catch(Exception e){
			this.sql.closeDatabase();
			return null;
		}
	}
	
	public boolean isLoggedIn() {
		if (mDBApi != null) {
			return mDBApi.getSession().isLinked();
		}
		return false;
	}
	
	public void unlink() {

		if (mDBApi != null) {
			// close session
			mDBApi.getSession().unlink();

			// remove keys
			clearKeys();
		}
	}
	
	public String getUserName(){
		this.sql.openDatabase();
		String account_aux=sql.getDropboxUserName(dropboxAccount);
		this.sql.closeDatabase();
		return account_aux;
	}
	
	public String getUser(){
		DropboxGetUser dgu=new DropboxGetUser(mDBApi);
		try {
			dgu.execute();
			String a=dgu.get();
			return a;
		} catch (InterruptedException e) {
			return "";
		} catch (ExecutionException e) {
			return "";
		}				
	}

	public void getFiles(String directory_path, AsyncTaskCompleteListener<ArrayList<Item>> cb,boolean dialog) {
		DropboxListDirectory ld = new DropboxListDirectory(mContext, mDBApi, directory_path, cb, dialog, dropboxAccount);
    	ld.execute();
	}
	
	public void downloadFile(String file_name, String file_id) {
		DropboxDownloadFile df=new DropboxDownloadFile(mContext, mDBApi,file_id, file_name);
		df.execute();
	}
	
	public void uploadFile(String file_name, String file_id) {
		File file = new File(file_name);
		DropboxUploadFile upload = new DropboxUploadFile(mContext, mDBApi, file_id, file);
        upload.execute();
	}
	
	public void deleteFile(String file_name, String file_id) {

		DropboxDeleteFile delf=new DropboxDeleteFile(mContext,mDBApi,file_id);
		try{	
        	delf.execute();
        	delf.get();
    	}catch(CancellationException ex){
    		Toast error = Toast.makeText(mContext, "Ejecución cancelada", Toast.LENGTH_LONG);
            error.show();
        }catch(ExecutionException ex){
        	Toast error = Toast.makeText(mContext, "Excepción de ejecución", Toast.LENGTH_LONG);
            error.show();
        }catch(InterruptedException ex){
        	Toast error = Toast.makeText(mContext, "Interrumpido mientras se espera por datos", Toast.LENGTH_LONG);
            error.show();
        }
	}
	
	public void deleteFolder(String file_name, String file_id) {

		DropboxDeleteFile delf=new DropboxDeleteFile(mContext,mDBApi,file_id);
		try{	
        	delf.execute();
        	delf.get();
    	}catch(CancellationException ex){
    		Toast error = Toast.makeText(mContext, "Ejecución cancelada", Toast.LENGTH_LONG);
            error.show();
        }catch(ExecutionException ex){
        	Toast error = Toast.makeText(mContext, "Excepción de ejecución", Toast.LENGTH_LONG);
            error.show();
        }catch(InterruptedException ex){
        	Toast error = Toast.makeText(mContext, "Interrumpido mientras se espera por datos", Toast.LENGTH_LONG);
            error.show();
        }
	}
	
	public void uploadFolder(String file_name, String file_id) {
		DropboxUploadFolder folder=new DropboxUploadFolder(mContext,mDBApi,file_name);
    	try{	
        	folder.execute();
        	folder.get();
    	}catch(CancellationException ex){
    		Toast error = Toast.makeText(mContext, "Ejecución cancelada", Toast.LENGTH_LONG);
            error.show();
        }catch(ExecutionException ex){
        	Toast error = Toast.makeText(mContext, "Excepción de ejecución", Toast.LENGTH_LONG);
            error.show();
        }catch(InterruptedException ex){
        	Toast error = Toast.makeText(mContext, "Interrumpido mientras se espera por datos", Toast.LENGTH_LONG);
            error.show();
        }
	}
}