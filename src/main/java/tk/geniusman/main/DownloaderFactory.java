package tk.geniusman.main;

/**
 * DownloaderFactory
 * 
 * @author liuyq
 *
 */
public class DownloaderFactory {

    /**
     * getInstance
     * 
     * @param type
     * @return the instance of Downloader
     */
    public static Downloader getInstance(Type type) throws Exception {
        return type.getClazz().newInstance();
    }
}
