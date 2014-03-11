package es.getbox.android.getboxapp.abstractionlayer;

import java.util.ArrayList;

import com.box.boxandroidlibv2.activities.OAuthActivity;

import android.content.Context;
import android.content.Intent;

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
	public final static int AUTH_BOX_REQUEST = 2;
	
	
	//Rutas para la navegación entre los directorios
    private String rutaDropboxActual;
    private String rutaBoxActual;
    private String[] directoriosDropbox;
    private String[] directoriosBox;
    private int posicionActualDropbox;
    private int posicionActualBox;
	
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
		
		dsp=new ArrayList<DropboxStorageProvider>();
		bsp=new ArrayList<BoxStorageProvider>();
		
		posicionActualDropbox=0;
		posicionActualBox=0;
		rutaDropboxActual="/";
		rutaBoxActual="0";
		directoriosDropbox=new String[100];
		directoriosBox=new String[100];
		directoriosDropbox[posicionActualDropbox]=rutaDropboxActual;
		directoriosBox[posicionActualBox]=rutaBoxActual;	
		sql.closeDatabase();	
	}
	
	public void restartRoutes(){
		posicionActualDropbox=0;
		posicionActualBox=0;
		rutaDropboxActual="/";
		rutaBoxActual="0";
		directoriosDropbox[posicionActualDropbox]=rutaDropboxActual;
		directoriosBox[posicionActualBox]=rutaBoxActual;
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
	
	public void newDBAccountFinish(){
		if (newDBAccount==true) {
			if(dsp_aux.finishAuthentication()){
				newDBAccount=false;
				dsp.add(dsp_aux);
				dbAccounts.add(dsp.get(newDropboxAccount).getUserName());
				newDropboxAccount++;
			}
        }
	}
	
	public void newDBAccount(){
		dsp_aux=new DropboxStorageProvider(context,newDropboxAccount);
		newDBAccount=true;
		dsp_aux.startAuthentication();
	}
	
	public void newBAccountFinish(int resultCode, Intent data){
		if (newBAccount==true) {
			bsp_aux.onAuthenticated(resultCode, data);
	        bsp.add(bsp_aux);
	        bAccounts.add(bsp.get(newBoxAccount).getUserName());
	        newBAccount=false;
			newBoxAccount++;
		}
	}
	
	public void newBAccount(GetBoxActivity gbActivity){
		bsp_aux=new BoxStorageProvider(context,newBoxAccount);
		Intent intent = OAuthActivity.createOAuthActivityIntent(context, bsp_aux.CLIENT_ID, bsp_aux.CLIENT_SECRET, false,
                bsp_aux.REDIRECT_URL);
        gbActivity.startActivityForResult(intent, AUTH_BOX_REQUEST);		
		newBAccount=true;
	}
	
	public void deleteAccount(){
		
	}
	
	public void getFiles(AsyncTaskCompleteListener<ArrayList<Item>> cb){
		for(int i=0;i<newDropboxAccount;i++){
        	dsp.get(i).getFiles("/",cb,false);
        }
        for(int i=0;i<newBoxAccount;i++){
        	bsp.get(i).getFiles("0",cb,false);
        }
	}
	
	public DropboxStorageProvider getDsp_aux() {
		return dsp_aux;
	}

	public void setDsp_aux(DropboxStorageProvider dsp_aux) {
		this.dsp_aux = dsp_aux;
	}

	public BoxStorageProvider getBsp_aux() {
		return bsp_aux;
	}

	public void setBsp_aux(BoxStorageProvider bsp_aux) {
		this.bsp_aux = bsp_aux;
	}

	public ArrayList<DropboxStorageProvider> getDsp() {
		return dsp;
	}

	public void setDsp(ArrayList<DropboxStorageProvider> dsp) {
		this.dsp = dsp;
	}

	public ArrayList<BoxStorageProvider> getBsp() {
		return bsp;
	}

	public void setBsp(ArrayList<BoxStorageProvider> bsp) {
		this.bsp = bsp;
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

	public String getRutaDropboxActual() {
		return rutaDropboxActual;
	}

	public void setRutaDropboxActual(String rutaDropboxActual) {
		this.rutaDropboxActual = rutaDropboxActual;
	}

	public String getRutaBoxActual() {
		return rutaBoxActual;
	}

	public void setRutaBoxActual(String rutaBoxActual) {
		this.rutaBoxActual = rutaBoxActual;
	}

	public String[] getDirectoriosDropbox() {
		return directoriosDropbox;
	}

	public void setDirectoriosDropbox(String[] directoriosDropbox) {
		this.directoriosDropbox = directoriosDropbox;
	}

	public String[] getDirectoriosBox() {
		return directoriosBox;
	}

	public void setDirectoriosBox(String[] directoriosBox) {
		this.directoriosBox = directoriosBox;
	}

	public int getPosicionActualDropbox() {
		return posicionActualDropbox;
	}

	public void setPosicionActualDropbox(int posicionActualDropbox) {
		this.posicionActualDropbox = posicionActualDropbox;
	}

	public int getPosicionActualBox() {
		return posicionActualBox;
	}

	public void setPosicionActualBox(int posicionActualBox) {
		this.posicionActualBox = posicionActualBox;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
}