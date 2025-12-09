package com.multimediachat.ui.views;

import java.util.ArrayList;
import java.util.List;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.util.Utilities;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class QuickActionPopup extends MyPopupWindow implements OnDismissListener {
	private View mRootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private ViewGroup mTrack;
	private ScrollView mScroller;

	private OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;

	private List<QuickActionItem> actionItems = new ArrayList<QuickActionItem>();
	private List<View> actionViews = new ArrayList<View>();

	private boolean mDidAction;
	private boolean reverseOrientationItem = false;
	private int mChildPos;
	private int mInsertPos;
	private int mAnimStyle;
	private int mOrientation;
	private int rootWidth=0;
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_TOP = 3;

	/**
	 * Constructor for default vertical layout
	 * 
	 * @param context  Context
	 */
	public QuickActionPopup(Context context) {
		this(context, VERTICAL);
	}

	/**
	 * Constructor allowing orientation override
	 * 
	 * @param context    Context
	 * @param orientation Layout orientation, can be vartical or horizontal
	 */
	public QuickActionPopup(Context context, int orientation) {
		super(context);

		mOrientation = orientation;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical);
		}

		mAnimStyle  = ANIM_GROW_FROM_LEFT;
		mChildPos   = 0;
	}

	public QuickActionPopup(Context context, int orientation, int animStyle) {
		super(context);

		mOrientation = orientation;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.popup_horizontal);
		} else {
			setRootViewId(R.layout.popup_vertical);
		}

		mAnimStyle  = animStyle;
		mChildPos   = 0;
	}
	/**
	 * Set the background of the popup and the two arrows. Must be 9-patch.
	 * @param popup
	 * @param arrowUp
	 * @param arrowDown
	 */
	public void setBackgroundResources(int popup, int arrowUp, int arrowDown){

		if(popup!=0 && arrowUp!=0 && arrowDown!=0){

			mScroller.setBackgroundResource(popup);
			mArrowDown.setImageResource(arrowDown);
			mArrowUp.setImageResource(arrowUp);

		}
	}
	
	public void setBackgroundDrawables(Drawable popup, Drawable arrowUp, Drawable arrowDown){
		{
			if ( android.os.Build.VERSION.SDK_INT >= 16 ) {
				mScroller.setBackground(popup);
				mArrowDown.setBackground(arrowDown);
				mArrowUp.setBackground(arrowUp);
			}else{
				mScroller.setBackgroundDrawable(popup);
				mArrowDown.setBackgroundDrawable(arrowDown);
				mArrowUp.setBackgroundDrawable(arrowUp);
			}
		}
	}


	/**
	 * Get action item at an index
	 * 
	 * @param index  Index of item (position from callback)
	 * 
	 * @return  Action Item at the position
	 */
	public QuickActionItem getActionItem(int index) {
		return actionItems.get(index);
	}

	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	private void setRootViewId(int id) {
		mRootView   = mInflater.inflate(id, null);
		mTrack  = (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowDown  = (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp    = (ImageView) mRootView.findViewById(R.id.arrow_up);
		mScroller   = (ScrollView) mRootView.findViewById(R.id.scroller);

		mRootView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		setContentView(mRootView);
	}

	/**
	 * Set animation style
	 * 
	 * @param mAnimStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mItemClickListener = listener;
	}

	/**
	 * Add action item
	 * 
	 * @param action  {@link QuickActionItem}
	 */
	public void addActionItem(QuickActionItem action) {
		actionItems.add(action);

		String title    = action.getTitle();
		Drawable icon   = action.getIcon();

		View container;

		if (mOrientation == HORIZONTAL && !reverseOrientationItem) {
			container = mInflater.inflate(R.layout.action_item_horizontal, null);
		} else {
			container = mInflater.inflate(R.layout.action_item_vertical, null);
		}

		ImageView img   = (ImageView) container.findViewById(R.id.iv_icon);
		TextView text   = (TextView) container.findViewById(R.id.tv_title);
		View divider= (View) container.findViewById(R.id.v_divider);

		if (icon != null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}

		if (title != null) {
			text.setText(title);
			text.setSelected(true);
			if (action.getTitleColor() != -1)
				text.setTextColor(action.getTitleColor());
		} else {
			text.setVisibility(View.GONE);
		}

		divider.setVisibility(actionItems.size() > 1 ? View.VISIBLE : View.GONE);

		final int pos   =  mChildPos;
		final int actionId  = action.getActionId();

		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(QuickActionPopup.this, pos, actionId);
				}
				dismiss();
			}
		});

		container.setFocusable(true);
		container.setClickable(true);            
		mTrack.addView(container, mInsertPos);
		actionViews.add(container);
		
		mChildPos++;
		mInsertPos++;
	}

	public void addActionItem(QuickActionItem action, int position) {
		actionItems.add(position, action);

		String title    = action.getTitle();
		Drawable icon   = action.getIcon();

		View container;

		if (mOrientation == HORIZONTAL && !reverseOrientationItem) {
			container = mInflater.inflate(R.layout.action_item_horizontal, null);
		} else {
			container = mInflater.inflate(R.layout.action_item_vertical, null);
		}

		ImageView img   = (ImageView) container.findViewById(R.id.iv_icon);
		TextView text   = (TextView) container.findViewById(R.id.tv_title);
		View divider= (View) container.findViewById(R.id.v_divider);

		if (icon != null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}

		if (title != null) {
			text.setText(title);
			text.setSelected(true);
			if (action.getTitleColor() != -1)
				text.setTextColor(action.getTitleColor());
		} else {
			text.setVisibility(View.GONE);
		}

		divider.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

		final int pos   =  mChildPos;
		final int actionId  = action.getActionId();

		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
					mItemClickListener.onItemClick(QuickActionPopup.this, pos, actionId);
				}

				if (!getActionItem(pos).isSticky()) {
					mDidAction = true;

					dismiss();
				}
			}
		});

		container.setFocusable(true);
		container.setClickable(true);
		mTrack.addView(container, position);
		actionViews.add(position, container);

		mChildPos++;
		mInsertPos++;
	}

	public void removeActionItem(int position) {
		actionItems.remove(position);
		mTrack.removeViewAt(position);
		actionViews.remove(position);
		mChildPos--;
		mInsertPos--;
	}

	public void setActionIcon(int index, Drawable icon)
	{
		ImageView imageView = (ImageView) actionViews.get(index).findViewById(R.id.iv_icon);

		if ( icon != null )
			imageView.setImageDrawable(icon);
		else
			imageView.setVisibility(View.GONE);
	}

	public View getActionView(int position) {
		if ( actionViews.size() > position )
			return actionViews.get(position);
		
		return null;
	}
	
	public void updateActionViewIcon(Drawable drawable, int position) {
		View actionView = getActionView(position);
		if ( actionView != null ) {
			ImageView img = (ImageView) actionView.findViewById(R.id.iv_icon);
			if ( img != null ) {
				if ( drawable != null ) {
					img.setImageDrawable(drawable);
					img.setVisibility(View.VISIBLE);
				}
				else{
					img.setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor                                
	 * 
	 */
	/*public void show (View anchor) {
		preShow();

		int xPos, yPos, arrowPos;

		mDidAction          = false;

		int[] location      = new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootHeight = mRootView.getMeasuredHeight();

		if (rootWidth == 0) {

			rootWidth = mRootView.getMeasuredWidth();
		}
		
		DisplayMetrics metrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(metrics);

		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;

		if ( mAnimStyle == ANIM_GROW_FROM_LEFT ) {
			if ((anchorRect.left + rootWidth) > screenWidth) {


				xPos = anchorRect.left - (rootWidth - anchor.getWidth());
				xPos = (xPos < 0) ? 0 : xPos;

				arrowPos = anchorRect.centerX() - xPos;

			} else {
				if (anchor.getWidth() > rootWidth) {
					xPos = anchorRect.centerX() - (rootWidth / 2);


				} else {
					xPos = anchorRect.left;
				}

				arrowPos = anchorRect.centerX() - xPos;
			}
		}
		else {	//This case is only used in moments, myposts's more button
				xPos = anchorRect.left - rootWidth - Utilities.dpToPx(mContext, 10);
				arrowPos = anchorRect.centerX() - xPos;
		}

		int dyTop           = anchorRect.top;
		int dyBottom        = screenHeight - anchorRect.bottom;

		boolean onTop       = (dyTop > dyBottom);

		if ( mAnimStyle == ANIM_GROW_FROM_LEFT ) {
			if (onTop) {
				if (rootHeight > dyTop) {
					yPos = 15;
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyTop - anchor.getHeight();
				} else {
					yPos = anchorRect.top - rootHeight - 15;
				}
			} else {
				yPos = anchorRect.bottom + 15;

				if (rootHeight > dyBottom) {
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyBottom;
				}
			}
		} else {
			yPos = anchorRect.bottom - rootHeight;
		}

		//showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);

		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}*/

	public void show(View anchor) {
		preShow();
		int xPos, yPos;
		mDidAction = false;
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int rootHeight = mRootView.getMeasuredHeight();
		if (rootWidth == 0) {
			rootWidth = mRootView.getMeasuredWidth();
		}
		DisplayMetrics metrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(metrics);
		int screenWidth = metrics.widthPixels;
		int screenHeight = metrics.heightPixels;
		if (mAnimStyle == ANIM_GROW_FROM_RIGHT) {
			if ((anchorRect.left + rootWidth) > screenWidth) {
				xPos = anchorRect.left - (rootWidth - anchor.getWidth());
				xPos = (xPos < 0) ? 0 : xPos;
			} else {
				if (anchor.getWidth() > rootWidth) {
					xPos = anchorRect.centerX() - (rootWidth / 2);
				} else {
					xPos = anchorRect.left;
				}
			}
		} else {    //This case is only used in moments, myposts's more button
			xPos = anchorRect.left;
			if (xPos > screenWidth - rootWidth)
				xPos = screenWidth - rootWidth;
		}

		int dyTop = anchorRect.top + rootHeight;
		int dyBottom = screenHeight;
		boolean onTop = (dyTop > dyBottom);
		if (mAnimStyle == ANIM_GROW_FROM_RIGHT) {
			if (onTop) {
				if (rootHeight > dyTop) {
					yPos = 0;
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyTop - anchor.getHeight();
				} else {
					yPos = anchorRect.top - rootHeight;
				}
			} else {
				yPos = anchorRect.bottom;

				if (rootHeight > dyBottom) {
					LayoutParams l = mScroller.getLayoutParams();
					l.height = dyBottom;
				}
			}
		} else {
			yPos = anchorRect.bottom;
			if (onTop)
				yPos = yPos - rootHeight;
		}

		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if  *displayed on top of anchor view and vice versa
	 */

	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
//		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		/*switch (mAnimStyle) {
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
			
		case ANIM_GROW_FROM_RIGHT:
			//mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			mWindow.setAnimationStyle(R.style.Animations_PopUpMenu_Slide_Right);
			break;

		}*/
		switch (mAnimStyle) {
			case ANIM_GROW_FROM_LEFT:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
				break;

			case ANIM_GROW_FROM_RIGHT:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
				break;

			case ANIM_GROW_FROM_TOP:
				mWindow.setAnimationStyle(R.style.Animations_PopDownMenu_Top);
				break;

		}
	}


	private void showArrow(int whichArrow, int requestedX) {

		final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
		final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

		final int arrowWidth = mArrowUp.getMeasuredWidth();

		showArrow.setVisibility(View.VISIBLE);

		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();

		param.leftMargin = requestedX - arrowWidth / 2;

		hideArrow.setVisibility(View.INVISIBLE);
	}

	/**
	 * Listener for dismissing the window.
	 */

	public void setOnDismissListener(QuickActionPopup.OnDismissListener listener) {
		setOnDismissListener(this);

		mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}

	/**
	 * If we want to reverse the item orientation.
	 */

	public boolean isReverseOrientationItem() {
		return reverseOrientationItem;
	}

	public void setReverseOrientationItem(boolean reverseOrientationItem) {
		this.reverseOrientationItem = reverseOrientationItem;
	}

	/**
	 * Listener for item click
	 *
	 */
	public interface OnActionItemClickListener {

		void onItemClick(QuickActionPopup source, int pos, int actionId);
	}

	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		void onDismiss();
	}

	public void setActionState(int index, boolean state) {
		TextView textView = (TextView) actionViews.get(index).findViewById(R.id.tv_title);
		textView.setEnabled(state);
		ImageView imageView = (ImageView) actionViews.get(index).findViewById(R.id.iv_icon);
		imageView.setEnabled(state);
	}
}
