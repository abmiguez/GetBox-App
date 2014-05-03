package es.getbox.android.getboxapp.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.ObjectUtils.Null;
import org.jasypt.util.password.StrongPasswordEncryptor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.utils.Item;
import es.getbox.android.getboxapp.utils.SQL;

public class MySQL {

	static String host      = "193.147.87.47"; 
	static String baseDatos = "getboxapp";
    static String usuario   = "getboxuser";
    static String password  = "getboxpass";
    static String cadCon	= "jdbc:mysql://"+host+"/"+baseDatos;

    public static Connection con;
    public static Statement st;
    
    private Context context;

	private SQL sql;
    
    public MySQL(Context context){
    	this.context=context;
		sql=new SQL(context);
    }
    
    /**
     * Crea la conexion con la BBD
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    public static void crearConexion() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
       
    	Class.forName( "com.mysql.jdbc.Driver").newInstance();

    	try{        	
        	con = DriverManager.getConnection( cadCon, usuario, password);
            st = con.createStatement();
        } catch (Exception e) {
        	
        }
        
    }

    /**
     * Cierra la conexion con la BBDD
     */
    public static void cerrarConexion() {
        try {
            if (st != null) {
                st.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
        }
    }
    
    public boolean deleteUser(String user){
    	final String username=user;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			String q ="DELETE FROM BOXTOKENS WHERE "
    					+ "USERID='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return false;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	    	q ="DELETE FROM DROPBOXTOKENS WHERE "
    					+ "USERID='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return false;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	    	q ="UPDATE USERS SET DEL=1 WHERE "
    					+ "USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return false;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return true;
            }
        };
        if(isOnline()){
        	task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean refreshUser(String user, String password){
    	final String username=user;
    	final String pass=password;
    	
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    			
    			String encryptedPassword = passwordEncryptor.encryptPassword(pass);
    			
    			String q ="UPDATE USERS SET PASSWORD='"+encryptedPassword+"' WHERE "
    					+ "USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return false;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return true;
            }
        };
        if(isOnline()){
        	task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public void login(String user, String pass,AsyncTaskCompleteListener<Boolean> c){
    	final String username=user;
    	final String passwrd=pass;
    	final AsyncTaskCompleteListener<Boolean> callback=c;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    			String q = "SELECT PASSWORD FROM USERS WHERE USERNAME='"+
            username+"' AND DEL=0";
    			boolean a=false;
    			boolean x=false;
    	    	try {
    	            crearConexion();
    	            ResultSet rs = st.executeQuery( q );
    	            if (rs.next()) {   
    	            	if (passwordEncryptor.checkPassword(passwrd, rs.getString("PASSWORD"))) {
    	            		  a=true;
    	            	} else {
    	            	  	  a=false;
    	            	}
    	            }
    	            rs.close();
    	        } catch (Exception e) {
    	            Log.i("ex",""+e.getMessage());
        	    	a=false;
        	    	x=true;        	    	
    	        } finally {
    	        	if(x!=true){
						cerrarConexion();
					}
    	        }
	            return a; 
            }
    		@Override
    	    protected void onPostExecute(Boolean result) { 
    	        callback.onTaskComplete(result);
    	    }
        };
        task.execute();
    }
    
    public void getRefresh(String userid, String user, AsyncTaskCompleteListener<String> c){
    	final String username=user;
    	final String id=userid;
    	final AsyncTaskCompleteListener<String> callback= c;
    	AsyncTask<Null, Integer, String> task = new AsyncTask<Null, Integer, String>() {
    		@Override
            protected String doInBackground(Null... params) {
    			String q ="SELECT REFRESHTOKEN FROM BOXTOKENS WHERE "
    					+ "USERID='"+id+"' AND USERNAME='"+username+"'";
    	    	String result="";
    			try {
    	            crearConexion();
    	            ResultSet rs = st.executeQuery( q );
    	            if (rs.next()) {
    	            	result=rs.getString("REFRESHTOKEN");
    	            }
    	            rs.close();
    	        } catch (Exception e) {
    	            Log.i("ex",""+e.getMessage());
    	            return "";
    	        }
    	        cerrarConexion();
				return result; 
            }
    		

    		@Override
    	    protected void onPostExecute(String result) { 
    	        callback.onTaskComplete(result);
    	    }
        };
        task.execute();
		
    }

    public boolean comprobarContrasena(String user, String pass){
    	final String username=user;
    	final String passwrd=pass;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    			String q = "SELECT PASSWORD FROM USERS WHERE USERNAME='"+
            username+"'";
    			boolean a=false;
    			boolean x=false;
    	    	try {
    	            crearConexion();
    	            ResultSet rs = st.executeQuery( q );
    	            if (rs.next()) {   
    	            	if (passwordEncryptor.checkPassword(passwrd, rs.getString("PASSWORD"))) {
    	            		  a=true;
    	            	} else {
    	            	  	  a=false;
    	            	}
    	            }
    	            rs.close();
    	        } catch (Exception e) {
    	            Log.i("ex",""+e.getMessage());
        	    	a=false;
        	    	x=true;        	    	
    	        } finally {
    	        	if(x!=true){
						cerrarConexion();
					}
    	        }
	            return a; 
            }
        };
        if(isOnline()){
		    task.execute();
		    try{
		    	return task.get();
		    }catch(Exception e){
		    	Log.i("MYSQL",""+e.getMessage());
		    	return false;
		    }
        }else{
        	return false;
        }
    }
    
    public void vincularDropbox(String user,AsyncTaskCompleteListener<Boolean> c){
    	final String username=user;

    	final AsyncTaskCompleteListener<Boolean> callback=c;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			String q = "SELECT * FROM DROPBOXTOKENS  WHERE "
    					+ "USERID='"+username+"'";
    			try {
    	            crearConexion();
    	            ResultSet  rs = st.executeQuery( q );
    	            int i=0;
    	            sql.openDatabase();
    	            while(rs.next()){
    	            	sql.insertDropbox(i, rs.getString("TOKENKEY"), rs.getString("TOKENSECRET"), rs.getString("USERNAME"), rs.getLong("SPACE"));
    	            i++;
    	            }
    	            sql.closeDatabase();
    	            rs.close();
    	        } catch (Exception e) {
    	            Log.i("ex",""+e.getMessage()); 
    	            return false;
    	        } 
    			cerrarConexion();
				return true;
    		}

    		@Override
    	    protected void onPostExecute(Boolean result) { 
    	        callback.onTaskComplete(result);
    	    }
        };
        task.execute();
    }
    
    public void vincularBox(String user, AsyncTaskCompleteListener<Boolean> c){
    	final String username=user;
    	final AsyncTaskCompleteListener<Boolean> callback=c;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			String q = "SELECT * FROM BOXTOKENS WHERE "
    					+ "USERID='"+username+"'";
    			ResultSet rs;
    			try {
    	            crearConexion();
    	            rs = st.executeQuery( q );
    	            int i=0;
    	            sql.openDatabase();
    	            while(rs.next()){
    	            	sql.insertBox(i, rs.getString("ACCESSTOKEN"), rs.getString("USERNAME"), rs.getLong("SPACE"));
    	            	sql.updateBoxToken(i, rs.getString("REFRESHTOKEN"));
    	            i++;
    	            }
    	            sql.closeDatabase();
    	            rs.close();
    	        } catch (Exception e) {
    	            Log.i("ex",""+e.getMessage());  
    	            return false;
    	        } 
    			cerrarConexion();
				return true;
    		}
    		

    		@Override
    	    protected void onPostExecute(Boolean result) { 
    	        callback.onTaskComplete(result);
    	    }
        };
	    task.execute();
    }
    
    public void comprobarDuplicidad(String user, String pass, String email, String name, AsyncTaskCompleteListener<Integer>a){
    	final String username=user;
    	final String passwrd=pass;
    	final String mail=email;
    	final String nameuser=name;
    	final AsyncTaskCompleteListener<Integer>callback=a;
    	
    	AsyncTask<Null, Integer, Integer> task = new AsyncTask<Null, Integer,Integer>() {
    		@Override
            protected Integer doInBackground(Null... params) {
    			ResultSet rs;
    			String q ="SELECT DEL FROM USERS WHERE USERNAME='"+username+"'";
    			try {
    	            crearConexion();
    	            rs = st.executeQuery( q );
    			} catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return 1;
    			}
    			try{
		            if (rs.next()) {  
		            	if(rs.getInt("DEL")==0){
		            		return 2;
		            	}else{
		            		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		        			String encryptedPassword = passwordEncryptor.encryptPassword(passwrd);
		            		q="UPDATE USERS SET "
		            				+ "MAIL='"+mail+"', PASSWORD='"+encryptedPassword+"', NAME='"+nameuser+""
		            						+ "', DEL=0"
		            						+ " WHERE USERNAME='"+username+"'";
		            		st.executeUpdate( q );
		            		return 5;
		            		
		            	}
		            }else{
		            	q="SELECT * FROM USERS WHERE MAIL='"+mail+"'";
		            	rs = st.executeQuery( q );
	            		if (rs.next()) {
	    	            	return 3;
	            		}
		            }
    			} catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return 4;
    			}
	            cerrarConexion();
	            return 0;
            }

    		@Override
    	    protected void onPostExecute(Integer result) { 
    	        callback.onTaskComplete(result);
    	    }
        };
        task.execute();
    }
    
    public boolean registrar(String user, String pass, String email, String name){
    	final String username=user;
    	final String passwrd=pass;
    	final String mail=email;
    	final String nameuser=name;
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
    			String encryptedPassword = passwordEncryptor.encryptPassword(passwrd);
    			String q ="INSERT INTO USERS ( NAME, MAIL, USERNAME, PASSWORD ) VALUES ( '" +
    					nameuser + "', '" +
    					mail + "', '" +
    	    			username + "', '" +
    	    			encryptedPassword + "' )";
    			boolean x=false;
    			boolean a=false;
    	    	try {
    	            crearConexion();
    	            Log.i("a",q);
    	            st.executeUpdate( q );
    	            a=true;
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	a=false;
        	    	x=true;        	    	
    	        } finally {
    	        	if(x!=true){
						cerrarConexion();
					}
    	        }
	            return a; 
            }
        };
        if(isOnline()){
		    task.execute();
		    try{
		    	boolean aux=task.get();
		    	if(aux){
		    		Toast.makeText(context, "El usuario ha sido registrado correctamente",
		    				Toast.LENGTH_LONG).show();
		    	}
		    	return aux;
		    }catch(Exception e){
		    	Log.i("MYSQL",""+e.getMessage());
		    	return false;
		    }
        }else{
        	return false;
        }
    }
    
    public boolean insertDropbox(String tokenkey, String tokensecret, String user, long spaceused,String uName){
    	final String username=user;
    	final String name=uName;
    	final String key=tokenkey;
    	final String secret=tokensecret;
    	final long space=spaceused;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="INSERT INTO DROPBOXTOKENS (TOKENKEY, TOKENSECRET, USERNAME, SPACE, USERID ) VALUES ( '" +
    					key + "', '" +
    					secret + "', '" +
    	    			username + "', '" +
    	    			space + "', '"  +
    	    			name + "' )";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean insertBox(String accesstoken, String user, long spaceused,String uName){
    	final String username=user;
    	final String name=uName;
    	final String access=accesstoken;
    	final long space=spaceused;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="INSERT INTO BOXTOKENS (ACCESSTOKEN, USERNAME, SPACE, USERID ) VALUES ( '" +
    					access + "', '" +
    	    			username + "', '" +
    	    			space + "', '"  +
    	    			name + "' )";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean actualizarBoxSpace(String user, long spaceused, String uName){
    	final String username=user;
    	final String name=uName;
    	final long space=spaceused;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="UPDATE BOXTOKENS SET SPACE='" +
    					space + "' WHERE "
    					+ "USERID='"+name+"' AND USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean actualizarBoxToken(String user, String refreshtoken, String uName){
    	final String username=user;
    	final String name=uName;
    	final String refresh=refreshtoken;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="UPDATE BOXTOKENS SET REFRESHTOKEN='" +
    					refresh + "' WHERE "
    					+ "USERID='"+name+"' AND USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean actualizarDropbox(String user, long spaceused, String uName){
    	final String username=user;
    	final String name=uName;
    	final long space=spaceused;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="UPDATE DROPBOXTOKENS SET SPACE='" +
    					space + "' WHERE "
    					+ "USERID='"+name+"' AND USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean deleteDropbox(String user, String uName){
    	final String username=user;
    	final String name=uName;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="DELETE FROM DROPBOXTOKENS WHERE "
    					+ "USERID='"+name+"' AND USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean deleteBox(String user, String uName){
    	final String username=user;
    	final String name=uName;
    	AsyncTask<Null, Integer, Void> task = new AsyncTask<Null, Integer, Void>() {
    		@Override
            protected Void doInBackground(Null... params) {
    			String q ="DELETE FROM BOXTOKENS WHERE "
    					+ "USERID='"+name+"' AND USERNAME='"+username+"'";
    	    	try {
    	            crearConexion();
    	            st.executeUpdate( q );
    	        } catch (Exception e) {
    	        	Log.i("ex",""+e.getMessage());
        	    	return null;       	    	
    	        } 
    	    	cerrarConexion();
    	    	
    	        return null;
            }
        };
        if(isOnline()){
		    task.execute();
		    return true;
        }else{
        	return false;
        }
    }
    
    public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    	return true;
    	}

    	return false;
    }
}//class