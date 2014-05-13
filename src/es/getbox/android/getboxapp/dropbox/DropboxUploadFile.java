package es.getbox.android.getboxapp.dropbox;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;

import es.getbox.android.getboxapp.GetBoxActivity;

public class DropboxUploadFile extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private String mPath;
    private File mFile;
    private GetBoxActivity gba;
    private UploadRequest mRequest;
    private Context mContext;


    public DropboxUploadFile(Context context, DropboxAPI<?> api, String dropboxPath,
            File file,GetBoxActivity g) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();
        gba=g;
        mApi = api;
        mPath = dropboxPath;
        mFile = file;
    }


    @Override
    protected void onPreExecute() {
    	Toast.makeText(mContext, "Subiendo archivo...", Toast.LENGTH_LONG).show();
    	super.onPreExecute();
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + mFile.getName();
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),null);

            if (mRequest != null) {
                mRequest.upload();
            }

        } catch (Exception e) {

            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            showToast(mFile.getName()+" subido con exito");
            gba.actualizarDirectorio();
        } else {
            showToast("Ha ocurrido un error mientras se subía el archivo");
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}