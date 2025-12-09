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

import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatGroup extends ImEntity {
    private ChatGroupManager mManager;
    private Address mAddress;
    private String mName;
    private Vector<Contact> mMembers;
    private CopyOnWriteArrayList<GroupMemberListener> mMemberListeners;

    public ChatGroup(Address address, String name, ChatGroupManager manager) {
        this(address, name, null, manager);
    }

    public ChatGroup(Address address, String name, Collection<Contact> members, 
            ChatGroupManager manager) {
        mAddress = address;
        mName = name;
        mManager = manager;
        mMembers = new Vector<Contact>();

        if (members != null) {
            mMembers.addAll(members);
        }
        
        mMemberListeners = new CopyOnWriteArrayList<GroupMemberListener>();
    }
    
    @Override
    public Address getAddress() {
        return mAddress;
    }

    /**
     * Gets the name of the group.
     * 
     * @return the name of the group.
     */
    public String getName() {
        return mName;
    }
    
    public void setName(String aName){
        mName = aName;
    }

    public void addMemberListener(GroupMemberListener listener) {
        mMemberListeners.add(listener);
    }

    public void removeMemberListener(GroupMemberListener listener) {
        mMemberListeners.remove(listener);
    }

    /**
     * Gets an unmodifiable collection of the members of the group.
     * 
     * @return an unmodifiable collection of the members of the group.
     */
    public List<Contact> getMembers() {
        return Collections.unmodifiableList(mMembers);
    }
    
    public Contact getMember(String userName) {
    	Contact groupMember = null;
    	List<Contact> groupMembers = getMembers();
		for ( int i = 0; i < groupMembers.size(); i++ )
		{
			Contact tmpContact = groupMembers.get(i);
			if ( tmpContact.getAddress().getAddress().equals(userName))
			{
				groupMember = tmpContact;
				break;
			}
		}
		return groupMember;
    }

    /**
     * Adds a member to this group. TODO: more docs on async callbacks.
     * 
     * @param contact the member to add.
     */
    public synchronized void addMemberAsync(Contact contact) {
        mManager.addGroupMemberAsync(this, contact);
        notifyMemberJoined(contact);
    }

    /**
     * Removes a member from this group. TODO: more docs on async callbacks.
     * 
     * @param contact the member to cii_remove.
     */
    public synchronized void removeMemberAsync(Contact contact) {
        mManager.removeGroupMemberAsync(this, contact);
        notifyMemberLeft(contact);
    }
    
    public synchronized void updateGroupMemberLeft(Contact contact) {
    	mManager.removeGroupMemberAsync(this, contact);
    	notifyMemberLeft(contact);
    }

    /**
     * Notifies that a contact has been invited into this group.
     *
     * @param contact the contact who has been invited into the group.
     */
    public synchronized void notifyMemberInvited(Contact contact) {
        if ( !mMembers.contains(contact) )
            mMembers.add(contact);

        for (GroupMemberListener listener : mMemberListeners) {
            listener.onMemberInvited(this, contact);
        }
    }

    /**
     * Notifies that a contact has joined into this group.
     * 
     * @param contact the contact who has joined into the group.
     */
    void notifyMemberJoined(Contact contact) {
    	if ( !mMembers.contains(contact) )
    		mMembers.add(contact);
    	
        for (GroupMemberListener listener : mMemberListeners) {
            listener.onMemberJoined(this, contact);
        }
    }

    /**
     * Notifies that a contact has left this group.
     * 
     * @param contact the contact who has left this group.
     */
    void notifyMemberLeft(Contact contact) {
        if (mMembers.remove(contact)) {
            for (GroupMemberListener listener : mMemberListeners) {
                listener.onMemberLeft(this, contact);
            }
        }
    }

    /**
     * Notifies that previous operation on this group has failed.
     * 
     * @param error the error information.
     */
    void notifyGroupMemberError(ImErrorInfo error) {
        for (GroupMemberListener listener : mMemberListeners) {
            listener.onError(this, error);
        }
    }
    
    public void removeAllMembers(){
    	mMembers.clear();
    }
    
    public void addMembers(Collection<Contact> members){
    	mMembers.addAll(members);
    }
    
    @Override
    public boolean isGroup() {
        return true;
    }
}
