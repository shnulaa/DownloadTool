package tk.geniusman.downloader;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import tk.geniusman.thread.DefaultThreadFactoryExt;
import tk.geniusman.thread.DefaultThreadPoolExecutorExt;
import tk.geniusman.worker.DefaultDownloadWorker;

/**
 * DefaultDownloader
 * 
 * @author liuyq
 *
 */
public class DefaultDownloader extends AbstractDownloader {

    /**
     * DefaultDownloader
     * 
     * @param args
     */
    public DefaultDownloader(Args args) {
        super(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends ExecutorService> T startMainTask(Args args) throws Exception {
        final long fileSize = args.getFileSize();
        final int threadNum = args.getThreadNumber();
        final URL url = args.getUrl();
        final File dFile = new File(args.getFullPath());

        // ExecutorService service = Executors.newFixedThreadPool(threadNum, new
        // DefaultThreadFactoryExt());

        ExecutorService service = new DefaultThreadPoolExecutorExt(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactoryExt());
        final long per = fileSize / threadNum;
        IntStream.rangeClosed(1, threadNum).boxed()
                .map((v) -> new DefaultDownloadWorker((v - 1) * per + 1, v * per, fileSize, url, dFile))
                .forEach((o) -> service.submit(o));
        return (T) service;
    }

    /**
     * for test
     * 
     * @param args
     */
    public static void main(String[] args) {
        long fileSize = 15000;
        int threadNum = 15;
        System.out.println(fileSize / threadNum);
        System.out.println(fileSize % threadNum);
        final long per = fileSize / threadNum;

        IntStream.rangeClosed(1, threadNum).boxed()
                .map((v) -> new DefaultDownloadWorker((v - 1) * per + 1, v * per, fileSize, null, null))
                .forEach((o) -> System.out.println(o.getStart() + "---" + o.getEnd()));

    }

}
