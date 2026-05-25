package org.example.npbk.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.*;

public class BudgetService {
    private final BudgetRepository repository; private final TransactionRepository transactionRepository;
    public BudgetService(BudgetRepository repository, TransactionRepository transactionRepository){ this.repository=repository; this.transactionRepository=transactionRepository; }
    public List<BudgetLineViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::recalculate); return rows; }
    public void save(BudgetLineViewModel row){ recalculate(row); validate(row); repository.save(row); }
    public void saveAll(List<BudgetLineViewModel> rows){ rows.forEach(r -> { recalculate(r); validate(r); }); repository.saveAll(rows); }
    public void recalculate(BudgetLineViewModel row){
        BigDecimal actual = transactionRepository.sumBudgetEffect(row.getFund(), row.getBudgetCategory());
        if (row.getLineType() == BudgetLineType.EXPENSE && actual.signum() < 0) actual = actual.abs();
        row.setActualAmount(actual);
        row.setVarianceAmount(row.getPlannedAmount().subtract(actual));
    }
    public List<String> validate(BudgetLineViewModel row){
        List<String> messages=new java.util.ArrayList<>();
        SpreadsheetValidation.require(messages,"Fund",row.getFund()); SpreadsheetValidation.require(messages,"Budget category",row.getBudgetCategory());
        SpreadsheetValidation.nonNegative(messages,"Planned amount",row.getPlannedAmount());
        return SpreadsheetValidation.apply(row,messages);
    }
}
