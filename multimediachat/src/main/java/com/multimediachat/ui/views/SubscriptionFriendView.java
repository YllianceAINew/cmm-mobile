
package com.multimediachat.ui.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.IContactListManager;
import com.multimediachat.app.im.IImConnection;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.service.StatusBarNotifier;


public class SubscriptionFriendView extends LinearLayout {
	public static final String[] CONTACT_PROJECTION = { Imps.Contacts._ID, Imps.Contacts.PROVIDER,
		Imps.Contacts.ACCOUNT, Imps.Contacts.USERNAME,
		Imps.Contacts.NICKNAME, Imps.Contacts.TYPE,
		Imps.Contacts.SUBSCRIPTION_TYPE,
		Imps.Contacts.SUBSCRIPTION_STATUS,
		Imps.Contacts.AVATAR_DATA,
		Imps.Contacts.GENDER,
		Imps.Contacts.STATUS,
		Imps.Contacts.REGION,
		Imps.Contacts.SUBSCRIPTIONMESSAGE
	};

	private Context 	mContext;
	private Activity    mActivity;
	ImApp mApp;

	public SubscriptionFriendView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mActivity = (Activity)mContext;
		mApp = (ImApp) mActivity.getApplication();
	}

	private ViewHolder mHolder = null;

	class ViewHolder 
	{
		TextView 				mNickName;
		TextView	 			mConfirmBtn;
		TextView	 			mCancelBtn;
		CircularImageView 		mAvatar;
		TextView			    mGreeting;


		private void showConfirmDlg(final String address, final long providerId, final int sub_type, final String nickName, final boolean isAccept) throws Exception{

            final Dialog dlg = GlobalFunc.createDialog(mContext, R.layout.confirm_dialog, true);

			TextView title = dlg.findViewById(R.id.msgtitle);
			title.setText(R.string.str_add_friend);

			TextView content = dlg.findViewById(R.id.msgcontent);

			Button dlg_btn_ok = dlg.findViewById(R.id.btn_ok);
			Button dlg_btn_cancel = dlg.findViewById(R.id.btn_cancel);
			dlg_btn_ok.setText(getContext().getString(R.string.yes));
			dlg_btn_cancel.setText(getContext().getString(R.string.no));
			dlg_btn_cancel.setVisibility(View.VISIBLE);

			dlg_btn_cancel.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					try{
						dlg.dismiss();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});
            dlg_btn_ok.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {

					//check network state
					if (!((ImApp)((Activity)mContext).getApplication()).isServerLogined()) {
						GlobalFunc.showToast(mContext, R.string.network_error, false);
						try{
							dlg.dismiss();
						}catch(Exception e){
							e.printStackTrace();
						}
						return;
					}

					try
					{
						ImApp mApp = (ImApp)mActivity.getApplication();
						IImConnection conn = mApp.getConnection(providerId);

						if ( conn == null )
						{
							GlobalFunc.showToast(mContext, R.string.error_message_network_connect, false);
							return;
						}


						final IContactListManager contactListMgr = conn.getContactListManager();

						if ( contactListMgr == null )
						{
							GlobalFunc.showToast(mContext, R.string.error_message_network_connect, false);
							return;
						}

						final Contact contact = contactListMgr.getContact(address);
						int contactId = Math.abs(contact.hashCode());
						NotificationManager nMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
						if ( contact != null ) {
							if (isAccept) {
								contactListMgr.approveSubscription(contact);
								nMgr.cancel(StatusBarNotifier.sub_notify_start_id + contactId);
							}
							else {
								if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
									contactListMgr.declineSubscription(contact);
									nMgr.cancel(StatusBarNotifier.sub_notify_start_id + contactId);
								}
								else
									contactListMgr.cancelRequest(contact);
							}
						}
						else{
							DatabaseUtils.deleteSubscription(mContext.getContentResolver(), address);
						}
					}catch(Exception e) {
						e.printStackTrace();
					}

					try{
						dlg.dismiss();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});

			String confirm_str;

			if (isAccept)
				confirm_str = mContext.getString(R.string.confirm_accept, nickName);
			else {
				if (sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM)
					confirm_str = mContext.getString(R.string.confirm_decline, nickName);
				else
					confirm_str = mContext.getString(R.string.confirm_cancel, nickName);
			}

			content.setText(confirm_str);

			dlg.setCanceledOnTouchOutside(false);
			dlg.show();
		}

		void setOnClickListenerConfirmBtn(final long providerId, final String address, final int sub_type, final String nickName) {
			mConfirmBtn.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					if ( sub_type == Imps.Contacts.SUBSCRIPTION_TYPE_FROM) {
						try {
							showConfirmDlg(address, providerId, sub_type, nickName, true);
						} catch(Exception e) {
							e.printStackTrace();
						}
					}

				}
			});
			mCancelBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						showConfirmDlg(address, providerId, sub_type, nickName, false);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mHolder = (ViewHolder)getTag();

		if (mHolder == null)
		{
			mHolder = new ViewHolder();

			mHolder.mNickName = (TextView) findViewById(R.id.contactNickName);
			mHolder.mAvatar = (CircularImageView)findViewById(R.id.imgPropile);
			mHolder.mConfirmBtn = (TextView)findViewById(R.id.btn_confirm);
			mHolder.mCancelBtn = (TextView)findViewById(R.id.btn_cancel);
			mHolder.mGreeting = (TextView) findViewById(R.id.txt_greeting);

			setTag(mHolder);
		}

		mHolder.mAvatar.setBorderWidth(0);

	}

	public void bind(Cursor cursor) {
		mHolder = (ViewHolder)getTag();

		final long providerId = cursor.getLong(cursor.getColumnIndex(Imps.Contacts.PROVIDER));
		final String address = cursor.getString(cursor.getColumnIndex(Imps.Contacts.USERNAME));
		String nickname = cursor.getString(cursor.getColumnIndex(Imps.Contacts.NICKNAME));
		int sub_status = cursor.getInt(cursor.getColumnIndex(Imps.Contacts.SUBSCRIPTION_TYPE));
		String greeting = cursor.getString(cursor.getColumnIndex(Imps.Contacts.SUBSCRIPTIONMESSAGE));

		mHolder.mNickName.setText(nickname);
		mHolder.mNickName.setSelected(true);

		if ( greeting != null && !greeting.trim().equals("") ){
			mHolder.mGreeting.setVisibility(View.VISIBLE);
			mHolder.mGreeting.setText(greeting);
			mHolder.mGreeting.setSelected(true);
		}
		else{
			mHolder.mGreeting.setVisibility(View.GONE);
		}

		if ( sub_status == Imps.Contacts.SUBSCRIPTION_TYPE_FROM ) {
			mHolder.mConfirmBtn.setVisibility(View.VISIBLE);
			mHolder.mCancelBtn.setText(R.string.decline_invitation);
		} else {
			findViewById(R.id.ly_register).setVisibility(View.GONE);
			mHolder.mCancelBtn.setText(R.string.cancel);
		}

		GlobalFunc.showAvatar(getContext(), address, mHolder.mAvatar);

		mHolder.setOnClickListenerConfirmBtn(providerId, address, sub_status, nickname);
	}
}
