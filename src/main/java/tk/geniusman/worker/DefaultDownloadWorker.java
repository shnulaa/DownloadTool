package tk.geniusman.worker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import tk.geniusman.manager.Manager;

/**
 * DefaultDownloadWorker implements Callable <br>
 * get stream from the URL with header Range bytes=start-end <br>
 * This worker only download the range with specified start <br>
 * and end position
 * 
 * @author liuyq
 */
public class DefaultDownloadWorker implements Callable<Void> {

    /** one thread download connection timeout **/
    private static final int THREAD_DOWNLOAD_TIMEOUT = 5000;
    /** one thread download max retry count **/
    private static final int THREAD_MAX_RETRY_COUNT = 10;
    /** the instance of Manager **/
    private final Manager m = Manager.getInstance();

    private String key; // (start - end)
    private long start; // the start position to download
    private long end; // the end position to download
    private URL url; // the URL will be download
    private File dFile; // the desc file to be saved
    private AtomicLong current; // the current position
    private long fileSize;// total file size

    /**
     * DownloadWorker
     * 
     * @param start
     * @param end
     * @param url
     * @param dFile
     */
    public DefaultDownloadWorker(long start, long end, long fileSize, URL url, File dFile) {
        this.start = start;
        this.current = new AtomicLong(start);
        this.end = end;
        this.url = url;
        this.dFile = dFile;
        this.key = (String.valueOf(this.start) + "-" + String.valueOf(this.end));
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return String.format("start: %s, end: %s, current: %s", start, end, current);
    }

    @Override
    public Void call() throws Exception {
        int retryCount = 0;
        final Thread t = Thread.currentThread();
        while (retryCount++ < THREAD_MAX_RETRY_COUNT) {
            HttpURLConnection con = null;
            HttpURLConnection.setFollowRedirects(true);
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(THREAD_DOWNLOAD_TIMEOUT);
                con.setConnectTimeout(THREAD_DOWNLOAD_TIMEOUT);

                if (getCurrent() >= end) {
                    // System.err.println("Already complete..");
                    return null;
                }

                con.setRequestProperty("Range", "bytes=" + getCurrent() + "-" + end);

                try (BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                        RandomAccessFile file = new RandomAccessFile(dFile, "rw");) {
                    file.seek(getCurrent());
                    final byte[] bytes = new byte[1024];

                    int readed = 0;
                    while (!t.isInterrupted() && !m.terminate && (readed = bis.read(bytes)) != -1) {

                        while (m.isPause()) {
                            LockSupport.park();
                        }

                        // duplicate check due to terminate this thread as soon
                        // as possible
                        if (t.isInterrupted() || m.terminate) {
                            break;
                        }

                        file.write(bytes, 0, readed);
                        current.getAndAdd(readed);
                        m.getListener().change(current.get(), fileSize, t);
                        m.alreadyRead.getAndAdd(readed);
                    }
                    break;
                } catch (Exception e) {
                    // System.err.println("exception occurred while
                    // download..");
                    // e.printStackTrace();
                    Thread.sleep(1000);
                    continue; // write exception or read timeout, retry
                }
            } catch (Exception ex) {
                // System.err.println("exception occurred while download..");
                // ex.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // System.err.println("InterruptedException occurred while
                    // download..");
                    // e.printStackTrace();
                }
                continue;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        return null;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getCurrent() {
        return current.get();
    }

    public String getKey() {
        return key;
    }

    //
    // private static void addHeader(URLConnection connection) {
    // connection.setRequestProperty("User-Agent",
    // "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like
    // Gecko) Chrome/47.0.2526.106 Safari/537.36");
    // connection.setRequestProperty("Upgrade-Insecure-Requests", "100");
    // connection.setRequestProperty("Accept",
    // "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    // connection.setRequestProperty("Connection", "keep-alive");
    // connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
    // connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
    // connection.setRequestProperty("Cookie",
    // "kuaichuanid=7737D93877EE6FEA2110BB086960418B"); // thunder
    //
    // }
}