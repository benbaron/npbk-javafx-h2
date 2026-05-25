package org.example.npbk.ui;

import org.example.npbk.db.Database;

/** Read-only table browser for reference/admin records such as Chart of Accounts. */
public class ReferenceTablePanel extends QueryTablePanel {
    public ReferenceTablePanel(Database database, String title, String tableName) {
        super(database, title, tableName);
    }
}
