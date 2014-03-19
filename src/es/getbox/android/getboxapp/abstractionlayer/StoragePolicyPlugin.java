package es.getbox.android.getboxapp.abstractionlayer;

import java.util.ArrayList;

import es.getbox.android.getboxapp.box.BoxStorageProvider;
import es.getbox.android.getboxapp.dropbox.DropboxStorageProvider;

public class StoragePolicyPlugin{
	
	public StoragePolicyPlugin(){
		
	}
	
	public int[]  storagePolicy(int newDropboxAccount, int newBoxAccount, ArrayList<DropboxStorageProvider> dsp, ArrayList<BoxStorageProvider> bsp){
		int[] cuenta=new int[2];
		long longDB=0;
		long longB=0;
		int intDB=0;
		int intB=0;
		for(int i=0;i<newDropboxAccount;i++){
			if(dsp.get(i).getSpaceUsed()>longDB && dsp.get(i).getNoLocation()){
				longDB=dsp.get(i).getSpaceUsed();
				intDB=i;
			}
        }
        for(int i=0;i<newBoxAccount;i++){
        	if(bsp.get(i).getSpaceUsed()>longB && !bsp.get(i).getDirectory(bsp.get(i).getDirectories().size()-1).equals("")){
				longB=bsp.get(i).getSpaceUsed();
				intB=i;
			}
        }
        if(longDB>longB){
        	cuenta[0]=0;
            cuenta[1]=intDB;
            return cuenta;
        }else{
        	cuenta[0]=1;
            cuenta[1]=intB;
            return cuenta;
        }
	}
}