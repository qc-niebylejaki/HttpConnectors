package qc.tutorials.connectors.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO: need refactor
@Slf4j
public class ApacheHttpClient<R, P> implements AutoCloseable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    private final CloseableHttpClient client;
    private final URI baseUri;

    public ApacheHttpClient(URI baseAisInitiatorUri) {
        this(baseAisInitiatorUri, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    public ApacheHttpClient(URI baseAisInitiatorUri, int connectionTimeout, int socketTimeout) {
        this.baseUri = Objects.requireNonNull(baseAisInitiatorUri);
        this.client = createClient(connectionTimeout, socketTimeout);
    }

    private CloseableHttpClient createClient(int connectionTimeout, int socketTimeout) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public P post(URI uri, R request, Class<P> responseClass) throws Exception {
        HttpPost httpPost = new HttpPost();
        httpPost.setEntity(new StringEntity(OBJECT_MAPPER.writeValueAsString(request), ContentType.APPLICATION_JSON));
        httpPost.setURI(uri);

        return executeRequest(httpPost, responseClass);
    }

    public P post(URI uri, Map<String, String> params, Class<P> responseClass) throws Exception {
        HttpPost httpPost = new HttpPost();
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>();
        params.forEach((key, value) -> nameValuePairs.add(new BasicNameValuePair(key, value)));
        httpPost.setURI(uri);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));

        return executeRequest(httpPost, responseClass);
    }

    public <T> T get(URI uri, Map<String, String> params, Class<T> responseClass) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(uri);
        params.forEach(uriBuilder::addParameter);

        HttpGet httpGet = new HttpGet(uri);
        return executeRequest(httpGet, responseClass);
    }

    private <T> T executeRequest(HttpRequestBase httpMethod, Class<T> responseClass) throws Exception {
        try (CloseableHttpResponse response = this.client.execute(httpMethod)) {
            int statusCode = response.getStatusLine()
                    .getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Received unacceptable status code [" + statusCode + "]");
            }

            return OBJECT_MAPPER.readValue(response.getEntity().getContent(), responseClass);
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(this.client)) {
            this.client.close();
        }
    }
}
