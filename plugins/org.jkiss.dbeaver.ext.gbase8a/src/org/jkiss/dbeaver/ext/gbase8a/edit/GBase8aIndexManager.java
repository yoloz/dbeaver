package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableIndex;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableIndexColumn;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLIndexManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;
import org.jkiss.utils.CommonUtils;

import java.util.Map;


public class GBase8aIndexManager extends SQLIndexManager<GBase8aTableIndex, GBase8aTable> implements DBPRefreshableObject {
    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aTableIndex> getObjectsCache(GBase8aTableIndex object) {
        return object.getTable().getContainer().getIndexCache();
    }

    protected String getDropIndexPattern(GBase8aTableIndex index) {
        return "ALTER TABLE %TABLE% DROP INDEX \"%INDEX_SHORT%\"";
    }

    protected void appendIndexColumnModifiers(StringBuilder decl, DBSTableIndexColumn indexColumn) {
        String subPart = ((GBase8aTableIndexColumn) indexColumn).getSubPart();
        if (!CommonUtils.isEmpty(subPart)) {
            decl.append(" (").append(subPart).append(")");
        }
        if (!indexColumn.isAscending()) {
            decl.append(" DESC");
        }
    }

    public String getDescription() {
        return null;
    }


    public DBSObject getParentObject() {
        return null;
    }


    public DBPDataSource getDataSource() {
        return null;
    }


    public String getName() {
        return null;
    }


    public boolean isPersisted() {
        return false;
    }


    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    @Override
    protected GBase8aTableIndex createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aTable table = (GBase8aTable) container;
        return new GBase8aTableIndex(
                table,
                false,
                "INDEX",
                DBSIndexType.HASHED,
                null,
                false);
    }
}
