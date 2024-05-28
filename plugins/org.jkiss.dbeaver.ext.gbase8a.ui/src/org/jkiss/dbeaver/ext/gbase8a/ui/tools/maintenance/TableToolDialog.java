package org.jkiss.dbeaver.ext.gbase8a.ui.tools.maintenance;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.ui.editors.sql.dialogs.GenerateMultiSQLDialog;
import org.jkiss.dbeaver.ui.editors.sql.dialogs.SQLScriptProgressListener;
import org.jkiss.dbeaver.ui.editors.sql.dialogs.SQLScriptStatusDialog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPartSite;
import org.jkiss.utils.CommonUtils;


public abstract class TableToolDialog
        extends GenerateMultiSQLDialog<GBase8aTable> {
    public TableToolDialog(IWorkbenchPartSite partSite, String title, Collection<GBase8aTable> objects) {
        super(partSite, title, objects, true);
    }


    protected SQLScriptProgressListener<GBase8aTable> getScriptListener() {
        return new SQLScriptStatusDialog<>(getTitle() + " progress", null) {
            protected void createStatusColumns(Tree objectTree) {
                TreeColumn msgColumn = new TreeColumn(objectTree, 0);
                msgColumn.setText("Message");
            }


            public void processObjectResults(@NotNull GBase8aTable object, @Nullable DBCStatement statement, @Nullable DBCResultSet resultSet) throws DBCException {
                if (resultSet == null) {
                    return;
                }
                Map<String, String> statusMap = new LinkedHashMap<String, String>();
                while (resultSet.nextRow()) {
                    statusMap.put(
                            CommonUtils.toString(resultSet.getAttributeValue("Msg_type")),
                            CommonUtils.toString(resultSet.getAttributeValue("Msg_text")));
                }
                TreeItem treeItem = getTreeItem(object);
                if (treeItem != null && !statusMap.isEmpty())
                    if (statusMap.size() == 1) {
                        treeItem.setText(1, statusMap.values().iterator().next());
                    } else {
                        String statusText = statusMap.get("status");
                        if (!CommonUtils.isEmpty(statusText)) {
                            treeItem.setText(1, statusText);
                        }
                        for (Map.Entry<String, String> status : statusMap.entrySet()) {
                            if (!status.getKey().equals("status")) {
                                TreeItem subItem = new TreeItem(treeItem, 0);
                                subItem.setText(0, status.getKey());
                                subItem.setText(1, status.getValue());
                            }
                        }
                        treeItem.setExpanded(true);
                    }
            }
        };
    }
}