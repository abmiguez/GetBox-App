package es.getbox.android.getboxapp.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;

public class DropboxDownloadFile extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private String mPath;
    //private long mFileLength;
    private Context mContext;
    private String nFile;
        
    private CharSequence contentText;
    private CharSequence contentTitle;
    private PendingIntent contentIntent;
    private int HELLO_ID = 1;
    private long time;
    private int icon;
    private CharSequence tickerText;
    private NotificationManager notificationManager;
    private Notification notification;


    public DropboxDownloadFile(Context context, DropboxAPI<?> api, String dropboxPath,String newfile) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();
        nFile=newfile;
        mApi = api;
        mPath = dropboxPath;        
    }
    
    @SuppressWarnings("deprecation")
	private void downloadNotification(){
    	 String[] resul = mPath.split("/");
    	 String ns = Context.NOTIFICATION_SERVICE;
         notificationManager = (NotificationManager) mContext.getSystemService(ns);

         icon = R.drawable.stat_sys_download;
         //the text that appears first on the status bar
         tickerText = "Descargando archivo...";
         time = System.currentTimeMillis();
         notification = new Notification(icon, tickerText, time);
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         
         contentTitle = resul[resul.length-1]+"";
         //the text that needs to change
         contentText = "Descargando...";
         Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
         notificationIntent.setType("audio/*");
         contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
         notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
         notificationManager.notify(HELLO_ID, notification);
    }
    
    private Intent openFile() {
        // Create URI
    	String url=nFile;
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
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	downloadNotification();
    }

    @SuppressWarnings("unused")
	@Override
    protected Boolean doInBackground(Void... params) {
    	FileOutputStream outputStream = null;
    	try {
    		File file = new File(nFile);
    		outputStream = new FileOutputStream(file);
    		DropboxFileInfo info = mApi.getFile(mPath, null,
    							outputStream,null);
    		
        } catch (Exception e) {
        	return false;
        } finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					return false;
				}
			}
		}

		return true;
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
            contentIntent=PendingIntent.getActivity(mContext, 0, openFile(), 0);
            notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
            notificationManager.notify(HELLO_ID, notification);
        } else {
        	contentText =  "Error al descargar el archivo";
    		tickerText = "Error al descargar el archivo";
            icon= R.drawable.stat_sys_download_done;
            notification = new Notification(icon, tickerText, time);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
            notificationManager.notify(HELLO_ID, notification);
        	
        }
    }
}