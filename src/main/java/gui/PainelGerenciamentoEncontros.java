package gui;

import dao.EncontroDAO;
import modelo.Encontro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PainelGerenciamentoEncontros extends VBox {

    private final EncontroDAO encontroDAO = new EncontroDAO();
    private TableView<Encontro> tabelaEncontros;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PainelGerenciamentoEncontros(TelaPrincipalGUI app) {
        super(10);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));

        Label titulo = new Label("Gerenciamento de Encontros");
        titulo.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        titulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        tabelaEncontros = new TableView<>();
        tabelaEncontros.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        HBox.setHgrow(tabelaEncontros, Priority.ALWAYS);
        VBox.setVgrow(tabelaEncontros, Priority.ALWAYS);

        // 1. Configurar Colunas da Tabela
        // CORREÇÃO: Coluna definida como LocalDate para evitar ClassCastException
        TableColumn<Encontro, LocalDate> dataCol = new TableColumn<>("Data");
        dataCol.setCellValueFactory(new PropertyValueFactory<>("dataEncontro"));
        dataCol.setMinWidth(150);
        // Formata a data
        dataCol.setCellFactory(column -> new TableCell<Encontro, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Item é LocalDate, formatamos diretamente.
                    setText(formatter.format(item));
                }
            }
        });

        TableColumn<Encontro, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("realizado"));
        statusCol.setMinWidth(100);
        // Lógica de exibição do status (Exclusão Lógica/Física)
        statusCol.setCellFactory(column -> new TableCell<Encontro, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Realizado/Futuro" : "Cancelado/Não Realizado (Exclusão Lógica)");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });


        // 2. Coluna de Ações (Editar e Excluir)
        TableColumn<Encontro, Void> acoesCol = new TableColumn<>("Ações");
        acoesCol.setCellFactory(param -> new TableCell<Encontro, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");
            private final HBox pane = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.setOnAction(event -> {
                    Encontro encontro = getTableView().getItems().get(getIndex());

                    // Permite editar APENAS se a data for FUTURA
                    if (encontro.getDataEncontro().isBefore(LocalDate.now())) {
                        new Alert(Alert.AlertType.WARNING, "Não é permitido editar encontros passados.").showAndWait();
                    } else {
                        // Implementação da Edição: Abre a janela modal PainelEdicaoEncontro
                        PainelEdicaoEncontro painelEdicao = new PainelEdicaoEncontro(encontro, PainelGerenciamentoEncontros.this);
                        painelEdicao.show();
                    }
                });

                btnExcluir.setOnAction(event -> {
                    Encontro encontro = getTableView().getItems().get(getIndex());
                    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                            "Deseja realmente EXCLUIR/CANCELAR o encontro em " + encontro.getDataEncontro().format(formatter) + "?",
                            ButtonType.YES, ButtonType.NO);
                    confirmation.showAndWait();

                    if (confirmation.getResult() == ButtonType.YES) {
                        try {
                            // Funcionalidade 4: Exclusão Lógica/Física
                            encontroDAO.excluirLogicamente(encontro.getIdEncontro());
                            recarregarTabela();
                            new Alert(Alert.AlertType.INFORMATION, "Encontro processado. Verifique o status para exclusão lógica/física.").showAndWait();
                        } catch (RuntimeException e) {
                            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });


        tabelaEncontros.getColumns().addAll(dataCol, statusCol, acoesCol);

        recarregarTabela();

        this.getChildren().addAll(titulo, tabelaEncontros);
    }

    public void recarregarTabela() {
        // Importação de List está correta
        List<Encontro> encontros = encontroDAO.listarTodos();
        ObservableList<Encontro> data = FXCollections.observableArrayList(encontros);
        tabelaEncontros.setItems(data);
    }
}