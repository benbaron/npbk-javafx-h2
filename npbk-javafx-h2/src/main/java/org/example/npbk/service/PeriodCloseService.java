package org.example.npbk.service;

import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.*;

public class PeriodCloseService {
    private final PeriodCloseRepository repository; private final TransactionRepository transactionRepository; private final OutstandingRepository outstandingRepository; private final SupplementalLineRepository supplementalRepository;
    public PeriodCloseService(PeriodCloseRepository repository, TransactionRepository transactionRepository, OutstandingRepository outstandingRepository, SupplementalLineRepository supplementalRepository){ this.repository=repository; this.transactionRepository=transactionRepository; this.outstandingRepository=outstandingRepository; this.supplementalRepository=supplementalRepository; }
    public List<PeriodCloseViewModel> load(){ var rows=repository.findAll(); rows.forEach(this::validate); return rows; }
    public void save(PeriodCloseViewModel row){ validate(row); repository.save(row); }
    public void saveAll(List<PeriodCloseViewModel> rows){ rows.forEach(this::validate); repository.saveAll(rows); }
    public List<String> validate(PeriodCloseViewModel row){
        List<String> messages=new java.util.ArrayList<>(); SpreadsheetValidation.require(messages,"Period start",row.getPeriodStart()); SpreadsheetValidation.require(messages,"Period end",row.getPeriodEnd()); SpreadsheetValidation.startBeforeEnd(messages,row.getPeriodStart(),row.getPeriodEnd());
        if(transactionRepository.countErrors()>0) messages.add("ERROR: Ledger has validation errors.");
        long openOutstanding = outstandingRepository.findAll().stream().filter(o -> o.getStatus()==OutstandingStatus.OPEN).count();
        if(openOutstanding>0) messages.add("WARNING: " + openOutstanding + " outstanding item(s) remain open.");
        long unreviewedSchedules = supplementalRepository.findAll().stream().filter(s -> s.getValidationState()==ValidationState.ERROR).count();
        if(unreviewedSchedules>0) messages.add("ERROR: Supplemental schedules have validation errors.");
        if(!row.isBankReconciled()) messages.add("WARNING: Bank reconciliation has not been confirmed for this close.");
        if(!row.isSchedulesReviewed()) messages.add("WARNING: Asset/liability schedules have not been marked reviewed.");
        if(!row.isReportsGenerated()) messages.add("WARNING: Reports have not been marked generated.");
        if(messages.stream().noneMatch(m -> m.startsWith("ERROR")) && row.isBankReconciled() && row.isSchedulesReviewed() && row.isReportsGenerated()) row.setCloseStatus(PeriodCloseStatus.READY_TO_CLOSE);
        return SpreadsheetValidation.apply(row,messages);
    }
}
