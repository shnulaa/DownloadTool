package tk.geniusman.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import tk.geniusman.manager.Manager;

/**
 * 
 * @author liuyq
 *
 */
public class DefaultThreadPoolExecutorExt extends ThreadPoolExecutor {

    private static final Manager m = Manager.getInstance();

    /**
     * DefaultThreadPoolExecutorExt
     * 
     * @param corePoolSize
     * @param maximumPoolSize
     * @param keepAliveTime
     * @param unit
     * @param workQueue
     */
    public DefaultThreadPoolExecutorExt(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory threadFoctory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFoctory);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        m.addThread(t);
    }

    protected void afterExecute(Runnable r, Throwable t) {
        // m
    }

}
