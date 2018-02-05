package cf.nirvandil.clientvds.service;

import cf.nirvandil.clientvds.service.impl.Isp4DomainsManipulator;
import cf.nirvandil.clientvds.service.impl.Isp5DomainsManipulator;
import cf.nirvandil.clientvds.service.impl.VestaDomainsManipulator;
import com.jcraft.jsch.Session;

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
 * <p>
 * Factory for creating manipulator based on panel
 */

public abstract class ManipulatorFactory {

    public static DomainsManipulator createManipulator(final String panel, final Session session) {
        switch (panel) {
            case "vesta":
                return new VestaDomainsManipulator(session);
            case "isp4":
                return new Isp4DomainsManipulator(session);
            case "isp5":
                return new Isp5DomainsManipulator(session);
            default:
                //if unknown(?!) return ISP4
                return new Isp4DomainsManipulator(session);
        }
    }
}
