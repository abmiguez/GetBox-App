package es.getbox.android.getboxapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;

import es.getbox.android.getboxapp.abstractionlayer.AbstractionLayer;
import es.getbox.android.getboxapp.fragments.FragmentArchives;
import es.getbox.android.getboxapp.fragments.FragmentClose;
import es.getbox.android.getboxapp.fragments.FragmentAccounts;
import es.getbox.android.getboxapp.fragments.FragmentOptions;
import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.mysql.MySQL;
import es.getbox.android.getboxapp.utils.Item;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GetBoxActivity extends Activity implements OnClickListener{
	//Tag y SharedPreferences
	private static final String TAG = "GetBox";
	private SharedPreferences mPrefs; 
	
	//Rutas
	private String rutaGetbox;
	private String rutaBD;
	private String rutaCamera; 
	
	//Listar los directorios
	private ArrayList<Item> listDirectory;
	private boolean boolArchives;
	
	//Interfaz    
	private ListView archivos;
	private Button buttonLgnLogin;
	private Button buttonLgnRegister;
	private Button buttonRgstrRegister;
	private Button buttonRgstrBack;
	private short button;
	private ProgressDialog dialog;
	private boolean errorToast;
	
	//Drawer
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mOptionTitles;	
	private short actualDrawer; 
     
    //Subidas de la camara
	private final static int NEW_PICTURE = 1;
    private String mCameraFileName;
    private boolean goFromUpload; 
   
	//Capa de abstraccion
	private AbstractionLayer aLayer;  
	
	//Consultas mySQL
	private MySQL mySql;
	
	//Callback Listeners
	public ItemCallback itemCallback;
	public BooleanCallback boolCallback;
	public IntCallback intCallback;
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Creamos los directorios de la aplicacion en el almacenamiento externo y el de la BD
        rutaGetbox=Environment.getExternalStorageDirectory().getPath()+"/GetBox/";
    	rutaBD=Environment.getExternalStorageDirectory().getPath()+"/GetBox/DB";
    	rutaCamera=Environment.getExternalStorageDirectory().getPath()+"/GetBox/Photos";
    	File file=new File(rutaGetbox);
        if(!file.exists())
        	file.mkdirs();
        file=new File(rutaBD);
        if(!file.exists())
            file.mkdirs();
        file=new File(rutaCamera);
        if(!file.exists())
            file.mkdirs();
        
        if (savedInstanceState != null) {
            mCameraFileName = savedInstanceState.getString("mCameraFileName");   
        }  
        
        boolArchives=false;
        errorToast=true;
    	itemCallback=new ItemCallback();
 		dialog=new ProgressDialog(this);
		mySql=new MySQL(this);
		aLayer=new AbstractionLayer(this,this,itemCallback);
		mPrefs = this.getSharedPreferences("LOGIN",0);
		if (mPrefs.getBoolean("deleteAccount",false)) {
			showToast("Cuenta eliminada");
			SharedPreferences.Editor ed = mPrefs.edit();
	        ed.putBoolean("deleteAccount",false);
	        ed.commit();
		}
        if (mPrefs.getBoolean("logueado",false)) {
        	listDirectory=new ArrayList<Item>();
        	aLayer.startAutentication();
        	showMain();
        	button=2;
        } else {
        	noTitle();
        	boolCallback=new BooleanCallback();
        	intCallback=new IntCallback();
        	showLogIn();
        	button=0;
        }
    }
    
    protected void onResume() {
        super.onResume();
        if(mPrefs.getBoolean("logueado",false)){
	       //aLayer.finishAutentication();        
	       if( aLayer.newDBAccountFinish()){
	    	   restartAccountsFragment();
	    	   Toast.makeText(this, "Autenticado con exito", Toast.LENGTH_LONG).show();
	            
	       }
        }
    }
    
    protected void onPause(){
    	super.onPause();
    	/*SharedPreferences.Editor ed = mPrefs.edit();
        ed.putBoolean("logueado",mLoggedIn);
        ed.commit();*/
    }

    protected void onStop(){
    	super.onStop();
    }

    protected void onDestroy(){
    	super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.get_box, menu);   
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
    	
    	boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.actualizar).setVisible(!drawerOpen);
		menu.findItem(R.id.nueva_carpeta).setVisible(!drawerOpen);
		menu.findItem(R.id.subir).setVisible(!drawerOpen);
		menu.findItem(R.id.salir).setVisible(!drawerOpen);
		menu.findItem(R.id.anhadir).setVisible(!drawerOpen);
		menu.findItem(R.id.sincronizar).setVisible(!drawerOpen);
    	if(!drawerOpen){
	    	menu.findItem(R.id.sincronizar).setVisible(false);
	    	menu.findItem(R.id.actualizar).setVisible(false);
			menu.findItem(R.id.nueva_carpeta).setVisible(false);
			menu.findItem(R.id.subir).setVisible(false);
			menu.findItem(R.id.salir).setVisible(true);
			menu.findItem(R.id.anhadir).setVisible(false);
	    	switch(actualDrawer){
			case 0:
				menu.findItem(R.id.actualizar).setVisible(true);
				menu.findItem(R.id.nueva_carpeta).setVisible(true);
				menu.findItem(R.id.subir).setVisible(true);
				break;
			case 1:
				menu.findItem(R.id.sincronizar).setVisible(true);
				menu.findItem(R.id.anhadir).setVisible(true);			
				break;
			}
    	}    	
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.subir_foto:
        	goFromUpload=true;
        	Intent intent = new Intent();
            // Picture from camera
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");

            String newPicFile = df.format(date) + ".jpg";
            String outPath = rutaCamera + "/" + newPicFile;
            File outFile = new File(outPath);

            mCameraFileName = outFile.toString();
            Uri outuri = Uri.fromFile(outFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
            Log.i(TAG, "Importing New Picture: " + mCameraFileName);
            try {
                startActivityForResult(intent, NEW_PICTURE);
            } catch (ActivityNotFoundException e) {
                showToast("There doesn't seem to be a camera.");
            }
            return true;	
        case R.id.salir:
        	this.finish();
			return true;	
        case R.id.actualizar:
        	if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
				listDirectory.clear();
	        	aLayer.actualize(itemCallback);
	        	showDialog("Actualizando...");
			}
        	return true;
        
        case R.id.nueva_carpeta:
        	AlertDialog dialog = crearCarpetaDialog();
        	dialog.show();
        	return true;
        	
        case R.id.sincronizar:
        	showDialog("Sincronizando cuentas...");
        	aLayer.accountsSincToBD();
        	return true;
        
        case R.id.subir_archivo:
        	goFromUpload=true;
        	Intent file_explorer = new Intent(GetBoxActivity.this,FileExplorerActivity.class);
            startActivityForResult(file_explorer, 555);// <-- ¿?
            return true;
            
        case R.id.anhadir_dropbox:
        	aLayer.newDBAccount();
            return true;
            
        case R.id.anhadir_box:
        	aLayer.newBAccount(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
   
    private void selectItem(int position) {
        // update the main content by replacing fragments
    	Fragment fragment;
        Bundle args = new Bundle();
        fragment = new FragmentArchives();
        switch(position) {        
        case 0:
            listDirectory.clear(); 
            aLayer.restartWidget(); 
            if(!isOnline()){
            	showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
	        	showDialog("Sincronizando...");  	
	            if(aLayer.zero()){
	            	hideDialog();
	            	showToast("Aun no has sincronizado ninguna cuenta");
	            }else{
	            	aLayer.initFiles(itemCallback);
	            }
			}
    		boolArchives=true;
    		args.putInt(FragmentArchives.ARG_ARCHIVE_NUMBER, position);
            fragment.setArguments(args);
        break;
        case 1:        	
        	fragment = new FragmentAccounts();
        	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, position);
        	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
        	args.putStringArrayList("arrayB", aLayer.getbAccounts());
        	args.putString("userName", mPrefs.getString("userName",""));
        	fragment.setArguments(args);
        	boolArchives=false;
        break;
        case 2:
        	fragment = new FragmentOptions();
    		args.putInt(FragmentOptions.ARG_OPTION_NUMBER, position);
    		args.putString("userName", mPrefs.getString("userName",""));
            fragment.setArguments(args);
            boolArchives=false;
        break;
        case 3:
        	fragment = new FragmentClose();
        	boolArchives=false;
        	mPrefs = getSharedPreferences("Splash",0);
			SharedPreferences.Editor ed = mPrefs.edit();
	        ed.putBoolean("splash",false);
	        ed.commit();    				
		    mPrefs = getSharedPreferences("LOGIN",0);
	        ed = mPrefs.edit();
	        ed.putBoolean("logueado",false);
	        ed.commit();
        	Intent intento = new Intent(this,GetBoxActivity.class);
        	startActivity(intento);
			this.finish();
        break;
    	} 
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        
        actualDrawer=(short)position;
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mOptionTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);  
    }
  
    public void initArchives(View rootView){
    	Myonclicklistneer myonclicklistneer = new Myonclicklistneer();
		archivos = (ListView) rootView.findViewById (R.id.archivos);
        archivos.setAdapter(new CustomIconLabelAdapter(this));
        registerForContextMenu(archivos);
        archivos.setOnItemClickListener(myonclicklistneer);
    }
    
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if(mPrefs.getBoolean("logueado",false)){
        	mDrawerToggle.syncState(); 
        }
        
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private void showMain(){
		setContentView(R.layout.activity_getbox);

        mTitle = mDrawerTitle = getTitle();
        mOptionTitles = getResources().getStringArray(R.array.options_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mOptionTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        selectItem(0);
    }
    
    private void noTitle(){
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    
    private void showLogIn(){  
    	setContentView(R.layout.login);
    	invalidateOptionsMenu();
		buttonLgnLogin = (Button) findViewById(R.id.buttonLgnLogin);
		buttonLgnLogin.setOnClickListener(this);
		buttonLgnRegister = (Button) findViewById(R.id.buttonLgnRegister);
		buttonLgnRegister.setOnClickListener(this);
    }
    
    private void showRegister(){  
    	setContentView(R.layout.register);
    	invalidateOptionsMenu();
		buttonRgstrRegister = (Button) findViewById(R.id.buttonRgstrRegister);
		buttonRgstrRegister.setOnClickListener(this);
		buttonRgstrBack = (Button) findViewById(R.id.buttonRgstrBack);
		buttonRgstrBack.setOnClickListener(this);
    }
    
    
    private void showDialog(String message){
    	dialog.setMessage(message); 
		dialog.show();    	
    }
    
    public void hideDialog(){
    	if (dialog.isShowing()) { 
        	dialog.dismiss(); 
        } 
    }
    
    private AlertDialog crearCarpetaDialog(){ 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText nCarpeta = new EditText(this);
    	builder.setTitle("Select Items");
    	builder.setView(nCarpeta); 
    	builder.setTitle("Nueva Carpeta"); 
   	 	builder.setMessage("Escribe el nombre de la carpeta"); 
   	 	builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() { 
   	 		public void onClick(DialogInterface dialog, int whichButton) { 	
	   	 		if(!isOnline()){
					showToast("Error de red. Compruebe su conexión a Internet.");
				}else{
	   	 			String nC=nCarpeta.getText().toString();
	            	listDirectory.add(aLayer.uploadFolder(nC));
	            	orderDirectory();
				}
   	 		} 
   	 	});
   	 	builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() { 
   	 		public void onClick(DialogInterface dialog, int whichButton) { 
   	 			
   		 	}//OnClick 
   	 	});
   	 	AlertDialog dialog = builder.create();
   	 	nCarpeta.requestFocus();

    	return dialog;
     } 
    
    public void onClick(View v) { 
    	switch (button) {
    		case 0:
    			if(v.getId()==buttonLgnLogin.getId()){
    				EditText lgnUser=(EditText) findViewById(R.id.edtTxtLgnUser);
    				EditText lgnPass=(EditText) findViewById(R.id.edtTxtLgnPass);
    				InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		        inputMethodManager.hideSoftInputFromWindow(lgnUser.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(lgnPass.getWindowToken(), 0);
    		        if(!isOnline()){
						showToast("Error al conectar con la base de datos");
					}else{
						showDialog("Logueandose, espere...");
						mySql.login(lgnUser.getText().toString(),lgnPass.getText().toString(),boolCallback);
					}
				}
				if(v.getId()==buttonLgnRegister.getId()){
					showRegister();
					button=1;
				}
    		break;
    		
    		case 1:
    			if(v.getId()==buttonRgstrBack.getId()){
					showLogIn();
					button=0;
				}
				if(v.getId()==buttonRgstrRegister.getId()){
    				EditText regUser=(EditText) findViewById(R.id.edtTxtRgstrUser);
    				EditText regPass=(EditText) findViewById(R.id.edtTxtRgstrPass);
    				EditText regRePass=(EditText) findViewById(R.id.edtTxtRgstrRePass);
    				EditText regEmail=(EditText) findViewById(R.id.edtTxtRgstrMail);
    				EditText regName=(EditText) findViewById(R.id.edtTxtRgstrName);
    				InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		        inputMethodManager.hideSoftInputFromWindow(regUser.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(regRePass.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(regPass.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(regEmail.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(regName.getWindowToken(), 0);
	    			if(regUser.getText().toString().equals("") || regPass.getText().toString().equals("") || regRePass.getText().toString().equals("")
    						|| regEmail.getText().toString().equals("") || regName.getText().toString().equals("")){
	    				showToast("Hay campos incompletos");
	    			}else{
    		        	if(!regRePass.getText().toString().equals(regPass.getText().toString())){
    		        		showToast("Las contraseñas no coiciden");
	    				}else{
	    					if(!isOnline()){
	    						showToast("Error al conectar con la base de datos");
	    					}else{
	    						mySql.comprobarDuplicidad(regUser.getText().toString(),
	    							regPass.getText().toString(),
	    							regEmail.getText().toString(),regName.getText().toString(),intCallback);
	    						showDialog("Registrando la cuenta...");
	    					}
	    				}
					}
				}
    		break;
    	}
	}//onClick
        
    public void setErrorToast(boolean e){
    	errorToast=e;
    }
    
    public void onListingComplete(ArrayList<Item> result) {
    	try{
    		archivos = (ListView) findViewById (R.id.archivos);
    		archivos.setEnabled(false);
    		
	    	if(result.size()>0){
	    		if(result.get(0).getName().equals("")){
	    			aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
	    		}else{
	    			if(result.get(0).getName().equals("fail") && result.get(0).getId().equals("refresh")){
	    				errorToast=false;	    				    				
	    			}else{
			    		aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
				    	for(int i=0;i<result.size();i++){
				    		listDirectory.add(result.get(i));
				    	}
		    		}
	    		}
	    	}
	    	Set<Item> hs = new LinkedHashSet<Item>();
	        hs.addAll(listDirectory);
	        listDirectory.clear();
	        listDirectory.addAll(hs);
	        orderDirectory();
    		Myonclicklistneer myonclicklistneer = new Myonclicklistneer();
    		registerForContextMenu(archivos);
            archivos.setOnItemClickListener(myonclicklistneer);

            boolean auxBool=aLayer.enableWidget();
	        archivos.setEnabled(auxBool);
	        if(auxBool){
	        	hideDialog();
	        	if(!errorToast){
					hideDialog();
	    			showDialog("Sincronizando cuentas...");
    				aLayer.failSincToBD();
					errorToast=true;
				}	
	        }
    	}catch(Exception e){}
    }
    
    class Myonclicklistneer implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        	if( (listDirectory.get(position).getName()).indexOf(".")<0 ) {
        		if(!isOnline()){
					showToast("Error de red. Compruebe su conexión a Internet.");
				}else{	
	        		archivos = (ListView) findViewById (R.id.archivos);
	        		archivos.setEnabled(false);
	        		String aux=listDirectory.get(position).getName();
	        		
	        		aLayer.navigateTo(listDirectory.get(position).getName(),itemCallback);
	        		listDirectory.clear();
	        		mTitle = aux;
	                getActionBar().setTitle(aLayer.getRoute());
	                showDialog("Cargando...");
				}
        	}
        }

    }
    
    class CustomIconLabelAdapter extends ArrayAdapter {
 		Context context;

 		CustomIconLabelAdapter(Context context) {
 			super(context, R.layout.row_icon_label, listDirectory);
 			this.context = context;			
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			View row = inflater.inflate(R.layout.row_icon_label, null);
 			
 			TextView label = (TextView) row.findViewById(R.id.label);
 			ImageView icon = (ImageView) row.findViewById(R.id.icon);
 			
 			label.setText(listDirectory.get(position).getName());
 		
 			if( (listDirectory.get(position).getName()).indexOf(".")<0 ) 
 				icon.setImageResource(R.drawable.foldericon);
 			else
 				icon.setImageResource(R.drawable.archiveicon);
 			return (row);
 		}// getView
 	}// CustomIconLabelAdapter
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
     
        MenuInflater inflater = getMenuInflater();
        
        if(v.getId() == R.id.archivos)
        {
            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)menuInfo;
     
            menu.setHeaderTitle(
            		listDirectory.get(info.position).getName());
            
            inflater.inflate(R.menu.context_menu_archivos, menu);
            if((listDirectory.get(info.position).getName()).indexOf(".")<0){
                menu.findItem(R.id.descargar).setVisible(false);
        	}
        }
        
        if(v.getId() == R.id.DBAccounts)
        {
            AdapterView.AdapterContextMenuInfo info = 
                (AdapterView.AdapterContextMenuInfo)menuInfo;
            ListView accounts = (ListView) findViewById (R.id.DBAccounts);
            menu.setHeaderTitle(
                accounts.getAdapter().getItem(info.position).toString());
     
            inflater.inflate(R.menu.context_menu_dbaccounts, menu);
        }
        
        if(v.getId() == R.id.BAccounts)
        {
            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)menuInfo;
            ListView accounts = (ListView) findViewById (R.id.BAccounts);
            menu.setHeaderTitle(
                accounts.getAdapter().getItem(info.position).toString());
            inflater.inflate(R.menu.context_menu_baccounts, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info =
    	        (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.descargar:
            	aLayer.download(listDirectory.get(info.position));
            	return true;
            case R.id.borrar:
            	final int index=info.position;
            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    @Override
            	    public void onClick(DialogInterface dialog, int which) {
            	        switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	        	aLayer.delete(listDirectory.get(index));
                    		listDirectory.remove(index);
                    		ListView archivos=(ListView) findViewById(R.id.archivos); 
                    		archivos.setAdapter(new CustomIconLabelAdapter(GetBoxActivity.this));
                    		break;

            	        case DialogInterface.BUTTON_NEGATIVE:
            	        	showToast("Borrado cancelado");
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(GetBoxActivity.this);
            	builder.setMessage("¿Seguro que deseas borrar?").setPositiveButton("Si", dialogClickListener)
            	    .setNegativeButton("No", dialogClickListener).show();
                return true;
            case R.id.deleteDBAccount:
            	if(!isOnline()){
					Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_LONG).show();		
				}else{
	            	aLayer.deleteAccount("dropbox",info.position);
	            	restartAccountsFragment();
				}
    			return true;
            case R.id.deleteBAccount:
            	if(!isOnline()){
					Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_LONG).show();		
				}else{
	            	aLayer.deleteAccount("box",info.position);
	                restartAccountsFragment();
				}
            	return true;
            default:
            	return super.onContextItemSelected(item);
        }
    }
    
    public void restartAccountsFragment(){
    	Fragment fragment;
        Bundle args = new Bundle();
    	fragment = new FragmentAccounts();
    	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, 1);
    	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
    	args.putStringArrayList("arrayB", aLayer.getbAccounts());
    	args.putString("userName", mPrefs.getString("userName",""));
    	fragment.setArguments(args);
    	boolArchives=false;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
    
    
    
 // This is what gets called on finishing a media piece to import
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
        	if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
        	// return from file upload
	            if (resultCode == Activity.RESULT_OK) {
	                Uri uri = null;
	                if (data != null) {
	                    uri = data.getData();
	                }
	                if (uri == null && mCameraFileName != null) {
	                    uri = Uri.fromFile(new File(mCameraFileName));
	                }
	                Log.i(TAG,mCameraFileName);
	                if (uri != null) {
	                	listDirectory.add(aLayer.uploadFile(mCameraFileName));
	                	orderDirectory();
	                }
	            }else {
	                Log.w(TAG, "Unknown Activity Result from mediaImport: "
	                        + resultCode);
	            }
			}
        }
        if (resultCode == RESULT_OK && requestCode == 555) {
        	if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
	        	try{
	            	if (data.hasExtra("archivo_seleccionado")) {
	                listDirectory.add(aLayer.uploadFile(data.getExtras().getString("archivo_seleccionado")));
	                orderDirectory();
	            }
	            }catch(NullPointerException e){
	            	Log.i(TAG,"Se ha saido del gestor sin seleccionar archivo");
	            }
			}	
        }
        if (requestCode == aLayer.AUTH_BOX_REQUEST && aLayer.isNewBAccount()) {
        	aLayer.newBAccountFinish(resultCode, data);
        	restartAccountsFragment();
        } 
        
    }
    
    private void orderDirectory(){
    	Collections.sort(listDirectory, new Comparator<Item>(){
	        	 
			@Override
			public int compare(Item o1, Item o2) {
				return (o1.getName().toLowerCase()).compareTo(o2.getName().toLowerCase());
			}
			
			
		});
		ArrayList<Item> aux=new ArrayList<Item> ();
        for(int i=0; i<listDirectory.size();i++){
        	if(listDirectory.get(i).getName().indexOf(".")>0){
        		aux.add(listDirectory.get(i));
        		listDirectory.remove(i);
        	}
        }
        for(int i=0; i<aux.size();i++){
        	listDirectory.add(aux.get(i));
        }
        archivos=(ListView) findViewById(R.id.archivos); 
		archivos.setAdapter(new CustomIconLabelAdapter(GetBoxActivity.this));
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    
    public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    	return true;
    	}

    	return false;
    }
    
    public void onBackPressed(){
    	if(boolArchives==false){
    		this.finish();
    	}else{
    		if(aLayer.enableBack()){
	    		if(aLayer.goBack(itemCallback)){
	    			listDirectory.clear();
	    			if(aLayer.getPosicionActual()>0){
	    				mTitle = aLayer.getRoute();
	                    getActionBar().setTitle(mTitle);
	                    showDialog("Cargando...");
	    			}
	    			else{
	    				mTitle = mOptionTitles[0];
	                    getActionBar().setTitle(mTitle);
	                    showDialog("Cargando...");
	    			}
	    		}else{
	    			this.finish();
	    		}
    		}
    	}
    	
    }   
    
    public void finishLogin(){
        hideDialog();
        Intent intent = new Intent(this,GetBoxActivity.class);
		startActivity(intent);
        this.finish();
    }
    
    public void actualize(int boxAccount){
    	if(!isOnline()){
			showToast("Error de red. Compruebe su conexión a Internet.");
		}else{
			//listDirectory.clear();
        	aLayer.actualizeBox(itemCallback,boxAccount);
        	showDialog("Sincronizando...");
		}	
    }
    
    public void login(boolean result){
		EditText lgnUser=(EditText) findViewById(R.id.edtTxtLgnUser);
		EditText lgnPass=(EditText) findViewById(R.id.edtTxtLgnPass);
    	if(result){
			mPrefs = getSharedPreferences("Splash",0);
			SharedPreferences.Editor ed = mPrefs.edit();
	        ed.putBoolean("splash",true);
	        ed.commit(); 
		    mPrefs = getSharedPreferences("LOGIN",0);
			ed = mPrefs.edit();
	        ed.putBoolean("logueado",true);
	        ed.commit();
	        ed.putString("userName",lgnUser.getText().toString());
	        ed.commit();
	        
	       	aLayer.sincToBD();
		}else{  
			hideDialog();
			showToast("Nombre de usuario o contraseña incorrectos");
			//lgnUser.setText("");
			lgnPass.setText("");   
			
		}
    }
    
    public void comprobarDuplicidad(int result){
    	EditText regUser=(EditText) findViewById(R.id.edtTxtRgstrUser);
		EditText regPass=(EditText) findViewById(R.id.edtTxtRgstrPass);
		EditText regEmail=(EditText) findViewById(R.id.edtTxtRgstrMail);
		EditText regName=(EditText) findViewById(R.id.edtTxtRgstrName);
    	switch (result){
		case 0:
			mySql.registrar(regUser.getText().toString(),
					regPass.getText().toString(),
					regEmail.getText().toString(),regName.getText().toString());
			hideDialog();
			showLogIn();
			button=0;
		break;
		case 2:
			hideDialog();
			Toast.makeText(this, "El nombre de usuario no está disponible", Toast.LENGTH_LONG).show();	
			regUser.setText("");
		break;
		case 3:
			hideDialog();
			Toast.makeText(this, "El email introducido ya está siendo utilizado en otra cuenta", Toast.LENGTH_LONG).show();
			regEmail.setText("");
		break;
		case 5:
			hideDialog();
			Toast.makeText(this, "El usuario ha sido registrado correctamente",
    				Toast.LENGTH_LONG).show();
			showLogIn();
			button=0;
		break;
		case 4:
			hideDialog();
			showToast("Error al conectar con la base de datos"); 
		break;
	}
    }
    
    public class ItemCallback implements AsyncTaskCompleteListener<ArrayList<Item>>{
    	public void onTaskComplete( ArrayList<Item> result){
    		onListingComplete(result);
    	}
    }
    
    public class BooleanCallback implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		login(result);
    	}
    }
    
    public class IntCallback implements AsyncTaskCompleteListener<Integer>{
    	public void onTaskComplete( Integer result){
    		comprobarDuplicidad(result);
    	}
    }
}