package mknv.psm.server.web.exception;

/**
 *
 * @author mknv
 */
public class ErrorInfo {

    private String message;

    public ErrorInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ErrorInfo{" + "message=" + message + '}';
    }

}
