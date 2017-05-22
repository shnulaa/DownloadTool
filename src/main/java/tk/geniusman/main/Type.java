package tk.geniusman.main;

public enum Type {

    FORK_JOIN(ForkJoinDownloader.class), DEFAULT(DefaultDownloader.class);

    private Class<? extends AbstractDownloader> clazz;

    private Type(Class<? extends AbstractDownloader> clazz) {
        this.setClazz(clazz);
    }

    public Class<? extends AbstractDownloader> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends AbstractDownloader> clazz) {
        this.clazz = clazz;
    }

}
