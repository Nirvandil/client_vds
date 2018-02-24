package cf.nirvandil.clientvds.model

class Action(val type: ActionType,
             val domainsContent: String,
             val phpMod: String,
             val templatePath: String,
             val token: String)
