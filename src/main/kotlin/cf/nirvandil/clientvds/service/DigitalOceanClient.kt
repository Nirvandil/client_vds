package cf.nirvandil.clientvds.service

interface DigitalOceanClient {
    fun addDomain(domain: String, ip: String, token: String): String

    fun removeDomain(domain: String, token: String): String
}
