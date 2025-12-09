package com.multimediachat.ui.emotic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

import com.multimediachat.R;
import com.multimediachat.util.CenteredImageSpan;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class for annotating a CharSequence with spans to convert textual emoticons
 * to graphical ones.
 */

public class SmileyParser {
    // Singleton stuff

    private static SmileyParser sInstance = null;

    public static SmileyParser getInstance(Context context) {
        if (sInstance == null) {
            init(context);
        }
        return sInstance;
    }

    public static void init(Context context) {
        //GH - added a null check so instances will get reused
        if (sInstance == null)
            sInstance = new SmileyParser(context);
    }

    public static void destroyInstance() {
        if (sInstance != null) {
            sInstance = null;
        }
    }

    private final Context mContext;
    private final String[] mSmileyTexts;
    private final Pattern mPattern;
    private final HashMap<String, Integer> mSmileyToRes;

    private SmileyParser(Context context) {
        mContext = context;
        mSmileyTexts = mContext.getResources().getStringArray(DEFAULT_SMILEY_TEXTS);
        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
    }

    private static final int[] sIconIds = {
            R.drawable.emo_small_01, R.drawable.emo_small_02, R.drawable.emo_small_03,
            R.drawable.emo_small_04, R.drawable.emo_small_05, R.drawable.emo_small_06,
            R.drawable.emo_small_07, R.drawable.emo_small_08, R.drawable.emo_small_09,
            R.drawable.emo_small_10, R.drawable.emo_small_11,
            R.drawable.emo_small_13, R.drawable.emo_small_14, R.drawable.emo_small_15,
            R.drawable.emo_small_16, R.drawable.emo_small_17, R.drawable.emo_small_18,
            R.drawable.emo_small_19, R.drawable.emo_small_20, R.drawable.emo_small_21,
            R.drawable.good, R.drawable.no, R.drawable.ok, R.drawable.down, R.drawable.rain, R.drawable.lightning,
            R.drawable.sun, R.drawable.microphone, R.drawable.clock, R.drawable.email,
            R.drawable.candle, R.drawable.gift, R.drawable.star, R.drawable.heart, R.drawable.bulb, R.drawable.music,
            R.drawable.fuyun, R.drawable.rice, R.drawable.roses, R.drawable.film,
            R.drawable.aeroplane, R.drawable.umbrella, R.drawable.caonima, R.drawable.penguin,
            R.drawable.pig
    };

    public static final int DEFAULT_SMILEY_TEXTS = R.array.default_smiley_texts;

    /**
     * Builds the hashtable we use for mapping the string version
     * of a smiley (e.g. ":-)") to a resource ID for the icon version.
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        if (sIconIds.length != mSmileyTexts.length) {
            // Throw an exception if someone updated DEFAULT_SMILEY_RES_IDS
            // and failed to update arrays.xml
            throw new IllegalStateException("Smiley resource ID/text mismatch");
        }

        HashMap<String, Integer> smileyToRes =
                new HashMap<String, Integer>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mSmileyTexts[i], sIconIds[i]);
        }

        return smileyToRes;
    }

    private Pattern buildPattern() {
        StringBuilder patternString = new StringBuilder(mSmileyTexts.length * 3);
        patternString.append('(');
        for (String s : mSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }

        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }


    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such
     * as :-) with a graphical version.
     *
     * @param text A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any
     * recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text, int containerHeight) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);

        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = mSmileyToRes.get(matcher.group());

            builder.setSpan(getImageSpan(mContext, resId, containerHeight),
                    matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

    private ImageSpan getImageSpan(Context context, int drawableId, int targetHeight) {

        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);

        Bitmap bitmap = Bitmap.createScaledBitmap(originalBitmap, originalBitmap.getWidth() * targetHeight / originalBitmap.getHeight(), targetHeight, true);
        Drawable dr = new BitmapDrawable(context.getResources(), bitmap);
        dr.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

        CenteredImageSpan imgSpan = new CenteredImageSpan(dr, DynamicDrawableSpan.ALIGN_BOTTOM);

        return imgSpan;
    }
}

