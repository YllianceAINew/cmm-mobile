package com.multimediachat.app.im.engine;

public interface HeartbeatService {
    interface Callback {
        /**
         * Called on heartbeat schedule.
         * 
         * @return the offset in milliseconds that the method wants to be called
         *         the next time. Return 0 or negative value indicates to stop
         *         the schedule of this callback.
         */
        long sendHeartbeat();
    }

    /**
     * Start to schedule a heartbeat operation.
     * 
     * @param callback The operation wants to be called repeat.
     * @param triggerTime The time(in milliseconds) until the operation will be
     *            executed the first time.
     */
    void startHeartbeat(Callback callback, long triggerTime);

    /**
     * Stop scheduling a heartbeat operation.
     * 
     * @param callback The operation will be stopped.
     */
    void stopHeartbeat(Callback callback);
}
