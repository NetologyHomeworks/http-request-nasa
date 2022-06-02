import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    private static final String REQUEST_URI = "https://api.nasa.gov/planetary/apod?api_key=BCE0ggw3rLilwMdLDeTrXtRLwenNBToOLkl8Jh4F";

    public static void main(String[] args) {
        doNasaHttpRequest(REQUEST_URI);
    }

    private static void doNasaHttpRequest(String uri) {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            CloseableHttpResponse response = httpClient.execute(new HttpGet(uri));

            List<Nasa> list = jsonToList(response.getEntity().getContent());
            for (Nasa obj : list) {
                response = httpClient.execute(new HttpGet(obj.getUrl()));
                saveUrlDataToFile(obj.getUrl(), response.getEntity().getContent());
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static CloseableHttpClient createHttpClient() {
        return HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(30000)
                .setRedirectsEnabled(false)
                .build()).build();
    }

    private static List<Nasa> jsonToList(InputStream jsonData) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            return mapper.readValue(jsonData, new TypeReference<>() {});
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static void saveUrlDataToFile(String uri, InputStream data) {
        try (BufferedInputStream inpBuf = new BufferedInputStream(data);
             FileOutputStream fileOut = new FileOutputStream(new File(getFileNameFromUri(uri)))) {
            final short BUF_SIZE = 1024;
            byte[] buffer = new byte[BUF_SIZE];
            int read;
            while ((read = inpBuf.read(buffer, 0, BUF_SIZE)) != -1) {
                fileOut.write(buffer, 0, read);
            }
            data.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static String getFileNameFromUri(String uri) {
        try {
            return new File(new URI(uri).getPath()).getName();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
}
