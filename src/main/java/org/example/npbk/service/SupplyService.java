package org.example.npbk.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.SupplyRepository;

public class SupplyService {
    private final SupplyRepository repository;
    public SupplyService(SupplyRepository repository){ this.repository=repository; }
    public List<SupplyItemViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::recalculate); return rows; }
    public void save(SupplyItemViewModel row){ recalculate(row); validate(row); repository.save(row); }
    public void saveAll(List<SupplyItemViewModel> rows){ rows.forEach(r->{recalculate(r); validate(r);}); repository.saveAll(rows); }
    public void recalculate(SupplyItemViewModel row){ row.setTotalValue(row.getQuantity().multiply(row.getUnitCost()).setScale(2, java.math.RoundingMode.HALF_UP)); }
    public List<String> validate(SupplyItemViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Description",row.getDescription()); SpreadsheetValidation.nonNegative(messages,"Quantity",row.getQuantity()); SpreadsheetValidation.nonNegative(messages,"Unit cost",row.getUnitCost()); if(row.getQuantity().compareTo(BigDecimal.ZERO)==0) messages.add("WARNING: Quantity is zero.");
        return SpreadsheetValidation.apply(row,messages);
    }
}
