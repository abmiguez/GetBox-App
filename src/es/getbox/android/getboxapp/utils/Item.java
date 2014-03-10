package es.getbox.android.getboxapp.utils;

public class Item {
	private String mName;
	private String mId;
	private String mLocation;
	private int mAccount;
	
	public Item(){
	}
	
	public Item(String name, String id, String location, int account){
		this.mName=name;
		this.mId=id;
		this.mLocation=location;
		this.mAccount=account;
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
	
	public int getAccount(){
		return mAccount;
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
	
	public void setAccount(int account){
		this.mAccount=account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}
}