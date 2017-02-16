package cf.nirvandil.clientvds;

import com.jcraft.jsch.JSchException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;

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
 *
 * Handles pressing button. Contains JavaFX elements and provides methods
 * to get data for start needed action.
 */
class PressButtonHandler implements EventHandler<ActionEvent>
    {
        private final TextField ipField;
        private final PasswordField passField;
        private final TextField portField;
        private final TextField templateField;
        private final ProgressBar progressBar;
        private final TextArea domainsAreaContent;
        private final ToggleGroup phpMode;

        PressButtonHandler(final TextField ipField, final PasswordField passField, final TextField portField, final ProgressBar progressBar,
                           final TextArea domainsAreaContent, final ToggleGroup phpMode, final TextField templateField)
            {
                this.ipField = ipField;
                this.passField = passField;
                this.portField = portField;
                this.templateField = templateField;
                this.progressBar = progressBar;
                this.domainsAreaContent = domainsAreaContent;
                this.phpMode = phpMode;
            }

        @Override
        public void handle(final ActionEvent actionEvent)
            {
                final String action = ((Button) actionEvent.getSource()).getText();
                {
                    try
                        {
                            try
                                {
                                    final LogicFrame logicFrame = new LogicFrame(new ConnectionDetails(getIp(), getPass(), getPort()), progressBar);
                                    String templatePath = templateField.getText();
                                    if (!templatePath.endsWith("/"))
                                        templatePath += "/";
                                    logicFrame
                                            .MainAction(domainsAreaContent.getText(),
                                                    ((RadioButton) phpMode.getSelectedToggle()).getText(), action, templatePath);
                                }
                            catch (final IOException ioe)
                                {
                                    throw new MainException("Произошла ошибка ввода-вывода, проверьте наличие подключения!");
                                }
                            catch (final JSchException jshe)
                                {
                                    throw new MainException("Произошла ошибка при подключении, проверьте введённые данные!");
                                }
                            catch (final NumberFormatException nfe)
                                {
                                    throw new MainException("Некорректно указан порт для подключения!");
                                }
                            catch (final MainException me)
                                {
                                    System.err.println("Catches simple cf.nirvandil.clientvds.MainException");
                                }
                        }
                    catch (final MainException me)
                        {
                            System.err.println("Catches outer cf.nirvandil.clientvds.MainException");
                        }
                }
            }

        private String getIp() throws MainException
            {
                if (ipField.getText().equals(""))
                    throw new MainException("Все поля должны быть заполнены!");
                else return ipField.getText();
            }

        private String getPass() throws MainException
            {
                if (passField.getText().equals(""))
                    throw new MainException("Все поля должны быть заполнены!");
                else return passField.getText();
            }

        private int getPort() throws MainException
            {
                if (portField.getText().equals(""))
                    throw new MainException("Все поля должны быть заполнены!");
                else return Integer.parseInt(portField.getText());
            }
    }