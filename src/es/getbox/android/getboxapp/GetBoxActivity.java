package es.getbox.android.getboxapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import es.getbox.android.getboxapp.fragments.FragmentNewAccount;
import es.getbox.android.getboxapp.fragments.FragmentOptions;
import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.utils.Item;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GetBoxActivity extends Activity implements OnClickListener, AsyncTaskCompleteListener<ArrayList<Item>>{
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
	private Button buttonDropbox;
	private Button buttonBox;
	private Button buttonLgnLogin;
	private Button buttonLgnRegister;
	private Button buttonRgstrRegister;
	private Button buttonRgstrBack;
	private short button;
	
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
        
        aLayer=new AbstractionLayer(this);
		boolArchives=false;
		listDirectory=new ArrayList<Item>();		
		
		aLayer.startAutentication();
		
		mPrefs = this.getSharedPreferences("LOGIN",0);
        if (mPrefs.getBoolean("logueado",false)) {
        	showMain();
        	button=2;
        } else {
        	noTitle();
        	showLogIn();
        	button=0;
        }
    }
    
    protected void onResume() {
        super.onResume();
        aLayer.finishAutentication();        
        aLayer.newDBAccountFinish();
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
    	
    	if (true){
    	boolean visible=true;
    	if(actualDrawer>0) visible=false;    		
    	boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.actualizar).setVisible(!drawerOpen);
		menu.findItem(R.id.nueva_carpeta).setVisible(!drawerOpen);
		menu.findItem(R.id.subir).setVisible(!drawerOpen);
		menu.findItem(R.id.salir).setVisible(!drawerOpen);
    	
    	menu.findItem(R.id.actualizar).setVisible(visible);
        menu.findItem(R.id.nueva_carpeta).setVisible(visible);
        menu.findItem(R.id.subir).setVisible(visible);
    	}else{
    		menu.findItem(R.id.actualizar).setVisible(false);
    		menu.findItem(R.id.nueva_carpeta).setVisible(false);
    		menu.findItem(R.id.subir).setVisible(false);
    		menu.findItem(R.id.salir).setVisible(false);
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
        	listDirectory.clear();
        	aLayer.actualize(this);
        	return true;
        
        case R.id.nueva_carpeta:
        	AlertDialog dialog = crearCarpetaDialog();
        	dialog.show();
        	return true;
        
        case R.id.subir_archivo:
        	goFromUpload=true;
        	Intent file_explorer = new Intent(GetBoxActivity.this,FileExplorerActivity.class);
            startActivityForResult(file_explorer, 555);// <-- �?
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void initNewAccount(){
    	buttonDropbox=(Button) findViewById(R.id.buttonNewDropbox);
        buttonDropbox.setOnClickListener(this);
    	buttonBox=(Button) findViewById(R.id.buttonNewBox);
    	buttonBox.setOnClickListener(this);
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
            aLayer.initFiles(this);
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
        	fragment = new FragmentNewAccount();
    		args.putInt(FragmentNewAccount.ARG_NEWACCOUNT_NUMBER, position);
            fragment.setArguments(args);
            boolArchives=false;
        break;
        case 3:
        	fragment = new FragmentOptions();
    		args.putInt(FragmentOptions.ARG_OPTION_NUMBER, position);
            fragment.setArguments(args);
            boolArchives=false;
        break;
        case 4:
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
        
       // if(position==0) actualizar("/",true);
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
    
    private AlertDialog crearCarpetaDialog(){ 
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText nCarpeta = new EditText(this);
    	builder.setTitle("Select Items");
    	builder.setView(nCarpeta); 
    	builder.setTitle("Nueva Carpeta"); 
   	 	builder.setMessage("Escribe el nombre de la carpeta"); 
   	 	builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() { 
   	 		public void onClick(DialogInterface dialog, int whichButton) { 	
            	String nC=nCarpeta.getText().toString();
            	listDirectory.add(aLayer.uploadFolder(nC));
            	orderDirectory();
   	 		} 
   	 	});
   	 	builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() { 
   	 		public void onClick(DialogInterface dialog, int whichButton) { 
   	 			
   		 	}//OnClick 
   	 	});
   	 	AlertDialog dialog = builder.create();
    	return dialog;
     } 
    
    public void onClick(View v) { 
    	switch (button) {
    		case 0:
    			if(v.getId()==buttonLgnLogin.getId()){
    				EditText lgnUser=(EditText) findViewById(R.id.edtTxtLgnUser);
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
			        
			        Intent intent = new Intent(this,GetBoxActivity.class);
					startActivity(intent);
			        this.finish();
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
					showLogIn();
					button=0;
				}
    		break;
    		
    		case 2:
    			buttonDropbox=(Button) findViewById(R.id.buttonNewDropbox);
	        	buttonBox=(Button) findViewById(R.id.buttonNewBox);
	
	        	if (v.getId()==buttonDropbox.getId() ){
	    			aLayer.newDBAccount();
	    		}
	    		if (v.getId()==buttonBox.getId() ){
	    			aLayer.newBAccount(this);
	    		}
    		break;
    	}
	}//onClick
    
    
    public void onTaskComplete(ArrayList<Item> result) {
    	try{
    		archivos = (ListView) findViewById (R.id.archivos);
    		archivos.setEnabled(false);
    		
	    	if(result.size()>0){
	    		if(result.get(0).getName().equals("")){
	    			aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
	    		}else{
		    		aLayer.saveDirectory(result,result.get(0).getLocation(),result.get(0).getAccount());
			    	for(int i=0;i<result.size();i++){
			    		listDirectory.add(result.get(i));
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
    	}catch(Exception e){}
    }
    
    class Myonclicklistneer implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        	if( (listDirectory.get(position).getName()).indexOf(".")<0 ) {
        		
        		archivos = (ListView) findViewById (R.id.archivos);
        		archivos.setEnabled(false);
        		String aux=listDirectory.get(position).getName();
        		aLayer.navigateTo(listDirectory.get(position).getName(),GetBoxActivity.this);
        		listDirectory.clear();
        		mTitle = aux;
                getActionBar().setTitle(aLayer.getRoute());
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
                archivos.getAdapter().getItem(info.position).toString());
     
            inflater.inflate(R.menu.context_menu_archivos, menu);
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
                    		showToast("Borrado con exito");
                			break;

            	        case DialogInterface.BUTTON_NEGATIVE:
            	        	showToast("Borrado cancelado");
            	            break;
            	        }
            	    }
            	};

            	AlertDialog.Builder builder = new AlertDialog.Builder(GetBoxActivity.this);
            	builder.setMessage("�Seguro que deseas borrar?").setPositiveButton("Si", dialogClickListener)
            	    .setNegativeButton("No", dialogClickListener).show();
                return true;
            case R.id.deleteDBAccount:
            	aLayer.deleteAccount("dropbox",info.position);
            	restartAccountsFragment();
    			return true;
            case R.id.deleteBAccount:
            	aLayer.deleteAccount("box",info.position);
                restartAccountsFragment();
            	return true;
            default:
            	return super.onContextItemSelected(item);
        }
    }
    
    private void restartAccountsFragment(){
    	Fragment fragment;
        Bundle args = new Bundle();
    	fragment = new FragmentAccounts();
    	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, 1);
    	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
    	args.putStringArrayList("arrayB", aLayer.getbAccounts());
    	fragment.setArguments(args);
    	boolArchives=false;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
    
    
    
 // This is what gets called on finishing a media piece to import
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
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
        if (resultCode == RESULT_OK && requestCode == 555) {
            try{
            	if (data.hasExtra("archivo_seleccionado")) {
                listDirectory.add(aLayer.uploadFile(data.getExtras().getString("archivo_seleccionado")));
                orderDirectory();
            }
            }catch(NullPointerException e){
            	Log.i(TAG,"Se ha saido del gestor sin seleccionar archivo");
            }
        }
        if (requestCode == aLayer.AUTH_BOX_REQUEST && aLayer.isNewBAccount()) {
        	aLayer.newBAccountFinish(resultCode, data);
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
    
    public void onBackPressed(){
    	if(boolArchives==false){
    		this.finish();
    	}else{
    		if(aLayer.enableBack()){
	    		if(aLayer.goBack(this)){
	    			listDirectory.clear();
	    			if(aLayer.getPosicionActual()>0){
	    				mTitle = aLayer.getRoute();
	                    getActionBar().setTitle(mTitle);
	    			}
	    			else{
	    				mTitle = mOptionTitles[0];
	                    getActionBar().setTitle(mTitle);
	    			}
	    		}else{
	    			this.finish();
	    		}
    		}
    	}
    	
    }   
}