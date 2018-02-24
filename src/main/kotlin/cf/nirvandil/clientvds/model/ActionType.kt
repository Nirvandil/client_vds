package cf.nirvandil.clientvds.model

import cf.nirvandil.clientvds.util.ADD
import cf.nirvandil.clientvds.util.REMOVE

enum class ActionType {
    ADD_ACTION, REMOVE_ACTION;

    companion object {
        fun fromString(str: String): ActionType? {
            return mapOf(ADD to ADD_ACTION, REMOVE to REMOVE_ACTION)[str]
        }
    }
}