package es.getbox.android.getboxapp.mysql;

import java.io.File;
import java.sql.*;

import org.apache.commons.lang.ObjectUtils.Null;
import org.jasypt.util.password.StrongPasswordEncryptor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class MySQL {

	static String host      = "192.168.1.135"; 
	static String baseDatos = "getboxapp";
    static String usuario   = "getboxuser";
    static String password  = "getboxpass";
    static String cadCon	= "jdbc:mysql://"+host+"/"+baseDatos;

    public static Connection con;
    public static Statement st;
    
    private static Context context;

    
    public MySQL(Context context){
    	this.context=context;
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
    
    public boolean login(String user, String pass){
    	final String username=user;
    	final String passwrd=pass;
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
    	            Log.i("ex",e.getMessage());
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
		    	Log.i("MYSQL",e.getMessage());
		    	return false;
		    }
        }else{
        	return false;
        }
    }
    
    public int comprobarDuplicidad(String user, String pass, String email, String name){
    	final String username=user;
    	final String passwrd=pass;
    	final String mail=email;
    	final String nameuser=name;
    	AsyncTask<Null, Integer, Integer> task = new AsyncTask<Null, Integer,Integer>() {
    		@Override
            protected Integer doInBackground(Null... params) {
    			int option=0;
    			ResultSet rs;
    			String q ="SELECT DEL FROM USERS WHERE USERNAME='"+username+"'";
    			try {
    	            crearConexion();
    	            rs = st.executeQuery( q );
    			} catch (Exception e) {
    	        	Log.i("ex",e.getMessage());
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
    	        	Log.i("ex",e.getMessage());
        	    	return 4;
    			}
	            cerrarConexion();
	            return 0;
            }
        };
        if(isOnline()){
		    task.execute();
		    try{
		    	int aux=task.get();
		    	switch (aux){
			    	case 0:
			    		return 0;
			    	case 2:
			    		Toast.makeText(context, "El nombre de usuario no está disponible", Toast.LENGTH_LONG).show();
	            		return 1;
			    	case 3:
			    		Toast.makeText(context, "El email introducido ya está siendo utilizado en otra cuenta", Toast.LENGTH_LONG).show();
    	            	return 2;
			    	case 5:
			    		Toast.makeText(context, "El usuario ha sido registrado correctamente",
			    				Toast.LENGTH_LONG).show();
			    		return 3;
    	            default:    	            	
    	            	return 4;
		    	}
		    }catch(Exception e){
		    	Log.i("MYSQL",e.getMessage());
		    	return 4;
		    }
        }else{
        	return 4;
        }
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
    	        	Log.i("ex",e.getMessage());
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
		    	Log.i("MYSQL",e.getMessage());
		    	return false;
		    }
        }else{
        	return false;
        }
    }
    /*
    public static List<Familia> getDatosFamilia(){
		
		List<Familia> f = new ArrayList<Familia>();
		String q = "SELECT id, nombre FROM familia ORDER BY orden";
		
        try {
            crearConexion();

            ResultSet rs = st.executeQuery( q );
            while (rs.next()) {
                f.add(new Familia( rs.getInt("id"),
                                   rs.getString("nombre") ) );    
            }
            rs.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            cerrarConexion();
        }
        return f;
		
	}

	// Actualiza datos de la tabla familia en la BD.
	// Recibe como parámetro un objeto Familia.
	public static void updateFamilia( Familia f ){
		
		String q;
		
		if ( f.getId() == 0 ) {
			// Si el Id de la Familia está a cero es una nueva fila.
			q = "INSERT INTO familia ( id, nombre ) VALUES ( " +
				f.getId() + ", " +
				f.getNombre() + ")";			
		} else {
			// Si el id es distinto de cero es una actualización del registro.
			q = "UPDATE familia SET " +
				"nombre = " + f.getNombre() +
				"WHERE id = " + f.getId();			
		}
		
        try {
            crearConexion();

            st.executeUpdate( q );
            //Cuando es una nueva fila, averiguo el ultimo Id adsignado por la BD y lo pongo en el objeto Familia. 
            if ( f.getId() == 0 ){
                ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()");
                rs.first();
                f.setId( rs.getInt(1) );
                rs.close();            	
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            cerrarConexion();
        }
        return;
	}

	// Borra datos de la tabla familia en la BD.
	// Recibe como parámetro un objeto Familia.
	public static void deleteFamilia( Familia f ){
		
		String q;
		q = "DELETE FROM familia " +
			"WHERE id = " + f.getId();			
		
        try {
            crearConexion();
            st.executeUpdate( q );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            cerrarConexion();
        }
        return;
	}
*/
    
    public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    	return true;
    	}

    	return false;
    }
}//class