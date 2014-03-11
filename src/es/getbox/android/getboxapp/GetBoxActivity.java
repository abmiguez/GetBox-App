package es.getbox.android.getboxapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

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
	
	//Listar los directorios
	private ArrayList<Item> listDirectory;
	private boolean boolArchives;
	
	//Interfaz    
	private ListView archivos;
	private Button buttonDropbox;
	private Button buttonBox;
	
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
    	
        File file=new File(rutaGetbox);
        if(!file.exists())
        	file.mkdirs();
        file=new File(rutaBD);
        if(!file.exists())
            file.mkdirs();
        
        if (savedInstanceState != null) {
            mCameraFileName = savedInstanceState.getString("mCameraFileName");   
        }  
        
        aLayer=new AbstractionLayer(this);
		
		boolArchives=false;
		listDirectory=new ArrayList<Item>();		
		
		aLayer.startAutentication();
		
        showMain();
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
            String outPath = Environment.getExternalStorageDirectory().getPath()+"/MyDropbox/" + newPicFile;
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
            Date dates = new Date();
            DateFormat dfs = new SimpleDateFormat("dd-MM-yyyy kk:mm");
            String fecha = dfs.format(dates);
            return true;	
        case R.id.salir:
        	this.finish();
			return true;	
        case R.id.actualizar:
        	actualizar("",true);
        	return true;
        
        case R.id.nueva_carpeta:
        	AlertDialog dialog = crearCarpetaDialog();
        	dialog.show();
        	return true;
        
        case R.id.subir_archivo:
        	goFromUpload=true;
        	Intent file_explorer = new Intent(GetBoxActivity.this,FileExplorerActivity.class);
            startActivityForResult(file_explorer, 555);// <-- ¿?
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
    		args.putInt(FragmentArchives.ARG_ARCHIVE_NUMBER, position);
            fragment.setArguments(args);
            aLayer.getFiles(this);
            aLayer.restartRoutes();
    		boolArchives=true;
        break;
        case 1:        	
        	fragment = new FragmentAccounts();
        	args.putInt(FragmentAccounts.ARG_ACCOUNTS_NUMBER, position);
        	args.putStringArrayList("arrayDB", aLayer.getDbAccounts());
        	args.putStringArrayList("arrayB", aLayer.getbAccounts());
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
        	//mLoggedIn=false;
			SharedPreferences.Editor ed = mPrefs.edit();
	        ed.putBoolean("intent",true);
	        ed.commit();
	        ed = mPrefs.edit();
	        //ed.putBoolean("logueado",mLoggedIn);
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
       //if(mPrefs.getBoolean("logueado",false))
        mDrawerToggle.syncState();
        
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
            	//dsp.uploadFolder(rutaActual+nC,nC);
            	actualizar("",false);
            	Date date = new Date();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy kk:mm");
                String fecha = df.format(date);            	
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
    	buttonDropbox=(Button) findViewById(R.id.buttonNewDropbox);
    	buttonBox=(Button) findViewById(R.id.buttonNewBox);
		if (v.getId()==buttonDropbox.getId() ){
			aLayer.newDBAccount();
		}
		if (v.getId()==buttonBox.getId() ){
			aLayer.newBAccount(this);
		}
	}//onClick
    
    private void actualizar(String ruta,boolean dialog){
    	if (ruta==""){
    		//dsp.getFiles(rutaActual,this,dialog);
    	}else{ 
    		//dsp.getFiles(ruta,this,dialog);
    	}
    	
    }
    
    public void onTaskComplete(ArrayList<Item> result) {
    	try{
	    	if(result.size()>0){
		    	for(int i=0;i<result.size();i++){
		    		listDirectory.add(result.get(i));
		    	}
	    	}
	    	Set<Item> hs = new LinkedHashSet<Item>();
	        hs.addAll(listDirectory);
	        listDirectory.clear();
	        listDirectory.addAll(hs);
	    	if(listDirectory.size()>0){    		
	    		Myonclicklistneer myonclicklistneer = new Myonclicklistneer();
	    		archivos = (ListView) findViewById (R.id.archivos);
	            archivos.setAdapter(new CustomIconLabelAdapter(this));
	            registerForContextMenu(archivos);
	            archivos.setOnItemClickListener(myonclicklistneer);
	    	}else{
	    		showToast("null");
	    	}    	
    	}catch(Exception e){}
    }
    
    class Myonclicklistneer implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        	if( (listDirectory.get(position).getName()).indexOf(".")<0 ) {
        		
        		
        		/*rutaActual=(rutaActual.concat(folderName.get(position))).concat("/");
	        	posicionActual++;
	        	carpetaActual[posicionActual]=(folderName.get(position)).concat("/");
	        	actualizar("",false);
	        	if(posicionActual!=0){
	        		setTitle(rutaActual.substring(1, rutaActual.length()-1));
	        	}*/
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
     
            inflater.inflate(R.menu.context_menu_accounts, menu);
        }
        
        if(v.getId() == R.id.BAccounts)
        {
            AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo)menuInfo;
            ListView accounts = (ListView) findViewById (R.id.BAccounts);
            menu.setHeaderTitle(
                accounts.getAdapter().getItem(info.position).toString());
     
            inflater.inflate(R.menu.context_menu_accounts, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info =
    	        (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.descargar:
            	//dsp.downloadFile(Environment.getExternalStorageDirectory().getPath()+"/MyDropbox/"+folderName.get(info.position), rutaActual+folderName.get(info.position)); 
            	Date date = new Date();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy kk:mm");
                String fecha = df.format(date);
    			return true;
            case R.id.borrar:
            	//positionArray=info.position;
            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    @Override
            	    public void onClick(DialogInterface dialog, int which) {
            	        switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	        	//dsp.deleteFile(rutaActual+folderName.get(positionArray));
                    		actualizar("",false);
            	        	showToast("Borrado con exito");
            	        	Date date = new Date();
                            DateFormat df = new SimpleDateFormat("dd-MM-yyyy kk:mm");
                            String fecha = df.format(date);
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
            case R.id.deleteAccount:
            	aLayer.deleteAccount();
    			return true;
            default:
            	return super.onContextItemSelected(item);
        }
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
                File file = new File(mCameraFileName);

                if (uri != null) {
                	//dsp.upPicture(this, PHOTO_DIR, file);
                }
            }else {
                Log.w(TAG, "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
        if (resultCode == RESULT_OK && requestCode == 555) {
            try{
            	if (data.hasExtra("archivo_seleccionado")) {
            //	dsp.uploadFile(rutaActual, data.getExtras().getString("archivo_seleccionado"));
            	Date date = new Date();
                DateFormat df = new SimpleDateFormat("dd-MM-yyyy kk:mm");
                String fecha = df.format(date);
            }
            }catch(NullPointerException e){
            	Log.i(TAG,"Se ha saido del gestor sin seleccionar archivo");
            }
        }
        if (requestCode == aLayer.AUTH_BOX_REQUEST && aLayer.isNewBAccount()) {
        	aLayer.newBAccountFinish(resultCode, data);          
        } 
        
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
    /*
    public void onBackPressed(){
    	switch(nlayout){
    	case 0:	
    		this.finish();
    	break;
    	
    	case 1:
    		if (mLoggedIn && posicionActual==0) {
    			this.finish();
    		}else{
    			if(mLoggedIn){
    				rutaActual=rutaActual.replace(carpetaActual[posicionActual],"");
    				posicionActual--;
    				actualizar("",false);
    	    		if(posicionActual!=0){
    	        		setTitle(rutaActual.substring(1, rutaActual.length()-1));
    	        	}else{
    	        		setTitle("Archivos");    	       
    	        	}
    			}else{
    				showLogIn();
    			}    			
    		}
    	break;

    	default:
    		this.finish();
    		break;
    	}
    	
    }   */
}