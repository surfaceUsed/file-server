package application;

public enum HTTPRequest {

    GET("1"),
    PUT("2"),
    DELETE("3"),
    EXIT("exit"),
    INVALID_REQUEST("-1");

    private final String code;

    HTTPRequest(String  code) {
        this.code = code;
    }

    final String getCode() {
        return this.code;
    }

    public static HTTPRequest getRequest(String request) {
        for (HTTPRequest sr : HTTPRequest.values()) {
            if (sr.getCode().equals(request)) {
                return sr;
            }
        }
        return INVALID_REQUEST;
    }
}