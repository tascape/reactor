package com.tascape.qa.th.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Linsong Wang
 */
public class RestClient extends EntityCommunication {
    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    public static final String SYSPROP_CLIENT_CERT = "qa.th.comm.webserviceclient.CLIENT_CERT";

    private final String host;

    private final int port;

    private final String baseUri;

    private CloseableHttpClient client;

    private final Map<String, Long> responseTime = new HashMap<>();

    /**
     * 
     * @param host host DNS name or IP
     * @param port https for 443 or 8443, http for others
     */
    public RestClient(String host, int port) {
        this.host = host;
        this.port = port;
        if (port == 443 || port == 8443) {
            this.baseUri = "https://" + host + ":" + port;
        } else {
            this.baseUri = "http://" + host + ":" + port;
        }
    }

    @Override
    public void connect() throws Exception {
        SSLContextBuilder contextBuilder = SSLContexts.custom();
        contextBuilder.loadTrustMaterial(null, acceptingTrustStrategy);

        String cc = SYSCONFIG.getProperty(SYSPROP_CLIENT_CERT);
        LOG.debug("client cert {}", cc);

        RegistryBuilder registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", new PlainConnectionSocketFactory());

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setKeepAliveStrategy(this.keepAliveStrategy)
            .setRedirectStrategy(new LaxRedirectStrategy());

        if (cc != null) {
            try (FileInputStream instream = new FileInputStream(new File(cc))) {
                KeyStore ks = KeyStore.getInstance("pkcs12");
                ks.load(instream, "123".toCharArray());
                contextBuilder.loadKeyMaterial(ks, "123".toCharArray());
            }
        }
        SSLContext sslContext = contextBuilder.build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        registryBuilder.register("https", sslsf);
        httpClientBuilder.setSSLSocketFactory(sslsf);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = registryBuilder.build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        HttpHost h = new HttpHost(this.host, this.port);
        cm.setMaxPerRoute(new HttpRoute(h), 200);

        this.client = httpClientBuilder.setConnectionManager(cm).build();
    }

    @Override
    public void disconnect() throws Exception {
        this.client.close();
    }

    public String get(String endpoint) throws IOException {
        return this.get(endpoint, null, null);
    }

    public String get(String endpoint, String params) throws IOException {
        return this.get(endpoint, params, null);
    }

    public String get(String endpoint, String params, String requestId) throws IOException {
        String url = String.format("%s/%s?%s", this.baseUri, endpoint, params == null ? "" : params);
        LOG.debug("GET {}", url);
        HttpContext context = HttpClientContext.create();
        HttpGet get = new HttpGet(url);
        long start = System.currentTimeMillis();
        CloseableHttpResponse response = this.client.execute(get, context);
        if (requestId != null && !requestId.isEmpty()) {
            this.responseTime.put(requestId, System.currentTimeMillis() - start);
        }
        int code = response.getStatusLine().getStatusCode();
        String res = EntityUtils.toString(response.getEntity());
        if (code != 200) {
            LOG.warn("{}", response.getStatusLine());
            throw new IOException(res);
        }
        return res;
    }

    public String post(String endpoint, String params, String requestId) throws IOException {
        String url = String.format("%s/%s?%s", this.baseUri, endpoint, params == null ? "" : params);
        LOG.debug("POST {}", url);
        HttpContext context = HttpClientContext.create();
        HttpPost post = new HttpPost(url);
        long start = System.currentTimeMillis();
        CloseableHttpResponse response = this.client.execute(post, context);
        if (requestId != null && !requestId.isEmpty()) {
            this.responseTime.put(requestId, System.currentTimeMillis() - start);
        }
        int code = response.getStatusLine().getStatusCode();
        String res = EntityUtils.toString(response.getEntity());
        if (code != 200) {
            LOG.warn("{}", response.getStatusLine());
            throw new IOException(res);
        }
        return res;
    }

    public String put(String endpoint, String params) throws IOException {
        String url = String.format("%s/%s?%s", this.baseUri, endpoint, params == null ? "" : params);
        LOG.debug("PUT {}", url);
        HttpContext context = HttpClientContext.create();
        HttpPut put = new HttpPut(url);
        CloseableHttpResponse response = this.client.execute(put, context);
        int code = response.getStatusLine().getStatusCode();
        String res = EntityUtils.toString(response.getEntity());
        if (code != 200) {
            LOG.warn("{}", response.getStatusLine());
            throw new IOException(res);
        }
        return res;
    }

    public Long getResponseTime(String reqId) {
        return responseTime.get(reqId);
    }

    public void clearResponseTime(String reqId) {
        this.responseTime.remove(reqId);
    }

    public static String encode(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8");
    }

    private final TrustStrategy acceptingTrustStrategy = (X509Certificate[] certificate, String authType) -> true;

    private final ConnectionKeepAliveStrategy keepAliveStrategy = (HttpResponse response, HttpContext context) -> {
        HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
                try {
                    return Long.parseLong(value) * 1000;
                } catch (NumberFormatException ignore) {
                    LOG.trace(ignore.getMessage());
                }
            }
        }
        return 30000;
    };

    public static void main(String[] args) throws Exception {
        RestClient ws = new RestClient("www.reddit.com", 80);
        ws.connect();
        String status = ws.get("r/programming");
        LOG.info(status);
    }
}
