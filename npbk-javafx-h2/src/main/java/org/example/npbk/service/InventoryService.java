package org.example.npbk.service;

import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.InventoryRepository;

public class InventoryService {
    private final InventoryRepository repository;
    public InventoryService(InventoryRepository repository){ this.repository=repository; }
    public List<InventoryItemViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::validate); return rows; }
    public void save(InventoryItemViewModel row){ validate(row); repository.save(row); }
    public void saveAll(List<InventoryItemViewModel> rows){ rows.forEach(this::validate); repository.saveAll(rows); }
    public List<String> validate(InventoryItemViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Description",row.getDescription()); SpreadsheetValidation.nonNegative(messages,"Quantity",row.getQuantity()); SpreadsheetValidation.nonNegative(messages,"Total value",row.getTotalValue()); if(row.getConfirmedDate()==null) messages.add("WARNING: Guardian/location confirmation date is blank.");
        return SpreadsheetValidation.apply(row,messages);
    }
}
