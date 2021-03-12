package ericsson;

import cn.hutool.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/3/12
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

	public void sendRequest() {
		for (int i = 1; i <= cntThreads; i++) {
			int threadNum = i ;
			new Thread(() -> {
				HashMap<String, Object> map = new HashMap<>();
				map.put("线程号", threadNum);
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
					reasonIfFail = "超时";
				}
				LOGGER.info("线程{}请求耗时{}秒,请求{}", threadNum, timeSpentSec,
						success ? "成功,返回值为：" + result : "失败,原因是：" + reasonIfFail);
			}).start();
		}
	}

	public int getCntThreads() {
		return cntThreads;
	}

	public void setCntThreads(int cntThreads) {
		this.cntThreads = cntThreads;
	}

	public static void main(String[] args) {
		MultiThreadPost post = new MultiThreadPost(10);
		post.sendRequest();
	}

}
