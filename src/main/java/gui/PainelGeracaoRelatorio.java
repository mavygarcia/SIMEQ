package gui;

import dao.EncontroDAO;
import dao.RelatorioDAO;
import modelo.Encontro;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

// IMPORTS FALTANDO CORRIGIDOS AQUI:
import java.time.LocalDate;
import javafx.scene.layout.Priority;

public class PainelGeracaoRelatorio extends VBox {

    private final EncontroDAO encontroDAO = new EncontroDAO();
    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    public PainelGeracaoRelatorio(Window owner) {
        super(15);
        this.setAlignment(Pos.TOP_CENTER);
        this.setPadding(new Insets(20));

        Label titulo = new Label("Geração de Relatório de Encontro (.txt)");
        titulo.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        titulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // 1. Campo para Selecionar o Encontro
        List<Encontro> encontros = encontroDAO.listarTodos();
        ObservableList<Encontro> obsEncontros = FXCollections.observableArrayList(encontros);

        ComboBox<Encontro> comboEncontro = new ComboBox<>(obsEncontros);
        comboEncontro.setPromptText("Selecione a data do encontro");

        // Exibe a data do encontro no formato DD/MM/AAAA
        comboEncontro.setConverter(new javafx.util.StringConverter<Encontro>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(Encontro encontro) {
                // Acessa o método getDataEncontro do modelo Encontro
                return encontro != null ? encontro.getDataEncontro().format(formatter) : null;
            }
            @Override
            public Encontro fromString(String string) { return null; }
        });

        // 2. Campo para Escolher o Local de Salvamento
        TextField txtCaminho = new TextField();
        txtCaminho.setPromptText("Clique para escolher o local de salvamento");
        txtCaminho.setDisable(true);

        Button btnBuscarCaminho = new Button("Buscar");
        btnBuscarCaminho.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Relatório");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos de Texto (*.txt)", "*.txt"));

            // Define o nome padrão do arquivo
            String nomePadrao = "Relatorio_SIMEQ_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            fileChooser.setInitialFileName(nomePadrao);

            File file = fileChooser.showSaveDialog(owner);
            if (file != null) {
                txtCaminho.setText(file.getAbsolutePath());
            }
        });

        HBox caminhoBox = new HBox(5, txtCaminho, btnBuscarCaminho);
        // Usa Priority para que o campo de texto ocupe o espaço restante
        HBox.setHgrow(txtCaminho, Priority.ALWAYS);

        // 3. Botão de Geração
        Button btnGerarRelatorio = new Button("Gerar Relatório");
        btnGerarRelatorio.setStyle("-fx-font-size: 16px; -fx-background-color: #FFC0CB;");

        // Layout
        grid.add(new Label("Encontro:"), 0, 0);
        grid.add(comboEncontro, 1, 0);
        grid.add(new Label("Salvar em:"), 0, 1);
        grid.add(caminhoBox, 1, 1);
        grid.add(btnGerarRelatorio, 1, 2);

        // Lógica de Geração
        btnGerarRelatorio.setOnAction(e -> {
            Encontro encontroSelecionado = comboEncontro.getValue();
            String caminho = txtCaminho.getText();

            if (encontroSelecionado == null || caminho.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Selecione um encontro e um local para salvar.").showAndWait();
                return;
            }

            try {
                // Funcionalidade 6: Geração de Relatório
                String resultado = relatorioDAO.gerarRelatorioEncontro(encontroSelecionado.getIdEncontro(), caminho);
                new Alert(Alert.AlertType.INFORMATION, resultado).showAndWait();
            } catch (IOException | RuntimeException ex) {
                new Alert(Alert.AlertType.ERROR, "Falha ao gerar relatório: " + ex.getMessage()).showAndWait();
            }
        });

        this.getChildren().addAll(titulo, grid);
    }
}