package cf.nirvandil.clientvds;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 02.09.2016.
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
 * This is main point of GUI, that initializes all elements and sets
 * handlers for buttons. Also it's JavaFX Application start point.
 */

public class Gui extends Application
    {
        private final GridPane root = new GridPane();
        private final TextField ipField = new TextField();
        private final TextField portField = new TextField();
        private final PasswordField passField = new PasswordField();
        private final Button help = new Button();
        private final TextField pathTemplateField = new TextField();
        private final Tooltip pathTemplateTooltip = new Tooltip("Укажите путь к каталогу, содержащему файлы для загрузки в создаваемые каталоги сайтов. \nЕсли это не нужно, " +
                "оставьте данное поле пустым. Путь указывается от корня сервера");
        private final ToggleGroup phpToggle = new ToggleGroup();
        private final RadioButton phpCGI = new RadioButton("PHP как CGI");
        private final RadioButton phpMod = new RadioButton("PHP как модуль");
        private final ProgressBar progressBar = new ProgressBar(0);
        private final TextArea domainsArea = new TextArea();
        private final Hyperlink hyperlink = new Hyperlink("Действительно качественный хостинг");
        private final Button addButton = new Button("Добавить");
        private final Button removeButton = new Button("Удалить");
        private final Tooltip ipFieldTooltip = new Tooltip("Укажите здесь IP-адрес своего сервера");
        private final Tooltip portFieldTooltip = new Tooltip("Укажите здесь порт SSH на сервере (обычно 3333 или 22)");
        private final Tooltip passFieldTooltip = new Tooltip("Введите здесь пароль пользователя root Вашего сервера");
        private final Tooltip domainsAreaTooltip = new Tooltip("Сюда введите список доменов, по одному в строке");
        private final Tooltip phpCGITooltip = new Tooltip("Использовать режим работы РНР как CGI (по умолчанию подходит всем)");
        private final Tooltip phpModTooltip = new Tooltip("Использовать режим работы РНР как модуль Apache (выбирайте, если знаете, что делаете)");
        private final Tooltip removeTooltip = new Tooltip(
                "Удалит указанный список доменов с сервера. Если таких доменов не существует, ничего не случится");
        private final String friendURI = "https://friendhosting.net";
        //In handler we pass links to our GUI elements, because later we must use their content
        private final PressButtonHandler pressButtonHandler = new PressButtonHandler(ipField, passField, portField, progressBar, domainsArea,
                                                                                     phpToggle, pathTemplateField);

        public Gui()
            {
                super();
                initIpField();
                initPortField();
                initHelp();
                initPassField();
                initPhpModToggle();
                initTemplatePathField();
                initProgressBar();
                initDomainsArea();
                initFriendHostingLink();
                initAddButton();
                initRemoveButton();
                initGridLayout();
            }

        private void initIpField()
            {
                ipField.setTooltip(ipFieldTooltip);
                ipField.setPromptText("127.0.0.1");
            }

        private void initPortField()
            {
                portField.setTooltip(portFieldTooltip);
                portField.setMaxWidth(60);
                portField.setPromptText("3333");
            }

        private void initHelp()
            {
                help.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream("/help.png"))));
                final Tooltip helpTooltip = new Tooltip("Нажмите, чтобы открыть страницу справки (в Интернете)");
                help.setTooltip(helpTooltip);
                help.setOnAction(actionEvent ->
                                 {
                                     try
                                         {
                                             DesktopApi.browse(new URI(
                                                     "http://wiki.friendhosting.net/index.php?title=Массовое_добавление_" +
                                                             "доменов_на_сервер_с_ISPmanager_или_VESTA"));
                                         }
                                     catch (final URISyntaxException urie)
                                         {
                                             System.err.println("Catch URI exception");
                                         }
                                 });
            }

        private void initPassField()
            {
                passField.setTooltip(passFieldTooltip);
            }

        private void initPhpModToggle()
            {
                phpCGI.setTooltip(phpCGITooltip);
                phpMod.setTooltip(phpModTooltip);
                phpCGI.setToggleGroup(phpToggle);
                phpMod.setToggleGroup(phpToggle);
                phpCGI.setSelected(true);
            }

        private void initTemplatePathField()
            {
                pathTemplateField.setTooltip(pathTemplateTooltip);
                pathTemplateField.setPromptText("/var/www/username/template/directory");
            }
        private void initProgressBar()
            {
                progressBar.setMinWidth(180);
            }

        private void initDomainsArea()
            {
                domainsArea.setTooltip(domainsAreaTooltip);
                domainsArea.setPromptText("example.ru");
            }

        private void initFriendHostingLink()
            {
                hyperlink.setOnAction((actionEvent ->
                {
                    try
                        {
                            DesktopApi.browse(new URI(friendURI));
                        }
                    catch (final URISyntaxException urie)
                        {
                            System.err.println("Catch URI exception");
                        }
                }));
                final Tooltip hyperlinkTooltip = new Tooltip(friendURI);
                hyperlink.setTooltip(hyperlinkTooltip);
            }

        private void initAddButton()
            {
                addButton.setOnAction(pressButtonHandler);
            }

        private void initRemoveButton()
            {
                removeButton.setTooltip(removeTooltip);
                removeButton.setOnAction(pressButtonHandler);
            }

        private void initGridLayout()
            {
                root.getStyleClass().add("pane");
                root.setHgap(8);
                root.setVgap(8);
                root.setPadding(new Insets(5, 5, 5, 5));
                root.getStyleClass().add("pane");
                root.add(new Text("Укажите IP-адрес и порт SSH здесь"), 0, 0);
                root.add(ipField, 1, 0);
                root.add(portField, 2, 0);
                root.add(new Text("Укажите здесь пароль root"), 0, 1);
                root.add(passField, 1, 1);
                root.add(phpCGI, 2, 1);
                root.add(phpMod, 3, 1);
                root.add(new Text("Здесь будет отображён прогресс операции"), 0, 2);
                root.add(progressBar, 1, 2);
                root.add(new Text("Путь к каталогу с шаблоном"), 0, 3);
                root.add(pathTemplateField, 1, 3, 2, 1);
                root.add(domainsArea, 0, 4, 3, 1);
                root.add(help, 3, 0);
                root.add(hyperlink, 0, 5, 2, 1);
                root.add(removeButton, 2, 5);
                root.add(addButton, 3, 5);
            }

        @Override
        public void start(final Stage primaryStage)
            {
                final Scene scene = new Scene(root);
                // Set "resizable" by binding width of window to domainsArea
                scene.widthProperty().addListener((observable, oldValue, newValue) ->
                        domainsArea.setMinWidth(newValue.doubleValue() - 180)
                );
                scene.heightProperty().addListener((observable, oldValue, newValue) ->
                        domainsArea.setMinHeight(newValue.doubleValue() - 180)
                );
                scene.getStylesheets().add("/style.css");
                primaryStage.setTitle("Автоматическое добавление доменов на серверы с ISPmanager и VESTA");
                primaryStage.setScene(scene);
                primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
                primaryStage.setOnCloseRequest((windowEvent -> System.exit(0)));
                primaryStage.show();
            }

        public static void main(final String[] args)
            {
                launch(args);
            }
    }