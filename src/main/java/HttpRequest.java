import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private String path;
    private Map<String, String> headers;
    private String body;

    public String getPath() {
        return path;
    }

    public HttpRequest(String path) {
        this.path = path;
        this.headers = new HashMap<>();
    }

    public String getHeader(String header) {
        return headers.get(header);
    }
    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
}
