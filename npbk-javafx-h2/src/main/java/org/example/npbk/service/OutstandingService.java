package org.example.npbk.service;

import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.OutstandingRepository;

public class OutstandingService {
    private final OutstandingRepository repository;
    public OutstandingService(OutstandingRepository repository){ this.repository=repository; }
    public List<OutstandingItemViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::recalculate); return rows; }
    public void save(OutstandingItemViewModel row){ recalculate(row); validate(row); repository.save(row); }
    public void saveAll(List<OutstandingItemViewModel> rows){ rows.forEach(r -> { recalculate(r); validate(r); }); repository.saveAll(rows); }
    public void recalculate(OutstandingItemViewModel row){ if(row.getClearedDate()!=null && row.getStatus()==OutstandingStatus.OPEN) row.setStatus(OutstandingStatus.CLEARED); }
    public List<String> validate(OutstandingItemViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Date issued/received",row.getDateIssued()); SpreadsheetValidation.require(messages,"Name",row.getName()); SpreadsheetValidation.require(messages,"Bank account",row.getBankAccount()); SpreadsheetValidation.nonZero(messages,"Amount",row.getAmount());
        return SpreadsheetValidation.apply(row,messages);
    }
}
