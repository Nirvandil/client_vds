package cf.nirvandil.clientvds.service.impl;

import cf.nirvandil.clientvds.service.DigitalOceanClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DigitalOceanClientImpl implements DigitalOceanClient {
    private static final String ADDRESS = "https://api.digitalocean.com/v2/domains";

    @Override
    @SneakyThrows({UnsupportedEncodingException.class, IOException.class, InterruptedException.class})
    public String addDomain(String domain, String ip, String token) {
        TimeUnit.MILLISECONDS.sleep(50);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(ADDRESS);
        String json = "{\"name\":\"" + domain + "\", \"ip_address\":\"" + ip + "\"}";
        StringEntity stringEntity = new StringEntity(json);
        post.setEntity(stringEntity);
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        CloseableHttpResponse httpResponse = client.execute(post);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        client.close();
        if (statusCode == HttpStatus.SC_CREATED) {
            log.info("Domain {} created on Digital Ocean with IP address {}", domain, ip);
            return "";
        } else {
            log.error("Domain {} can't be created on Digital Ocean.", domain);
            return ("DO");
        }
    }

    @Override
    @SneakyThrows
    public String removeDomain(String domain, String token) {
        TimeUnit.MILLISECONDS.sleep(50);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpDelete delete = new HttpDelete(ADDRESS + "/" + domain);
        delete.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        delete.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        CloseableHttpResponse httpResponse = client.execute(delete);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        client.close();
        if (statusCode == HttpStatus.SC_NO_CONTENT) {
            log.info("Domain {} removed from Digital Ocean.", domain);
            return "";
        } else {
            log.error("Domain {} can't be removed from Digital Ocean, answer was {}", domain, statusCode);
            return ("DO");
        }
    }
}
