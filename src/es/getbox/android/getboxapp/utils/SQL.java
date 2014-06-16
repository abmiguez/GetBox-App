package es.getbox.android.getboxapp.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.widget.Toast;

public class SQL{
	private SQLiteDatabase db;
	private Context context;
    
	//constructor
	public SQL(Context cntxt){
		context=cntxt;		
	}
	
	//abrir base de datos creandola si es necesario
    public void openDatabase() {
        try {        	
            String SDcardPath = Environment.getExternalStorageDirectory().getPath();
            
            String myDbPath = SDcardPath + "/GetBox/DB/" + "getBoxDB.db";
            db = SQLiteDatabase.openDatabase(
        			myDbPath,
    				null,
    				SQLiteDatabase.CREATE_IF_NECESSARY);       	
        	 }
        catch (SQLiteException e) {
        	 Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();        	
        }
    }
    
    //cerrar base de datos
    public void closeDatabase(){
    	db.close();
    }
    
    //crear tablas
    public void createTables() {
    	db.beginTransaction();
		try {
			//dropboxTokens
			db.execSQL("create table dropboxTokens ("
					+ " ID integer PRIMARY KEY autoincrement, " 
			        + " dropboxAccount integer, tokenKey text, tokenSecret text, userName text, space number);  ");
			db.setTransactionSuccessful();
    		
		} catch (SQLException e1) {			
		}
		finally {
    		db.endTransaction();
    	}
		
		db.beginTransaction();
		try {
			//boxTokens
			db.execSQL("create table boxTokens ("
					+ " ID integer PRIMARY KEY autoincrement, " 
			        + " boxAccount integer, accesstoken text, refreshtoken text, userName text, space number);  ");
			db.setTransactionSuccessful();
    		
		} catch (SQLException e1) {			
		}
		finally {
    		db.endTransaction();
    	}
    }
    
    
    //insertar cuenta de dropbox
    public void insertDropbox(int dropboxAccount,String tokenKey, String tokenSecret, String userName, long space){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into dropboxTokens (dropboxAccount,tokenKey, tokenSecret, userName, space) "
    					         + " values ('"+dropboxAccount+"','"+tokenKey+"','"+tokenSecret+"','"+userName+"','"+space+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //actualizar cuenta de dropbox
    public void updateDropboxAccount(int dropboxAccount,int newDropboxAccount){
    	db.beginTransaction();
    	try {
    		db.execSQL( " update dropboxTokens "
    				+ " set dropboxAccount =  '" + newDropboxAccount + "'"
    				+ " where dropboxAccount = '" + dropboxAccount + "' " );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //actualizar espacio de dropbox
    public void updateDropboxSpace(int dropboxAccount,long space){
    	db.beginTransaction();
    	try {
    		db.execSQL( " update dropboxTokens "
    				+ " set space =  '" + space + "'"
    				+ " where dropboxAccount = '" + dropboxAccount + "' " );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //eliminar cuenta de dropbox
    public void deleteDropbox(int dropboxAccount){
    	db.beginTransaction();
    	try {
    		db.execSQL( "delete from dropboxTokens where dropboxAccount='"+dropboxAccount+"';" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //insertar cuenta de box
    public void insertBox(int boxAccount,String accesstoken,String username, long space){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into boxTokens (boxAccount,accesstoken,refreshtoken,userName,space) "
    					         + " values ('"+boxAccount+"','"+accesstoken+"','','"+username+"','"+space+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //actualizar cuenta de box
    public void updateBoxAccount(int boxAccount,int newBoxAccount){
    	db.beginTransaction();
    	try {
    		db.execSQL( " update boxTokens "
    				+ " set boxAccount =  '" + newBoxAccount + "'"
    				+ " where boxAccount = '" + boxAccount + "' " );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //actualizar refresh token de la cuenta de box
    public void updateBoxToken(int boxAccount,String refreshtoken){
    	db.beginTransaction();
    	try {
    		db.execSQL( " update boxTokens "
    				+ " set refreshtoken =  '" + refreshtoken + "'"
    				+ " where boxAccount = '" + boxAccount + "' " );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //actualizar espacio de la cuenta de box
    public void updateBoxSpace(int boxAccount,long space){
    	db.beginTransaction();
    	try {
    		db.execSQL( " update boxTokens "
    				+ " set space =  '" + space + "'"
    				+ " where boxAccount = '" + boxAccount + "' " );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //eliminar cuenta de box
    public void deleteBox(int boxAccount){
    	db.beginTransaction();
    	try {
    		db.execSQL( "delete from boxTokens where boxAccount='"+boxAccount+"';" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    //contar el numero de cuentas de box o dropbox
    public int countAll(String tableName){
    	String mySQL ="select count(*) as Total from "+tableName+"";
    	Cursor c1 = db.rawQuery(mySQL, null);
    	int index = c1.getColumnIndex("Total");
    	c1.moveToNext();
    	return c1.getInt(index);
    }
    
    //eliminar todas las cuentas de box
    public void deleteBox(){
    	db.beginTransaction();
    	try {
    		db.execSQL( "delete from boxTokens;" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}
    	
    }
    //eliminar todas las cuentas
    public void deleteAll(){
    	db.beginTransaction();
    	try {
    		db.execSQL( "delete from boxTokens;" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}
    	db.beginTransaction();
    	try {
    		db.execSQL( "delete from dropboxTokens;" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}
    }
    
    //recuperar los tokens de dropbox de una cuenta
    public ArrayList<String> getDropboxTokens(int dropboxAccount) {
    	ArrayList<String> tokens= new ArrayList<String>();
    	try {
    		String mySQL ="select tokenKey,tokenSecret from dropboxTokens where " +
    				"dropboxAccount="+dropboxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							tokens.add(c.getString(c.getColumnIndex("tokenKey")));
							tokens.add(c.getString(c.getColumnIndex("tokenSecret")));
						}while (c.moveToPrevious());
					}
			}	
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return tokens;
    } 
    
    //recuperar el nombre de usuario de una cuenta de dropbox
    public String getDropboxUserName(int dropboxAccount) {
    	String userName="";
    	try {
    		String mySQL ="select userName from dropboxTokens where " +
    				"dropboxAccount="+dropboxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							userName=c.getString(c.getColumnIndex("userName"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return userName;
    }
    
    //recuperar el access token de una cuenta de box
    public String getBoxAccessTokens(int boxAccount) {
    	String token="";
    	try {
    		String mySQL ="select accesstoken from boxTokens where " +
    				"boxAccount="+boxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							token=c.getString(c.getColumnIndex("accesstoken"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return token;
    }
    
    //recuperar el espacio de una cuenta de dropbox
	public long getDropboxSpace(int dropboxAccount) {
		long space=0;
		try {
			String mySQL ="select space from dropboxTokens where " +
					"dropboxAccount="+dropboxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							space=c.getLong(c.getColumnIndex("space"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
		return space;
	}

	//recuperar el refresh token de una cuenta de box
    public String getBoxRefreshTokens(int boxAccount) {
    	String token="";
    	try {
    		String mySQL ="select refreshtoken from boxTokens where " +
    				"boxAccount="+boxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							token=c.getString(c.getColumnIndex("refreshtoken"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return token;
    }
    
    //recuperar el nombre de usuario de una cuenta de box
    public String getBoxUserName(int boxAccount) {
    	String userName="";
    	try {
    		String mySQL ="select userName from boxTokens where " +
    				"boxAccount="+boxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							userName=c.getString(c.getColumnIndex("userName"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return userName;
    }
    
    //recuperar el espacio disponible de una cuenta de box
    public long getBoxSpace(int boxAccount) {
    	long space=0;
    	try {
    		String mySQL ="select space from boxTokens where " +
    				"boxAccount="+boxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							space=c.getLong(c.getColumnIndex("space"));
						}while (c.moveToPrevious());
					}
			}
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return space;
    }
    
    
}//class