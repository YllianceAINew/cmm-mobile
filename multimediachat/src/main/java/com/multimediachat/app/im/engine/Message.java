package com.multimediachat.app.im.engine;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/** Represents an instant message send between users. */
public class Message implements Parcelable {
    
    private String mId;
    private Address mFrom;
    private Address mTo;
    private String mBody;
    private Date mDate;
    private int mType;
    private long mTimeDiff;
    private String mOperType;
    private String mOperMessage;
    private String mOperMsgId;
    private long mServerTime;
    
    /**
     * @param msg
     * @throws NullPointerException if msg is null.
     */
    public Message(String msg) {
        if (msg == null) {
            throw new NullPointerException("null msg");
        }
        mBody = msg;
    }

    public Message(Parcel source) {
        mId = source.readString();
        mFrom = AddressParcelHelper.readFromParcel(source);
        mTo = AddressParcelHelper.readFromParcel(source);
        mBody = source.readString();
        long time = source.readLong();
        if (time != -1) {
            mDate = new Date(time);
        }
        mType = source.readInt();
        mTimeDiff = source.readLong();
        mOperType = source.readString();
        mOperMessage = source.readString();
        mOperMsgId = source.readString();
    }

    /**
     * Gets an identifier of this message. May be <code>null</code> if the
     * underlying protocol doesn't support it.
     * 
     * @return the identifier of this message.
     */
    public String getID() {
        return mId;
    }

    /**
     * Gets the body of this message.
     * 
     * @return the body of this message.
     */
    public String getBody() {
        return mBody;
    }

    /**
     * Gets the address where the message is sent from.
     * 
     * @return the address where the message is sent from.
     */
    public Address getFrom() {
        return mFrom;
    }

    /**
     * Gets the address where the message is sent to.
     * 
     * @return the address where the message is sent to.
     */
    public Address getTo() {
        return mTo;
    }

    public Date getDateTime() {
        if (mDate == null) {
            return null;
        }
        return new Date(mDate.getTime());
    }
    
    public long getTimeDiff(){
    	return mTimeDiff;
    }
    
    public String getOperType(){
    	return mOperType;
    }
    
    public String getOperMessage(){
    	return mOperMessage;
    }
    
    public String getOperMsgId(){
    	return mOperMsgId;
    }

    public void setID(String id) {
        mId = id;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public void setFrom(Address from) {
        mFrom = from;
    }

    public void setTo(Address to) {
        mTo = to;
    }

    public void setDateTime(Date dateTime) {
        long time = dateTime.getTime();
        if (mDate == null) {
            mDate = new Date(time);
        } else {
            mDate.setTime(time);
        }
    }
    
    public void setTimeDiff(long timeDiff) {
    	mTimeDiff = timeDiff;
    }
    
    public void setOperType(String _operType) {
    	mOperType = _operType;
    }
    
    public void setOperMessage(String _operMessage) {
    	mOperMessage = _operMessage;
    }
    
    public void setOperMsgId(String _opermsgid) {
    	mOperMsgId = _opermsgid;
    }
    

    public String toString() {
        return "From: " + mFrom.getAddress() + " To: " + mTo.getAddress() + " " + mBody;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        AddressParcelHelper.writeToParcel(dest, mFrom);
        AddressParcelHelper.writeToParcel(dest, mTo);
        dest.writeString(mBody);
        dest.writeLong(mDate == null ? -1 : mDate.getTime());
        dest.writeInt(mType);
        dest.writeLong(mTimeDiff);
        dest.writeString(mOperType);
        dest.writeString(mOperMessage);
        dest.writeString(mOperMsgId);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    


    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public void setServerTime(long mTime){
        mServerTime = mTime;
    }

    public long getServerTime(){
        return mServerTime;
    }

}
