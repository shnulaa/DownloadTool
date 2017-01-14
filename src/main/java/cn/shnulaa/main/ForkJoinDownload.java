package cn.shnulaa.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.shnulaa.bar.ProgressBar;
import cn.shnulaa.manager.Manager;
import cn.shnulaa.thread.ForkJoinWorkerThreadFactoryExt;
import cn.shnulaa.worker.DownloadWorker;
import cn.shnulaa.worker.SnapshotWorker;

/**
 * "https://github.com/hashem78/android_hardware_qcom_gps/archive/msm8x94.zip");
 * "http://184.164.76.104/pukiwiki.20150612.tar.gz");
 * "http://speed.myzone.cn/pc_elive_1.1.rar"); "); <br>
 * "http://down.360safe.com/cse/360cse_8.5.0.126.exe"); <br>
 * "http://down.360safe.com/cse/360cse_8.5.0.126.exe"); <br>
 * 
 * ForkJoin download
 * 
 * @author liuyq
 * 
 */
public final class ForkJoinDownload {
	/** the file destination folder **/
	private static final String FOLDER = "./";
	/** ForkJoinPool pool size **/
	private static final int POOL_SIZE = 15;
	/** the instance of Manager **/
	private static final Manager m = Manager.getInstance();

	/**
	 * main
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		if (args == null || args.length < 3) {
			System.err.println("argument error..");
			System.err.println("usage: java -jar [XX.jar] [downloadUrl] [threadNumber] [savedPath] [savedName]");
			return;
		}

		if (args[0] == null || args[0].isEmpty()) {
			System.err.println("the download url must specified..");
			return;
		}
		final String downloadURL = args[0];

		int threadNumber = POOL_SIZE;
		try {
			threadNumber = Integer.valueOf(args[1]);
		} catch (Exception ex) {
			System.err.println("threadNumber error, use default..");
		}

		String savedPath = FOLDER;
		if (args[2] != null || !args[2].isEmpty()) {
			savedPath = args[2];
		}

		String fileName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
		if (args[3] != null && !args[3].isEmpty()) {
			fileName = args[3];
		}

		final URL url = new URL(downloadURL);
		HttpURLConnection.setFollowRedirects(true);

		URLConnection connection = null;
		try {

			connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {

				int code = ((HttpURLConnection) connection).getResponseCode();
				System.out.println("response code: " + code);

				final long size = ((HttpURLConnection) connection).getContentLength();
				System.out.println("remote file content size:" + size);

				if (code != 200 || size <= 0) {
					System.err.println("remote file size is negative, skip download...");
					throw new RuntimeException("remote file size is negative, skip download...");
				}

				String fullPath = savedPath + fileName;
				if (!savedPath.endsWith(File.separator)) {
					fullPath = savedPath + File.separator + fileName;
				}

				final String sFilePath = fullPath + ".s";
				final File sFile = new File(sFilePath);

				long start = System.currentTimeMillis();
				ScheduledExecutorService s = null;
				ForkJoinPool pool = null;
				try {
					s = Executors.newSingleThreadScheduledExecutor();

					pool = new ForkJoinPool(threadNumber, new ForkJoinWorkerThreadFactoryExt(), null, false);

					recovery(sFile);
					m.setSize(size);
					pool.submit(new DownloadWorker(0, size, url, new File(fullPath)));
					s.scheduleAtFixedRate(new SnapshotWorker(sFile, size), 0, 1, TimeUnit.SECONDS);
				} finally {
					if (pool != null) {
						pool.shutdown();
					}
					pool.awaitTermination(30, TimeUnit.HOURS);

					if (s != null) {
						s.shutdown();
					}
					if (sFile.exists()) {
						sFile.delete();
					}
				}

				System.out.print(ProgressBar.showBarByPoint(100, 100, 70, m.getPerSecondSpeed(), true));
				System.out.flush();
				long end = System.currentTimeMillis();
				System.out.println("cost time: " + (end - start) / 1000 + "s");
				m.getPlistener().change(1, 0, Thread.currentThread());
				m.getFlistener().finish(false, "Download Complete Successfully..");
			} else {
				System.err.println("The destination url http connection is not support.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			m.getFlistener().finish(false, "Download Complete With Exception..");
		} finally {
			if (connection instanceof HttpURLConnection) {
				if (connection != null)
					((HttpURLConnection) connection).disconnect();
			} else {
				System.err.println("connection is not the instance of HttpURLConnection..");
			}
		}
	}

	/**
	 * recovery from the snapshot file
	 * 
	 * @param sFile
	 *            the snapshot file
	 */
	private static void recovery(final File sFile) {
		if (sFile.exists()) {
			Manager re = readObject(sFile);
			m.recovry(re);
			m.recovery = true;
		}
	}

	/**
	 * read the Object from file
	 * 
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> T readObject(File path) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			return (T) ois.readObject();
		} catch (Exception e) {
			System.err.println("exception occurred when read object.");
			e.printStackTrace();
		}
		return null;
	}

}
