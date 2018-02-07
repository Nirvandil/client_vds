package cf.nirvandil.clientvds.service;

public interface DigitalOceanClient {
    String addDomain(String domain, String ip, String token);

    String removeDomain(String domain, String token);
}
