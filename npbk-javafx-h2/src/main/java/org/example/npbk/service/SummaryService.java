package org.example.npbk.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.npbk.model.*;
import org.example.npbk.repo.*;

public class SummaryService {
    private final SummaryRepository summaryRepository;
    private final TransactionRepository transactionRepository;
    private final OutstandingRepository outstandingRepository;
    public SummaryService(SummaryRepository summaryRepository, TransactionRepository transactionRepository, OutstandingRepository outstandingRepository) {
        this.summaryRepository = summaryRepository; this.transactionRepository = transactionRepository; this.outstandingRepository = outstandingRepository;
    }
    public SummarySettingsViewModel load() { SummarySettingsViewModel m = summaryRepository.load(); refreshTotals(m); validate(m); return m; }
    public void save(SummarySettingsViewModel model) { validate(model); summaryRepository.save(model); }
    public void refreshTotals(SummarySettingsViewModel m) {
        m.setBankTotal(transactionRepository.sumBankEffect()); m.setBudgetTotal(transactionRepository.sumBudgetEffect());
        BigDecimal open = outstandingRepository.findAll().stream().filter(r -> r.getStatus() == OutstandingStatus.OPEN).map(r -> r.getDirection() == OutstandingDirection.INCOMING ? r.getAmount() : r.getAmount().negate()).reduce(BigDecimal.ZERO, BigDecimal::add);
        m.setOpenOutstandingTotal(open);
    }
    public List<String> validate(SummarySettingsViewModel m) {
        List<String> messages = new java.util.ArrayList<>();
        SpreadsheetValidation.require(messages, "Organization name", m.getOrganizationName());
        SpreadsheetValidation.require(messages, "Report period start", m.getPeriodStart());
        SpreadsheetValidation.require(messages, "Report period end", m.getPeriodEnd());
        SpreadsheetValidation.startBeforeEnd(messages, m.getPeriodStart(), m.getPeriodEnd());
        return SpreadsheetValidation.apply(m, messages);
    }
}
