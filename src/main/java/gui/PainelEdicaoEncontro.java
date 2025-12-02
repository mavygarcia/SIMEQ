package gui;

import dao.EncontroDAO;
import modelo.Encontro;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Janela modal para edição de um Encontro específico.
 * A edição é permitida apenas para encontros futuros.
 */
public class PainelEdicaoEncontro extends Stage {

    private final EncontroDAO encontroDAO = new EncontroDAO();
    private final Encontro encontroOriginal;
    private final PainelGerenciamentoEncontros painelGerenciamento; // Para callback de recarga
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PainelEdicaoEncontro(Encontro encontro, PainelGerenciamentoEncontros painelGerenciamento) {
        this.encontroOriginal = encontro;
        this.painelGerenciamento = painelGerenciamento;

        // Configuração da janela modal
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Editar Encontro - " + encontro.getDataEncontro().format(formatter));

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));

        // Título
        Label titulo = new Label("Editar Encontro");
        titulo.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        titulo.setTextFill(javafx.scene.paint.Color.web("#FF66B2"));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // 1. Campo de Data
        DatePicker datePicker = new DatePicker(encontro.getDataEncontro());

        // Bloqueia a alteração da data se o encontro for passado (mantém a data original como a única opção)
        if (encontro.getDataEncontro().isBefore(LocalDate.now())) {
            datePicker.setDisable(true);
            formGrid.add(new Label("Data (Encontro Passado, Não Editável):"), 0, 0);
        } else {
            formGrid.add(new Label("Data:"), 0, 0);
        }
        formGrid.add(datePicker, 1, 0);

        // 2. Campo de Status (para reversão de exclusão lógica, por exemplo)
        CheckBox chkRealizado = new CheckBox("Encontro Realizado/Futuro");
        chkRealizado.setSelected(encontro.isRealizado());
        formGrid.add(new Label("Status:"), 0, 1);
        formGrid.add(chkRealizado, 1, 1);

        // 3. Botões de Ação
        Button btnSalvar = new Button("Salvar Alterações");
        btnSalvar.setStyle("-fx-font-size: 14px; -fx-background-color: #FFC0CB;");
        Button btnCancelar = new Button("Cancelar");

        HBox botoes = new HBox(15, btnSalvar, btnCancelar);
        botoes.setAlignment(Pos.CENTER);

        // Ação Salvar
        btnSalvar.setOnAction(e -> salvarEdicao(datePicker.getValue(), chkRealizado.isSelected()));

        // Ação Cancelar
        btnCancelar.setOnAction(e -> this.close());

        root.getChildren().addAll(titulo, formGrid, botoes);
        Scene scene = new Scene(root, 400, 300);
        this.setScene(scene);
    }

    private void salvarEdicao(LocalDate novaData, boolean novoStatus) {
        try {
            // Cria um novo objeto encontro com as alterações
            Encontro encontroAtualizado = new Encontro();
            encontroAtualizado.setIdEncontro(encontroOriginal.getIdEncontro());
            encontroAtualizado.setDataEncontro(novaData);
            encontroAtualizado.setRealizado(novoStatus);

            // Regra de Negócio: O DAO se encarregará de verificar se a edição é permitida
            encontroDAO.editar(encontroAtualizado);

            new Alert(Alert.AlertType.INFORMATION, "Encontro editado com sucesso!").showAndWait();

            // 4. Callback para atualizar a tabela principal
            painelGerenciamento.recarregarTabela();
            this.close();

        } catch (IllegalArgumentException e) {
            // Captura a exceção de regra de negócio, como "Não é permitido editar encontros passados."
            new Alert(Alert.AlertType.ERROR, "Erro de Regra de Negócio: " + e.getMessage()).showAndWait();
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Falha ao salvar a edição: " + e.getMessage()).showAndWait();
        }
    }
}