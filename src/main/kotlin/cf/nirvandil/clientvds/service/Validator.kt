package cf.nirvandil.clientvds.service

interface Validator {

    fun validateIpAddress(ip: String)
    fun getValidDomains(domainsContent: String) : List<String>
}
