package org.example.npbk.service;

import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.BankingRepository;

public class BankingService {
    private final BankingRepository repository;
    public BankingService(BankingRepository repository){ this.repository=repository; }
    public List<BankStatementLineViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::validate); return rows; }
    public void save(BankStatementLineViewModel row){ validate(row); repository.save(row); }
    public void saveAll(List<BankStatementLineViewModel> rows){ rows.forEach(this::validate); repository.saveAll(rows); }
    public List<String> validate(BankStatementLineViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Bank account",row.getBankAccount()); SpreadsheetValidation.require(messages,"Posted date",row.getPostedDate()); SpreadsheetValidation.nonZero(messages,"Amount",row.getAmount()); if(row.getStatus()==BankStatementLineStatus.MATCHED && row.getMatchedTransactionId()==null) messages.add("ERROR: Matched statement line must identify a transaction id.");
        return SpreadsheetValidation.apply(row,messages);
    }
}
