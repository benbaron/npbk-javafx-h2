package org.example.npbk.ui;

import java.math.BigDecimal;
import org.example.npbk.model.*;
import org.example.npbk.repo.LookupRepository;
import org.example.npbk.service.BudgetService;

import javafx.collections.*; import javafx.geometry.Insets; import javafx.scene.Node; import javafx.scene.control.*; import javafx.scene.control.cell.ComboBoxTableCell; import javafx.scene.layout.*;

public class BudgetView {
    private final BorderPane root=new BorderPane(); private final TableView<BudgetLineViewModel> table=new TableView<>(); private final ObservableList<BudgetLineViewModel> rows=FXCollections.observableArrayList(); private final TextArea validationArea=new TextArea(); private final Label status=new Label("Ready.");
    private final LookupRepository lookups; private final BudgetService service;
    public BudgetView(LookupRepository lookups, BudgetService service){ this.lookups=lookups; this.service=service; build(); load(); }
    public Node getRoot(){ return root; }
    private void build(){
        table.setEditable(true); table.getSelectionModel().setCellSelectionEnabled(true); table.setItems(rows);
        table.getColumns().add(SpreadsheetUi.rowNumberColumn(table)); table.getColumns().add(comboString("Fund", "fund", lookups.funds(), 150)); table.getColumns().add(comboString("Budget Category", "budgetCategory", lookups.budgetCategories(), 220)); table.getColumns().add(typeCol()); table.getColumns().add(money("Planned", "planned", true, 120)); table.getColumns().add(money("Actual", "actual", false, 120)); table.getColumns().add(money("Variance", "variance", false, 120)); table.getColumns().add(text("Notes", "notes", 260)); table.getColumns().add(stateCol());
        table.getSelectionModel().selectedItemProperty().addListener((o,old,row)->show(row)); SpreadsheetUi.installNavigation(table);
        Button add=new Button("Add Line"); add.setOnAction(e->addRow()); Button save=new Button("Save Selected"); save.setOnAction(e->saveSelected()); Button saveAll=new Button("Save All"); saveAll.setOnAction(e->saveAll()); Button recalc=new Button("Recalculate Actuals"); recalc.setOnAction(e->recalcAll());
        HBox toolbar=new HBox(8,add,save,saveAll,recalc,status); toolbar.setPadding(new Insets(8)); validationArea.setEditable(false); validationArea.setPrefRowCount(5);
        root.setTop(toolbar); root.setCenter(table); root.setBottom(new VBox(4,new Label("Validation"),validationArea)); BorderPane.setMargin(root.getBottom(), new Insets(8));
    }
    private TableColumn<BudgetLineViewModel,String> comboString(String title,String prop,ObservableList<String> choices,int width){ TableColumn<BudgetLineViewModel,String> c=new TableColumn<>(title); c.setCellValueFactory(v->prop.equals("fund")?v.getValue().fundProperty():v.getValue().budgetCategoryProperty()); c.setCellFactory(ComboBoxTableCell.forTableColumn(choices)); c.setOnEditCommit(e->{ if(prop.equals("fund")) e.getRowValue().setFund(e.getNewValue()); else e.getRowValue().setBudgetCategory(e.getNewValue()); after(e.getRowValue()); }); c.setPrefWidth(width); return c; }
    private TableColumn<BudgetLineViewModel,BudgetLineType> typeCol(){ TableColumn<BudgetLineViewModel,BudgetLineType> c=new TableColumn<>("Type"); c.setCellValueFactory(v->v.getValue().lineTypeProperty()); c.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(BudgetLineType.values()))); c.setOnEditCommit(e->{ e.getRowValue().setLineType(e.getNewValue()); after(e.getRowValue());}); c.setPrefWidth(100); return c; }
    private TableColumn<BudgetLineViewModel,String> text(String title,String prop,int width){ TableColumn<BudgetLineViewModel,String> c=new TableColumn<>(title); c.setCellValueFactory(v->v.getValue().notesProperty()); c.setCellFactory(v->EditingCells.textCell()); c.setOnEditCommit(e->{e.getRowValue().setNotes(e.getNewValue()); after(e.getRowValue());}); c.setPrefWidth(width); return c; }
    private TableColumn<BudgetLineViewModel,BigDecimal> money(String title,String prop,boolean edit,int width){ TableColumn<BudgetLineViewModel,BigDecimal> c=new TableColumn<>(title); c.setCellValueFactory(v->switch(prop){case "planned"->v.getValue().plannedAmountProperty(); case "actual"->v.getValue().actualAmountProperty(); default->v.getValue().varianceAmountProperty();}); c.setCellFactory(v->EditingCells.moneyCell()); c.setEditable(edit); if(edit)c.setOnEditCommit(e->{e.getRowValue().setPlannedAmount(e.getNewValue()); after(e.getRowValue());}); c.setPrefWidth(width); return c; }
    private TableColumn<BudgetLineViewModel,ValidationState> stateCol(){ TableColumn<BudgetLineViewModel,ValidationState> c=new TableColumn<>("State"); c.setCellValueFactory(v->v.getValue().validationStateProperty()); c.setEditable(false); c.setPrefWidth(90); return c; }
    private void after(BudgetLineViewModel row){ service.recalculate(row); service.validate(row); table.refresh(); show(row); }
    private void load(){ rows.setAll(service.load()); if(rows.isEmpty()) addRow(); }
    private void addRow(){ BudgetLineViewModel r=new BudgetLineViewModel(); rows.add(r); table.getSelectionModel().clearAndSelect(rows.size()-1, table.getColumns().get(1)); }
    private void saveSelected(){ BudgetLineViewModel r=table.getSelectionModel().getSelectedItem(); if(r==null)return; service.save(r); table.refresh(); show(r); status.setText("Budget line saved."); }
    private void saveAll(){ service.saveAll(rows); table.refresh(); status.setText("Saved " + rows.size() + " budget line(s)."); }
    private void recalcAll(){ rows.forEach(r->{service.recalculate(r); service.validate(r);}); table.refresh(); status.setText("Budget actuals recalculated from ledger."); }
    private void show(BudgetLineViewModel r){ validationArea.setText(r==null?"":(r.getValidationMessages().isEmpty()?"No validation issues.":String.join("\n",r.getValidationMessages()))); }
}
