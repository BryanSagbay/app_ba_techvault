package baustro.fin.ec.modules;

import baustro.fin.ec.dao.NoteDAO;
import baustro.fin.ec.model.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class NotesView extends BorderPane {

    private final NoteDAO dao = new NoteDAO();
    private ListView<Note> noteList;
    private ObservableList<Note> data;
    private TextArea editor;
    private TextField titleField, tagsField;
    private ComboBox<String> categoryCombo;
    private Note currentNote;

    public NotesView() {
        getStyleClass().add("content-area");
        setPadding(new Insets(0));
        setLeft(buildSidebar());
        setCenter(buildEditor());
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 1 0 0;");

        HBox header = new HBox(8);
        header.setPadding(new Insets(14, 12, 10, 12));
        header.setStyle("-fx-background-color: #181825;");
        Label title = new Label("Notas");
        title.getStyleClass().add("module-title");
        title.setStyle("-fx-font-size: 15px;");
        Button btnNew = new Button("+");
        btnNew.getStyleClass().add("btn-primary");
        btnNew.setStyle("-fx-font-size: 16px; -fx-padding: 2 10 2 10;");
        btnNew.setOnAction(e -> newNote());
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, btnNew);

        TextField search = new TextField();
        search.setPromptText("Buscar notas...");
        search.getStyleClass().add("search-field");
        VBox.setMargin(search, new Insets(0, 8, 8, 8));
        search.textProperty().addListener((o, old, val) -> {
            if (val == null || val.trim().isEmpty()) data.setAll(dao.findAll());
            else data.setAll(dao.search(val));
        });

        noteList = new ListView<Note>();
        noteList.getStyleClass().add("table-view");
        noteList.setStyle("-fx-border-color: transparent;");
        VBox.setVgrow(noteList, Priority.ALWAYS);

        data = FXCollections.observableArrayList(dao.findAll());
        noteList.setItems(data);
        noteList.setCellFactory(lv -> new ListCell<Note>() {
            @Override
            protected void updateItem(Note n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setGraphic(null); return; }
                VBox box = new VBox(2);
                Label t = new Label(n.getTitle() != null ? n.getTitle() : "Sin titulo");
                t.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold;");
                String tagText = (n.getTags() != null && !n.getTags().trim().isEmpty())
                    ? n.getCategory() + "  -  " + n.getTags()
                    : n.getCategory();
                Label cat = new Label(tagText);
                cat.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 11px;");
                box.getChildren().addAll(t, cat);
                box.setPadding(new Insets(4, 8, 4, 8));
                setGraphic(box); setText(null);
            }
        });
        noteList.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) loadNote(n);
        });

        Button btnDel = new Button("Eliminar nota");
        btnDel.getStyleClass().add("btn-danger");
        btnDel.setMaxWidth(Double.MAX_VALUE);
        btnDel.setOnAction(e -> deleteSelected());
        VBox.setMargin(btnDel, new Insets(8));

        sidebar.getChildren().addAll(header, search, noteList, btnDel);
        return sidebar;
    }

    private VBox buildEditor() {
        VBox pane = new VBox(12);
        pane.setPadding(new Insets(20));
        pane.setStyle("-fx-background-color: #1e1e2e;");

        titleField = new TextField();
        titleField.setPromptText("Titulo de la nota");
        titleField.getStyleClass().add("text-field");
        titleField.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 12 10 12;");

        HBox metaRow = new HBox(12);
        categoryCombo = new ComboBox<String>(FXCollections.observableArrayList(
            "GENERAL", "PROCEDIMIENTO", "CONFIGURACION", "INCIDENTE", "BANCO", "PERSONAL"));
        categoryCombo.getStyleClass().add("combo-box");
        categoryCombo.setValue("GENERAL");

        tagsField = new TextField();
        tagsField.setPromptText("tags, separados, por, comas");
        tagsField.getStyleClass().add("text-field");
        HBox.setHgrow(tagsField, Priority.ALWAYS);
        metaRow.getChildren().addAll(categoryCombo, tagsField);

        editor = new TextArea();
        editor.setPromptText("Escribe aqui tu nota... (texto libre, procedimientos, configuraciones, etc.)");
        editor.getStyleClass().add("text-area");
        editor.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");
        VBox.setVgrow(editor, Priority.ALWAYS);

        Button btnSave = new Button("Guardar");
        btnSave.getStyleClass().add("btn-primary");
        btnSave.setOnAction(e -> saveCurrentNote());

        HBox footer = new HBox(btnSave);
        footer.setStyle("-fx-alignment: CENTER_RIGHT;");

        pane.getChildren().addAll(titleField, metaRow, editor, footer);
        return pane;
    }

    private void newNote() {
        currentNote = null;
        titleField.clear(); editor.clear(); tagsField.clear();
        categoryCombo.setValue("GENERAL");
        titleField.requestFocus();
    }

    private void loadNote(Note n) {
        currentNote = n;
        titleField.setText(n.getTitle() != null ? n.getTitle() : "");
        editor.setText(n.getContent() != null ? n.getContent() : "");
        categoryCombo.setValue(n.getCategory() != null ? n.getCategory() : "GENERAL");
        tagsField.setText(n.getTags() != null ? n.getTags() : "");
    }

    private void saveCurrentNote() {
        String t = titleField.getText();
        if (t == null || t.trim().isEmpty()) {
            titleField.setPromptText("El titulo es requerido");
            return;
        }
        if (currentNote == null) {
            Note n = new Note();
            n.setTitle(t); n.setContent(editor.getText());
            n.setCategory(categoryCombo.getValue()); n.setTags(tagsField.getText());
            dao.save(n);
        } else {
            currentNote.setTitle(t); currentNote.setContent(editor.getText());
            currentNote.setCategory(categoryCombo.getValue()); currentNote.setTags(tagsField.getText());
            dao.update(currentNote);
        }
        data.setAll(dao.findAll());
    }

    private void deleteSelected() {
        Note sel = noteList.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar la nota \"" + sel.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { dao.delete(sel.getId()); data.setAll(dao.findAll()); newNote(); }
        });
    }
}
