package es.getbox.android.getboxapp.abstractionlayer;

import java.io.File;
import java.util.ArrayList;

import com.box.boxandroidlibv2.activities.OAuthActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import es.getbox.android.getboxapp.GetBoxActivity;
import es.getbox.android.getboxapp.box.BoxStorageProvider;
import es.getbox.android.getboxapp.dropbox.DropboxStorageProvider;
import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.mysql.MySQL;
import es.getbox.android.getboxapp.utils.Item;
import es.getbox.android.getboxapp.utils.SQL;

public class AbstractionLayer{
	
	//Objetos de las APIs
	private DropboxStorageProvider dsp_aux;
	private BoxStorageProvider bsp_aux;
	private ArrayList<DropboxStorageProvider> dsp;
	private ArrayList<BoxStorageProvider> bsp;	
	
	//Manejar cuentas
	private int newBoxAccount;
	private int newDropboxAccount;
	private ArrayList<String> dbAccounts;
	private ArrayList<String> bAccounts;
	private boolean newDBAccount;
	private boolean newBAccount;
	private int accountsCounter;
	private int accountsUsed;
	public final static int AUTH_BOX_REQUEST = 2;
	
	//Plugins
	private StoragePolicyPlugin spp;
	
	//Rutas para la navegación entre los directorios
    private ArrayList<String> directoriosDropbox;
    private int posicionActual;
   
	
	//SQL
	private SQL sql;
	private MySQL mySql;
	
	//Contexto
	private Context context;
	private GetBoxActivity gba;
	
	//Callback listeners
	private int sync;
	
	//Constructor
	public AbstractionLayer(Context context, GetBoxActivity gba, AsyncTaskCompleteListener<ArrayList<Item>> c){
		this.context=context;
		mySql=new MySQL(context);
		
		this.gba=gba;
		sql=new SQL(this.context);
		sql.openDatabase();	
		sql.createTables();
		sync=0;
		newDropboxAccount=sql.countAll("dropboxTokens");
		newBoxAccount=sql.countAll("boxTokens");
		dbAccounts=new ArrayList<String>();
		bAccounts=new ArrayList<String>();
		accountsCounter=0;
		accountsUsed=0;
		
		dsp=new ArrayList<DropboxStorageProvider>();
		bsp=new ArrayList<BoxStorageProvider>();
		spp=new StoragePolicyPlugin();
		
		directoriosDropbox=new ArrayList<String>();
		directoriosDropbox.add("/");
		posicionActual=0;
		sql.closeDatabase();	
	}
	
	//true si no hay cuentas false si hay cuentas vinculadas
	public boolean zero(){
		if(newBoxAccount==0 && newDropboxAccount==0){
			return true;
		}else{
			return false;
		}
	}
	
	//obtiene el espacio disponible total de todas las cuentas
	public long getSpaceAvaliable(){
		long space=0;
		//todas las de dropbox
		this.sql.openDatabase();
		for(int i=0;i<newDropboxAccount;i++){
			space=space+dsp.get(i).getSpaceUsed();
    	}
		//todas las de box
		this.sql.closeDatabase();
		this.sql.openDatabase();
		for(int i=0;i<newBoxAccount;i++){
			space=space+bsp.get(i).getSpaceUsed();
        }
		this.sql.closeDatabase();
		return space;
	}
	
	//sube un archivo dependiendo de una politica de subida
	public Item uploadFile(String file_name){
		//elige la cuenta
		int[] cuenta=spp.storagePolicy(newDropboxAccount, newBoxAccount, dsp, bsp);
        Item item;
        File aux=new File(file_name);        
        //si es de dropbox
        if(cuenta[0]==0){
        	String rutaDropbox="";
    		for(int i=0;i<directoriosDropbox.size();i++){
            	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
            }
        	dsp.get(cuenta[1]).uploadFile(file_name, rutaDropbox);
        	dsp.get(cuenta[1]).setSpace();
        	item=new Item(aux.getName(),rutaDropbox+file_name,"dropbox",cuenta[1]);
        //si es de box
        }else{
        	bsp.get(cuenta[1]).uploadFile(file_name,
        			bsp.get(cuenta[1]).getDirectory(posicionActual));
        	bsp.get(cuenta[1]).setSpace();
        	item=new Item(aux.getName(),bsp.get(cuenta[1]).getDirectory(posicionActual)+file_name,"box",cuenta[1]);
        }
		return item;
	}
	
	//crea una nueva carpeta
	public Item uploadFolder(String file_name){
		String rutaDropbox="";
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }
		//elige a donde subir
		int[] cuenta=spp.storagePolicy(newDropboxAccount, newBoxAccount, dsp, bsp);
        Item item;
        File aux=new File(file_name);        
        //si es de dropbox
        if(cuenta[0]==0){
        	dsp.get(cuenta[1]).uploadFolder(rutaDropbox+file_name, rutaDropbox);
        	item=new Item(aux.getName(),rutaDropbox+file_name,"dropbox",cuenta[1]);
        //si es de box
        }else{
        	bsp.get(cuenta[1]).uploadFolder(file_name, 
        			bsp.get(cuenta[1]).getDirectory(posicionActual));
        	item=new Item(aux.getName(),bsp.get(cuenta[1]).getDirectory(posicionActual)+file_name,"box",cuenta[1]);
        }
        return item;
	}
	
	//reinicia los contadores que activan/desactivan el lisview de archivos
	public void restartWidget(){
		accountsCounter=0;
		accountsUsed=0; 
	}
	
	//activa o desactiva el listview de archivos dependiendo de si listó o no todas las cuentas
	public boolean enableWidget(){
		accountsCounter++;   
		if(accountsCounter==accountsUsed){
			accountsCounter=0;
			accountsUsed=0;
			return true;
		}else{
			return false;
		}
	}
	
	//activa o desactiva el boton atras dependiendo de si listó o no todas las cuentas
	public boolean enableBack(){
		if(posicionActual==0){
			return true;
		}		
		if(accountsCounter==accountsUsed){
			return true;
		}else{
			return false;
		}
	}
	
	//retornar las posicion actual del array de rutas
	public int getPosicionActual() {
		return posicionActual;
	}

	//retorna la ruta actual
	public String getRoute() {
		String route="";
		for(int i=1;i<=posicionActual;i++){
			route=route+directoriosDropbox.get(i);
		}
		return route.substring(0, route.length()-1);
	}
	
	//vuelve a autenticar una cuenta de dropbox que daba error
	public void boxRefresh(int account){
		bsp.get(account).autenticate();
	}
	
	//descargar un archivo
	public void download(Item item){
		//dependiendo de si es de box o dropbox
		if(item.getName().indexOf(".")>0){
			//dropbox
			if(item.getLocation()=="dropbox"){
				dsp.get(item.getAccount()).downloadFile(item.getName(),item.getId());
			//box
			}else{
				if(item.getLocation()=="box"){
					bsp.get(item.getAccount()).downloadFile(item.getName(),item.getId());
				}
			}
		}else{
			//si intentamos descargar un directorio
			Toast.makeText(context, "No se pueden descargar directorios", Toast.LENGTH_LONG).show();
		}
	}
	
	//eliminar un archivo o carpeta
	public void delete(Item item){
		String rutaDropbox="";
		SharedPreferences mPrefs = context.getSharedPreferences("LOGIN",0);
		//recuperamos la ruta total
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }
		
		//si es un archivo
		if(item.getName().indexOf(".")>0){
			//dropbox
			if(item.getLocation()=="dropbox"){
				dsp.get(item.getAccount()).deleteFile(item.getName(),rutaDropbox+item.getName());
				dsp.get(item.getAccount()).setSpace();
				mySql.actualizarDropbox(dsp.get(item.getAccount()).getUserName(), dsp.get(item.getAccount()).getSpaceUsed(),mPrefs.getString("userName",""));
			//box
			}else{
				if(item.getLocation()=="box"){
					bsp.get(item.getAccount()).deleteFile(item.getName(),item.getId());
					bsp.get(item.getAccount()).setSpace();
					mySql.actualizarBoxSpace(bsp.get(item.getAccount()).getUserName(), bsp.get(item.getAccount()).getSpaceUsed(),mPrefs.getString("userName",""));
				}
			}
			//si es una carpeta
		}else{ 
			//dropbox
			for(int i=0;i<newDropboxAccount;i++){
				if(dsp.get(i).getNoLocation()){
					dsp.get(i).deleteFolder(item.getName(),item.getId());
				}
	        }
			//box
			for(int i=0;i<newBoxAccount;i++){
	        	if(bsp.get(i).getDirectory(posicionActual)!=""){
	        		ArrayList<Item>aux=bsp.get(i).getCurrentDirectory();
	        		for(int j=0;j<aux.size();j++){
		        		if(aux.get(j).getName().equals(item.getName())){
		        			bsp.get(i).deleteFolder(item.getName(),aux.get(j).getId());
		        		}
		        	}
	        	}
	        }
		}
	}
	
	//reiniciar los atributos que se usan para las rutas volviendo a la raiz
	public void restartRoutes(){
		directoriosDropbox.clear();
		directoriosDropbox.add("/");
		posicionActual=0;
		for(int i=0;i<newBoxAccount;i++){
			bsp.get(i).restartRoutes();
		}
	}

	//comenzar la autenticacion de todas las cuentas vinculadas
	public void startAutentication(){
		this.sql.openDatabase();
		//autenticamos todas las de dropbox
		for(int i=0;i<newDropboxAccount;i++){
			dsp.add(new DropboxStorageProvider(context,i,gba));
			dsp.get(i).startAuthentication();
    	}
		this.sql.closeDatabase();
		//finalizamos su autenticacion
		finishAutentication();
		this.sql.openDatabase();
		bAccounts.clear();
		//autenticamos las de boz
		for(int i=0;i<newBoxAccount;i++){
			bsp.add(new BoxStorageProvider(context,i,gba));
    	   	bsp.get(i).autenticate();
    	   	bAccounts.add(sql.getBoxUserName(i));
        }
		this.sql.closeDatabase();
	}
	
	//termina de autenticar las cuentas de dropbox
	public void finishAutentication(){
		this.sql.openDatabase();
		dbAccounts.clear();
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).finishAuthentication();        	
        	dbAccounts.add(sql.getDropboxUserName(i));
        }
		this.sql.closeDatabase();		
	}
	
	//terminar la vinculacion de una nueva cuenta de dropbox
	public boolean newDBAccountFinish(){
		//si se ha comenzado una vinculacion
		if (newDBAccount==true) {
			//terminar de autenticar
			if(dsp_aux.finishAuthentication()){
				newDBAccount=false;
				dsp.add(dsp_aux);
				dbAccounts.add(dsp.get(newDropboxAccount).getUserName());
				newDropboxAccount++;
				return true;
			}
        }
		return false;
	}
	
	//comenzar a vincular una nueva cuenta de dropbox
	public void newDBAccount(){
		dsp_aux=new DropboxStorageProvider(context,newDropboxAccount,gba);
		newDBAccount=true;
		dsp_aux.startAuthentication();
	}
	
	//terminar la vinculacion de una nueva cuenta de box
	public boolean newBAccountFinish(int resultCode, Intent data){
		//si se ha comenzado una vinculacion
		if (newBAccount==true) {
			//terminar de autenticar
			bsp_aux.onAuthenticated(resultCode, data);
			bsp.add(bsp_aux);
	        bAccounts.add(bsp.get(newBoxAccount).getUserName());
	        newBAccount=false;
			newBoxAccount++;
			return true;
		}
		return false;
	}
	
	//comenzar a vincular una nueva cuenta de box
	@SuppressWarnings("static-access")
	public void newBAccount(GetBoxActivity gbActivity){
		bsp_aux=new BoxStorageProvider(context,newBoxAccount,gba);
		Intent intent = OAuthActivity.createOAuthActivityIntent(context, bsp_aux.CLIENT_ID, bsp_aux.CLIENT_SECRET, false,
                bsp_aux.REDIRECT_URL);
        gbActivity.startActivityForResult(intent, AUTH_BOX_REQUEST);		
		newBAccount=true;
	}
	
	//desvincular una cuenta
	public void deleteAccount(String id,int position){
		this.sql.openDatabase();
		SharedPreferences mPrefs = context.getSharedPreferences("LOGIN",0);
		//de dropbox
		if(id=="dropbox"){
			//eliminamos de la bd remota
			mySql.deleteDropbox((dsp.get(position)).getUserName(),mPrefs.getString("userName",""));
			//del array
			dsp.remove(position);
			dbAccounts.remove(position);
			//y de la local
			this.sql.deleteDropbox(position);
			if(position<newDropboxAccount){
				for (int i=position+1;i<newDropboxAccount;i++){
					this.sql.updateDropboxAccount(position,position-1);
				}
			}	
			newDropboxAccount--;
		}
		//de box
		if(id=="box"){
			//eliminanos de la bd remota
			mySql.deleteBox((bsp.get(position)).getUserName(),mPrefs.getString("userName",""));
			//del array
			bsp.remove(position);
			bAccounts.remove(position);
			//y de la local
			this.sql.deleteBox(position);
			if(position<newBoxAccount){
				for (int i=position+1;i<newBoxAccount;i++){
					this.sql.updateBoxAccount(position,position-1);
				}
			}			
			newBoxAccount--;
		}
		this.sql.closeDatabase();
	}
	
	//llama al metodo set del directorio actual de una cuenta de box
	public void saveDirectory(ArrayList<Item> result, String location, int account){
		if(location=="box"){
    		bsp.get(account).saveDirectory(result);
		}
	}
	
	//listar los archivos de una cuenta de box
	public void actualizeBox(AsyncTaskCompleteListener<ArrayList<Item>> cb, int boxAccount){
		if(!bsp.get(boxAccount).getDirectory(posicionActual).equals("")){
    		bsp.get(boxAccount).getFiles(bsp.get(boxAccount).getDirectory(posicionActual),cb,false);
        	accountsUsed++;
        }
	}
	
	//actualiza el directorio actual
	public void actualize(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		//si es la raiz
		if(posicionActual==0){
			initFiles(cb);
		//sino
		}else{
			//lista los de dropboz
			String rutaDropbox="";
			for(int i=0;i<directoriosDropbox.size();i++){
	        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
	        }		
			for(int i=0;i<newDropboxAccount;i++){
	        	dsp.get(i).getFiles(rutaDropbox,cb,false);
	        	accountsUsed++;
	        }
			//lista los de box
			for(int i=0;i<newBoxAccount;i++){
	        	if(!bsp.get(i).getDirectory(posicionActual).equals("")){
	        		bsp.get(i).getFiles(bsp.get(i).getDirectory(posicionActual),cb,false);
	            	accountsUsed++;
		        }
	        }  
		}
	}
	
	//navegar a la carpeta indicada, listando sus ficheros
	public void navigateTo(String route,AsyncTaskCompleteListener<ArrayList<Item>> cb){
		String rutaDropbox="";
		//dropbox
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }		
		this.directoriosDropbox.add(route+"/");
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).getFiles(rutaDropbox+route+"/",cb,false);
        	accountsUsed++;
        }
		//box
		for(int i=0;i<newBoxAccount;i++){
        	//si el directorio anterior ya no estaba en la cuenta
			if(!bsp.get(i).getDirectory(posicionActual).equals("")){
        		ArrayList<Item>aux=bsp.get(i).getCurrentDirectory();
        		boolean entro=false;
        		for(int j=0;j<aux.size();j++){
        			//si este directorio esta en la cuenta
	        		if(aux.get(j).getName().equals(route)){
	                	bsp.get(i).getFiles(aux.get(j).getId(),cb,false);
	                	accountsUsed++;
	                	bsp.get(i).addDirectory(aux.get(j).getId());
	                	entro=true;
	        		}
        		}
        		//sino esta en la cuenta
        		if(!entro){
        			bsp.get(i).addDirectory("");
            		bsp.get(i).saveDirectory(new ArrayList<Item>());
        		}
        	//sino
	        }else{
        		bsp.get(i).addDirectory("");
        		bsp.get(i).saveDirectory(new ArrayList<Item>());
        	}
        }
		posicionActual++;
	}
	
	//navegar a la carpeta anterior
	public boolean goBack(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		//si estamos en la raiz
		if(posicionActual==0){
			return false;
		//sino
		}else{
			//dropbox
			String rutaDropbox="";
			for(int i=0;i<directoriosDropbox.size()-1;i++){
	        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
	        }		
			this.directoriosDropbox.remove(posicionActual);
			for(int i=0;i<newDropboxAccount;i++){
	        	dsp.get(i).getFiles(rutaDropbox,cb,false);
	        	accountsUsed++;
	        }
			//box
	        for(int i=0;i<newBoxAccount;i++){
	        	if(!bsp.get(i).getDirectory(posicionActual-1).equals("")){
	        		bsp.get(i).getFiles(bsp.get(i).getDirectory(posicionActual-1),cb,false);
	            	accountsUsed++;
		        }else{
		        	bsp.get(i).saveDirectory(new ArrayList<Item>());
		        }

	        	bsp.get(i).removeDirectory(posicionActual);
	        }			
			
			posicionActual--;
			return true;
		}
	}
	
	//recupera los archivos de las carpetas raizes de las cuentas
	public void initFiles(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		//dropbox
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).getFiles("/",cb,false);
        	accountsUsed++;
        }
		//box
        for(int i=0;i<newBoxAccount;i++){
        	bsp.get(i).getFiles("0",cb,false);
        	accountsUsed++;
        }
		
        restartRoutes();
	}
	
	//sincronizar con la bd en el login
	public void sincToBD(){
		SharedPreferences mPrefs = context.getSharedPreferences("LOGIN",0);
		//eliminamos lo que podams tener en la bd local
		sql.openDatabase();
		sql.deleteAll();
		sql.closeDatabase();
		//recuperamos las cuentas de la bd remota
		DropboxCallback dbc=new DropboxCallback();
		BoxCallback bbc=new BoxCallback();
		mySql.vincularDropbox(mPrefs.getString("userName",""),dbc);
		mySql.vincularBox(mPrefs.getString("userName",""),bbc);
		
	}
	
	//callback para las cuentas de box
	private class BoxCallback implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		//si aun no acabo las de dropbox
    		if(sync==0){
    			sync++;
    		//si acabaron
    		}else{
    			if(isOnline()){
    				gba.finishLogin();
    			}
    			sync=0;
    		}  
    	}
    }
	
	//callback para las cuentas de dropbox
	private class DropboxCallback implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		//si aun no acabaron las de box
    		if(sync==0){
    			sync++;
    		//si acabaron
    		}else{
    			if(isOnline()){
    				gba.finishLogin();
    			}
    			sync=0;
    		}    		
    	}
    }
	
	//se llama si hubo un error al autenticar una cuenta de box
	public void failSincToBD(){
		SharedPreferences mPrefs = context.getSharedPreferences("LOGIN",0);
		//borra de la bd local las de box
		sql.openDatabase();
		sql.deleteBox();
		sql.closeDatabase();
		//vuelve a vincularlas
		BoxCallbackFail bbc=new BoxCallbackFail();
		mySql.vincularBox(mPrefs.getString("userName",""),bbc);
		
	}
	
	//callback para vincular cuenta de box que da error
	private class BoxCallbackFail implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		if(isOnline()){
    			//borra todas las cuentas 
				gba.hideDialog();
				bsp.clear();
				dsp.clear();
				//y las vuelve a recuperar de la bd local sincronizada
				sql.openDatabase();
				sync=0;
				newDropboxAccount=sql.countAll("dropboxTokens");
				newBoxAccount=sql.countAll("boxTokens");
				sql.closeDatabase();
				dbAccounts.clear();
				bAccounts.clear();
				accountsCounter=0;
				accountsUsed=0;
				sync=0;
				directoriosDropbox.clear();
				directoriosDropbox.add("/");
				posicionActual=0;
				//las autentica
				startSincAutentication();
				gba.setErrorToast(true);
				Toast.makeText(context, "Cuentas sincronizadas", Toast.LENGTH_LONG).show();
			} 
    	}
    }
	
	//sincronizar las cuentas con la base de datos
	public void accountsSincToBD(){
		SharedPreferences mPrefs = context.getSharedPreferences("LOGIN",0);
		//elimino de la bd local las cuentas existentes
		sql.openDatabase();
		sql.deleteAll();
		sql.closeDatabase();
		//vinculo la db local con la bd 
		DropboxCallbackSinc dbc=new DropboxCallbackSinc();
		BoxCallbackSinc bbc=new BoxCallbackSinc();
		mySql.vincularDropbox(mPrefs.getString("userName",""),dbc);
		mySql.vincularBox(mPrefs.getString("userName",""),bbc);
		
	}
	
	//callback para sincronizar las cuentas de box
	private class BoxCallbackSinc implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		//si aun no acabaron las cuentas de dropbox
    		if(sync==0){
    			sync++;
    		//si acabaron
    		}else{
    			if(isOnline()){
    				//borra todas las cuentas 
    				gba.hideDialog();
    				bsp.clear();
    				dsp.clear();
    				//y las vuelve a recuperar de la bd local sincronizada
    				sql.openDatabase();
    				sync=0;
    				newDropboxAccount=sql.countAll("dropboxTokens");
    				newBoxAccount=sql.countAll("boxTokens");
    				sql.closeDatabase();
    				dbAccounts.clear();
    				bAccounts.clear();
    				accountsCounter=0;
    				accountsUsed=0;
    				sync=0;
    				directoriosDropbox.clear();
    				directoriosDropbox.add("/");
    				posicionActual=0;
    				//las autentica
    				startSincAutentication();
    				gba.restartAccountsFragment();
    				Toast.makeText(context, "Cuentas sincronizadas", Toast.LENGTH_LONG).show();
    			}
    			sync=0;
    		}  
    	}
    }
	
	//callback para sincronizar las cuentas de dropbox
	private class DropboxCallbackSinc implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		//si aun no acabaron las de box
    		if(sync==0){
    			sync++;
    		//si acabaron
    		}else{
    			if(isOnline()){
    				//borra todas las cuentas
    				gba.hideDialog();
    				bsp.clear();
    				dsp.clear();
    				sql.openDatabase();
    				sync=0;
    				// y las vuelve a recuperar de la bd local sincronizada
    				newDropboxAccount=sql.countAll("dropboxTokens");
    				newBoxAccount=sql.countAll("boxTokens");
    				sql.closeDatabase();
    				dbAccounts.clear();
    				bAccounts.clear();
    				accountsCounter=0;
    				accountsUsed=0;
    				sync=0;
    				directoriosDropbox.clear();
    				directoriosDropbox.add("/");
    				posicionActual=0;
    				//autentica las cuentas
    				startSincAutentication();
    				gba.restartAccountsFragment();
    				Toast.makeText(context, "Cuentas sincronizadas", Toast.LENGTH_LONG).show();	
    			}
    			sync=0;
    		}    		
    	}
    }
	
	//autenticar las cuentas recien sincronizadas con la base de datos
	public void startSincAutentication(){
		this.sql.openDatabase();
		//dropbox
		for(int i=0;i<newDropboxAccount;i++){
			dsp.add(new DropboxStorageProvider(context,i,gba));
			dsp.get(i).startAuthentication();
    	}
		this.sql.closeDatabase();
		finishAutentication();
		this.sql.openDatabase();
		bAccounts.clear();
		//box
		for(int i=0;i<newBoxAccount;i++){
			bsp.add(new BoxStorageProvider(context,i,gba));
    	   	bsp.get(i).sincAutenticate();
    	   	bAccounts.add(sql.getBoxUserName(i));
        }
		this.sql.closeDatabase();
	}
	
	//comprueba si hay red disponible
	public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    	return true;
    	}

    	return false;
    }

	//metodos get y set necesarios
	
	public int getNewBoxAccount() {
		return newBoxAccount;
	}

	public void setNewBoxAccount(int newBoxAccount) {
		this.newBoxAccount = newBoxAccount;
	}

	public int getNewDropboxAccount() {
		return newDropboxAccount;
	}

	public void setNewDropboxAccount(int newDropboxAccount) {
		this.newDropboxAccount = newDropboxAccount;
	}

	public ArrayList<String> getDbAccounts() {
		return dbAccounts;
	}

	public void setDbAccounts(ArrayList<String> dbAccounts) {
		this.dbAccounts = dbAccounts;
	}

	public ArrayList<String> getbAccounts() {
		return bAccounts;
	}

	public void setbAccounts(ArrayList<String> bAccounts) {
		this.bAccounts = bAccounts;
	}

	public boolean isNewDBAccount() {
		return newDBAccount;
	}

	public void setNewDBAccount(boolean newDBAccount) {
		this.newDBAccount = newDBAccount;
	}

	public boolean isNewBAccount() {
		return newBAccount;
	}

	public void setNewBAccount(boolean newBAccount) {
		this.newBAccount = newBAccount;
	}
}