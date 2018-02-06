package cf.nirvandil.clientvds;

import cf.nirvandil.clientvds.exc.MainException;
import cf.nirvandil.clientvds.model.ConnectionDetails;
import com.jcraft.jsch.JSchException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 28.09.16.
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
 * Handles pressing button. Contains JavaFX elements and provides methods
 * to get data for start needed action.
 */
@Slf4j
public class PressButtonHandler implements EventHandler<ActionEvent> {
    private static final String MESSAGE_EMPTY_FIELDS = "Все поля, кроме токена, должны быть заполнены!";
    private final TextField ipField;
    private final PasswordField passField;
    private final TextField portField;
    private final TextField templateField;
    private final ProgressBar progressBar;
    private final TextArea domainsAreaContent;
    private final ToggleGroup phpMode;
    private final TextField tokenField;

    PressButtonHandler(final TextField ipField, final PasswordField passField, final TextField portField, final ProgressBar progressBar,
                       final TextArea domainsAreaContent, final ToggleGroup phpMode, final TextField templateField,
                       final TextField tokenField) {
        this.ipField = ipField;
        this.passField = passField;
        this.portField = portField;
        this.templateField = templateField;
        this.progressBar = progressBar;
        this.domainsAreaContent = domainsAreaContent;
        this.phpMode = phpMode;
        this.tokenField = tokenField;
    }

    @Override
    public void handle(final ActionEvent actionEvent) {
        validateInput();
        final String action = ((Button) actionEvent.getSource()).getText();
        try {
            try {
                final LogicFrame logicFrame = new LogicFrame(new ConnectionDetails(getIp(), getPass(), getPort()), progressBar);
                String templatePath = templateField.getText();
                if (!templatePath.endsWith("/"))
                    templatePath += "/";
                logicFrame
                        .MainAction(domainsAreaContent.getText(),
                                ((RadioButton) phpMode.getSelectedToggle()).getText(), action, templatePath, tokenField.getText());
            } catch (final IOException ioe) {
                throw new MainException("Произошла ошибка ввода-вывода, проверьте наличие подключения!");
            } catch (final JSchException jshe) {
                throw new MainException("Произошла ошибка при подключении, проверьте введённые данные!");
            } catch (final NumberFormatException nfe) {
                throw new MainException("Некорректно указан порт для подключения!");
            } catch (final MainException me) {
                log.error("Catches simple cf.nirvandil.clientvds.exc.MainException");
            }
        } catch (final MainException me) {
            log.error("{}", me);
        }
    }

    private void validateInput() {
        if (ipField.getText().isEmpty() || passField.getText().isEmpty() || portField.getText().isEmpty()) {
            throw new MainException(MESSAGE_EMPTY_FIELDS);
        }
    }

    private String getIp() throws MainException {
        return ipField.getText();
    }

    private String getPass() throws MainException {
        return passField.getText();
    }

    private int getPort() throws MainException {
        return Integer.parseInt(portField.getText());
    }
}