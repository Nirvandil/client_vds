package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.service.Validator
import cf.nirvandil.clientvds.util.DOMAIN_INVALID_MESSAGE
import cf.nirvandil.clientvds.util.IP_INVALID_MESSAGE
import cf.nirvandil.clientvds.util.MAX_DOMAINS
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

class ValidatorImpl : Validator {
    private val domainValidator = DomainValidator.getInstance()
    private val ipValidator = InetAddressValidator.getInstance()

    override fun getValidDomains(domainsContent: String): List<String> {
        val domains = transformToDomains(domainsContent)
        if (domains.size > MAX_DOMAINS)
            throw MainException("За один раз можно добавить не более $MAX_DOMAINS доменов!")
        if (domains.any { !domainValidator.isValid(it) })
            throw MainException(DOMAIN_INVALID_MESSAGE)
        return domains
    }

    override fun validateIpAddress(ip: String) {
        if (!ipValidator.isValidInet4Address(ip))
            throw MainException(IP_INVALID_MESSAGE)
    }

    private fun transformToDomains(domains: String): List<String> = domains.split("\n".toRegex()).map { it.trim() }

}