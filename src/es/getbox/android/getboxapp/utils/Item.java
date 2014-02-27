package es.getbox.android.getboxapp.utils;

public class Item {
	private String mName;
	private String mId;
	private String mLocation;
	
	public Item(){
	}
	
	public Item(String name, String id, String location){
		this.mName=name;
		this.mId=id;
		this.mLocation=location;
	}
	
	public String getName(){
		return mName;
	}

	public String getId(){
		return mId;
	}
	
	public String getLocation(){
		return mLocation;
	}
	
	public void setName(String name){
		this.mName=name;
	}

	public void setId(String id){
		this.mId=id;
	}
	
	public void setLocation(String location){
		this.mLocation=location;
	}
}