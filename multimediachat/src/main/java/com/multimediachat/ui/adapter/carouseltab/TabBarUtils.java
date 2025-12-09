package com.multimediachat.ui.adapter.carouseltab;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.multimediachat.R;

public class TabBarUtils
{
	public static final String TAG = "TabBar";
	private static ScreenInfo screenInfo;

	private static ScreenInfo getScreenInfo(final Context context) {
		if (TabBarUtils.screenInfo == null) {
			TabBarUtils.screenInfo = new ScreenInfo(context);
		}
		return TabBarUtils.screenInfo;
	}

	public static int scale(final Context context, final int portraitValue, final int landscapeValue) {
		return getScreenInfo(context).scale(portraitValue, landscapeValue);
	}

	public static int value(final Context context, final int portraitValue, final int landscapeValue) {
		return getScreenInfo(context).value(portraitValue, landscapeValue);
	}

	private static class ScreenInfo
	{
		private Display display;
		private float ratio;

		private ScreenInfo(final Context context) {
			this(context, context.getResources().getDisplayMetrics());
		}

		private ScreenInfo(final Context context, final DisplayMetrics displayMetrics) {
			super();
			display = ((WindowManager)context.getSystemService("window")).getDefaultDisplay();
			ratio = displayMetrics.densityDpi / 480.0f;
		}

		private int scale(int baseValue) {
			baseValue = (int)(baseValue * ratio);
			if (baseValue % 2 == 1) {
				baseValue = baseValue + 1;
			}
			return baseValue;
		}

		private int scale(final int portraitValue, final int landscapeValue) {
			return scale(value(portraitValue, landscapeValue));
		}

		private int value(final int portraitValue, final int landscapeValue) {
			final int rotation = display.getRotation();
			if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
				return portraitValue;
			}
			return landscapeValue;
		}
	}

	public static class color
	{
		public static int backgroundColor(final Context context, final boolean b) {
			return multiply(context);
		}

		public static int backgroundLight(final Context context) {
			return context.getResources().getColor(R.color.ap_background_color);
		}

		public static int category(final Context context) {
			return ThemeColor.getColor(ThemeColor.DIALPAD_KEY_TEXT_PRESS_BG);
		}

		public static int categoryLight(final Context context) {
			return ThemeColor.getColor(ThemeColor.DIALPAD_KEY_TEXT_PRESS_BG);
		}

		public static int landscapeBackground(final Context context) {
			return context.getResources().getColor(R.color.list_item_bg_bottom_color);
		}

		public static int landscapeTextColor(final Context context) {
			return context.getResources().getColor(R.color.dark_primaryfont_color);
		}

		public static int multiply(final Context context) {
			return ThemeColor.getColor(ThemeColor.ACTIONBAR_BG);
		}

		public static int overlay(final Context context) {
			return 0xff333300;
		}

		public static int portriatTextColor(final Context context) {
			return context.getResources().getColor(R.color.dark_primaryfont_color);
		}
	}

	public static class dimen
	{
		public static int headerHeight(final Context context, final boolean isAutomotive) {
			return 250;
		}

		public static int height(final Context context, final boolean isAutomotive) {
			if (isAutomotive) {
				return TabBarUtils.scale(context, 128, 128);
			}
			return TabBarUtils.scale(context, 190, 84);
		}

		public static int indicatorThickness(final Context context, final boolean isAutomotive) {
			if (isAutomotive) {
				return TabBarUtils.scale(context, 20, 20);
			}
			return TabBarUtils.scale(context, 14, 12);
		}

		public static int m1(final Context context) {
			return context.getResources().getDimensionPixelSize(R.dimen.margin_l);
		}

		public static int m2(final Context context) {
			return context.getResources().getDimensionPixelSize(R.dimen.margin_m);
		}

		public static int m3(final Context context) {
			return context.getResources().getDimensionPixelSize(R.dimen.margin_s);
		}
	}

	public static class drawable
	{
		public static Drawable background(final Context context) {
			return context.getResources().getDrawable(R.color.list_item_bg_bottom_color);
		}

		public static Drawable darkTextSeletor(final Context context) {
			return context.getResources().getDrawable(R.drawable.list_selector_dark);
		}

		public static Drawable headerBackground(final Context context) {
			return (Drawable)new ColorDrawable(0xff0e5e8c);
		}

		public static Drawable lightTextSeletor(final Context context) {
			return context.getResources().getDrawable(R.drawable.list_selector_light);
		}

		/*public static Drawable popupDivider(final Context context) {
			return context.getResources().getDrawable(R.drawable.common_tab_div);
		}*/
	}
}

