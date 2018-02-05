package cf.nirvandil.clientvds.util;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 04.09.16.
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
 * This class is used for platform-dependent opening hyperlinks in
 * external browser
 */
@Slf4j
public abstract class DesktopApi {

    public static void browse(final URI uri) {
        if ((!browseDESKTOP(uri))) {
            openSystemSpecific(uri.toString());
        }
    }

    private static void openSystemSpecific(final String what) {

        final EnumOS os = getOs();

        if (os.isLinux()) {
            if (runCommand("kde-open", "%s", what)) return;
            if (runCommand("gnome-open", "%s", what)) return;
            if (runCommand("xdg-open", "%s", what)) return;
        }

        if (os.isMac()) {
            if (runCommand("open", "%s", what)) return;
        }

        if (os.isWindows()) {
            runCommand("explorer", "%s", what);
        }

    }

    private static boolean browseDESKTOP(final URI uri) {

        try {
            if (!Desktop.isDesktopSupported()) {
                logErr("Platform is not supported.");
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                logErr("BROWSE is not supported.");
                return false;
            }

            Desktop.getDesktop().browse(uri);

            return true;
        } catch (final Throwable t) {
            logErr("Error using desktop browse.", t);
            return false;
        }
    }

    private static boolean runCommand(final String command, final String args, final String file) {

        final String[] parts = prepareCommand(command, args, file);

        try {
            final Process p = Runtime.getRuntime().exec(parts);
            if (p == null) return false;

            try {
                final int retval = p.exitValue();
                if (retval == 0) {
                    logErr("Process ended immediately.");
                    return false;
                } else {
                    logErr("Process crashed.");
                    return false;
                }
            } catch (final IllegalThreadStateException itse) {
                return true;
            }
        } catch (final IOException e) {
            logErr("Error running command.", e);
            return false;
        }
    }

    private static String[] prepareCommand(final String command, final String args, final String file) {

        final List<String> parts = new ArrayList<>();
        parts.add(command);

        if (args != null) {
            for (String s : args.split(" ")) {
                s = String.format(s, file);

                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    private static void logErr(final String msg, final Throwable t) {
        log.error(msg);
        log.error("{}", t);
    }

    private static void logErr(final String msg) {
        log.error(msg);
    }

    private static EnumOS getOs() {

        final String s = System.getProperty("os.name").toLowerCase();

        if (s.contains("win")) {
            return EnumOS.windows;
        }

        if (s.contains("mac")) {
            return EnumOS.macos;
        }

        if (s.contains("solaris")) {
            return EnumOS.solaris;
        }

        if (s.contains("sunos")) {
            return EnumOS.solaris;
        }

        if (s.contains("linux")) {
            return EnumOS.linux;
        }

        if (s.contains("unix")) {
            return EnumOS.linux;
        } else {
            return EnumOS.unknown;
        }
    }

    private enum EnumOS {
        linux, macos, solaris, unknown, windows;

        public boolean isLinux() {

            return this == linux || this == solaris;
        }

        public boolean isMac() {

            return this == macos;
        }

        public boolean isWindows() {

            return this == windows;
        }
    }
}