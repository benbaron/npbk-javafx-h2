package org.example.npbk.ui;

import org.example.npbk.model.SummarySettingsViewModel;
import org.example.npbk.service.SummaryService;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SummaryView {
    private final BorderPane root = new BorderPane();
    private final SummaryService service;
    private SummarySettingsViewModel model;
    private final TextArea validationArea = new TextArea();
    private final Label status = new Label("Ready.");

    public SummaryView(SummaryService service) { this.service = service; build(); load(); }
    public Node getRoot() { return root; }

    private void build() {
        TextField org = new TextField(); TextField branch = new TextField(); TextField kingdom = new TextField();
        DatePicker start = new DatePicker(); DatePicker end = new DatePicker(); TextField prepared = new TextField(); TextArea notes = new TextArea(); notes.setPrefRowCount(3);
        Label bankTotal = new Label(); Label budgetTotal = new Label(); Label outstandingTotal = new Label();
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(8); form.setPadding(new Insets(12));
        int r=0;
        form.add(new Label("Organization"),0,r); form.add(org,1,r++);
        form.add(new Label("Branch"),0,r); form.add(branch,1,r++);
        form.add(new Label("Kingdom"),0,r); form.add(kingdom,1,r++);
        form.add(new Label("Period Start"),0,r); form.add(start,1,r++);
        form.add(new Label("Period End"),0,r); form.add(end,1,r++);
        form.add(new Label("Prepared By"),0,r); form.add(prepared,1,r++);
        form.add(new Label("Notes"),0,r); form.add(notes,1,r++);
        form.add(new Label("Calculated Bank Total"),0,r); form.add(bankTotal,1,r++);
        form.add(new Label("Calculated Budget Total"),0,r); form.add(budgetTotal,1,r++);
        form.add(new Label("Open Outstanding Net"),0,r); form.add(outstandingTotal,1,r++);
        ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(150); ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS); form.getColumnConstraints().addAll(c0,c1);

        Button save = new Button("Save Summary"); Button refresh = new Button("Refresh Totals");
        save.setOnAction(e -> { copyFromControls(org,branch,kingdom,start,end,prepared,notes); service.save(model); showValidation(); status.setText("Summary saved."); });
        refresh.setOnAction(e -> { copyFromControls(org,branch,kingdom,start,end,prepared,notes); service.refreshTotals(model); service.validate(model); updateTotals(bankTotal,budgetTotal,outstandingTotal); showValidation(); status.setText("Totals refreshed from ledger/outstanding data."); });
        HBox toolbar = new HBox(8, save, refresh, status); toolbar.setPadding(new Insets(8));
        validationArea.setEditable(false); validationArea.setPrefRowCount(6);
        VBox bottom = new VBox(4, new Label("Validation"), validationArea); bottom.setPadding(new Insets(8));
        root.setTop(toolbar); root.setCenter(form); root.setBottom(bottom);

        root.sceneProperty().addListener((obs, old, scene) -> {
            if(scene!=null) {
                org.textProperty().bindBidirectional(model.organizationNameProperty()); branch.textProperty().bindBidirectional(model.branchNameProperty()); kingdom.textProperty().bindBidirectional(model.kingdomNameProperty());
                start.valueProperty().bindBidirectional(model.periodStartProperty()); end.valueProperty().bindBidirectional(model.periodEndProperty()); prepared.textProperty().bindBidirectional(model.preparedByProperty()); notes.textProperty().bindBidirectional(model.notesProperty());
                updateTotals(bankTotal,budgetTotal,outstandingTotal); showValidation();
            }
        });
    }
    private void load(){ model=service.load(); }
    private void copyFromControls(TextField org,TextField branch,TextField kingdom,DatePicker start,DatePicker end,TextField prepared,TextArea notes){
        model.setOrganizationName(org.getText()); model.setBranchName(branch.getText()); model.setKingdomName(kingdom.getText()); model.setPeriodStart(start.getValue()); model.setPeriodEnd(end.getValue()); model.setPreparedBy(prepared.getText()); model.setNotes(notes.getText());
    }
    private void updateTotals(Label bank, Label budget, Label outstanding){ bank.setText(model.getBankTotal().toPlainString()); budget.setText(model.getBudgetTotal().toPlainString()); outstanding.setText(model.getOpenOutstandingTotal().toPlainString()); }
    private void showValidation(){ validationArea.setText(model.getValidationMessages().isEmpty()?"No validation issues.":String.join("\n", model.getValidationMessages())); }
}
