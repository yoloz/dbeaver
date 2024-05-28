package org.jkiss.dbeaver.ext.gbase8a.ui.tools;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;

import java.util.Collection;


public class GBase8aDatabaseExportInfo {
    @NotNull
    private GBase8aCatalog database;
    @Nullable
    private Collection<GBase8aTableBase> tables;

    public GBase8aDatabaseExportInfo(@NotNull GBase8aCatalog database, @Nullable Collection<GBase8aTableBase> tables) {
        this.database = database;
        this.tables = tables;
    }

    @NotNull
    public GBase8aCatalog getDatabase() {
        return this.database;
    }

    @Nullable
    public Collection<GBase8aTableBase> getTables() {
        return this.tables;
    }


    public String toString() {
        return String.valueOf(this.database.getName()) + " " + this.tables;
    }
}
