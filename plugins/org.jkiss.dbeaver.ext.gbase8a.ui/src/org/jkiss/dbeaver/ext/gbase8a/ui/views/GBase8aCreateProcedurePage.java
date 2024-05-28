package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.editors.object.struct.BaseObjectEditPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


public class GBase8aCreateProcedurePage extends BaseObjectEditPage {

    private final DBSObjectContainer container;
    private String name;
    private DBSProcedureType type;
    private final String createType;

    public GBase8aCreateProcedurePage(DBSObjectContainer container, String createType) {
        super(GBase8aMessages.dialog_struct_create_procedure_title);
        this.container = container;
        this.createType = createType;
    }

    @Override
    public DBSObject getObject() {
        return this.container;
    }

    protected Control createPageContents(Composite parent) {
        Composite propsGroup = new Composite(parent, 0);
        propsGroup.setLayout(new GridLayout(2, false));
        GridData gd = new GridData(768);
        propsGroup.setLayoutData(gd);

        UIUtils.createLabelText(propsGroup, GBase8aMessages.dialog_struct_create_procedure_label_container, DBUtils.getObjectFullName(this.container, DBPEvaluationContext.UI)).setEditable(false);
        final Text nameText = UIUtils.createLabelText(propsGroup, GBase8aMessages.dialog_struct_create_procedure_label_name, null);
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateProcedurePage.this.name = nameText.getText();
            }
        });
        final Combo typeCombo = UIUtils.createLabelCombo(propsGroup, GBase8aMessages.dialog_struct_create_procedure_combo_type, 12);
        typeCombo.add(DBSProcedureType.PROCEDURE.name());
        typeCombo.add(DBSProcedureType.FUNCTION.name());
        typeCombo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateProcedurePage.this.type = (typeCombo.getSelectionIndex() == 0) ? DBSProcedureType.PROCEDURE : DBSProcedureType.FUNCTION;
                nameText.setText((GBase8aCreateProcedurePage.this.type == DBSProcedureType.PROCEDURE) ? "NewProcedure" : "NewFunction");
            }
        });
        typeCombo.select(0);

        if (this.createType.equals(DBSProcedureType.PROCEDURE.name())) {
            typeCombo.select(0);
        } else {
            typeCombo.select(1);
        }
        typeCombo.setEnabled(false);

        propsGroup.setTabList(new Control[]{nameText, typeCombo});
        return propsGroup;
    }

    public DBSProcedureType getProcedureType() {
        return this.type;
    }

    public String getProcedureName() {
        return DBObjectNameCaseTransformer.transformName(this.container.getDataSource(), this.name);
    }
}