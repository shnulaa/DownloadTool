package tk.geniusman.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tk.geniusman.manager.Manager;
import tk.geniusman.worker.SnapshotWorker;

/**
 * AbstractDownloader
 * 
 * @author liuyq
 *
 */
public abstract class AbstractDownloader implements Downloader {

    /** the instance of Manager **/
    private static final Manager m = Manager.getInstance();

    @Override
    public void start(final Args args) throws Exception {
        long start = System.currentTimeMillis();
        try {
            // check the argument
            checkArgs(args);

            // check the remote file is exist
            checkRemoteFile(args);

            ScheduledExecutorService s = null;
            ExecutorService es = null;
            try {
                recovery(args.getFullTmpPath());
                m.setSize(args.getFileSize());
                es = startMainTask(args);
                s = startScheduledTask(args);
            } catch (Exception e) {
                throw e;
            } finally {
                if (es != null) {
                    es.shutdown();
                }
                es.awaitTermination(30, TimeUnit.HOURS);

                if (s != null) {
                    s.shutdown();
                }
                if (args.getFullTmpPath().exists()) {
                    args.getFullTmpPath().delete();
                }
            }
            // System.out.print(ProgressBar.showBarByPoint(100, 100, 70,
            // m.getPerSecondSpeed(), true));
            System.out.flush();
            long end = System.currentTimeMillis();
            System.out.println("cost time: " + (end - start) / 1000 + "s");
            m.getPlistener().change(1, 0, Thread.currentThread());
            m.getFlistener().finish(false, "Download Complete Successfully..");
        } catch (Exception e) {
            e.printStackTrace();
            m.getFlistener().finish(false, "Download Complete With Exception..");
        } finally {

        }

    }

    @Override
    public void terminate() {
        m.terminate();
    }

    /**
     * startMainTask
     * 
     * @return
     */
    protected abstract <T extends ExecutorService> T startMainTask(final Args args) throws Exception;

    /**
     * startScheduledTask
     * 
     * @param size
     * @param sFile
     */
    private ScheduledExecutorService startScheduledTask(final Args args) {
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(new SnapshotWorker(args.getFullTmpPath(), args.getFileSize()), 0, 1, TimeUnit.SECONDS);
        return s;
    }

    /**
     * checkRemoteFile
     * 
     * @param args
     * @throws MalformedURLException
     */
    private long checkRemoteFile(Args args) throws Exception {
        final URL url = new URL(args.getDownloadUrl());
        args.setUrl(url);

        HttpURLConnection.setFollowRedirects(true);

        URLConnection connection = null;
        try {

            connection = url.openConnection();
            connection.setRequestProperty("Accept-Encoding", "identity");
            if (connection instanceof HttpURLConnection) {

                int code = ((HttpURLConnection) connection).getResponseCode();
                System.out.println("response code: " + code);

                final long size = ((HttpURLConnection) connection).getContentLength();
                System.out.println("remote file content size:" + size);

                if (code != 200 || size <= 0) {
                    System.err.println("remote file size is negative, skip download...");
                    throw new RuntimeException("remote file size is negative, skip download...");
                }
                args.setFileSize(size);
                return size;
            } else {
                throw new RuntimeException("The destination url http connection is not support.");
            }
        } catch (Exception e) {
            throw e;
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
