package org.example.npbk.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.npbk.db.Database;
import org.example.npbk.model.LookupOption;
import org.example.npbk.model.TransactionEditorViewModel;
import org.example.npbk.service.NormalizedTransactionService;
import org.example.npbk.validation.NormalizedTransactionValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NormalizedTransactionRepositoryTest
{
    private Database database;
    private AccountingLookupRepository lookups;
    private NormalizedTransactionService service;

    @BeforeEach
    void setUp() throws Exception
    {
        database = new Database("jdbc:h2:mem:normalized_transaction_test;DB_CLOSE_DELAY=-1");
        database.initialize();
        lookups = new AccountingLookupRepository(database);
        service = new NormalizedTransactionService(
            new NormalizedTransactionRepository(database),
            new NormalizedTransactionValidationService());
    }

    @Test
    void savesAndReloadsHeaderAndBalancedLines() throws Exception
    {
        TransactionEditorViewModel model = TransactionEditorViewModel.blank();
        model.setTransactionDate(LocalDate.of(2026, 2, 1));
        model.setReferenceNumber("CHK-1001");
        model.setLegalName("The Scholar's Abode");
        model.setDescription("Meeting space for Q1");
        model.setBankAccount(findExact(lookups.bankAccounts(), "Checking"));

        LookupOption generalFund = findExact(lookups.funds(), "General Fund");
        LookupOption meetingSpace = findExact(lookups.budgetCategories(), "Meeting Space");
        model.getLines().get(0).setAccount(findEnding(lookups.accounts(), "Undep. & non-interest cash"));
        model.getLines().get(0).setFund(generalFund);
        model.getLines().get(0).setBudgetCategory(meetingSpace);
        model.getLines().get(0).setCreditAmount(new BigDecimal("300.00"));

        model.getLines().get(1).setAccount(findEnding(lookups.accounts(), "21b Occupancy - Activity Rel"));
        model.getLines().get(1).setFund(generalFund);
        model.getLines().get(1).setBudgetCategory(meetingSpace);
        model.getLines().get(1).setDebitAmount(new BigDecimal("300.00"));

        NormalizedTransactionService.SaveResult saved = service.save(model);

        assertTrue(saved.saved(), () -> String.join("\n", saved.messages()));
        assertNotNull(model.getId());

        TransactionEditorViewModel loaded = service.load(model.getId());
        assertEquals(LocalDate.of(2026, 2, 1), loaded.getTransactionDate());
        assertEquals("CHK-1001", loaded.getReferenceNumber());
        assertEquals("The Scholar's Abode", loaded.getLegalName());
        assertEquals(2, loaded.getLines().size());
        assertEquals(new BigDecimal("300.00"), loaded.totalDebits());
        assertEquals(new BigDecimal("300.00"), loaded.totalCredits());

        try (var conn = database.getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) FROM transaction_lines WHERE transaction_id = ?"))
        {
            ps.setLong(1, model.getId());
            try (var rs = ps.executeQuery())
            {
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }
        }
    }

    private LookupOption findExact(List<LookupOption> options, String name)
    {
        return options.stream()
            .filter(option -> name.equals(option.displayName()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Lookup not found: " + name));
    }

    private LookupOption findEnding(List<LookupOption> options, String name)
    {
        return options.stream()
            .filter(option -> option.displayName().endsWith(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Lookup not found: " + name));
    }
}
