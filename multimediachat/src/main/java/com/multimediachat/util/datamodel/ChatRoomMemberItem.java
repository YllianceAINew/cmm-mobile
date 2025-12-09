package com.multimediachat.util.datamodel;

import com.multimediachat.app.im.provider.Imps;

public class ChatRoomMemberItem {
	public String address;
	public String nickName;
	public int    type;
	public int    status;
	
	public static int STATUS_NORMAL = 1;
	public static int STATUS_BLOCKED = 2;
	
	public static int TYPE_ME = 1;
	public static int TYPE_FRIEND = 0;
	public ChatRoomMemberItem()
	{
		address = "";
		nickName = "";
		status = Imps.GroupMembers.TYPE_NORMAL;
		type = TYPE_FRIEND;
	}
}
