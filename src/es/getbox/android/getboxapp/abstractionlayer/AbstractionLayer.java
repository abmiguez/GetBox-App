package es.getbox.android.getboxapp.abstractionlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.box.boxandroidlibv2.activities.OAuthActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import es.getbox.android.getboxapp.GetBoxActivity;
import es.getbox.android.getboxapp.box.BoxStorageProvider;
import es.getbox.android.getboxapp.dropbox.DropboxStorageProvider;
import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
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
	
	//Contexto
	private Context context;
	
	public AbstractionLayer(Context context){
		this.context=context;
		
		sql=new SQL(this.context);
		sql.openDatabase();	
		sql.createTables();
		
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
	
	public Item uploadFile(String file_name){
        int[] cuenta=spp.storagePolicy(newDropboxAccount, newBoxAccount, dsp, bsp);
        Item item;
        File aux=new File(file_name);        
        if(cuenta[0]==0){
        	String rutaDropbox="";
    		for(int i=0;i<directoriosDropbox.size();i++){
            	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
            }
        	dsp.get(cuenta[1]).uploadFile(file_name, rutaDropbox);
        	//dsp.get(cuenta[1]).setSpaceUsed(dsp.get(cuenta[1]).getSpace());
        	item=new Item(aux.getName(),rutaDropbox+file_name,"dropbox",cuenta[1]);
        }else{
        	bsp.get(cuenta[1]).uploadFile(file_name,
        			bsp.get(cuenta[1]).getDirectory(posicionActual));
        	//bsp.get(cuenta[1]).setSpaceUsed(bsp.get(cuenta[1]).getSpace());
        	item=new Item(aux.getName(),bsp.get(cuenta[1]).getDirectory(posicionActual)+file_name,"box",cuenta[1]);
        }
		return item;
	}
	
	public Item uploadFolder(String file_name){
		String rutaDropbox="";
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }
		Log.i("a",rutaDropbox);
		int[] cuenta=spp.storagePolicy(newDropboxAccount, newBoxAccount, dsp, bsp);
        Item item;
        File aux=new File(file_name);        
        if(cuenta[0]==0){
        	dsp.get(cuenta[1]).uploadFolder(rutaDropbox+file_name, rutaDropbox);
        	item=new Item(aux.getName(),rutaDropbox+file_name,"dropbox",cuenta[1]);
        }else{
        	bsp.get(cuenta[1]).uploadFolder(file_name, 
        			bsp.get(cuenta[1]).getDirectory(posicionActual));
        	item=new Item(aux.getName(),bsp.get(cuenta[1]).getDirectory(posicionActual)+file_name,"box",cuenta[1]);
        }
        return item;
	}
	
	public void restartWidget(){
		accountsCounter=0;
		accountsUsed=0; 
	}
	
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
	
	public int getPosicionActual() {
		return posicionActual;
	}

	public String getRoute() {
		String route="";
		for(int i=1;i<=posicionActual;i++){
			route=route+directoriosDropbox.get(i);
		}
		return route.substring(0, route.length()-1);
	}
	
	public void download(Item item){
		if(item.getName().indexOf(".")>0){
			if(item.getLocation()=="dropbox"){
				dsp.get(item.getAccount()).downloadFile(item.getName(),item.getId());
			}else{
				if(item.getLocation()=="box"){
					bsp.get(item.getAccount()).downloadFile(item.getName(),item.getId());
				}
			}
		}else{
			Toast.makeText(context, "No se pueden descargar directorios", Toast.LENGTH_LONG).show();
		}
	}
	
	public void delete(Item item){
		String rutaDropbox="";
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }
		if(item.getName().indexOf(".")>0){
			if(item.getLocation()=="dropbox"){
				dsp.get(item.getAccount()).deleteFile(item.getName(),rutaDropbox+item.getName());
				dsp.get(item.getAccount()).setSpaceUsed(dsp.get(item.getAccount()).getSpace());
			}else{
				if(item.getLocation()=="box"){
					bsp.get(item.getAccount()).deleteFile(item.getName(),item.getId());
					bsp.get(item.getAccount()).setSpaceUsed(bsp.get(item.getAccount()).getSpace());
				}
			}
		}else{ 
			for(int i=0;i<newDropboxAccount;i++){
				if(dsp.get(i).getNoLocation()){
					dsp.get(i).deleteFolder(item.getName(),item.getId());
				}
	        }
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
	
	public void restartRoutes(){
		directoriosDropbox.clear();
		directoriosDropbox.add("/");
		posicionActual=0;
		for(int i=0;i<newBoxAccount;i++){
			bsp.get(i).restartRoutes();
		}
	}

	public void startAutentication(){
		this.sql.openDatabase();
		for(int i=0;i<newDropboxAccount;i++){
			dsp.add(new DropboxStorageProvider(context,i));
			dsp.get(i).startAuthentication();
    	}
		bAccounts.clear();
		for(int i=0;i<newBoxAccount;i++){
			bsp.add(new BoxStorageProvider(context,i));
    	   	bsp.get(i).autenticate();
    	   	bAccounts.add(sql.getBoxUserName(i));
        }
		this.sql.closeDatabase();
	}
	
	public void finishAutentication(){
		this.sql.openDatabase();
		dbAccounts.clear();
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).finishAuthentication();        	
        	dbAccounts.add(sql.getDropboxUserName(i));
        }
		this.sql.closeDatabase();		
	}
	
	public boolean newDBAccountFinish(){
		if (newDBAccount==true) {
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
	
	public void newDBAccount(){
		dsp_aux=new DropboxStorageProvider(context,newDropboxAccount);
		newDBAccount=true;
		dsp_aux.startAuthentication();
	}
	
	public boolean newBAccountFinish(int resultCode, Intent data){
		if (newBAccount==true) {
			bsp_aux.onAuthenticated(resultCode, data);
	        bsp.add(bsp_aux);
	        bAccounts.add(bsp.get(newBoxAccount).getUserName());
	        newBAccount=false;
			newBoxAccount++;
			return true;
		}
		return false;
	}
	
	public void newBAccount(GetBoxActivity gbActivity){
		bsp_aux=new BoxStorageProvider(context,newBoxAccount);
		Intent intent = OAuthActivity.createOAuthActivityIntent(context, bsp_aux.CLIENT_ID, bsp_aux.CLIENT_SECRET, false,
                bsp_aux.REDIRECT_URL);
        gbActivity.startActivityForResult(intent, AUTH_BOX_REQUEST);		
		newBAccount=true;
	}
	
	public void deleteAccount(String id,int position){
		this.sql.openDatabase();
		if(id=="dropbox"){
			dsp.remove(position);
			dbAccounts.remove(position);
			this.sql.deleteDropbox(position);
			if(position<newDropboxAccount){
				for (int i=position+1;i<newDropboxAccount;i++){
					this.sql.updateDropboxAccount(position,position-1);
				}
			}	
			newDropboxAccount--;
		}
		if(id=="box"){
			bsp.remove(position);
			bAccounts.remove(position);
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
	
	public void saveDirectory(ArrayList<Item> result, String location, int account){
		if(location=="box"){
    		bsp.get(account).saveDirectory(result);
		}
	}
	
	public void actualize(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		if(posicionActual==0){
			initFiles(cb);
		}else{
			String rutaDropbox="";
			for(int i=0;i<directoriosDropbox.size();i++){
	        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
	        }		
			for(int i=0;i<newDropboxAccount;i++){
	        	dsp.get(i).getFiles(rutaDropbox,cb,false);
	        	accountsUsed++;
	        }
			for(int i=0;i<newBoxAccount;i++){
	        	if(!bsp.get(i).getDirectory(posicionActual).equals("")){
	        		bsp.get(i).getFiles(bsp.get(i).getDirectory(posicionActual),cb,false);
	            	accountsUsed++;
		        }
	        }  
		}
	}
	
	public void navigateTo(String route,AsyncTaskCompleteListener<ArrayList<Item>> cb){
		String rutaDropbox="";
		for(int i=0;i<directoriosDropbox.size();i++){
        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
        }		
		this.directoriosDropbox.add(route+"/");
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).getFiles(rutaDropbox+route+"/",cb,false);
        	accountsUsed++;
        }
		
		for(int i=0;i<newBoxAccount;i++){
        	if(!bsp.get(i).getDirectory(posicionActual).equals("")){
        		ArrayList<Item>aux=bsp.get(i).getCurrentDirectory();
        		boolean entro=false;
        		for(int j=0;j<aux.size();j++){
	        		if(aux.get(j).getName().equals(route)){
	                	bsp.get(i).getFiles(aux.get(j).getId(),cb,false);
	                	accountsUsed++;
	                	bsp.get(i).addDirectory(aux.get(j).getId());
	                	entro=true;
	        		}
        		}
        		if(!entro){
        			bsp.get(i).addDirectory("");
            		bsp.get(i).saveDirectory(new ArrayList<Item>());
        		}
	        }else{
        		bsp.get(i).addDirectory("");
        		bsp.get(i).saveDirectory(new ArrayList<Item>());
        	}
        }
		posicionActual++;
	}
	
	public boolean goBack(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		if(posicionActual==0){
			return false;
		}else{
			String rutaDropbox="";
			for(int i=0;i<directoriosDropbox.size()-1;i++){
	        	rutaDropbox=rutaDropbox+directoriosDropbox.get(i);
	        }		
			this.directoriosDropbox.remove(posicionActual);
			for(int i=0;i<newDropboxAccount;i++){
	        	dsp.get(i).getFiles(rutaDropbox,cb,false);
	        	accountsUsed++;
	        }
			
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
	
	public void initFiles(AsyncTaskCompleteListener<ArrayList<Item>> cb){
			
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).getFiles("/",cb,false);
        	accountsUsed++;
        }
        for(int i=0;i<newBoxAccount;i++){
        	bsp.get(i).getFiles("0",cb,false);
        	accountsUsed++;
        }
		
        restartRoutes();
	}
	

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