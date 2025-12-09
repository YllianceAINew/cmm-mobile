package com.multimediachat.app;
import java.util.ArrayList;
import java.util.HashMap;

public class NotificationCenter {
    public static final int closeChats = 5;
	public final static int appNeedFinish = 1000;
	
	public final static int group_chat_invited = 6000;
    public final static int group_room_leave = 6001;

    public final static int pica_connection_logined = 7000;
    public final static int user_photo_changed = 7001;

	public final static int albumsDidLoaded = 50008;
	public final static int chat_list_update = 50010;
    public final static int chat_photos_and_videos_selected = 50013;

    public final static int photocrop_photos_compressed = 50017;
    public final static int qrcode_photos_compressed = 50016;
    public final static int chat_video_compressed = 50011;
    public final static int chat_photos_compressed = 50009;
    public final static int editpost_photos_compressed = 50014;
    public final static int editpost_videos_compressed = 50015;
    public final static int setbg_photos_compressed = 50018;

	public final static int friend_list_reload = 80001;

    final private HashMap<Integer, ArrayList<Object>> observers = new HashMap<Integer, ArrayList<Object>>();
    final private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<Integer, Object>();
    final private HashMap<Integer, Object> addAfterBroadcast = new HashMap<Integer, Object>();

    private int broadcasting = 0;

    private static volatile NotificationCenter Instance = null;
    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }

    public void postNotificationName(int id, Object... args) {
        synchronized (observers) {
            broadcasting++;
            ArrayList<Object> objects = observers.get(id);
            if (objects != null) {
                for (Object obj : objects) {
                    ((NotificationCenterDelegate)obj).didReceivedNotification(id, args);
                }
            }
            broadcasting--;
            if (broadcasting == 0) {
                if (!removeAfterBroadcast.isEmpty()) {
                    for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
                        removeObserver(entry.getValue(), entry.getKey());
                    }
                    removeAfterBroadcast.clear();
                }
                if (!addAfterBroadcast.isEmpty()) {
                    for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
                        addObserver(entry.getValue(), entry.getKey());
                    }
                    addAfterBroadcast.clear();
                }
            }
        }
    }

    public void addObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting != 0) {
                addAfterBroadcast.put(id, observer);
                return;
            }
            ArrayList<Object> objects = observers.get(id);
            if (objects == null) {
                observers.put(id, (objects = new ArrayList<Object>()));
            }
            if (objects.contains(observer)) {
                return;
            }
            objects.add(observer);
        }
    }

    public void removeObserver(Object observer, int id) {
        synchronized (observers) {
            if (broadcasting != 0) {
                removeAfterBroadcast.put(id, observer);
                return;
            }
            ArrayList<Object> objects = observers.get(id);
            if (objects != null) {
                objects.remove(observer);
                if (objects.size() == 0) {
                    observers.remove(id);
                }
            }
        }
    }
}
