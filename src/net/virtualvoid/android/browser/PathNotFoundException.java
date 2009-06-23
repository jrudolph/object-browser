package net.virtualvoid.android.browser;

public class PathNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String segment;

    public PathNotFoundException(String segment, Throwable throwable) {
        super(throwable);
        this.segment = segment;
    }
    @Override
    public String getMessage() {
        return "Path segment "+segment+" not found";
    }
    public String getSegment() {
        return segment;
    }
}
