package cf.nirvandil.clientvds.model

data class DomainDescriptor(val domain: String,
                            val owner: String,
                            val ip: String = "",
                            val phpMode: String = "",
                            val templatePath: String = "")