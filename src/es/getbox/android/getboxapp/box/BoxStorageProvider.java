package es.getbox.android.getboxapp.box;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.ObjectUtils.Null;

import android.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.activities.OAuthActivity;
import com.box.boxandroidlibv2.dao.BoxAndroidOAuthData;
import com.box.boxjavalibv2.authorization.OAuthRefreshListener;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.interfaces.IAuthData;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileUploadRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxFolderRequestObject;

import es.getbox.android.getboxapp.GetBoxActivity;
import es.getbox.android.getboxapp.interfaces.AsyncTaskCompleteListener;
import es.getbox.android.getboxapp.mysql.MySQL;
import es.getbox.android.getboxapp.utils.Item;
import es.getbox.android.getboxapp.utils.SQL;

public class BoxStorageProvider { 

	//Atributos
	private Context context;
	private int boxAccount;
	private BoxAndroidClient mClient;
	public static final String CLIENT_ID = "nq0so01we5nic9rqysh6zmn5fd1g15ma";
    public static final String CLIENT_SECRET = "g5O8FfBPaeTYJR35aVhVfZ9VURBn2rZ6";
    public static final String REDIRECT_URL = "http://localhost/";
    private SQL sql;
    private ArrayList<Item> currentDirectory;
    private ArrayList<String> directories;
	private MySQL mySql;
	private SharedPreferences mPrefs;
	private GetBoxActivity gba;
	
	//Constructor
    public BoxStorageProvider(Context context, int newBoxAccount,GetBoxActivity g){
    	this.context=context;
    	this.boxAccount=newBoxAccount;
    	this.sql=new SQL(context);
    	this.currentDirectory=new ArrayList<Item>();
    	this.directories=new ArrayList<String>();
    	this.directories.add("0");
    	this.mySql=new MySQL(context);
    	this.mPrefs= context.getSharedPreferences("LOGIN",0);
    	this.gba=g;
    }
        
    //terminar la autenticacion de una nueva cuenta 
    @SuppressWarnings({ "unused", "static-access" })
	public void onAuthenticated(int resultCode, Intent data) {
		
    	if (Activity.RESULT_OK != resultCode) {
   		Toast.makeText(context, "Fallo al autenticar", Toast.LENGTH_LONG).show();
       }
       else {
    	   BoxAndroidOAuthData oauth = data.getParcelableExtra(OAuthActivity.BOX_CLIENT_OAUTH);
           BoxAndroidClient client = new BoxAndroidClient(this.CLIENT_ID, this.CLIENT_SECRET, null, null);
           client.authenticate(oauth);
           if (client == null) {
               Toast.makeText(context, "Fallo al autenticar", Toast.LENGTH_LONG).show();
           }
           else {
           	this.mClient=client;
           	String accesstoken=oauth.getAccessToken();
           	mClient.addOAuthRefreshListener(new OAuthRefreshListener() {

                @Override
                public void onRefresh(IAuthData newAuthData) {
                	try {
                		BoxOAuthToken oauthObject=mClient.getAuthData();
						String refreshtoken=newAuthData.getRefreshToken();
						sql.openDatabase();
			           	sql.updateBoxToken(boxAccount, refreshtoken);
			            sql.closeDatabase();
			            mySql.actualizarBoxToken(getUserName(), refreshtoken, mPrefs.getString("userName",""));
			            oauthObject.setRefreshToken(refreshtoken);
			            mClient.authenticate(oauthObject);
			            
					} catch (Exception e) {
						
					}                	
                }

            });

           
           	this.sql.openDatabase();
           	sql.insertBox(boxAccount, accesstoken,getUser(),getSpace());
           	mySql.insertBox(accesstoken, getUserName(), getSpaceUsed(), mPrefs.getString("userName",""));
            Toast.makeText(context, "Autenticado con exito", Toast.LENGTH_LONG).show();
            refresh();
            this.sql.closeDatabase();
           }
       }

   }
    
    //terminar la autenticación de una cuenta ya vinculada
    public void autenticate(){ 
    	RefreshCallback c=new RefreshCallback();
		this.sql.openDatabase();
    	mySql.getRefresh(mPrefs.getString("userName",""), getUserName(),c);
		sql.closeDatabase();
    }
    
    //terminar la autenticacion de una cuenta ya vinculada que da error
    @SuppressWarnings({ "unused", "static-access" })
	public void sincAutenticate(){ 
		BoxOAuthToken oauthObject=new BoxOAuthToken();
    	this.sql.openDatabase();
    	oauthObject.setAccessToken(sql.getBoxAccessTokens(boxAccount));
    	if(!sql.getBoxRefreshTokens(boxAccount).equals("")){
    		oauthObject.setRefreshToken(sql.getBoxRefreshTokens(boxAccount));
    	}
    	BoxAndroidClient client = new BoxAndroidClient(this.CLIENT_ID, this.CLIENT_SECRET, null, null);

		this.sql.closeDatabase();
    	client.authenticate(oauthObject);
		if (client == null) {
			Toast.makeText(context, "Fallo al autenticar", Toast.LENGTH_LONG).show();
		}
		else {
        	this.mClient=client;
        	//mClient.
        	mClient.addOAuthRefreshListener(new OAuthRefreshListener() {

             @Override
             public void onRefresh(IAuthData newAuthData) {
            	 try{
            		BoxOAuthToken oauthObject=mClient.getAuthData();
					String refreshtoken=newAuthData.getRefreshToken();
					sql.openDatabase();
		           	sql.updateBoxToken(boxAccount, refreshtoken);
		            sql.closeDatabase();
		            mySql.actualizarBoxToken(getUserName(), refreshtoken, mPrefs.getString("userName",""));
		            oauthObject.setRefreshToken(refreshtoken);
		            mClient.authenticate(oauthObject);
            	 }catch(Exception e){
            		 
            	 }
             }
 
         });
        refresh();
        }
    }
    
    //refrescar el refresh token
    public void refresh(){
    	AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {
    		@Override
            protected Boolean doInBackground(Null... params) {
    			try {
    				mClient.getOAuthDataController().doRefresh();
    				return true;
    			} catch (AuthFatalFailureException e) {
    				e.printStackTrace();
    				return false;
    			}
    		}
        };
        task.execute();
    }
    
    //recuperar el nombre de usuario de la bd local
    public String getUserName(){
		this.sql.openDatabase();
    	String account_aux= sql.getBoxUserName(boxAccount);
		this.sql.closeDatabase();
		return account_aux;
	}
    
    //recuperar el nombre de usuario de la bd remota
    public String getUser(){
    	BoxGetUser dgu=new BoxGetUser(mClient);
		try {
			dgu.execute();
			String a=dgu.get();
			return a;
		} catch (InterruptedException e) {
			return "";
		} catch (ExecutionException e) {
			return "";
		}
    }
    
    //recuperar el espacio disponible de la bd local
    public long getSpaceUsed(){
		this.sql.openDatabase();
    	long space= sql.getBoxSpace(boxAccount);
		this.sql.closeDatabase();
		return space;
	}
    
    //actualizar el espacio disponible en la bd local
    public void setSpaceUsed(long space){
		this.sql.openDatabase();
    	sql.updateBoxSpace(boxAccount,space);
		this.sql.closeDatabase();
	}
    
    //recuperar el espacio disponible en la bd remota
    public long getSpace(){
    	BoxGetSpace dgs=new BoxGetSpace(mClient,null);
		try {
			dgs.execute();
			long a=dgs.get();
			return a;
		} catch (InterruptedException e) {
			return 0;
		} catch (ExecutionException e) {
			return 0;
		}
    }
    
    //actualizar el espacio disponible en la bd remota
    public void setSpace(){
    	BoxSpaceCallback bsc= new BoxSpaceCallback(); 
    	BoxGetSpace dgs=new BoxGetSpace(mClient,bsc);
		dgs.execute();
    }
    
    //listar un directorio
    public void getFiles(String directory_path,AsyncTaskCompleteListener<ArrayList<Item>> cb,boolean dialog){
    	BoxListDirectory task = new BoxListDirectory(directory_path,context,getUserName(),cb, this.getClient(),boxAccount);
        task.execute();
    }
    
    //descargar un archivo
    public void downloadFile(String file_name, String file_id) {
        final String fPath= file_id;
        final String fName=  file_name;
        AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {

        	private CharSequence contentText;
            private CharSequence contentTitle;
            private PendingIntent contentIntent;
            private int HELLO_ID = 1;
            private long time;
            private int icon;
            private CharSequence tickerText;
            private NotificationManager notificationManager;
            private Notification notification;
            
            @SuppressWarnings("deprecation")
			public void downloadNotification(){
               	 String ns = Context.NOTIFICATION_SERVICE;
                    notificationManager = (NotificationManager) context.getSystemService(ns);

                    icon = R.drawable.stat_sys_download;
                    //the text that appears first on the status bar
                    tickerText = "Descargando archivo...";
                    time = System.currentTimeMillis();
                    notification = new Notification(icon, tickerText, time);
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    
                    contentTitle = fName;
                    //the text that needs to change
                    contentText = "Descargando...";
                    Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                    notificationIntent.setType("audio/*");
                    contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
                    notificationManager.notify(HELLO_ID, notification);
               }
               
               private Intent openFile() {
                   // Create URI
               	String url=fName;
                   File file=new File(url);
                   Uri uri = Uri.fromFile(file);
                   
                   Intent intent = new Intent(Intent.ACTION_VIEW);
                   // Check what kind of file you are trying to open, by comparing the url with extensions.
                   // When the if condition is matched, plugin sets the correct intent (mime) type, 
                   // so Android knew what application to use to open the file
                   if (url.toString().endsWith(".doc") || url.toString().endsWith(".docx")||
                   		url.toString().endsWith(".DOC") || url.toString().endsWith(".DOCX")) {
                       // Word document
                       intent.setDataAndType(uri, "application/msword");
                   } else if(url.toString().endsWith(".pdf") || url.toString().endsWith(".PDF")) {
                       // PDF file
                       intent.setDataAndType(uri, "application/pdf");
                   } else if(url.toString().endsWith(".ppt") || url.toString().endsWith(".pptx")||
                   		url.toString().endsWith(".PPT") || url.toString().endsWith(".PPTX")) {
                       // Powerpoint file
                       intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                   } else if(url.toString().endsWith(".xls") || url.toString().endsWith(".xlsx")||
                   		url.toString().endsWith(".XLS") || url.toString().endsWith(".XLSX")) {
                       // Excel file
                       intent.setDataAndType(uri, "application/vnd.ms-excel");
                   } else if(url.toString().endsWith(".zip") || url.toString().endsWith(".rar")|| url.toString().endsWith(".gz")||
                   		url.toString().endsWith(".ZIP") || url.toString().endsWith(".RAR")|| url.toString().endsWith(".GZ")) {
                       // WAV audio file
                       intent.setDataAndType(uri, "package/*");
                   } else if(url.toString().endsWith(".rtf")||url.toString().endsWith("RTF")) {
                       // RTF file
                       intent.setDataAndType(uri, "application/rtf");
                   } else if(url.toString().endsWith(".wav") || url.toString().endsWith(".mp3")||
                   		url.toString().endsWith(".WAV") || url.toString().endsWith(".MP3")) {
                       // WAV audio file
                       intent.setDataAndType(uri, "audio/x-wav");
                   } else if(url.toString().endsWith(".gif")||url.toString().endsWith(".GIF")) {
                       // GIF file
                       intent.setDataAndType(uri, "image/gif");
                   } else if(url.toString().endsWith(".jpg") || url.toString().endsWith(".jpeg") || url.toString().endsWith(".png")||
                   		url.toString().endsWith(".JPG") || url.toString().endsWith(".JPEG") || url.toString().endsWith(".PNG")) {
                       // JPG file
                       intent.setDataAndType(uri, "image/jpeg");
                   } else if(url.toString().endsWith(".txt")||url.toString().endsWith("TXT")) {
                       // Text file
                       intent.setDataAndType(uri, "text/plain");
                   } else if(url.toString().endsWith(".3gp") || url.toString().endsWith(".mpg") || url.toString().endsWith(".mpeg") || url.toString().endsWith(".mpe") || url.toString().endsWith(".mp4") || url.toString().endsWith(".avi")||
                   		url.toString().endsWith(".3GP") || url.toString().endsWith(".MPG") || url.toString().endsWith(".MPEG") || url.toString().endsWith(".MPE") || url.toString().endsWith(".MP4") || url.toString().endsWith(".AVI")) {
                       // Video files
                       intent.setDataAndType(uri, "video/*");
                   } else if(url.toString().endsWith(".apk")||url.toString().endsWith(".APK") ) {
                       // Apk files
                       intent.setDataAndType(uri, "application/vnd.android.package-archive");
                   } else {        	
                       //if you want you can also define the intent type for any other file
                       
                       //additionally use else clause below, to manage other unknown extensions
                       //in this case, Android will show all applications installed on the device
                       //so you can choose which application to use
                       intent.setDataAndType(uri, "*/*");
                   }
                   
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                   return intent;
               }
        	
            @SuppressWarnings("deprecation")
			@Override
            protected void onPostExecute(Boolean result) {
            	if (result) {
            		contentText =  "Descarga completada";
            		tickerText = "Descargado con éxito";
                    icon= R.drawable.stat_sys_download_done;
                    notification = new Notification(icon, tickerText, time);
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    contentIntent=PendingIntent.getActivity(context, 0, openFile(), 0);
                    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
                    notificationManager.notify(HELLO_ID, notification);
                } else {
                	contentText =  "Error al descargar el archivo";
            		tickerText = "Error al descargar el archivo";
                    icon= R.drawable.stat_sys_download_done;
                    notification = new Notification(icon, tickerText, time);
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
                    notificationManager.notify(HELLO_ID, notification);
                	
                }
            	super.onPostExecute(result);
            }

            @Override
            protected void onPreExecute() {
            	super.onPreExecute();
            	downloadNotification();
            }

            @Override
            protected Boolean doInBackground(Null... params) {
                BoxAndroidClient client = mClient;
                try {
                    File f = new File(Environment.getExternalStorageDirectory().getPath()+"/GetBox/", fName);
                    System.out.println(f.getAbsolutePath());
                    client.getFilesManager().downloadFile(fPath, f, null, null);
                    
                }
                catch (Exception e) {
                	return false;
                }
                return true;
            }
        };
        task.execute();
    }

    //subir un archivo
    public void uploadFile(String file_name, String file_id) {
        final String fPath=file_id;
        final String fName=file_name;
        AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {

            @Override
            protected void onPostExecute(Boolean result) {
            	if(result){
            		Toast.makeText(context, fName+" subido con éxito", Toast.LENGTH_LONG).show();
            		gba.actualizarDirectorio();
            	}else{
            		Toast.makeText(context, "Ha ocurrido un error mientras se subía el archivo", Toast.LENGTH_LONG).show();
            	}
            	super.onPostExecute(result);
            }

            @Override
            protected void onPreExecute() {
            	Toast.makeText(context, "Subiendo archivo...", Toast.LENGTH_LONG).show();
            	super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Null... params) {
                BoxAndroidClient client = mClient;
                try {
                    File file = new File(fName);                    
                    client.getFilesManager().uploadFile(
                        BoxFileUploadRequestObject.uploadFileRequestObject(fPath, file.getName(), file, client.getJSONParser()));
                }
                catch (Exception e) {
                	return false;
                }
                return true;
            }
        };
        task.execute();
    }    
    
    
    //eliminar un archivo
    public void deleteFile(String file_name, String file_id) {
        final String fPath=file_id;
        final String fName=file_name;
        AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {

            @Override
            protected void onPostExecute(Boolean result) {
            	if(result){
            		Toast.makeText(context, fName+" eliminado con éxito", Toast.LENGTH_LONG).show();
            		
            	}else{
            		Toast.makeText(context, "Ha ocurrido un error mientras se eliminaba el archivo", Toast.LENGTH_LONG).show();
            		gba.actualizarDirectorio();
            	}
            	super.onPostExecute(result);
            }

            @Override
            protected Boolean doInBackground(Null... params) {
                BoxAndroidClient client = mClient;
                try {
                	BoxFileRequestObject requestObj =
                		    BoxFileRequestObject.deleteFileRequestObject();
                		client.getFilesManager().deleteFile(fPath, requestObj);
                }
                catch (Exception e) {
                	return false;
                }
                return true;
            }
        };
        task.execute();
    }    
    
    //eliminar una carpeta
    public void deleteFolder(String file_name, String file_id) {
        final String fPath=file_id;
        final String fName=file_name;
        AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {

            @Override
            protected void onPostExecute(Boolean result) {
            	if(result){
            		Toast.makeText(context, fName+" eliminado con éxito", Toast.LENGTH_LONG).show();
            	}else{
            		Toast.makeText(context, "Ha ocurrido un error mientras se eliminaba el archivo", Toast.LENGTH_LONG).show();
            		gba.actualizarDirectorio();
            	}
            	super.onPostExecute(result);
            }

            @Override
            protected Boolean doInBackground(Null... params) {
                BoxAndroidClient client = mClient;
                try {
                	BoxFolderRequestObject requestObj =
                		    BoxFolderRequestObject.deleteFolderRequestObject(true);
                		client.getFoldersManager().deleteFolder(fPath, requestObj);
                }
                catch (Exception e) {
                	return false;
                }
                return true;
            }
        };
        task.execute();
    }   
    
    //crear una carpeta
    public void uploadFolder(String file_name, String file_id) {
        final String fPath=file_id;
        final String fName=file_name;
        AsyncTask<Null, Integer, Boolean> task = new AsyncTask<Null, Integer, Boolean>() {

            @Override
            protected void onPostExecute(Boolean result) {
            	if (result){
            		Toast.makeText(context, fName+" creada con éxito", Toast.LENGTH_LONG).show();
            		gba.actualizarDirectorio();
            	}else{
            		Toast.makeText(context, "Ha ocurrido un error mientras se creaba la carpeta", Toast.LENGTH_LONG).show();
            	}
            	super.onPostExecute(result);
            }

            @Override
            protected Boolean doInBackground(Null... params) {
                BoxAndroidClient client = mClient;
                try {
                    client.getFoldersManager().createFolder(
                        BoxFolderRequestObject.createFolderRequestObject(fName,fPath));
                }
                catch (Exception e) {
                	return false;
                }
                return true;
            }
        };
        task.execute();
    }   
    

    public void restartRoutes(){
    	directories.clear();
    	directories.add("0");
    	currentDirectory.clear();
    }
    
    
    //callbacks
    
    public class BoxSpaceCallback implements AsyncTaskCompleteListener<Long>{
    	public void onTaskComplete( Long result){
    		setSpaceUsed(result);
    	}
    }
    
    public class RefreshCallback implements AsyncTaskCompleteListener<String>{
    	@SuppressWarnings("unused")
		public void onTaskComplete( String result){
    		sql.openDatabase();
    		BoxOAuthToken oauthObject=new BoxOAuthToken();
        	oauthObject.setAccessToken(sql.getBoxAccessTokens(boxAccount));
        	sql.closeDatabase();
        	String reftoken=result;
        	sql.openDatabase();
        	if(!sql.getBoxRefreshTokens(boxAccount).equals(reftoken)){
    			sql.updateBoxToken(boxAccount, reftoken);
    		}
        	sql.closeDatabase();
        	sql.openDatabase();
        	if(!sql.getBoxRefreshTokens(boxAccount).equals("")){
        		oauthObject.setRefreshToken(sql.getBoxRefreshTokens(boxAccount));
        	}
        	BoxAndroidClient client = new BoxAndroidClient(CLIENT_ID, CLIENT_SECRET, null, null);

    		sql.closeDatabase();
        	client.authenticate(oauthObject);
    		if (client == null) {
    			Toast.makeText(context, "Fallo al autenticar", Toast.LENGTH_LONG).show();
    		}
    		else {
            	mClient=client;
            	//mClient.
            	mClient.addOAuthRefreshListener(new OAuthRefreshListener() {

                 @Override
                 public void onRefresh(IAuthData newAuthData) {
                	 try{
                		BoxOAuthToken oauthObject=mClient.getAuthData();
    					String refreshtoken=newAuthData.getRefreshToken();
    					sql.openDatabase();
    		           	sql.updateBoxToken(boxAccount, refreshtoken);
    		            sql.closeDatabase();
    		            mySql.actualizarBoxToken(getUserName(), refreshtoken, mPrefs.getString("userName",""));
    		            oauthObject.setRefreshToken(refreshtoken);
    		            mClient.authenticate(oauthObject);
                	 }catch(Exception e){
                		 
                	 }
                 }
     
             });
            refresh();
            }
    		gba.actualize(boxAccount);
    	}
    }
    
    
    
    //gets and sets
    
    public BoxAndroidClient getClient(){
    	return this.mClient;
    }
    
    public ArrayList<String> getDirectories() {
		return directories;
	}
    
    public String getDirectory(int position) {
		return directories.get(position);
	}

	public void addDirectory(String folder) {
		this.directories.add(folder);
	}
	
	public void removeDirectory(int position){
		this.directories.remove(position);
	}

	public void saveDirectory(ArrayList<Item> currentDirectory){
    	this.currentDirectory=currentDirectory;
    }
    
    public ArrayList<Item> getCurrentDirectory(){
    	return this.currentDirectory;
    }
}