package es.getbox.android.getboxapp.fragments;

import es.getbox.android.getboxapp.GetBoxActivity;
import es.getbox.android.getboxapp.R;
import es.getbox.android.getboxapp.mysql.MySQL;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class FragmentOptions extends Fragment {
    public static final String ARG_OPTION_NUMBER = "option_number";

    private TextView userName;
    private Button refresh;
    private Button delete;
    
    public FragmentOptions() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_config, container, false);
        int i = getArguments().getInt(ARG_OPTION_NUMBER);
        String option = getResources().getStringArray(R.array.options_array)[i];
        getActivity().setTitle(option);
        final SharedPreferences mPrefs = getActivity().getSharedPreferences("LOGIN",0);
        userName= (TextView) rootView.findViewById (R.id.textConfigUser);
        userName.setText(getArguments().getString("userName"));
        final EditText confPassOld=(EditText) rootView.findViewById(R.id.edtTxtConfigPassOld);
        final EditText confPass=(EditText) rootView.findViewById(R.id.edtTxtConfigPass1);
		final EditText confRePass=(EditText) rootView.findViewById(R.id.edtTxtConfigPass2);
		final EditText confPassDel=(EditText) rootView.findViewById(R.id.edtTxtConfigPassDel);
		refresh = (Button) rootView.findViewById(R.id.buttonCnfgChange);
		refresh.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v)
            {          
            	MySQL mysql=new MySQL(getActivity());
            	InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		        inputMethodManager.hideSoftInputFromWindow(confPass.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confRePass.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confPassOld.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confPassDel.getWindowToken(), 0);
				if(confPassOld.getText().toString().equals("") || confPass.getText().toString().equals("") || confRePass.getText().toString().equals("") ){
		        	Toast.makeText(getActivity(), "Hay campos incompletos", Toast.LENGTH_LONG).show();
    			}else{
    				if(!mysql.comprobarContrasena(mPrefs.getString("userName",""), confPassOld.getText().toString())){
    					Toast.makeText(getActivity(), "La contraseña actual no es correcta", Toast.LENGTH_LONG).show();	
	        		}else{
			        	if(!confRePass.getText().toString().equals(confPass.getText().toString())){
			        		Toast.makeText(getActivity(), "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
						}else{
							if(mysql.refreshUser(mPrefs.getString("userName",""), confPass.getText().toString())){
								
								Toast.makeText(getActivity(), "Contraseña cambiada con éxito", Toast.LENGTH_LONG).show();	
		        			}else{
								Toast.makeText(getActivity(), "Ha ocurrido un error al conectar con la Base de Datos", Toast.LENGTH_LONG).show();	
		        			}
		    			}
    				}
    			}
            } 
		});
		delete = (Button) rootView.findViewById(R.id.buttonCnfgDelete);
		delete.setOnClickListener(new OnClickListener()
		   {
            @Override
            public void onClick(View v)
            {
            	InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		        inputMethodManager.hideSoftInputFromWindow(confPass.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confRePass.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confPassOld.getWindowToken(), 0);
		        inputMethodManager.hideSoftInputFromWindow(confPassDel.getWindowToken(), 0);
				
            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            	    @Override
            	    public void onClick(DialogInterface dialog, int which) {
            	    	MySQL mysql=new MySQL(getActivity());
                    	
            	    	switch (which){
            	        case DialogInterface.BUTTON_POSITIVE:
            	        	if(!mysql.comprobarContrasena(mPrefs.getString("userName",""), confPassDel.getText().toString())){
            					Toast.makeText(getActivity(), "La contraseña es incorrecta", Toast.LENGTH_LONG).show();	
        	        		}else{
	            	        	if(mysql.deleteUser(mPrefs.getString("userName",""))){
	            					SharedPreferences.Editor ed = mPrefs.edit();
	            			        ed.putBoolean("logueado",false);
	            			        ed.commit();
	            			        ed = mPrefs.edit();
	            			        ed.putBoolean("deleteAccount",true);
	            			        ed.commit();
	            		        	Intent intento = new Intent(getActivity(),GetBoxActivity.class);
	            		        	startActivity(intento);
	            		        	getActivity().finish();
	            				}else{
	            					Toast.makeText(getActivity(), "Ha ocurrido un error al conectar con la Base de Datos", Toast.LENGTH_LONG).show();	
	            				}
        	        		}
            	        break;

            	        case DialogInterface.BUTTON_NEGATIVE:
            	        	break;
            	        }
            	    }
            	};
            	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            	builder.setMessage("¿Seguro que deseas eliminar la cuenta?").setPositiveButton("Si", dialogClickListener)
            	    .setNegativeButton("No", dialogClickListener).show();
            	
            } 
  }); 

        
        return rootView;
    }
}
