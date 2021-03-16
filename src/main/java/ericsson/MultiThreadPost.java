package ericsson;

import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Date 2021/3/16
 */
public class MultiThreadPost {
	private int cntThreads;
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiThreadPost.class);

	/**
	 * 需要的线程数量
	 *
	 * @param nThreads
	 */
	public MultiThreadPost(int nThreads) {
		this.cntThreads = nThreads;
	}

	public AtomicBoolean sendRequest() {
		AtomicBoolean flag = new AtomicBoolean(true);
		ExecutorService executor = Executors.newFixedThreadPool(cntThreads);
		for (int i = 1; i <= cntThreads; i++) {
			int threadNum = i;
			executor.submit(()->{
				HashMap<String, Object> map = new HashMap<>();
				map.put("threadNumber", threadNum);
				String result = "";
				boolean success = true;
				String reasonIfFail = "";
				long startTime = System.nanoTime();
				try {
					result = HttpUtil.post("http://localhost:8081", map, threadNum * 1001);
				} catch (Exception e) {
					success = false;
					reasonIfFail = e.getMessage();
				}
				long timeSpentNano = System.nanoTime() - startTime;
				long timeSpentSec = TimeUnit.SECONDS.convert(timeSpentNano,
						TimeUnit.NANOSECONDS);
				if (timeSpentNano > threadNum * 1e9) {
					success = false;
					if(flag.get()){
						flag.set(false);
					}
					reasonIfFail = "timeout";
				}
				// 对请求状态进行判断，如果发送失败，将flag设置为false返回。
				//LOGGER.info("Thread {} take {} seconds, request was {}", threadNum, timeSpentSec,
						//success ? "successful, returned value is ：" + result : "failed, the reason is ：" + reasonIfFail);
				if(!success) {
					LOGGER.info("Thread {} take {} seconds, request was {}", threadNum, timeSpentSec,
						"failed, the reason is ：" + reasonIfFail);
				}
			});
		}
		executor.shutdown();
		try {
			executor.awaitTermination(6, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return flag;
	}

	public int getCntThreads() {
		return cntThreads;
	}

	public void setCntThreads(int cntThreads) {
		this.cntThreads = cntThreads;
	}

	public static void main(String[] args) {
		MultiThreadPost post = new MultiThreadPost(1000);
		AtomicBoolean atomicBoolean = post.sendRequest();

		if(atomicBoolean.get()){
			System.out.println("All requests were successful!");
		} else {
			System.out.println("Some requests were failed, See the jbit.log file for more information...");
		}
	}

}
