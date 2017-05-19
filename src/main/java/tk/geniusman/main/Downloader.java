package tk.geniusman.main;

import java.io.File;

/**
 * the interface of the downloader
 * 
 * @author liuyq
 *
 */
public interface Downloader {
    /** the file destination folder **/
    static final String FOLDER = "./";
    /** ForkJoinPool pool size **/
    static final int POOL_SIZE = 15;

    /**
     * start the downloader with specified argument
     * 
     * @param args
     *            the argument
     */
    void start(Args args) throws Exception;

    /**
     * terminate the downloader
     */
    void terminate();

    default boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    default boolean isNull(Integer integer) {
        return (integer == null);
    }

    default boolean isNotEmpty(String str) {
        return (!isEmpty(str));
    }

    /**
     * 
     * @param args
     * @return
     */
    default void checkArgs(final Args args) {
        if (args == null || isEmpty(args.getDownloadUrl()) || isNull(args.getThreadNumber())
                || isEmpty(args.getSavedPath())) {
            System.err.println("argument error..");
            System.err.println("usage: java -jar [XX.jar] [downloadUrl] [threadNumber] [savedPath] [savedName]");
            throw new RuntimeException("argument error..");
        }

        if (isEmpty(args.getSavedPath())) {
            args.setSavedPath(FOLDER);
        }

        String downloadURL = args.getDownloadUrl();
        String fileName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
        if (isEmpty(args.getFullFileName())) {
            args.setFullFileName(fileName);
        }

        String fullPath = args.getSavedPath() + args.getFullFileName();
        if (!args.getSavedPath().endsWith(File.separator)) {
            args.setFullPath(args.getSavedPath() + File.separator + args.getFullFileName());
        } else {
            args.setFullPath(fullPath);
        }

        final String sFilePath = fullPath + ".s";
        args.setFullTmpPath(new File(sFilePath));
    }
}
