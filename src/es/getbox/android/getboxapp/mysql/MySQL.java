package es.getbox.android.getboxapp.mysql;

import java.io.File;
import java.sql.*;

import org.apache.commons.lang.ObjectUtils.Null;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.box.boxandroidlibv2.BoxAndroidClient;

public class MySQL {

	static String host      = "185.28.20.223"; 
	static String baseDatos = "u856845316_users";
    static String usuario   = "u856845316_getbx";
    static String password  = "getboxapp";
    static String cadCon	= "jdbc:mysql://"+host+"/"+baseDatos;

    public static Connection con;
    public static Statement st;

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
            System.out.println(e.getMessage());
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
    			String q = "SELECT USERNAME, PASSWORD FROM USERS WHERE USERNAME='"+
            username+"' AND PASS='"+passwrd+"'";
    			
    	    	try {
    	            crearConexion();

    	            ResultSet rs = st.executeQuery( q );
    	            while (rs.next()) {   
    	            }
    	            rs.close();
    	        } catch (Exception e) {
    	            System.out.println(e.getMessage());
        	    	return false;
    	        } finally {
    	            cerrarConexion();
    	            return true; 
    	        }
            }
        };
        task.execute();
        try{
        	return task.get();
        }catch(Exception e){
        	Log.i("MYSQL",e.getMessage());
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
}//class