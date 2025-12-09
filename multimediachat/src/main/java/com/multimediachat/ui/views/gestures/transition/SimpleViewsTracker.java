package com.multimediachat.ui.views.gestures.transition;

import android.support.annotation.NonNull;

import com.multimediachat.ui.views.gestures.transition.tracker.SimpleTracker;

/**
 * @deprecated Use {@link GestureTransitions} class with {@link SimpleTracker} instead.
 */
@SuppressWarnings({ "unused", "deprecation" }) // Class is left for compatibility
@Deprecated
public abstract class SimpleViewsTracker implements ViewsTracker<Integer> {

    @Override
    public int getPositionForId(@NonNull Integer id) {
        return id;
    }

    @Override
    public Integer getIdForPosition(int position) {
        return position;
    }

}