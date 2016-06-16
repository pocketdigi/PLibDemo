package security;

import android.os.SystemClock;

/**
 * 时间管理,如果不设置服务器时间，读取返回的是本地时间
 * @author fhp
 */
public class TimestampUtils {
	long serverTime = 0;
	// 设置服务器时间时的设备启动时长
	long elapsedRealtimeWhenSetServerTime = 0;
	private static TimestampUtils instance;
	private TimestampUtils() {
	}

	public static TimestampUtils getInstance() {
		if (instance == null) {
			instance = new TimestampUtils();
		}
		return instance;
	}
	
	/**
	 * 设置服务器时间
	 * @param timestamp
	 */
	public void setServerTime(long timestamp) {
		serverTime = timestamp;
		elapsedRealtimeWhenSetServerTime = SystemClock.elapsedRealtime();
	}
	/**
	 * 读取时间，如果之前设过服务器时间，返回的就是服务器时间(会随时间变化而变化)
	 * 如果没设过，返回的就是System.currentTimeMillis();
	 * @return
	 */
	public long getTimeStamp() {
		if(serverTime==0) {
			return System.currentTimeMillis();
		}
		return serverTime+SystemClock.elapsedRealtime()-elapsedRealtimeWhenSetServerTime;
	}
}
