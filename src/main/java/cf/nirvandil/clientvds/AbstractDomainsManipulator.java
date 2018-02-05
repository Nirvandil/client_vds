package cf.nirvandil.clientvds;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
 * This is abstract class, that implements some common futures for all
 * domains manipulators
 */
abstract class AbstractDomainsManipulator implements DomainsManipulator {
    private final Session session;

    AbstractDomainsManipulator(final Session session) {
        this.session = session;
    }

    List<String> getCommandOutput(final String command) throws IOException, JSchException {
        final ChannelExec exec = getChannelExec();
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        final BufferedReader errorReader = new BufferedReader(new InputStreamReader(exec.getErrStream()));
        exec.setCommand(command);
        exec.connect();
        final List<String> out = new ArrayList<>();
        String line;
        while ((line = inputReader.readLine()) != null)
            out.add(line);
        while ((line = errorReader.readLine()) != null)
            out.add(line);
        exec.disconnect();
        return out;
    }

    ChannelExec getChannelExec() throws JSchException {
        return (ChannelExec) session.openChannel("exec");
    }

    @Override
    public void reportDone() {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Выполнено");
        alert.setContentText("Указанные домены обработаны!");
        alert.setHeaderText("Операция завершена успешно");
        final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
        alert.showAndWait();
    }

    @Override
    public String askUserOfPanel(final List<String> users) throws MainException {
        final ChoiceDialog<String> dialog = new ChoiceDialog<>(users.get(0), users);
        dialog.setTitle("Владелец");
        dialog.setHeaderText("Укажите, какому пользователю в\nпанели управления будут добавлены домены");
        dialog.setContentText("Имя пользователя: ");
        final Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
        final Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        } else
            throw new MainException("Необходимо обязательно указывать пользователя!");
    }

    boolean checkPathExist(String path) throws IOException, JSchException {
        return ((Integer.parseInt(getCommandOutput("test -e " + path + " && echo 0").get(0)) == 0));
    }

    public abstract String constructDomainPath(String owner, String domainName);
}
