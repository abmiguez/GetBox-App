package es.getbox.android.getboxapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
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
import es.getbox.android.getboxapp.fragments.FragmentAbout;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GetBoxActivity extends Activity implements OnClickListener{
	//SharedPreferences
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
	private short button;
	private ProgressDialog dialog;
	private boolean errorToast;
	private boolean inRegister;
	
	//Drawer
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mOptionTitles;	
	private short actualDrawer; 
	private short lastDrawer;
     
    //Subidas de la camara
	private final static int NEW_PICTURE = 1;
    private String mCameraFileName;
   
	//Capa de abstraccion
	private AbstractionLayer aLayer;  
	
	//Consultas mySQL
	private MySQL mySql;
	
	//Callback Listeners
	private ItemCallback itemCallback;
	private BooleanCallback boolCallback;
	private IntCallback intCallback;
	 
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
        
        //Inicializamos variables
        if (savedInstanceState != null) {
            mCameraFileName = savedInstanceState.getString("mCameraFileName");   
        }  
        lastDrawer=0;
        inRegister=false;
        boolArchives=false;
        errorToast=true;
    	itemCallback=new ItemCallback();
 		dialog=new ProgressDialog(this);
		mySql=new MySQL(this);
		aLayer=new AbstractionLayer(this,this,itemCallback);
		mPrefs = this.getSharedPreferences("LOGIN",0);
		//Si acabamos de eliminar la cuenta
		if (mPrefs.getBoolean("deleteAccount",false)) {
			showToast("Cuenta eliminada");
			SharedPreferences.Editor ed = mPrefs.edit();
	        ed.putBoolean("deleteAccount",false);
	        ed.commit();
		}
		//Si ya hemos iniciado sesión anteriormente
        if (mPrefs.getBoolean("logueado",false)) {
        	listDirectory=new ArrayList<Item>();
        	aLayer.startAutentication();
        	showMain();
        	button=2;
        //Si todavía no se ha iniciado sesión hay que mostrar el login
        } else {
        	invalidateOptionsMenu();
        	boolCallback=new BooleanCallback();
        	intCallback=new IntCallback();
        	showLogIn();
        	button=0;
        }
    }
    
    protected void onResume() {
        super.onResume();
        //Si estamos logueados y acabamos de vincular una cuenta, aqui se termina la vinculacion
        if(mPrefs.getBoolean("logueado",false)){
	       if( aLayer.newDBAccountFinish()){
	    	   restartAccountsFragment();
	    	   Toast.makeText(this, "Autenticado con exito", Toast.LENGTH_LONG).show();        
	       }
        }
    }
    
    protected void onPause(){
    	super.onPause();
    }

    protected void onStop(){
    	super.onStop();
    }

    protected void onDestroy(){
    	super.onDestroy();
    }

    //Crear el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.get_box, menu);   
        return super.onCreateOptionsMenu(menu);
    }

    //Preparamos el menu dependiendo de donde nos encontremos en la aplicacion
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mPrefs = this.getSharedPreferences("LOGIN",0);
        if(mPrefs.getBoolean("logueado",false)){
	    	//Si el drawer esta abierto no mostramos menu
        	boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
			menu.findItem(R.id.actualizar).setVisible(!drawerOpen);
			menu.findItem(R.id.nueva_carpeta).setVisible(!drawerOpen);
			menu.findItem(R.id.subir).setVisible(!drawerOpen);
			menu.findItem(R.id.salir).setVisible(!drawerOpen);
			menu.findItem(R.id.anhadir).setVisible(!drawerOpen);
			menu.findItem(R.id.acercade).setVisible(!drawerOpen);
			menu.findItem(R.id.sincronizar).setVisible(!drawerOpen);
	    	if(!drawerOpen){
	    		//Salir y acerca de siempre visibles el resto solo en ciertos casos
		    	menu.findItem(R.id.sincronizar).setVisible(false);
		    	menu.findItem(R.id.actualizar).setVisible(false);
				menu.findItem(R.id.nueva_carpeta).setVisible(false);
				menu.findItem(R.id.subir).setVisible(false);
				menu.findItem(R.id.acercade).setVisible(true);
				menu.findItem(R.id.salir).setVisible(true);
				menu.findItem(R.id.anhadir).setVisible(false);
		    	switch(actualDrawer){
		    	//Si estamos en el navegador de archivos
				case 0:
					menu.findItem(R.id.actualizar).setVisible(true);
					menu.findItem(R.id.nueva_carpeta).setVisible(true);
					menu.findItem(R.id.subir).setVisible(true);
					break;
				//Si estamos en el gestor de cuentas
				case 1:
					menu.findItem(R.id.sincronizar).setVisible(true);
					menu.findItem(R.id.anhadir).setVisible(true);			
					break;
				}
	    	}   
	    //Si el drawer esta abierto no mostramos menu
    	}else{
    		menu.findItem(R.id.sincronizar).setVisible(false);
	    	menu.findItem(R.id.actualizar).setVisible(false);
			menu.findItem(R.id.nueva_carpeta).setVisible(false);
			menu.findItem(R.id.subir).setVisible(false);
			menu.findItem(R.id.salir).setVisible(false);
			menu.findItem(R.id.anhadir).setVisible(false);
			menu.findItem(R.id.acercade).setVisible(false);
    	}
        return super.onPrepareOptionsMenu(menu);
    }

    //Seleccionar accion del menu
    @SuppressLint("SimpleDateFormat") @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //Diferentes acciones
        switch(item.getItemId()) {
        //Subir una nueva foto, se llama a la camara del dispositivo y se saca la foto.
        case R.id.subir_foto:
        	Intent intent = new Intent();
            // Picture from camera
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");

            //La foto tambien se guarda en el dispositivo en la carpeta Getbox/Photos
            String newPicFile = df.format(date) + ".jpg";
            String outPath = rutaCamera + "/" + newPicFile;
            File outFile = new File(outPath);

            mCameraFileName = outFile.toString();
            Uri outuri = Uri.fromFile(outFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
            try {
                startActivityForResult(intent, NEW_PICTURE);
            } catch (ActivityNotFoundException e) {
                showToast("There doesn't seem to be a camera.");
            }
            return true;
        //Salir de la aplicacion
        case R.id.salir:
        	this.finish();
			return true;
		//Actualizar el directorio actual en el navegador de archivos
        case R.id.actualizar:
        	//Si no hay red da error de red y sino actualiza
        	if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
				listDirectory.clear();
	        	aLayer.actualize(itemCallback);
	        	showDialog("Actualizando...");
			}
        	return true;
        //Crea un dialog con un formulario para crear la nueva carpeta
        case R.id.nueva_carpeta:
        	AlertDialog dialog = crearCarpetaDialog();
        	dialog.show();
        	return true;
        //Sincronizar las cuentas con la base de datos	
        case R.id.sincronizar:
        	showDialog("Sincronizando cuentas...");
        	aLayer.accountsSincToBD();
        	return true;
        //Ventana de acerca de	
        case R.id.acercade:
        	//Cambia el fragment
        	Fragment fragment;
            Bundle args = new Bundle();
        	fragment = new FragmentAbout();
        	args.putInt(FragmentAbout.ARG_ABOUT_NUMBER, 1);
        	fragment.setArguments(args);
        	boolArchives=false;
        	lastDrawer=actualDrawer;
        	actualDrawer=7;
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        	return true;
        //Subir un archivo de la SDCard, se llama a la actividad de seleccion de archivo de la SD
        case R.id.subir_archivo:
        	Intent file_explorer = new Intent(GetBoxActivity.this,FileExplorerActivity.class);
            startActivityForResult(file_explorer, 555);// <-- ¿?
            return true;
        //Vincular una nueva cuenta de dropbox    
        case R.id.anhadir_dropbox:
        	aLayer.newDBAccount();
            return true;
        //Vincular una nueva cuenta de box
        case R.id.anhadir_box:
        	aLayer.newBAccount(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    //Click listener para el drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
   
    //Seleccionar un item del drawer
    private void selectItem(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        fragment = new FragmentArchives();
        switch(position) {        
        //Navegador de ficheros
        case 0:
            listDirectory.clear(); 
            aLayer.restartWidget(); 
            //Comprobamos que hay red
            if(!isOnline()){
            	showToast("Error de red. Compruebe su conexión a Internet.");
			//Si hay red sincronizamos, si no tenemos cuentas vinculadas muestra mensaje
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
        //Gestor de cuentas
        case 1:        	
        	fragment = new FragmentAccounts();
        	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, position);
        	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
        	args.putStringArrayList("arrayB", aLayer.getbAccounts());
        	//Recupera el espacio disponible y el nombre de usuario
        	args.putLong("spaceAvaliable",aLayer.getSpaceAvaliable());
        	args.putString("userName", mPrefs.getString("userName",""));
        	fragment.setArguments(args);
        	boolArchives=false;
        break;
        //Configuracion
        case 2:
        	fragment = new FragmentOptions();
    		args.putInt(FragmentOptions.ARG_OPTION_NUMBER, position);
    		//Recupera el nombre de usuario
    		args.putString("userName", mPrefs.getString("userName",""));
            fragment.setArguments(args);
            boolArchives=false;
        break;
        //Cerrar sesion
        case 3:
        	fragment = new FragmentClose();
        	boolArchives=false;
        	//Ciera este intent y crea uno nuevo
        	mPrefs = getSharedPreferences("LOGIN",0);
		    SharedPreferences.Editor ed = mPrefs.edit();
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
        mDrawerList.setItemChecked(position, true);
        setTitle(mOptionTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);  
    }
  
    //Inicializa el listview del navegador de ficheros
    public void initArchives(View rootView){
    	Myonclicklistneer myonclicklistneer = new Myonclicklistneer();
		archivos = (ListView) rootView.findViewById (R.id.archivos);
        archivos.setAdapter(new CustomIconLabelAdapter(this));
        registerForContextMenu(archivos);
        archivos.setOnItemClickListener(myonclicklistneer);
    }
    
    //Poner un titulo en la barra de titulo de la app
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * Cuando se usa ActionBar hay que llamar a 
     * onPostCreate() y a onConfigurationChanged()
     */
 
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
       if(mPrefs.getBoolean("logueado",false)){
        	mDrawerToggle.syncState(); 
        }
        
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    //Si ya hay sesion iniciada se muestra las funcionalidades
    private void showMain(){
		setContentView(R.layout.activity_getbox);
		//Inicializar action bar
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
        //Si no hay cuentas vinculadas se muestra el gestor de cuentas
        if(aLayer.zero()){
        	selectItem(1);
        	showToast("Sincroniza tus cuentas de Dropbox y Box pulsando en +");
       //Si las hay, se muestra el navegador de ficheros
        }else{
            selectItem(0);        	
        }
    }
    
    //Muestra el formulario de login
    private void showLogIn(){ 
    	inRegister=false;
    	setContentView(R.layout.login);
    	setTitle("Iniciar sesión en Getbox");
		buttonLgnLogin = (Button) findViewById(R.id.buttonLgnLogin);
		buttonLgnLogin.setOnClickListener(this);
		buttonLgnRegister = (Button) findViewById(R.id.buttonLgnRegister);
		buttonLgnRegister.setOnClickListener(this);
    }
    
    //Muestra el formulario de registro
    private void showRegister(){  
    	inRegister=true;
    	setContentView(R.layout.register);
    	invalidateOptionsMenu();
    	setTitle("Registrate en Getbox");
		buttonRgstrRegister = (Button) findViewById(R.id.buttonRgstrRegister);
		buttonRgstrRegister.setOnClickListener(this);
    }
    
    //Mostrar un dialog con un mensaje, se utiliza para los mensajes de cargando...,
    //actualizando..., etc
    private void showDialog(String message){
    	dialog.setMessage(message); 
		dialog.show();    	
    }
   
    //Ocultar el dialog
    public void hideDialog(){
    	if (dialog.isShowing()) { 
        	dialog.dismiss(); 
        } 
    }
    
    //Incializa el dialog para crear una neuva carpeta
    private AlertDialog crearCarpetaDialog(){ 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText nCarpeta = new EditText(this);
    	builder.setTitle("Select Items");
    	builder.setView(nCarpeta); 
    	builder.setTitle("Nueva Carpeta"); 
   	 	builder.setMessage("Escribe el nombre de la carpeta"); 
   	 	//Crear la carpeta
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
   	 	//Cancelar el creado
   	 	builder.setNeutralButton("Cancelar", new DialogInterface.OnClickListener() { 
   	 		public void onClick(DialogInterface dialog, int whichButton) { 
   	 			
   		 	} 
   	 	});
   	 	AlertDialog dialog = builder.create();
   	 	nCarpeta.requestFocus();

    	return dialog;
     } 
    
    //On click 
    public void onClick(View v) { 
    	switch (button) {
    		case 0:
    			//boton de login
    			if(v.getId()==buttonLgnLogin.getId()){
    				EditText lgnUser=(EditText) findViewById(R.id.edtTxtLgnUser);
    				EditText lgnPass=(EditText) findViewById(R.id.edtTxtLgnPass);
    				InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		        inputMethodManager.hideSoftInputFromWindow(lgnUser.getWindowToken(), 0);
    		        inputMethodManager.hideSoftInputFromWindow(lgnPass.getWindowToken(), 0);
    		        //si no hay red
    		        if(!isOnline()){
						showToast("Error al conectar con la base de datos");
					}else{
						//si deja el usuario vacion
						if(lgnUser.getText().toString().equals("")){
							showToast("Introduce un usuario válido");
						//logueandose
						}else{
							showDialog("Logueandose, espere...");
							mySql.login(lgnUser.getText().toString(),lgnPass.getText().toString(),boolCallback);
						}
					}
				}
    			//acceder a la ventana de registro
				if(v.getId()==buttonLgnRegister.getId()){
					showRegister();
					button=1;
				}
    		break;
    		//boton de registro
    		case 1:
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
	    			//si hay campos incompletos
    		        if(regUser.getText().toString().equals("") || regPass.getText().toString().equals("") || regRePass.getText().toString().equals("")
    						|| regEmail.getText().toString().equals("") || regName.getText().toString().equals("")){
	    				showToast("Hay campos incompletos");
	    			}else{
	    				//si las contraseñas no coinciden
    		        	if(!regRePass.getText().toString().equals(regPass.getText().toString())){
    		        		showToast("Las contraseñas no coiciden");
	    				}else{
	    					//si no hay red
	    					if(!isOnline()){
	    						showToast("Error al conectar con la base de datos");
	    					//registrando
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
	}
    
    //mostrar o no toast de error
    public void setErrorToast(boolean e){
    	errorToast=e;
    }
    
    //proceso de listar un directorio, se llamara tantas veces a esta funcion
    //como cuentas de almacenamiento haya vinculadas
    private void onListingComplete(ArrayList<Item> result) {
    	try{
    		archivos = (ListView) findViewById (R.id.archivos);
    		//no se permite la interacion con el listview hasta que se listen todos
    		archivos.setEnabled(false);
    		
	    	if(result.size()>0){
	    		//si no listamos nada de una cuenta de box
	    		if(result.get(0).getName().equals("")){
	    			aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
	    		}else{
	    			//si no listamos nada de una cuenta de dropbox
	    			if(result.get(0).getName().equals("fail") && result.get(0).getId().equals("refresh")){
	    				errorToast=false;
	    			//si listamos algo
	    			}else{
			    		aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
				    	for(int i=0;i<result.size();i++){
				    		listDirectory.add(result.get(i));
				    	}
		    		}
	    		}
	    	}

	    	//vamos añadiendo ficheros al listview y ordenandolos alfabeticamente
	    	Set<Item> hs = new LinkedHashSet<Item>();
	        hs.addAll(listDirectory);
	        listDirectory.clear();
	        listDirectory.addAll(hs);
	        orderDirectory();
    		Myonclicklistneer myonclicklistneer = new Myonclicklistneer();
    		registerForContextMenu(archivos);
            archivos.setOnItemClickListener(myonclicklistneer);
            //si ya listamos todas las cuentas aceptamos la interaccion con el widget y 
            //ocultamos el dialog
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
    
    //click listener del listview de archivos
    class Myonclicklistneer implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        	if( (listDirectory.get(position).getName()).indexOf(".")<0 ) {
        		//si no hay red
        		if(!isOnline()){
					showToast("Error de red. Compruebe su conexión a Internet.");
				//si hay red
        		}else{	
	        		archivos = (ListView) findViewById (R.id.archivos);
	        		archivos.setEnabled(false);
	        		String aux=listDirectory.get(position).getName();
	        		//listamos el directorio
	        		aLayer.navigateTo(listDirectory.get(position).getName(),itemCallback);
	        		listDirectory.clear();
	        		mTitle = aux;
	                getActionBar().setTitle(aLayer.getRoute());
	                showDialog("Cargando...");
				}
        	}
        }

    }
    
    //iconos del listview dependiendo del tipo de archivo
    @SuppressWarnings("rawtypes")
	class CustomIconLabelAdapter extends ArrayAdapter {
 		Context context;

 		@SuppressWarnings("unchecked")
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
 			//dependiendo de que tipo de archivo sea le ponemos un icono distinto
 			String name=listDirectory.get(position).getName();
 			if( !name.contains(".") ) {
 				icon.setImageResource(R.drawable.foldericon);
 			}else{
 				if(name.endsWith(".jpg") || name.endsWith(".JPG") ||
							name.endsWith(".jpeg") || name.endsWith(".JPEG")){
 					icon.setImageResource(R.drawable.jpgicon);
 				}else{
 					if(name.endsWith(".txt") || name.endsWith(".TXT") ){
 	 					icon.setImageResource(R.drawable.txticon);
 	 				}else{
	 	 				if(name.endsWith(".gif") || name.endsWith(".GIF") ){
	 	 					icon.setImageResource(R.drawable.gificon);
	 	 				}else{
		 	 				if(name.endsWith(".png") || name.endsWith(".PNG") ){
		 	 					icon.setImageResource(R.drawable.pngicon);
		 	 				}else{
			 	 				if(name.endsWith(".pdf") || name.endsWith(".PDF") ){
			 	 					icon.setImageResource(R.drawable.pdficon);
			 	 				}else{
			 	 					if(name.endsWith(".doc") || name.endsWith(".DOC") ||
			 	 							name.endsWith(".docx") || name.endsWith(".DOCX") ||
			 	 							name.endsWith(".odt") || name.endsWith(".ODT")){
			 	 						icon.setImageResource(R.drawable.docicon);
			 	 					}else{
			 	 						if(name.endsWith(".ppt") || name.endsWith(".PPT") ||
				 	 							name.endsWith(".pptx") || name.endsWith(".PPTX") ||
				 	 							name.endsWith(".odp") || name.endsWith(".ODP")){
			 	 							icon.setImageResource(R.drawable.ppticon);		 	 							
			 	 						}else{
			 	 							if(name.endsWith(".xls") || name.endsWith(".XLS") ||
					 	 							name.endsWith(".xlsx") || name.endsWith(".XLSX") ||
					 	 							name.endsWith(".ods") || name.endsWith(".ODS")){
				 	 							icon.setImageResource(R.drawable.xlsicon);
			 	 							}else{
			 	 								if(name.endsWith(".zip") || name.endsWith(".ZIP") ){
			 				 	 					icon.setImageResource(R.drawable.zipicon);
			 	 								}else{
			 	 									if(name.endsWith(".rar") || name.endsWith(".RAR") ){
			 	 				 	 					icon.setImageResource(R.drawable.raricon);
			 	 									}else{
			 	 										if(name.endsWith(".apk") || name.endsWith(".APK") ){
				 	 				 	 					icon.setImageResource(R.drawable.apkicon);
				 	 									}else{
				 	 										if(name.endsWith(".epub") || name.endsWith(".EPUB") ){
					 	 				 	 					icon.setImageResource(R.drawable.epubicon);
					 	 									}else{
					 	 										if(name.endsWith(".exe") || name.endsWith(".EXE") ){
						 	 				 	 					icon.setImageResource(R.drawable.exeicon);
						 	 									}else{
						 	 										if(name.endsWith(".mp3") || name.endsWith(".MP3") ){
							 	 				 	 					icon.setImageResource(R.drawable.mp3icon);
							 	 									}else{
							 	 										if(name.endsWith(".wav") || name.endsWith(".WAV") ){
								 	 				 	 					icon.setImageResource(R.drawable.wavicon);
								 	 									}else{
								 	 										if(name.endsWith(".mp4") || name.endsWith(".MP4") ){
									 	 				 	 					icon.setImageResource(R.drawable.mp4icon);
									 	 									}else{
									 	 										if(name.endsWith(".avi") || name.endsWith(".AVI") ){
										 	 				 	 					icon.setImageResource(R.drawable.aviicon);
										 	 									}else{
										 	 										if(name.endsWith(".mpeg") || name.endsWith(".MPEG") ){
											 	 				 	 					icon.setImageResource(R.drawable.mpegicon);
											 	 									}else{
											 	 										icon.setImageResource(R.drawable.archivoicon); 
											 	 									}
										 	 									}
									 	 									}
								 	 									}
							 	 									}
						 	 									}
					 	 									}
				 	 									}
			 	 									}
			 	 								}
			 	 							}
			 	 						}
			 	 					}
			 	 				}
		 	 				}
	 	 				}
 	 				}
 				}
 			}
 			return (row);
 		}
 	}
    
    //Crear el menu contextual
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
     
        MenuInflater inflater = getMenuInflater();
        //menu para los ficheros
        if(v.getId() == R.id.archivos)
        {
            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)menuInfo;
     
            menu.setHeaderTitle(
            		listDirectory.get(info.position).getName());
            
            inflater.inflate(R.menu.context_menu_archivos, menu);
            //si es una carpeta no permite descargar
            if((listDirectory.get(info.position).getName()).indexOf(".")<0){
                menu.findItem(R.id.descargar).setVisible(false);
        	}
        }
        //menu para las cuentas de dropbox
        if(v.getId() == R.id.DBAccounts)
        {
            AdapterView.AdapterContextMenuInfo info = 
                (AdapterView.AdapterContextMenuInfo)menuInfo;
            ListView accounts = (ListView) findViewById (R.id.DBAccounts);
            menu.setHeaderTitle(
                accounts.getAdapter().getItem(info.position).toString());
     
            inflater.inflate(R.menu.context_menu_dbaccounts, menu);
        }
        //menu para las cuentas de box
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
    
    //seleccionar un item del menu contextual
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info =
    	        (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        	//descargar un archivo
            case R.id.descargar:
            	aLayer.download(listDirectory.get(info.position));
            	return true;
            //borrar un archivo
            case R.id.borrar:
            	final int index=info.position;
            	//dialog que pide confirmacion
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
            //desvincular cuenta de dropbox
            case R.id.deleteDBAccount:
            	//comprueba si hay red
            	if(!isOnline()){
					Toast.makeText(this, "Error al conectar con la base de datos", Toast.LENGTH_LONG).show();		
				}else{
	            	aLayer.deleteAccount("dropbox",info.position);
	            	restartAccountsFragment();
				}
    			return true;
    		//desvincular cuenta de box
            case R.id.deleteBAccount:
            	//comprueba si hay red
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
    
    //Restart del gestor de cuentas al vincular una nueva cuenta para que pueda aparecer
    public void restartAccountsFragment(){
    	Fragment fragment;
        Bundle args = new Bundle();
    	fragment = new FragmentAccounts();
    	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, 1);
    	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
    	args.putStringArrayList("arrayB", aLayer.getbAccounts());
    	args.putLong("spaceAvaliable",aLayer.getSpaceAvaliable());
    	args.putString("userName", mPrefs.getString("userName",""));
    	fragment.setArguments(args);
    	boolArchives=false;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
    
    
    
 	//Resultado que producen ciertas actividades
    @SuppressWarnings("static-access")
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //subida de la camara
    	if (requestCode == NEW_PICTURE) {
        	//comprobamos que hay red
    		if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
	            if (resultCode == Activity.RESULT_OK) {
	                Uri uri = null;
	                if (data != null) {
	                    uri = data.getData();
	                }
	                if (uri == null && mCameraFileName != null) {
	                    uri = Uri.fromFile(new File(mCameraFileName));
	                }
	                if (uri != null) {
	                	aLayer.uploadFile(mCameraFileName);
	                	orderDirectory();
	                }
	            }
			}
        }
    	//subir un archivo
        if (resultCode == RESULT_OK && requestCode == 555) {
        	//comprobamos que hay red
        	if(!isOnline()){
				showToast("Error de red. Compruebe su conexión a Internet.");
			}else{
	        	try{
	            	if (data.hasExtra("archivo_seleccionado")) {
	                aLayer.uploadFile(data.getExtras().getString("archivo_seleccionado"));
	                orderDirectory();
	            }
	            }catch(NullPointerException e){
	            }
			}	
        }
        //finalizar de vincular una cuenta de box
        if (requestCode == aLayer.AUTH_BOX_REQUEST && aLayer.isNewBAccount()) {
        	aLayer.newBAccountFinish(resultCode, data);
        	restartAccountsFragment();
        } 
        
    }
    
    //actualizar el directorio actual
    public void actualizarDirectorio(){
    	aLayer.actualize(itemCallback);
    }
    
    //ordear alfabeticamente el listview, y carpetas y archivos por separado
    private void orderDirectory(){
    	//por un lado los archivos
		ArrayList<Item> aux=new ArrayList<Item> ();
        for(int i=0; i<listDirectory.size();i++){
        	if(listDirectory.get(i).getName().indexOf(".")>=0){
        		aux.add(listDirectory.get(i));
        	}
        }
        
        Collections.sort(aux, new Comparator<Item>(){
       	 
			public int compare(Item o1, Item o2) {
				return (o1.getName().toLowerCase()).compareTo(o2.getName().toLowerCase());
			}
			
			
		});
        //por otro las carpetas
        ArrayList<Item> auxx=new ArrayList<Item> ();
        for(int i=0; i<listDirectory.size();i++){
        	if(listDirectory.get(i).getName().indexOf(".")<0){
        		auxx.add(listDirectory.get(i));
        	}
        }
        
        Collections.sort(auxx, new Comparator<Item>(){
       	 
			@Override
			public int compare(Item o1, Item o2) {
				return (o1.getName().toLowerCase()).compareTo(o2.getName().toLowerCase());
			}
						
		});
        
        listDirectory.clear();
        //lo unimos
        for(int i=0; i<auxx.size();i++){
        	listDirectory.add(auxx.get(i));
        }

        for(int i=0; i<aux.size();i++){
        	listDirectory.add(aux.get(i));
        }
        
        
        archivos=(ListView) findViewById(R.id.archivos); 
		archivos.setAdapter(new CustomIconLabelAdapter(GetBoxActivity.this));
    }
    
    //mostrar toast
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    
    //comprobar si hay red
    private boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

    	NetworkInfo netInfo = cm.getActiveNetworkInfo();

    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    	return true;
    	}

    	return false;
    }
    
    //acciones del boton atras
    public void onBackPressed(){
    	//si no estamos en el navegador de ficheros
    	if(boolArchives==false){
    		mPrefs = this.getSharedPreferences("LOGIN",0);
            if(!mPrefs.getBoolean("logueado",false)){
	    		//si estamos en login cerramos la app
            	if(!inRegister){
	        		this.finish();
	    		//si estamos en registro volvemos al login
            	}else{
	    			showLogIn();
					button=0;
	    		}
    		}else{
    			//si estamos en acerca de volcemos al anterior
    			if(actualDrawer==7){
	    			selectItem(lastDrawer);
    			//sino cerramos la app
    			}else{
    				this.finish();
    			}
    		}
    	//si estamos en el navegador de ficheros	
    	}else{
    		//si se permite volve atras
    		if(aLayer.enableBack()){
    			//se va al directorio anterior6
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
	    		//si estamos en el directorio raiz se cierra la app
	    		}else{
	    			this.finish();
	    		}
    		}
    	}
    	
    }   
    
    //finalizar el login
    public void finishLogin(){        
        Intent intent = new Intent(this,GetBoxActivity.class);
		startActivity(intent);
		hideDialog();
        this.finish();
    }
    
    //actualizar una cuenta de box que de error al listar
    public void actualize(int boxAccount){
    	if(!isOnline()){
			showToast("Error de red. Compruebe su conexión a Internet.");
		}else{
			aLayer.actualizeBox(itemCallback,boxAccount);
        	showDialog("Sincronizando...");
		}	
    }
    
    //login
    private void login(boolean result){
		EditText lgnUser=(EditText) findViewById(R.id.edtTxtLgnUser);
		EditText lgnPass=(EditText) findViewById(R.id.edtTxtLgnPass);
    	//login correcto
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
		//fallo de autenticacion
    	}else{  
			hideDialog();
			showToast("Nombre de usuario o contraseña incorrectos");
			lgnPass.setText("");   
			
		}
    }
    
    //comprobar que los campos esten correctos en el registro
    private void comprobarDuplicidad(int result){
    	EditText regUser=(EditText) findViewById(R.id.edtTxtRgstrUser);
		EditText regPass=(EditText) findViewById(R.id.edtTxtRgstrPass);
		EditText regEmail=(EditText) findViewById(R.id.edtTxtRgstrMail);
		EditText regName=(EditText) findViewById(R.id.edtTxtRgstrName);
    	switch (result){
		//registro correcto
    	case 0:
			mySql.registrar(regUser.getText().toString(),
					regPass.getText().toString(),
					regEmail.getText().toString(),regName.getText().toString());
			hideDialog();
			showLogIn();
			button=0;
		break;
		//nombre de usuario en uso
    	case 2:
			hideDialog();
			Toast.makeText(this, "El nombre de usuario no está disponible", Toast.LENGTH_LONG).show();	
			regUser.setText("");
		break;
		//email en uso
    	case 3:
			hideDialog();
			Toast.makeText(this, "El email introducido ya está siendo utilizado en otra cuenta", Toast.LENGTH_LONG).show();
			regEmail.setText("");
		break;
		//registro con exito si el usuario estaba borrado
    	case 5:
			hideDialog();
			Toast.makeText(this, "El usuario ha sido registrado correctamente",
    				Toast.LENGTH_LONG).show();
			showLogIn();
			button=0;
		break;
		//error de red o de bd
    	case 4:
			hideDialog();
			showToast("Error al conectar con la base de datos"); 
		break;
	}
    }
    
    
    //Callbacks
    
    //Para listar archivos
    private class ItemCallback implements AsyncTaskCompleteListener<ArrayList<Item>>{
    	public void onTaskComplete( ArrayList<Item> result){
    		onListingComplete(result);
    	} 
    }
    
    //Para hacer el login
    private class BooleanCallback implements AsyncTaskCompleteListener<Boolean>{
    	public void onTaskComplete( Boolean result){
    		login(result);
    	}
    }
    
    //Para hacer el registro
    private class IntCallback implements AsyncTaskCompleteListener<Integer>{
    	public void onTaskComplete( Integer result){
    		comprobarDuplicidad(result);
    	}
    }
}