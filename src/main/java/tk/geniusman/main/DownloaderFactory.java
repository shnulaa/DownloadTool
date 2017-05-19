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
        switch (type) {
        case FORK_JOIN:
            return type.getClazz().newInstance();
        default:
            break;
        }

        return null;
    }

}
