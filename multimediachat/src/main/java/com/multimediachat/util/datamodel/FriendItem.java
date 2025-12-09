package com.multimediachat.util.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendItem implements Parcelable {
	public String nickName;
	public String userName;
	public boolean isOnline;
	public String status;
	public char    firstChar;
	
	public FriendItem(){
		nickName = null;
		userName = null;
		isOnline = false;
		status = null;
	}

	public FriendItem(Parcel in){
		nickName = in.readString();
		userName = in.readString();
		isOnline = in.readByte() == 1 ? true : false;
		status = in.readString();
		firstChar = (char)in.readByte();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(nickName);
		dest.writeString(userName);
		dest.writeByte(isOnline?(byte)1:(byte)0);
		dest.writeString(status);
		dest.writeByte((byte)firstChar);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public FriendItem createFromParcel(Parcel in) {
			return new FriendItem(in);
		}

		public FriendItem[] newArray(int size) {
			return new FriendItem[size];
		}
	};
}
