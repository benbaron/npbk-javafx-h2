package org.example.npbk.service;

import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.SupplementalLineRepository;

public class SupplementalLineService {
    private final SupplementalLineRepository repository;
    public SupplementalLineService(SupplementalLineRepository repository){ this.repository=repository; }
    public List<SupplementalLineViewModel> loadAssetLines(){ var rows=repository.findByAssetSide(true); rows.forEach(this::recalculate); return rows; }
    public List<SupplementalLineViewModel> loadLiabilityLines(){ var rows=repository.findByAssetSide(false); rows.forEach(this::recalculate); return rows; }
    public void save(SupplementalLineViewModel row){ recalculate(row); validate(row); repository.save(row); }
    public void saveAll(List<SupplementalLineViewModel> rows){ rows.forEach(r->{ recalculate(r); validate(r); }); repository.saveAll(rows); }
    public void recalculate(SupplementalLineViewModel row){ row.setRemainingAmount(row.getAmount()); }
    public List<String> validate(SupplementalLineViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Description",row.getDescription()); SpreadsheetValidation.nonNegative(messages,"Amount",row.getAmount()); SpreadsheetValidation.startBeforeEnd(messages,row.getStartDate(),row.getEndDate());
        return SpreadsheetValidation.apply(row,messages);
    }
}
