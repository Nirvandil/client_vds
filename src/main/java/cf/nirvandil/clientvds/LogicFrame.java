package cf.nirvandil.clientvds;

import cf.nirvandil.clientvds.exc.MainException;
import cf.nirvandil.clientvds.model.ConnectionDetails;
import cf.nirvandil.clientvds.service.DomainsManipulator;
import cf.nirvandil.clientvds.service.ManipulatorFactory;
import cf.nirvandil.clientvds.tasks.AddingTask;
import cf.nirvandil.clientvds.tasks.RemovingTask;
import cf.nirvandil.clientvds.util.SocketFactoryWithTimeout;
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
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static javafx.scene.control.Alert.AlertType.WARNING;

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
public class LogicFrame {
    private static final int MAX_DOMAINS = 200;
    private final ConnectionDetails details;
    private final ProgressBar progressBar;
    private DomainsManipulator domainsManipulator;

    LogicFrame(final ConnectionDetails details, final ProgressBar progressBar) {
        this.details = details;
        this.progressBar = progressBar;
    }

    public void MainAction(final String domainsContent, final String phpMod, final String action, final String templatePath,
                           final String token)
            throws IOException, JSchException, MainException {
        final List<String> domains = getValidatedDomains(domainsContent);
        // Create domainsManipulator from factory by passing panel and session
        domainsManipulator = ManipulatorFactory.createManipulator(detectPanel(), getSshClientSession());
        final String owner = domainsManipulator.askUserOfPanel(domainsManipulator.getUsers());
        final Task<Map<String, List<String>>> task;
        switch (action) {
            case "Удалить":
                task = new RemovingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath, token);
                break;
            case "Добавить":
                task = new AddingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath, token);
                break;
            default:
                //If not initialized(?!), then adding
                task = new AddingTask(domains, details.getIp(), domainsManipulator, owner, phpMod, templatePath, token);
        }
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(WorkerStateEvent ->
                {
                    final Map<String, List<String>> result = task.getValue();
                    if (!result.isEmpty()) {
                        for (final String domain : result.keySet()) {
                            for (String message : result.get(domain)) {
                                final Alert alert = new Alert(WARNING);
                                final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                                stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
                                alert.setTitle("Внимание");
                                alert.setHeaderText("При обработке доменов возникли следующие предупреждения:");
                                alert.setContentText(message);
                                alert.showAndWait();
                            }
                        }
                    }
                    domainsManipulator.reportDone();
                }
        );
        new Thread(task).start();
    }

    private List<String> getValidatedDomains(final String doms) throws MainException {
        final List<String> domains = new ArrayList<>(asList(doms.split("\n")));
        domains.replaceAll(String::trim);
        if (domains.size() > MAX_DOMAINS)
            throw new MainException("За один раз можно добавить не более" + MAX_DOMAINS + " доменов!");
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
        channel.disconnect();
        return panel;
    }
}