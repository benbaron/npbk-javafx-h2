package org.example.npbk.model;

/**
 * Stable database identifier plus the user-facing display text used by editors.
 */
public record LookupOption(Long id, String displayName)
{
    public LookupOption
    {
        displayName = displayName == null ? "" : displayName;
    }

    public boolean isPresent()
    {
        return id != null;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
