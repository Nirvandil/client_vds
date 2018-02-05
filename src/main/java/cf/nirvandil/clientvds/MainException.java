package cf.nirvandil.clientvds;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Created by Vladimir Sukharev aka Nirvandil on 04.09.2016.
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
 * Class for visualise occurs exception by showing dialog box
 */

class MainException extends Exception {
    MainException(final String message) {
        super(message);
        notifyError(message);
    }

    MainException(final String message, final String header) {
        super(message);
        notifyError(message, header);
    }

    private void notifyError(final String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("К сожалению, возникла ошибка, её подробности описаны ниже");
        alert.setContentText(error);
        final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
        alert.showAndWait();
    }

    private void notifyError(final String error, final String header) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(error);
        final Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/friendlogo.png")));
        alert.showAndWait();
    }
}
