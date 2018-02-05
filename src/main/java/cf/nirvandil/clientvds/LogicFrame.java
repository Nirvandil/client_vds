package cf.nirvandil.clientvds;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.validator.routines.DomainValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
 * Main logic class. Creates domains manipulator based on detected panel,
 * starts action for remove or add domains by create proper task and reports
 * status operation in the and.
 */
class LogicFrame {
    private final ConnectionDetails details;
    private final ProgressBar progressBar;
    private DomainsManipulator domainsManipulator;

    LogicFrame(final ConnectionDetails details, final ProgressBar progressBar) {
        this.details = details;
        this.progressBar = progressBar;
    }

    void MainAction(final String domainsContent, final String phpMod, final String action, final String templatePath)
            throws IOException, JSchException, MainException {
        final List<String> domains = getValidatedDomains(domainsContent);
        // Create domainsManipulator from factory by passing panel and session
        domainsManipulator = ManipulatorFactory.createManipulator(detectPanel(), getSshClientSession());
        final String owner = domainsManipulator.askUserOfPanel(domainsManipulator.getUsers());
        final Task<Map<String, String>> task;
        switch (action) {
            case "Удалить":
                task = new RemovingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath);
                break;
            case "Добавить":
                task = new AddingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath);
                break;
            default:
                //If not initialized(?!), then adding
                task = new AddingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath);
        }
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(WorkerStateEvent ->
                {
                    final Map<String, String> result = task.getValue();
                    if (!result.isEmpty() && !result.containsValue("")) {
                        for (final String domain : result.keySet()) {
                            final Alert alert = new Alert(Alert.AlertType.WARNING);
                            final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
                            alert.setTitle("Внимание");
                            alert.setHeaderText("При обработке доменов возникли следующие предупреждения:");
                            alert.setContentText(result.get(domain));
                            alert.showAndWait();
                        }
                    }
                    domainsManipulator.reportDone();
                }
        );
        new Thread(task).start();
    }

    private List<String> getValidatedDomains(final String doms) throws MainException {
        final List<String> domains = new ArrayList<>(Arrays.asList(doms.split("\n")));
        domains.replaceAll(String::trim);
        if (domains.size() > 500) throw new MainException("За один раз можно добавить не более 500 доменов!");
        final DomainValidator validator = DomainValidator.getInstance();
        for (final String domain : domains) {
            if (!validator.isValid(domain))
                throw new MainException("Как минимум одно из доменных имён некорректно!" + domain);
        }
        return domains;
    }

    private Session getSshClientSession() throws JSchException, MainException {
        final JSch jsch = new JSch();
        final Session session = jsch.getSession("root", details.getIp(), details.getPort());
        session.setPassword(details.getPass());
        session.setSocketFactory(new SocketFactoryWithTimeout());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    private String detectPanel() throws IOException, JSchException, MainException {
        final String panel;
        final ChannelExec channel = (ChannelExec) getSshClientSession().openChannel("exec");
        final BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        final String command = "if [ -d /usr/local/vesta ]; then echo vesta; elif [ -d /usr/local/ispmgr ]; then echo isp4; " +
                "elif [ -d /usr/local/mgr5 ]; then echo isp5; else echo not_panel ; fi";
        channel.setCommand(command);
        channel.connect();
        panel = in.readLine();
        if (panel.equals("not_panel")) throw new MainException("Панель управления не обнаружена!",
                "Работа утилиты возможна только с \nISPmanager 4/5 Lite и VestaCP");
        return panel;
    }
}