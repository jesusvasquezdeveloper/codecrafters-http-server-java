import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class RequestHandler extends Thread {

    private Socket socket;
    private String[] args;
    private List<String> supportedEncodings;

    public RequestHandler(Socket socket, String[] args) {
        this.socket = socket;
        this.args = args;
        this.supportedEncodings = new ArrayList<>();
        this.supportedEncodings.add("gzip");
    }

    @Override
    public void run() {
        try {
            HttpRequest request = request(socket);

            String path = request.getPath();

            OutputStream out = socket.getOutputStream();

            if (path.equals("/")) {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            } else if (path.startsWith("/echo/")) {
                String echoWord = path.split("/echo/")[1];
                StringBuilder encodingHeader = new StringBuilder();
                Optional<String> acceptEncoding = Optional.ofNullable(request.getHeader("Accept-Encoding"));

                List<String> encodings = acceptEncoding.map(value -> Arrays.stream(value.split(","))
                    .map(String::trim).toList())
                    .orElse(new ArrayList<>());

                if (encodings.contains("gzip")) {
                    encodingHeader.append("Content-Encoding: gzip\r\n");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try (GZIPOutputStream gzipOutputStream =
                                 new GZIPOutputStream(byteArrayOutputStream)) {
                        gzipOutputStream.write(echoWord.getBytes(StandardCharsets.UTF_8));
                    }
                    byte[] gzipData = byteArrayOutputStream.toByteArray();

                    out.write(("HTTP/1.1 200 OK\r\n" + encodingHeader + "Content-Type: text/plain\r\nContent-Length:" + echoWord.length() + "\r\n\r\n").getBytes("UTF-8"));
                    out.write(gzipData);
                } else {
                    out.write(("HTTP/1.1 200 OK\r\n" + encodingHeader + "Content-Type: text/plain\r\nContent-Length:" + echoWord.length() + "\r\n\r\n" + echoWord).getBytes());
                }
            } else if (path.equals("/user-agent")) {
                String userAgent = request.getHeader("User-Agent");
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length:" + userAgent.length() + "\r\n\r\n" + userAgent).getBytes());

            } else if(path.startsWith("/files/")) {

                String directory = args[1];
                String file = path.split("/files/")[1];
                String finalFile = directory + file;

                if ("POST".equals(request.getMethod())) {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(finalFile))) {
                        bufferedWriter.write(request.getBody());
                        bufferedWriter.flush();
                    }

                    out.write("HTTP/1.1 201 Created\r\n\r\n".getBytes());

                }
                else {

                    StringBuilder stringBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new FileReader(finalFile))) {

                        String line = reader.readLine();
                        stringBuilder.append(line);

                        while((line = reader.readLine()) != null) {
                            stringBuilder.append("\n");
                            stringBuilder.append(line);
                        }

                        out.write(("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length:" + stringBuilder.length() + "\r\n\r\n" + stringBuilder).getBytes());

                    } catch (FileNotFoundException e) {
                        out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                    }

                }
            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpRequest request(Socket socket)  {

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String[] firstLine = bufferedReader.readLine().split(" ");

            String method = firstLine[0];
            String path = firstLine[1];

            HttpRequest request = new HttpRequest(path);
            request.setMethod(method);

            String line;
            while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                String[] header = line.split(": ");
                request.addHeader(header[0], header[1]);
            }

            StringBuilder body = new StringBuilder();
            while(bufferedReader.ready()) {
                body.append((char)bufferedReader.read());
            }

            request.setBody(body.toString());

            return request;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
