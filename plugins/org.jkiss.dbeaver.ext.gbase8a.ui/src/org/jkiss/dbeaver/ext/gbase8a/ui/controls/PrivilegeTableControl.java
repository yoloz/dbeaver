package org.jkiss.dbeaver.ext.gbase8a.ui.controls;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class PrivilegeTableControl extends Composite {
    private Table privTable;

    public PrivilegeTableControl(Composite parent, String title) {
        super(parent, 0);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        this.setLayout(gl);
        Composite privsGroup = UIUtils.createControlGroup(this, title, 1, 1808, 0);
        GridData gd = (GridData) privsGroup.getLayoutData();
        gd.horizontalSpan = 2;
        this.privTable = new Table(privsGroup, 2852);
        this.privTable.setHeaderVisible(true);
        gd = new GridData(1808);
        gd.minimumWidth = 300;
        this.privTable.setLayoutData(gd);
        this.privTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == 32) {
                    TableItem item = (TableItem) e.item;
                    PrivilegeTableControl.this.notifyPrivilegeCheck((GBase8aPrivilege) item.getData(), item.getChecked());
                }

            }
        });
        UIUtils.createTableColumn(this.privTable, 16384, GBase8aMessages.privilege_table_control_privilege);
        UIUtils.createTableColumn(this.privTable, 16384, GBase8aMessages.privilege_table_control_description);
        UIUtils.packColumns(this.privTable);
        Composite buttonsPanel = UIUtils.createPlaceholder(privsGroup, 3);
        buttonsPanel.setLayoutData(new GridData(768));
        Button checkButton = UIUtils.createPushButton(buttonsPanel, GBase8aMessages.privilege_table_control_checkall, (Image) null);
        checkButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TableItem[] var5;
                int var4 = (var5 = PrivilegeTableControl.this.privTable.getItems()).length;

                for (int var3 = 0; var3 < var4; ++var3) {
                    TableItem item = var5[var3];
                    if (!item.getChecked()) {
                        item.setChecked(true);
                        PrivilegeTableControl.this.notifyPrivilegeCheck((GBase8aPrivilege) item.getData(), true);
                    }
                }

            }
        });
        Button clearButton = UIUtils.createPushButton(buttonsPanel, GBase8aMessages.privilege_table_control_clearall, (Image) null);
        clearButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TableItem[] var5;
                int var4 = (var5 = PrivilegeTableControl.this.privTable.getItems()).length;

                for (int var3 = 0; var3 < var4; ++var3) {
                    TableItem item = var5[var3];
                    if (item.getChecked()) {
                        item.setChecked(false);
                        PrivilegeTableControl.this.notifyPrivilegeCheck((GBase8aPrivilege) item.getData(), false);
                    }
                }

            }
        });
    }

    private void notifyPrivilegeCheck(GBase8aPrivilege privilege, boolean checked) {
        Event event = new Event();
        event.detail = checked ? 1 : 0;
        event.widget = this;
        event.data = privilege;
        super.notifyListeners(24, event);
    }

    public void fillPrivileges(Collection<GBase8aPrivilege> privs) {
        if (!this.privTable.isDisposed()) {
            this.privTable.removeAll();
            Iterator var3 = privs.iterator();

            while (var3.hasNext()) {
                GBase8aPrivilege priv = (GBase8aPrivilege) var3.next();
                TableItem item = new TableItem(this.privTable, 0);
                item.setText(0, priv.getName());
                item.setText(1, priv.getDescription());
                item.setData(priv);
            }

            UIUtils.packColumns(this.privTable);
        }
    }

    public void fillGrants(List<GBase8aGrant> grants) {
        if (grants != null) {
            TableItem[] var5;
            int var4 = (var5 = this.privTable.getItems()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                TableItem item = var5[var3];
                GBase8aPrivilege privilege = (GBase8aPrivilege) item.getData();
                boolean checked = false;
                Iterator var9 = grants.iterator();

                label33:
                {
                    GBase8aGrant grant;
                    do {
                        if (!var9.hasNext()) {
                            break label33;
                        }

                        grant = (GBase8aGrant) var9.next();
                    } while (!grant.isAllPrivileges() && !grant.getPrivileges().contains(privilege) && (!grant.isGrantOption() || !privilege.isGrantOption()));

                    checked = true;
                }

                item.setChecked(checked);
            }

        }
    }

    public void checkPrivilege(GBase8aPrivilege privilege, boolean grant) {
        TableItem[] var6;
        int var5 = (var6 = this.privTable.getItems()).length;

        for (int var4 = 0; var4 < var5; ++var4) {
            TableItem item = var6[var4];
            if (item.getData() == privilege) {
                item.setChecked(grant);
                break;
            }
        }

    }
}
