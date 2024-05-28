package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndexColumn;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLIndexManager;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;

import org.jkiss.utils.CommonUtils;
import java.util.Map;


public class GBase8aFullIndexManager extends SQLIndexManager<GBase8aTableFullIndex, GBase8aTable> implements DBPRefreshableObject {

    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aTableFullIndex> getObjectsCache(GBase8aTableFullIndex object) {
        return object.getTable().getContainer().getFullIndexCache();
    }

    protected GBase8aTableFullIndex createDatabaseObject(
            DBRProgressMonitor monitor,
            DBECommandContext context,
            final Object container,
            Object from,
            Map<String, Object> options
    ) {
//        return (GBase8aTableFullIndex) (new UITask<GBase8aTableFullIndex>() {
//            private GBase8aTableFullIndex runTask() {
//                GBase8aCreateFullTextIndexDlg dialog = new GBase8aCreateFullTextIndexDlg(Display.getCurrent().getActiveShell(), parent.getContainer().getVcName(), parent.getContainer().getName(), parent.getName(), (GBase8aDataSource) parent.getDataSource(), null, monitor);
//                if (dialog.open() != 0) {
//                    return null;
//                }
//                GBase8aTableFullIndex index = new GBase8aTableFullIndex(
//                        parent,
//                        false,
//                        null,
//                        DBSIndexType.OTHER,
//                        null,
//                        false);
//                String indexName = dialog.getIndexName();
//                if (indexName == null) {
//                    return null;
//                }
//                index.setName(DBObjectNameCaseTransformer.transformObjectName(index, indexName));
//                return index;
//            }
//        }).execute();
        GBase8aTable table = (GBase8aTable) container;
        return new GBase8aTableFullIndex(
                table,
                false,
                "INDEX",
                DBSIndexType.OTHER,
                null,
                false);
    }

    protected String getDropIndexPattern(GBase8aTableFullIndex index) {
        return "ALTER TABLE %TABLE% DROP INDEX \"%INDEX_SHORT%\"";
    }

    protected void appendIndexColumnModifiers(StringBuilder decl, DBSTableIndexColumn indexColumn) {
        String subPart = ((GBase8aTableFullIndexColumn) indexColumn).getSubPart();
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
}
