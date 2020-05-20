package pl.plauszta.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import pl.plauszta.game.DifficultyLevel;
import pl.plauszta.game.Game;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    public MenuBar menuBar;

    @FXML
    public GridPane grid;

    Game game = Game.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { //TODO add timer and progress
        prepareGridOfGameBoard();
        prepareMenuBar();
    }

    private void prepareMenuBar() {
        Menu gameMenu = prepareGameMenu();
        Menu helpMenu = prepareHelpMenu();

        menuBar.getMenus().add(gameMenu);
        menuBar.getMenus().add(helpMenu);
    }

    private Menu prepareGameMenu() {
        Menu gameMenu = new Menu("Game");

        MenuItem newGameItem = new MenuItem("New Game");
        newGameItem.setOnAction(event -> {
            game.newGame();
            try {
                SceneChanger.changeScene(menuBar.getScene());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Menu changeDifficultyItem = prepareChangeDifficultyMenu();

        gameMenu.getItems().add(newGameItem);
        gameMenu.getItems().add(changeDifficultyItem);
        return gameMenu;
    }

    private Menu prepareChangeDifficultyMenu() {
        Menu changeDifficultyItem = new Menu("Change difficulty");
        RadioMenuItem easyItem = new RadioMenuItem("Easy");
        RadioMenuItem mediumItem = new RadioMenuItem("Medium");
        RadioMenuItem hardItem = new RadioMenuItem("Hard");
        RadioMenuItem customItem = new RadioMenuItem("Custom");

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(easyItem);
        toggleGroup.getToggles().add(mediumItem);
        toggleGroup.getToggles().add(hardItem);
        toggleGroup.getToggles().add(customItem);

        changeDifficultyItem.getItems().add(easyItem);
        changeDifficultyItem.getItems().add(mediumItem);
        changeDifficultyItem.getItems().add(hardItem);
        changeDifficultyItem.getItems().add(new SeparatorMenuItem());
        changeDifficultyItem.getItems().add(customItem);

        easyItem.setOnAction(event -> {
            game.setDifficultyLevel(DifficultyLevel.EASY);
            try {
                SceneChanger.changeScene(menuBar.getScene());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        mediumItem.setOnAction(event -> {
            game.setDifficultyLevel(DifficultyLevel.MEDIUM);
            try {
                SceneChanger.changeScene(menuBar.getScene());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        hardItem.setOnAction(event -> {
            game.setDifficultyLevel(DifficultyLevel.HARD);
            try {
                SceneChanger.changeScene(menuBar.getScene());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        customItem.setOnAction(event -> {
            Dialog<CustomGameParams> dialog = makeCustomInputDialog();
            Optional<CustomGameParams> parameters = dialog.showAndWait();
            if (parameters.isPresent()) {
                CustomGameParams customGameParams = parameters.get();
                game.setCustomGame(customGameParams.getX(), customGameParams.getY(), customGameParams.getNumber());
                try {
                    SceneChanger.changeScene(menuBar.getScene());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return changeDifficultyItem;
    }

    private Dialog<CustomGameParams> makeCustomInputDialog() { //TODO add validation of params
        Dialog<CustomGameParams> dialog = new Dialog<>();
        dialog.setTitle("Custom game");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Label rowsLabel = new Label("Enter number of rows");
        TextField rowsNumber = new TextField();
        Label columnsLabel = new Label("Enter number of columns");
        TextField columnsNumber = new TextField();
        Label minesNumberLabel = new Label("Enter number of mines");
        TextField minesNumber = new TextField();

        dialogPane.setContent(new VBox(6,
                rowsLabel, rowsNumber,
                columnsLabel, columnsNumber,
                minesNumberLabel, minesNumber));
        Platform.runLater(rowsNumber::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new CustomGameParams(Integer.parseInt(rowsNumber.getText()),
                        Integer.parseInt(columnsNumber.getText()),
                        Integer.parseInt(minesNumber.getText()));
            }
            return null;
        });
        return dialog;
    }

    private Menu prepareHelpMenu() {
        Menu helpMenu = new Menu("Help");
        MenuItem howToPlayItem = new MenuItem("How to play...");
        MenuItem aboutAuthorItem = new MenuItem("About author");

        howToPlayItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("How to play");
            alert.setHeaderText(null);
            alert.setContentText("Clear board without detonating mines. Each of the discovered fields has the number of mines that are in direct contact with the field (from zero to eight)."
                    +"\n\nYou can mark the position of the mine with the flag by clicking the right mouse button."
                    + "\n\nIf field containing a mine is revealed, the player loses the game.");
            alert.showAndWait();
        });
        aboutAuthorItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About author");
            alert.setHeaderText(null);
            alert.setContentText("Plauszta Zuzanna\nhttps://github.com/zplauszta");
            alert.showAndWait();
        });

        helpMenu.getItems().add(howToPlayItem);
        helpMenu.getItems().add(aboutAuthorItem);
        return helpMenu;
    }

    private void prepareGridOfGameBoard() { //TODO make first reavel cant be mine
        grid.getChildren().removeAll();
        for (int i = 0; i < game.getMines()[0].length; i++) {
            for (int j = 0; j < game.getMines().length; j++) {
                Button button = new Button();
                button.setPrefWidth(30);
                button.setPrefHeight(30);
                button.setId("" + j + " " + i);
                button.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        updateBoard(button);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        changeFlag(button);
                    }
                });
                grid.add(button, i, j);
            }
        }
    }

    private void changeFlag(Button button) {
        button.setText(button.getText().equals("F") ? "" : "F");
    }

    private void updateBoard(Button button) {
        if ("F".equals(button.getText())) {
            return;
        }
        String[] coords = button.getId().split(" ");
        int xButton = Integer.parseInt(coords[0]);
        int yButton = Integer.parseInt(coords[1]);

        if (game.getMines()[xButton][yButton]) {
            endGame(button);
        } else {
            updateButton(button, xButton, yButton);
            if (gameIsOver()) {
                lockButtons();
                showEndAlert("You win!");
            }
        }
    }

    private void endGame(Button button) {
        button.setText("X");
        lockButtons();
        showEndAlert("You lose!");
    }

    private void updateButton(Button button, int xButton, int yButton) {
        game.addHit();
        int mines = game.getGameBoard()[xButton][yButton];
        button.setText(mines + "");
        button.setDisable(true);

        if (mines == 0) {
            expandAllBlanks(xButton, yButton);
        }
    }

    private void expandAllBlanks(int xButton, int yButton) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                boolean inBounds = xButton + i >= 0 && xButton + i < game.getMines()[0].length
                        && yButton + j >= 0 && yButton + j < game.getMines().length;
                boolean isCurrentButton = i == 0 && j == 0;
                if (inBounds && !isCurrentButton) {
                    updateNeighbouringButton(xButton + i, yButton + j);
                }
            }
        }
    }

    private void updateNeighbouringButton(int xButton, int yButton) {
        Button neighbourButton = (Button) getNodeFromGridPane(xButton, yButton);
        if (neighbourButton != null && !neighbourButton.isDisable()) {
            updateButton(neighbourButton, xButton, yButton);
        }
    }

    private Node getNodeFromGridPane(int col, int row) {
        for (Node node : grid.getChildren()) {
            if (Integer.parseInt(node.getId().split(" ")[0]) == col && Integer.parseInt(node.getId().split(" ")[1]) == row) {
                return node;
            }
        }
        return null;
    }

    private void lockButtons() {
        for (Node child : grid.getChildren()) {
            child.setDisable(true);
        }
    }

    private boolean gameIsOver() {
        return game.isOver();
    }

    private void showEndAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
