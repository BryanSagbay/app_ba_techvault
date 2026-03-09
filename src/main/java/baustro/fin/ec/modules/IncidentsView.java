package baustro.fin.ec.modules;

import baustro.fin.ec.dao.IncidentDAO;
import baustro.fin.ec.dao.ServerDAO;
import baustro.fin.ec.model.Incident;
import baustro.fin.ec.model.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class IncidentsView extends VBox {

    private final IncidentDAO dao = new IncidentDAO();
    private final ServerDAO serverDAO = new ServerDAO();
    private TableView<Incident> table;
    private ObservableList<Incident> data;

    public IncidentsView() {
        setSpacing(16);
        setPadding(new Insets(24));
        getStyleClass().add("content-area");

        Label title = new Label("Incidentes / Correctivos");
        title.getStyleClass().add("module-title");

        HBox toolbar = buildToolbar();
        table = buildTable();
        data = FXCollections.observableArrayList(dao.findAll());
        table.setItems(data);

        VBox.setVgrow(table, Priority.ALWAYS);
        getChildren().addAll(title, toolbar, table);
    }

    private HBox buildToolbar() {
        TextField search = new TextField();
        search.setPromptText("Buscar por ticket, titulo, servicio...");
        search.setPrefWidth(300);
        search.getStyleClass().add("search-field");
        search.textProperty().addListener((o, old, val) -> {
            if (val == null || val.trim().isEmpty()) data.setAll(dao.findAll());
            else data.setAll(dao.search(val));
        });

        Button btnAdd  = new Button("+ Nuevo Incidente"); btnAdd.getStyleClass().add("btn-primary");
        Button btnEdit = new Button("Editar"); btnEdit.getStyleClass().add("btn-secondary");
        Button btnDel  = new Button("Eliminar"); btnDel.getStyleClass().add("btn-danger");

        btnAdd.setOnAction(e  -> openForm(null));
        btnEdit.setOnAction(e -> { Incident s = table.getSelectionModel().getSelectedItem(); if (s != null) openForm(s); });
        btnDel.setOnAction(e  -> deleteSelected());

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(10, search, spacer, btnAdd, btnEdit, btnDel);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private TableView<Incident> buildTable() {
        TableView<Incident> tv = new TableView<Incident>();
        tv.getStyleClass().add("table-view");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Incident, String> colTicket  = col("Ticket", "ticketNumber", 100);
        TableColumn<Incident, String> colTitle   = col("Titulo", "title", 200);
        TableColumn<Incident, String> colService = col("Servicio", "affectedService", 130);
        TableColumn<Incident, String> colServer  = col("Servidor", "serverName", 120);
        TableColumn<Incident, String> colDate    = col("Fecha", "createdAt", 130);

        TableColumn<Incident, String> colSeverity = new TableColumn<Incident, String>("Severidad");
        colSeverity.setCellValueFactory(new PropertyValueFactory<Incident, String>("severity"));
        colSeverity.setPrefWidth(90);
        colSeverity.setCellFactory(tc -> new TableCell<Incident, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(v);
                String cls;
                if ("CRITICA".equals(v)) cls = "badge-red";
                else if ("ALTA".equals(v)) cls = "badge-yellow";
                else if ("MEDIA".equals(v)) cls = "badge-blue";
                else cls = "badge-gray";
                b.getStyleClass().add(cls);
                setGraphic(b); setText(null);
            }
        });

        TableColumn<Incident, String> colStatus = new TableColumn<Incident, String>("Estado");
        colStatus.setCellValueFactory(new PropertyValueFactory<Incident, String>("status"));
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(tc -> new TableCell<Incident, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(v);
                String cls;
                if ("CERRADO".equals(v)) cls = "badge-green";
                else if ("ABIERTO".equals(v)) cls = "badge-red";
                else cls = "badge-yellow";
                b.getStyleClass().add(cls);
                setGraphic(b); setText(null);
            }
        });

        tv.getColumns().addAll(colTicket, colTitle, colSeverity, colStatus, colService, colServer, colDate);
        tv.setRowFactory(t -> {
            TableRow<Incident> row = new TableRow<Incident>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) openForm(row.getItem()); });
            return row;
        });
        return tv;
    }

    private TableColumn<Incident, String> col(String h, String p, double w) {
        TableColumn<Incident, String> c = new TableColumn<Incident, String>(h);
        c.setCellValueFactory(new PropertyValueFactory<Incident, String>(p));
        c.setPrefWidth(w);
        return c;
    }

    private void openForm(final Incident incident) {
        Dialog<Incident> dialog = new Dialog<Incident>();
        dialog.setTitle(incident == null ? "Nuevo Incidente" : "Editar Incidente");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #2a2a3e;");

        final TextField fTicket   = field("INC-2024-001");
        final TextField fTitle    = field("Descripcion corta del incidente");
        final TextField fService  = field("Servicio afectado");
        final TextArea  fDesc     = textarea("Descripcion detallada del incidente");
        final TextArea  fSolution = textarea("Como se soluciono?");
        final TextArea  fRoot     = textarea("Causa raiz identificada");
        final ComboBox<String> fSeverity = combo("CRITICA", "ALTA", "MEDIA", "BAJA");
        final ComboBox<String> fStatus   = combo("ABIERTO", "EN PROGRESO", "CERRADO");

        List<Server> servers = serverDAO.findAll();
        final ComboBox<Server> fServer = new ComboBox<Server>(FXCollections.observableArrayList(servers));
        fServer.getStyleClass().add("combo-box"); fServer.setPrefWidth(280);
        fServer.setPromptText("Seleccionar servidor...");

        final TextField fStart = field("2024-01-15");
        final TextField fEnd   = field("Fecha de resolucion");

        if (incident != null) {
            fTicket.setText(incident.getTicketNumber());
            fTitle.setText(incident.getTitle());
            fService.setText(incident.getAffectedService());
            fDesc.setText(incident.getDescription());
            fSolution.setText(incident.getSolution());
            fRoot.setText(incident.getRootCause());
            fSeverity.setValue(incident.getSeverity());
            fStatus.setValue(incident.getStatus());
            fStart.setText(incident.getStartDate());
            fEnd.setText(incident.getEndDate());
            for (Server s : servers) {
                if (s.getId() == incident.getServerId()) { fServer.setValue(s); break; }
            }
        }

        int r = 0;
        grid.add(lbl("Nro. Ticket"), 0, r); grid.add(fTicket, 1, r++);
        grid.add(lbl("Titulo *"), 0, r); grid.add(fTitle, 1, r++);
        grid.add(lbl("Severidad"), 0, r); grid.add(fSeverity, 1, r++);
        grid.add(lbl("Estado"), 0, r); grid.add(fStatus, 1, r++);
        grid.add(lbl("Servicio afectado"), 0, r); grid.add(fService, 1, r++);
        grid.add(lbl("Servidor"), 0, r); grid.add(fServer, 1, r++);
        grid.add(lbl("Fecha inicio"), 0, r); grid.add(fStart, 1, r++);
        grid.add(lbl("Fecha fin"), 0, r); grid.add(fEnd, 1, r++);
        grid.add(lbl("Descripcion"), 0, r); grid.add(fDesc, 1, r++);
        grid.add(lbl("Solucion"), 0, r); grid.add(fSolution, 1, r++);
        grid.add(lbl("Causa raiz"), 0, r); grid.add(fRoot, 1, r++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #2a2a3e; -fx-border-color: transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefSize(560, 580);

        ButtonType btnSave   = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnSave) {
                Incident inc = incident == null ? new Incident() : incident;
                inc.setTicketNumber(fTicket.getText());
                inc.setTitle(fTitle.getText());
                inc.setAffectedService(fService.getText());
                inc.setDescription(fDesc.getText());
                inc.setSolution(fSolution.getText());
                inc.setRootCause(fRoot.getText());
                inc.setSeverity(fSeverity.getValue());
                inc.setStatus(fStatus.getValue());
                inc.setStartDate(fStart.getText());
                inc.setEndDate(fEnd.getText());
                if (fServer.getValue() != null) inc.setServerId(fServer.getValue().getId());
                return inc;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(inc -> {
            if (inc.getTitle() == null || inc.getTitle().trim().isEmpty()) return;
            if (incident == null) dao.save(inc); else dao.update(inc);
            data.setAll(dao.findAll());
        });
    }

    private void deleteSelected() {
        Incident sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Eliminar el incidente \"" + sel.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmar eliminacion");
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) { dao.delete(sel.getId()); data.setAll(dao.findAll()); }
        });
    }

    private TextField field(String p) { TextField tf = new TextField(); tf.setPromptText(p); tf.getStyleClass().add("text-field"); tf.setPrefWidth(280); return tf; }
    private TextArea textarea(String p) { TextArea ta = new TextArea(); ta.setPromptText(p); ta.setPrefRowCount(3); ta.getStyleClass().add("text-area"); ta.setPrefWidth(280); return ta; }
    private ComboBox<String> combo(String... items) { ComboBox<String> cb = new ComboBox<String>(FXCollections.observableArrayList(items)); cb.getStyleClass().add("combo-box"); cb.setValue(items[0]); cb.setPrefWidth(280); return cb; }
    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}
