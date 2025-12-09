package com.multimediachat.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.multimediachat.service.FtpService;
import com.multimediachat.service.MessengerService;


/**
 * Automatically initiate the service and connect when the network comes on,
 * including on boot.
 */
public class BootCompletedListener extends BroadcastReceiver {
	@Override
	public synchronized void onReceive(Context context, Intent intent) {
		//new ImApp(context).startImServiceIfNeed();
		ComponentName comp = new ComponentName(context.getPackageName(), MessengerService.class.getName());
		context.startService(new Intent().setComponent(comp));
		comp = new ComponentName(context.getPackageName(), FtpService.class.getName());
		context.startService(new Intent().setComponent(comp)); 
	}
}
