package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.service.DigitalOceanClient
import cf.nirvandil.clientvds.util.DIGITAL_OCEAN_API_DOMAINS
import cf.nirvandil.clientvds.util.JSON
import lombok.SneakyThrows
import org.apache.http.HttpHeaders.AUTHORIZATION
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit.MILLISECONDS

class DigitalOceanClientImpl : DigitalOceanClient {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @SneakyThrows(UnsupportedEncodingException::class, IOException::class, InterruptedException::class)
    override fun addDomain(domain: String, ip: String, token: String): String {
        MILLISECONDS.sleep(50)
        return if (performRequest(HttpPost(DIGITAL_OCEAN_API_DOMAINS).apply {
                    entity = StringEntity("""{"name" : "$domain", "ip_address" : "$ip"}""")
                    setHeaders(authContentHeaders(token))
                })() == HttpStatus.SC_CREATED) {
            log.info("Domain $domain created on Digital Ocean with IP address $ip.")
            ""
        } else {
            log.error("Domain $domain can't be created on Digital Ocean.")
            "DO"
        }
    }

    private fun performRequest(request: HttpUriRequest) = {
        with(HttpClients.createDefault()) {
            val status = execute(request).statusLine.statusCode
            close()
            return@with status
        }
    }

    private fun authContentHeaders(token: String) =
            arrayOf(BasicHeader(AUTHORIZATION, "Bearer $token"), BasicHeader(CONTENT_TYPE, JSON))

    @SneakyThrows
    override fun removeDomain(domain: String, token: String): String {
        MILLISECONDS.sleep(50)
        return if (performRequest(HttpDelete(DIGITAL_OCEAN_API_DOMAINS + "/$domain").apply {
                    setHeaders(authContentHeaders(token))
                })() == HttpStatus.SC_NO_CONTENT) {
            log.info("Domain $domain removed from Digital Ocean.")
            ""
        } else {
            log.error("Domain $domain can't be removed from Digital Ocean.")
            "DO"
        }
    }
}