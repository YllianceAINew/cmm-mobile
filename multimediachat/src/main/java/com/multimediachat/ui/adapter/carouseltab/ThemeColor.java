package com.multimediachat.ui.adapter.carouseltab;

import android.graphics.Color;

public class ThemeColor {
	public static final int DIALPAD_KEY_NUMBER = 0;
	public static final int DIALPAD_KEY_ALPHABET = 1;
	public static final int DIALPAD_KEY_PRIMARY = 2;
	public static final int DIALPAD_KEY_SECONDARY = 3;
	public static final int DIALPAD_KEY_DIVIDER = 4;
	public static final int DIALPAD_KEY_PRESS_BG = 5;
	public static final int DIALPAD_DIGITS_DIVIDER = 6;
	public static final int DIALPAD_DIGITS_TEXT = 7;
	public static final int DIALPAD_DIGITS_BG = 8;
	public static final int DIALPAD_BG = 9;
	public static final int ACTIONBAR_BG = 10;
	public static final int DIALPAD_KEY_TEXT_PRESS_BG = 11;
	public static final int MAIN_BACKGROUND = 12;
	public static final int PAGE_MARGIN_COLOR = 13;
	public static final int PRIMARY_TEXT_COLOR = 14;
	public static final int SECONDARY_TEXT_COLOR = 15;
	public static final int LISTVIEW_DIVIDER_COLOR = 16;
	public static final int CATEGORY_BG = 17;
	public static final int DIVIDER_GRAY_COLOR = 18;
	
	public static final int HIGHLIGHT_BG_COLOR = 19;
	public static final int SEPARATOR_BG_COLOR = 20;
	public static final int CONTROL_COLOR = 21;
	public static final int FLOATING_ACTION_BUTTON_COLOR = 22;

	public static final int ACTIONBAR_BG_COLORS[] = new int[] {
			0xFF0E3E57/*0xff0073C3/*blue*/, 0xff0F0F0F/*black*/, 0xff159A79/*green*/, 0xff5D3D7F/*fuchsia*/
	};

	public static final int PAGE_MARGIN_COLORS[] = new int[] {
			0xff878787/*black*/, 0xff7FC2E2/*blue*/, 0xffA0D2B5/*green*/, 0xffFBD298/*yellow*/
	};

	public static int WHITE_COLOR = 0xffbbbbbb;
	public static int BLACK_COLOR = 0xff404040;
	public static int LISTVIEW_DIVIDER_WHITE_COLOR = 0x80b0b0b0;
	public static int LISTVIEW_DIVIDER_BLACK_COLOR = 0x80555555;

	public static final int MAIN_BG_COLOR[] = new int[] {
			0xffffffff/*white*/, 0xffffffff/*orange=0xffB8DDEF*/, 0xffffffff/*green*/, 0xffffffff/*yellow*/, 0xffffffff/*black*/ 
	};

	public static final int ALT_BG_COLOR = 0xff0E1927;//0xffB2CFE9;

	public static final int CARMODE_BG_COLOR = 0xff000000;
	public static final int CARMODE_ACTIONBAR_BG_COLOR = 0xff252525;
	public static final int CARMODE_DIGITS_BG_COLOR = 0xff000000;
	public static final int CARMODE_DIALPAD_BG_COLOR = 0xff000000;
	public static final int CARMODE_DIALPAD_KEY_COLOR = 0xffffffff;
	public static final int CARMODE_DIALPAD_KEY_DIVIDER_COLOR = 0xff3f3f3f;

	public static int s_ui_color;
	public static int s_bg_color;

	public static boolean isLightStyle() {
		if (ThemeColor.getColor(ThemeColor.MAIN_BACKGROUND) > 0xff808080) // white background
			return true;
		else // black background
			return false;
	}

	public static int getColor(int controlType) {
		int color = 0;

		switch (controlType) {
		case LISTVIEW_DIVIDER_COLOR:
			if (isLightStyle()) // white background
				color = LISTVIEW_DIVIDER_WHITE_COLOR;
			else // black background
				color = LISTVIEW_DIVIDER_BLACK_COLOR;
			break;

		case PRIMARY_TEXT_COLOR:
			if (isLightStyle()) // white background
				color = 0xff333333; // text color black
			else // black background
				color = WHITE_COLOR; // text color white
			break;

		case SECONDARY_TEXT_COLOR:
			if (isLightStyle()) // white background
				color = 0xff606060; // text color black
			else // black background
				color = 0xff8e8e8e; // text color white
			break;

		case MAIN_BACKGROUND:
				color = MAIN_BG_COLOR[s_bg_color];
			break;

		case PAGE_MARGIN_COLOR:
		{
			int rgb = getColor(ACTIONBAR_BG);
			float[] hsv = new float[3];
			Color.colorToHSV(rgb, hsv);
			hsv[1] *= 0.3;
			color = Color.HSVToColor(hsv);

		}
		break;

		case DIALPAD_KEY_NUMBER:
		case DIALPAD_KEY_PRIMARY:
			if (isLightStyle()) // white background
				color = 0xff333333;
			else // black background
				color = WHITE_COLOR;
			break;

		case DIALPAD_KEY_ALPHABET:
		case DIALPAD_KEY_SECONDARY:
			color = 0xff6B6B6B;
			break;

		case DIALPAD_KEY_DIVIDER:
		case DIALPAD_DIGITS_DIVIDER:
			color = 0xffafafaf;
			break;

		case DIALPAD_DIGITS_TEXT:
			color = 0xff000000;
			break;

		case DIVIDER_GRAY_COLOR:
			color = 0xffc8c8c8;
			break;
		case DIALPAD_DIGITS_BG:
		case DIALPAD_BG:
			color = 0xfff8f8f8;
			break;

		case DIALPAD_KEY_PRESS_BG:
			color = 0x33666666;
			break;

		case ACTIONBAR_BG:
			color = 0xff0f0f0f;
			if (s_bg_color < ACTIONBAR_BG_COLORS.length)
				color = ACTIONBAR_BG_COLORS[s_bg_color];
			break;

		case CATEGORY_BG:
			if (s_bg_color < ACTIONBAR_BG_COLORS.length)
				color = ACTIONBAR_BG_COLORS[s_bg_color];
			break;

		case DIALPAD_KEY_TEXT_PRESS_BG:
			color = 0xff0097e2;
			break;
		case HIGHLIGHT_BG_COLOR:
			color = 0xff1ac6fe;
			break;
		case SEPARATOR_BG_COLOR:
			if (isLightStyle()) // white background
				color = 0xfff1f2f2;
			else // black background
				color = 0xff3f3f3f;
			break;
		case CONTROL_COLOR:
			if (s_bg_color < ACTIONBAR_BG_COLORS.length)
				color = ACTIONBAR_BG_COLORS[s_bg_color];
			break;
		case FLOATING_ACTION_BUTTON_COLOR:
			color = 0xff0093a9;
			break;
		}

		return color;
	}
}

