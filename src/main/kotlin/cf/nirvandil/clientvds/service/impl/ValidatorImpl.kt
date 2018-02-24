package cf.nirvandil.clientvds.service.impl

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.service.Validator
import org.apache.commons.validator.routines.DomainValidator
import org.apache.commons.validator.routines.InetAddressValidator

private const val MAX_DOMAINS = 200
class ValidatorImpl : Validator {
    private val domainValidator = DomainValidator.getInstance()
    private val ipValidator = InetAddressValidator.getInstance()

    override fun getValidDomains(domainsContent: String): List<String> {
        val domains = transformToDomains(domainsContent)
        if (domains.size > MAX_DOMAINS)
            throw MainException("За один раз можно добавить не более $MAX_DOMAINS доменов!")
        if (domains.any { !domainValidator.isValid(it) })
            throw MainException("Как минимум одно из доменных имён некорректно!")
        return domains
    }

    override fun validateIpAddress(ip: String) {
        if (!ipValidator.isValidInet4Address(ip))
            throw MainException("IP-адрес не является корректным адресом IPv4!")
    }

    private fun transformToDomains(doms: String): List<String> = doms.split("\n".toRegex()).map { it.trim() }

}