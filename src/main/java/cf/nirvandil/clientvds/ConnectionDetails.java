package cf.nirvandil.clientvds;

import org.apache.commons.net.util.SubnetUtils;
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
 *
 * This class encapsulates and validates parameters for connection to server and
 * contains array with Friendhosting's networks to restrict work with only them.
 */
class ConnectionDetails
    {
        private final String[] ourNetsArray =
                {
                        "107.181.160.0/19",
                        "195.28.182.0/23",
                        "91.210.164.0/22",
                        "91.223.123.0/24",
                        "195.245.112.0/23",
                        "91.90.192.0/22",
                        "91.215.152.0/24",
                        "91.215.153.0/24",
                        "91.215.154.0/24",
                        "91.215.155.0/24",
                        "185.198.164.0/24"
                };
        private final String ip;
        private final String pass;
        private final int port;

        ConnectionDetails(final String ip, final String pass, final int port)
            {
                this.ip = ip;
                this.pass = pass;
                this.port = port;
            }

        String getIp() throws MainException
            {
                validateIpFormat();
                if (!isOurNet())
                    throw new MainException("Работа с утилитой возможна только из сетей Friendhosting!");
                else return this.ip;
            }

        String getPass()
            {
                return this.pass;
            }

        int getPort()
            {
                return port;
            }

        private void validateIpFormat() throws MainException
            {
                final InetAddressValidator validator = InetAddressValidator.getInstance();
                if (!validator.isValidInet4Address(this.ip))
                    throw new MainException("IP-адрес не является корректным адресом IPv4");
            }

        private boolean isOurNet()
            {
                for (final String subnet : ourNetsArray)
                    {
                        final SubnetUtils.SubnetInfo subnetInfo = (new SubnetUtils(subnet)).getInfo();
                        if (subnetInfo.isInRange(ip))
                            return true;
                    }
                return false;
            }
    }
