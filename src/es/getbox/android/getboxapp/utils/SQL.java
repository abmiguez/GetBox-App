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
    
	public SQL(Context cntxt){
		context=cntxt;		
	}
		
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
    }//createDatabase
    
    ///////////////////////////////////////////////////////////////////
    public void createTables() {
    	db.beginTransaction();
		try {
			//create table
			db.execSQL("create table dropboxTokens ("
					+ " ID integer PRIMARY KEY autoincrement, " 
			        + " dropboxAccount integer, tokenKey text, tokenSecret text, userName text);  ");
			db.setTransactionSuccessful();
    		
		    //Toast.makeText(context, "Table was created", Toast.LENGTH_LONG).show();
		} catch (SQLException e1) {			
			//Toast.makeText(context, e1.getMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
    		db.endTransaction();
    	}
		
		db.beginTransaction();
		try {
			//create table
			db.execSQL("create table boxTokens ("
					+ " ID integer PRIMARY KEY autoincrement, " 
			        + " boxAccount integer, token text);  ");
			db.setTransactionSuccessful();
    		
		    //Toast.makeText(context, "Table was created", Toast.LENGTH_LONG).show();
		} catch (SQLException e1) {			
			//Toast.makeText(context, e1.getMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
    		db.endTransaction();
    	}
    }
    
    public void makeCacheDirectory(){
    	db.beginTransaction();
		try {
			//create table
			db.execSQL("create table parentDirectory ("
					+ " ID integer PRIMARY KEY autoincrement, " 
			        + " archive text);  ");
			db.setTransactionSuccessful();
    		
		    //Toast.makeText(context, "Table was created", Toast.LENGTH_LONG).show();
		} catch (SQLException e1) {			
			//Toast.makeText(context, e1.getMessage(), Toast.LENGTH_LONG).show();
		}
		finally {
    		db.endTransaction();
    	}
    }
    
    public void insertDirectory(String archive){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into parentDirectory (archive) "
    					         + " values ('"+archive+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void insertDropbox(int dropboxAccount,String tokenKey, String tokenSecret, String userName){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into dropboxTokens (dropboxAccount,tokenKey, tokenSecret, userName) "
    					         + " values ('"+dropboxAccount+"','"+tokenKey+"','"+tokenSecret+"','"+userName+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void updateDropbox(int dropboxAccount,String tokenKey, String tokenSecret, String userName){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into dropboxTokens (dropboxAccount,tokenKey, tokenSecret, userName) "
    					         + " values ('"+dropboxAccount+"','"+tokenKey+"','"+tokenSecret+"','"+userName+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void deleteDropbox(int dropboxAccount,String tokenKey, String tokenSecret, String userName){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into dropboxTokens (dropboxAccount,tokenKey, tokenSecret, userName) "
    					         + " values ('"+dropboxAccount+"','"+tokenKey+"','"+tokenSecret+"','"+userName+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void insertBox(int boxAccount,String token){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into boxTokens (boxAccount,token) "
    					         + " values ('"+boxAccount+"','"+token+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void updateBox(int boxAccount,String token){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into boxTokens (boxAccount,token) "
    					         + " values ('"+boxAccount+"','"+token+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public void deleteBox(int boxAccount,String token){
    	db.beginTransaction();
    	try {
    		db.execSQL( "insert into boxTokens (boxAccount,token) "
    					         + " values ('"+boxAccount+"','"+token+"');" );
    		db.setTransactionSuccessful();
    	}
    	catch (SQLiteException e2) {
    		//report problem 
    		Toast.makeText(context, e2.getMessage(), Toast.LENGTH_LONG).show();
    	}
    	finally {
    		db.endTransaction();
    	}    	
    }
    
    public int countAll(String tableName){
    	//hard-coded SQL-select command with no arguments
    	String mySQL ="select count(*) as Total from "+tableName+"";
    	Cursor c1 = db.rawQuery(mySQL, null);
    	int index = c1.getColumnIndex("Total");
    	//advance to the next record (first rec. if necessary)
    	c1.moveToNext();
    	return c1.getInt(index);
    }
    
    public ArrayList<String> getDropboxTokens(int dropboxAccount) {
    	ArrayList<String> tokens= new ArrayList<String>();
    	try {
    		String mySQL ="select tokenKey,tokenSecret from dropboxTokens where " +
    				"dropboxAccount="+dropboxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			//String bdLogs="";
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							tokens.add(c.getString(c.getColumnIndex("tokenKey")));
							tokens.add(c.getString(c.getColumnIndex("tokenSecret")));
						}while (c.moveToPrevious());
					}
			}	
			//Toast.makeText(context, bdLogs, Toast.LENGTH_LONG).show();
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return tokens;
    } 
    
    public String getDropboxUserName(int dropboxAccount) {
    	String userName="";
    	try {
    		String mySQL ="select userName from dropboxTokens where " +
    				"dropboxAccount="+dropboxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			//String bdLogs="";
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							userName=c.getString(c.getColumnIndex("userName"));
						}while (c.moveToPrevious());
					}
			}
			//Toast.makeText(context, bdLogs, Toast.LENGTH_LONG).show();
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return userName;
    }
    
    public String getBoxTokens(int boxAccount) {
    	String token="";
    	try {
    		String mySQL ="select token from boxTokens where " +
    				"boxAccount="+boxAccount+"";
			Cursor c = db.rawQuery(mySQL, null);
			//String bdLogs="";
			if (c != null ) {
					if  (c.moveToLast()) {
						do {
							token=c.getString(c.getColumnIndex("token"));
						}while (c.moveToPrevious());
					}
			}
			//Toast.makeText(context, bdLogs, Toast.LENGTH_LONG).show();
			c.close();
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}    	
    	return token;
    } 
    
    public void dropTable(String tableName){
    	try {
			db.execSQL( " drop table "+tableName+"; "); 
			Toast.makeText(context, "Table dropped", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context,"dropTable()\n" + e.getMessage(), Toast.LENGTH_LONG).show();
		}  
    }
}//class