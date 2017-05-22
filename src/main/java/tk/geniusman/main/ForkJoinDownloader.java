package tk.geniusman.main;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import tk.geniusman.thread.ForkJoinWorkerThreadFactoryExt;
import tk.geniusman.worker.ForkJoinDownloadWorker;

/**
 * ForkJoinDownloader
 * 
 * @author liuyq
 *
 */
public class ForkJoinDownloader extends AbstractDownloader {

    @SuppressWarnings("unchecked")
    @Override
    protected <T extends ExecutorService> T startMainTask(final Args args) throws Exception {
        ForkJoinPool pool = new ForkJoinPool(args.getThreadNumber(), new ForkJoinWorkerThreadFactoryExt(), (t, e) -> {
            final String errorMessage = "Unknown Exception occurred " + "while using fock join thread pool, err: "
                    + e.getMessage();
            System.err.println(errorMessage);
        }, false);
        pool.submit(new ForkJoinDownloadWorker(0, args.getFileSize(), args.getFileSize(), args.getUrl(),
                new File(args.getFullPath())));
        return (T) pool;
    }

}
