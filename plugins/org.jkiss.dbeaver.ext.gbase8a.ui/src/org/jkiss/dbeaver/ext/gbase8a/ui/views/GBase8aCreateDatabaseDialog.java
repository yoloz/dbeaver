/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCharset;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCollation;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

/**
 * GBase8aCreateDatabaseDialog
 */
public class GBase8aCreateDatabaseDialog extends BaseDialog {
    public static final String DEFAULT_CHARSET_NAME = "utf8";
    private final GBase8aCatalog database;
    private String name;
    private GBase8aCharset charset;
    private GBase8aCollation collation;

    public GBase8aCreateDatabaseDialog(Shell parentShell, GBase8aCatalog database) {
        super(parentShell, GBase8aUIMessages.dialog_create_database_title, null);
        this.database = database;
    }

    @Override
    protected Composite createDialogArea(Composite parent) {
        final Composite composite = super.createDialogArea(parent);

        final Composite group = UIUtils.createComposite(composite, 2);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);

        final Text nameText = UIUtils.createLabelText(group, GBase8aUIMessages.dialog_create_database_database_name, "");
        nameText.addModifyListener(e -> {
            name = nameText.getText().trim();
            getButton(IDialogConstants.OK_ID).setEnabled(!name.isEmpty());
        });

        final Combo charsetCombo = UIUtils.createLabelCombo(group, GBase8aUIMessages.dialog_create_database_charset, SWT.BORDER | SWT.DROP_DOWN);
        for (GBase8aCharset cs : database.getDataSource().getCharsets()) {
            charsetCombo.add(cs.getName());
        }
        charset = database.getDataSource().getDefaultCharset();
        if (charset == null) {
            charset = database.getDataSource().getCharset(DEFAULT_CHARSET_NAME);
        }
        collation = database.getDataSource().getDefaultCollation();
        if (collation == null) {
            collation = charset.getDefaultCollation();
        }
        charsetCombo.setText(charset.getName());

        final Combo collationCombo = UIUtils.createLabelCombo(group, GBase8aUIMessages.dialog_create_database_collation, SWT.BORDER | SWT.DROP_DOWN);
        for (GBase8aCollation col : charset.getCollations()) {
            collationCombo.add(col.getName());
        }
        if (collation != null) {
            UIUtils.setComboSelection(collationCombo, collation.getName());
        }
        charsetCombo.addModifyListener(e -> {
            charset = database.getDataSource().getCharset(charsetCombo.getText());
            assert charset != null;

            collationCombo.removeAll();
            if (charset != null) {
                for (GBase8aCollation col : charset.getCollations()) {
                    collationCombo.add(col.getName());
                }
                collation = charset.getDefaultCollation();
            }
            if (collation != null) {
                UIUtils.setComboSelection(collationCombo, collation.getName());
            }
        });
        collationCombo.addModifyListener(e -> collation = charset == null ? null : charset.getCollation(collationCombo.getText()));

        return composite;
    }

    public String getName() {
        return name;
    }

    public GBase8aCharset getCharset() {
        return charset;
    }

    public GBase8aCollation getCollation() {
        return collation;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
}
