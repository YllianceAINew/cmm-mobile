/*
 * Copyright (C) 2007 Esmertec AG. Copyright (C) 2007 The Android Open Source
 * Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.multimediachat.app.im.engine;

import com.multimediachat.app.im.provider.Imps;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact extends ImEntity implements Parcelable {
    private Address mAddress;
    private String mName;
    private Presence mPresence;
    private double mDistance;
    
    public String status="";
    public String gender="";
    public String region="";
    public String hash = "";
    public String phoneNum = "";
    private String greeting = "";
    //public int kind = 0; // 0 : none, 1 : pica friend, 2 : from, 3 : to
    public int sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
    public int favor = 0;
    //public int type = 0; //0:normal, 1:blocked, 2:favor, 3:hidden
    
    public int contact_type = Imps.Contacts.TYPE_NORMAL;
    public String mFullName;
    public String mMobileName;

    public String mDescription;
    public String mMobile;

    public int star;
    public String userid;

    public Contact(Address address, String name) {
        mAddress = address;
        mName = name;
        mPresence = new Presence();
    }

    public Contact(Parcel source) {
        mAddress = AddressParcelHelper.readFromParcel(source);
        mName = source.readString();
        mDistance = source.readDouble();
        status = source.readString();
        gender = source.readString();
        region = source.readString();
        greeting = source.readString();
        hash = source.readString();
        sub_type = source.readInt();
        favor = source.readInt();
        contact_type = source.readInt();
        mFullName = source.readString();
        mMobileName = source.readString();
        mDescription = source.readString();
        mMobile = source.readString();
        star = source.readInt();
        userid = source.readString();
        phoneNum = source.readString();
        mPresence = new Presence(source);
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

    public void setUserid(String userid)
    {
        this.userid = userid;
    }

    /**
     * @author lightsky
     * @since 2014/05/08
     * @description : set profile data
     */
    public void setProfile(String status, String gender, String region) {
    	this.status = status == null ? " " : status;
    	this.gender = gender == null ? " " : gender;
    	this.region = region == null ? " " : region;
    }
    
    public String getStatus() {
    	return status;
    }

    public String getGender() {
    	return gender;
    }
    
    public String getRegion() {
    	return region;
    }

    public String getPhoneNum() {
        return phoneNum;
    }
    
    /**
     * @author : lightsky
     * @since : 2014/04/26
     * @description : set distance between nearby requsted user and self
     */
    public void setDistance(String dis) {
    	mDistance = Double.parseDouble(dis) * 1000; //unit 1m
    }
    
    /**
     * @author : lightsky
     * @since : 2014/04/26
     * @description : get distance between nearby requsted user and self
     */
    public double getDistance() {
    	return mDistance;
    }
    
    public void setAddress(Address address){
    	mAddress = address;
    }

    public Address getAddress() {
        return mAddress;
    }

    public String getName() {
        return mName;
    }
    
    public void setName( String aName ) {
        mName = aName;
    }

    public void setPhoneNum(String phoneNumer) {
        phoneNum = phoneNumer;
    }
    
    public String getGreeting(){
    	return greeting;
    }
    
    public void setGreeting( String aGreeting) {
    	greeting = aGreeting;
    }

    public Presence getPresence() {
        return mPresence;
    }
    
    /* Set the presence of the Contact. Note that this method is public but not
     * provide to the user.
     * 
     * @param presence the new presence
     */
    public void setPresence(Presence presence) {
        mPresence = presence;
    }

    public void writeToParcel(Parcel dest, int flags) {
        AddressParcelHelper.writeToParcel(dest, mAddress);
        dest.writeString(mName);
        dest.writeDouble(mDistance);
        dest.writeString(status);
        dest.writeString(gender);
        dest.writeString(region);
        dest.writeString(greeting);
        dest.writeString(hash);
        dest.writeInt(sub_type);
        dest.writeInt(favor);
        dest.writeInt(contact_type);
        dest.writeString(mFullName);
        dest.writeString(mMobileName);
        dest.writeString(mDescription);
        dest.writeString(mMobile);
        dest.writeInt(star);
        dest.writeString(userid);
        dest.writeString(phoneNum);

        mPresence.writeToParcel(dest, 0);
    }

    public int describeContents() {
        return 0;
    }
    
    public boolean equals(Object other) {
        
        return other instanceof Contact && mAddress.getBareAddress().equals(((Contact) other).getAddress().getBareAddress());
    }

    public int hashCode() {
        return mAddress.hashCode();
    }

    public final static Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
