package videobackend.video.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import org.elasticsearch.client.RestClientBuilder;

/**
 * 手动创建 ElasticsearchClient，避免 Spring Boot 自动配置在 HTTPS/SSL bundle + Java client 组合下失效的问题。
 */
@Configuration
public class ElasticsearchClientConfig {

    @Bean
    @ConditionalOnProperty(prefix = "videohub.elasticsearch", name = "enabled", havingValue = "true")
    public ElasticsearchClient elasticsearchClient(
            @Value("${spring.elasticsearch.uris}") String uris,
            @Value("${spring.elasticsearch.username:elastic}") String username,
            @Value("${spring.elasticsearch.password:}") String password,
            @Value("${spring.ssl.bundle.pem.eshttpca.truststore.certificate}") String caCertLocation
    ) throws Exception {

        String first = uris;
        if (uris.contains(",")) {
            first = uris.split(",")[0].trim();
        }

        // 支持：
        // - https://127.0.0.1:9200
        // - 127.0.0.1:9200
        String scheme = "https";
        String host;
        int port;
        if (first.contains("://")) {
            URI uri = URI.create(first);
            scheme = uri.getScheme() == null ? "https" : uri.getScheme();
            host = uri.getHost();
            port = uri.getPort();
        } else {
            String[] hp = first.split(":");
            host = hp[0].trim();
            port = Integer.parseInt(hp[1].trim());
        }
        scheme = scheme.toLowerCase(Locale.ROOT);

        SSLContext sslContext = buildSslContextFromCaCert(caCertLocation);

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder
                                .setSSLContext(sslContext)
                                .setDefaultCredentialsProvider(credentialsProvider)
                );

        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    private SSLContext buildSslContextFromCaCert(String caCertLocation) throws Exception {
        String path = caCertLocation;
        if (path.startsWith("file:")) {
            path = path.substring("file:".length());
        }
        // 只处理 file:/...；classpath: 由你另行提供即可
        if (!path.contains("\\") && !path.contains("/")) {
            // 兜底：保持原样，交给 FileInputStream 抛错更直观
        }

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert;
        try (InputStream is = new FileInputStream(path)) {
            caCert = cf.generateCertificate(is);
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("ca", caCert);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
}

