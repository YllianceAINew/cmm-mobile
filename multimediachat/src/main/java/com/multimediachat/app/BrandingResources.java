package com.multimediachat.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.multimediachat.app.im.plugin.BrandingResourceIDs;
import com.multimediachat.app.im.plugin.ImPlugin;
import com.multimediachat.app.im.plugin.ImPluginInfo;

/** The provider specific branding resources. */
public class BrandingResources {
    private static final String TAG = ImApp.LOG_TAG;

    private Map<Integer, Integer> mResMapping;
    private Resources mPackageRes;
  
    private BrandingResources mDefaultRes;

    /**
     * Creates a new BrandingResource of a specific plug-in. The resources will
     * be retrieved from the plug-in package.
     * 
     * @param context The current application context.
     * @param pluginInfo The info about the plug-in.
     * @param defaultRes The default branding resources. If the resource is not
     *            found in the plug-in, the default resource will be returned.
     */
    @SuppressWarnings("rawtypes")
	public BrandingResources(Context context, ImPluginInfo pluginInfo, BrandingResources defaultRes) {
        mDefaultRes = defaultRes;

        PackageManager pm = context.getPackageManager();
        try {
            mPackageRes = pm.getResourcesForApplication(pluginInfo.mPackageName);
        } catch (NameNotFoundException e) {
        	DebugConfig.error(TAG, "Can not load resources from package: " + pluginInfo.mPackageName);
        }
        // Load the plug-in directly from the apk instead of binding the service
        // and calling through the IPC binder API. It's more effective in this way
        // and we can avoid the async behaviors of binding service.
        ClassLoader classLoader = context.getClassLoader();
        try {
            Class cls = classLoader.loadClass(pluginInfo.mClassName);
            @SuppressWarnings("unchecked")
			Method m = cls.getMethod("onBind", Intent.class);
            ImPlugin plugin = (ImPlugin) m.invoke(cls.newInstance(), new Object[] { null });
            mResMapping = plugin.getResourceMap();
            
        } catch (ClassNotFoundException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalAccessException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (InstantiationException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (SecurityException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (NoSuchMethodException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (IllegalArgumentException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        } catch (InvocationTargetException e) {
        	DebugConfig.error(TAG, "Failed load the plugin resource map", e);
        }
    }

    /**
     * Creates a BrandingResource with application context and the resource ID
     * map. The resource will be retrieved from the context directly instead
     * from the plug-in package.
     * 
     * @param context
     * @param resMapping
     */
    public BrandingResources(Context context, Map<Integer, Integer> resMapping,
            BrandingResources defaultRes) {
        this(context.getResources(), resMapping, defaultRes);
    }

    public BrandingResources(Resources packageRes, Map<Integer, Integer> resMapping,
           BrandingResources defaultRes) {
        mPackageRes = packageRes;
        mResMapping = resMapping;
        mDefaultRes = defaultRes;
    }

    /**
     * Gets a drawable object associated with a particular resource ID defined
     * in {@link BrandingResourceIDs}
     * 
     * @param id The ID defined in
     *            {@link BrandingResourceIDs}
     * @return Drawable An object that can be used to draw this resource.
     */
    public Drawable getDrawable(int id) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getDrawable(resId);
        } else if (mDefaultRes != null) {
            return mDefaultRes.getDrawable(id);
        } else {
            return null;
        }
    }



    /**
     * Gets the string value associated with a particular resource ID defined in
     * {@link BrandingResourceIDs}
     * 
     * @param id The ID of the string resource defined in
     *            {@link BrandingResourceIDs}
     * @param formatArgs The format arguments that will be used for
     *            substitution.
     * @return The string data associated with the resource
     */
    public String getString(int id, Object... formatArgs) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getString(resId, formatArgs);
        } else if (mDefaultRes != null) {
            return mDefaultRes.getString(id, formatArgs);
        } else {
            return null;
        }
    }

    /**
     * Gets the string array associated with a particular resource ID defined in
     * {@link BrandingResourceIDs}
     * 
     * @param id The ID of the string resource defined in
     *            {@link BrandingResourceIDs}
     * @return The string array associated with the resource.
     */
    public String[] getStringArray(int id) {
        int resId = getPackageResourceId(id);
        if (resId != 0) {
            return mPackageRes.getStringArray(resId);
        } else if (mDefaultRes != null) {
            return mDefaultRes.getStringArray(id);
        } else {
            return null;
        }
    }

    private int getPackageResourceId(int id) {
        if (mResMapping == null || mPackageRes == null) {
            return 0;
        }
        Integer resId = mResMapping.get(id);
        return resId == null ? 0 : resId;
    }

}
