package cf.nirvandil.clientvds.service

import cf.nirvandil.clientvds.exc.MainException
import cf.nirvandil.clientvds.model.PanelType
import cf.nirvandil.clientvds.service.impl.Isp4DomainsManipulator
import cf.nirvandil.clientvds.service.impl.Isp5DomainsManipulator
import cf.nirvandil.clientvds.service.impl.VestaDomainsManipulator
import cf.nirvandil.clientvds.util.PANEL_DOES_NOT_EXIST_HEADER
import cf.nirvandil.clientvds.util.PANEL_DOES_NOT_EXIST_MESSAGE
import com.jcraft.jsch.Session

/**
 * Created by Vladimir Sukharev aka Nirvandil on 11.09.2016.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Factory for creating manipulator based on panel
 */

object ManipulatorFactory {

    fun createManipulator(panel: PanelType, session: Session): DomainsManipulator {
        return when (panel) {
            PanelType.VESTA -> VestaDomainsManipulator(session)
            PanelType.ISP4 -> Isp4DomainsManipulator(session)
            PanelType.ISP5 -> Isp5DomainsManipulator(session)
            PanelType.UNKNOWN -> throw MainException(PANEL_DOES_NOT_EXIST_MESSAGE, PANEL_DOES_NOT_EXIST_HEADER)
        }
    }
}
