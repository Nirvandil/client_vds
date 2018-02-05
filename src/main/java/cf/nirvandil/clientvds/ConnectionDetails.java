package cf.nirvandil.clientvds;

import lombok.Getter;
import org.apache.commons.validator.routines.InetAddressValidator;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 07.09.2016.
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
 * This class encapsulates and validates parameters for connection to server.
 */
class ConnectionDetails {
    private final String ip;
    @Getter
    private final String pass;
    @Getter
    private final int port;

    ConnectionDetails(final String ip, final String pass, final int port) {
        this.ip = ip;
        this.pass = pass;
        this.port = port;
    }

    String getIp() throws MainException {
        validateIpFormat();
        return this.ip;
    }

    private void validateIpFormat() throws MainException {
        final InetAddressValidator validator = InetAddressValidator.getInstance();
        if (!validator.isValidInet4Address(this.ip))
            throw new MainException("IP-адрес не является корректным адресом IPv4");
    }

}
