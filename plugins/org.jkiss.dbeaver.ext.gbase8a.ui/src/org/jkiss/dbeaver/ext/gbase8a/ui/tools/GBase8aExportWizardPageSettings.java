//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ui.dialogs.DialogUtils;
//import org.jkiss.dbeaver.ui.dialogs.tools.AbstractToolWizard;
//import cn.gbase.utils.CommonUtils;
//
//import java.io.File;
//
//import org.eclipse.jface.fieldassist.IContentProposalProvider;
//import org.eclipse.jface.fieldassist.IControlContentAdapter;
//import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
//import org.eclipse.jface.fieldassist.TextContentAdapter;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Text;
//
//class GBase8aExportWizardPageSettings
//        extends GBase8aWizardPageSettings<GBase8aExportWizard> {
//    private Text outputFolderText;
//    private Text outputFileText;
//    private Combo methodCombo;
//    private Button noCreateStatementsCheck;
//    private Button addDropStatementsCheck;
//
//    protected GBase8aExportWizardPageSettings(GBase8aExportWizard wizard) {
//        super(wizard, GBase8aMessages.tools_db_export_wizard_page_settings_page_name);
//        setTitle(GBase8aMessages.tools_db_export_wizard_page_settings_page_name);
//        setDescription(GBase8aMessages.tools_db_export_wizard_page_settings_page_description);
//    }
//
//    private Button disableKeysCheck;
//    private Button extendedInsertsCheck;
//    private Button dumpEventsCheck;
//    private Button commentsCheck;
//    private Button removeDefiner;
//    private Button binaryInHex;
//
//    public boolean isPageComplete() {
//        return (super.isPageComplete() && ((GBase8aExportWizard) this.wizard).getOutputFolder() != null);
//    }
//
//
//    public void createControl(Composite parent) {
//        Composite composite = UIUtils.createPlaceholder(parent, 1);
//
//        SelectionAdapter selectionAdapter = new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aExportWizardPageSettings.this.updateState();
//            }
//        };
//
//        Group methodGroup = UIUtils.createControlGroup(composite, GBase8aMessages.tools_db_export_wizard_page_settings_group_exe_method, 1, 768, 0);
//        this.methodCombo = new Combo((Composite) methodGroup, 12);
//        this.methodCombo.setLayoutData(new GridData(768));
//        this.methodCombo.add(GBase8aMessages.tools_db_export_wizard_page_settings_combo_item_online_backup);
//        this.methodCombo.add(GBase8aMessages.tools_db_export_wizard_page_settings_combo_item_lock_tables);
//        this.methodCombo.add(GBase8aMessages.tools_db_export_wizard_page_settings_combo_item_normal);
//        this.methodCombo.select(((GBase8aExportWizard) this.wizard).method.ordinal());
//        this.methodCombo.addSelectionListener((SelectionListener) selectionAdapter);
//
//        Group settingsGroup = UIUtils.createControlGroup(composite, GBase8aMessages.tools_db_export_wizard_page_settings_group_settings, 3, 768, 0);
//        this.noCreateStatementsCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_no_create, ((GBase8aExportWizard) this.wizard).noCreateStatements);
//        this.noCreateStatementsCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.addDropStatementsCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_add_drop, ((GBase8aExportWizard) this.wizard).addDropStatements);
//        this.addDropStatementsCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.disableKeysCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_disable_keys, ((GBase8aExportWizard) this.wizard).disableKeys);
//        this.disableKeysCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.extendedInsertsCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_ext_inserts, ((GBase8aExportWizard) this.wizard).extendedInserts);
//        this.extendedInsertsCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.dumpEventsCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_dump_events, ((GBase8aExportWizard) this.wizard).dumpEvents);
//        this.dumpEventsCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.commentsCheck = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_addnl_comments, ((GBase8aExportWizard) this.wizard).comments);
//        this.commentsCheck.addSelectionListener((SelectionListener) selectionAdapter);
//        this.removeDefiner = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_remove_definer, ((GBase8aExportWizard) this.wizard).removeDefiner);
//        this.removeDefiner.addSelectionListener((SelectionListener) selectionAdapter);
//        this.binaryInHex = UIUtils.createCheckbox((Composite) settingsGroup, GBase8aMessages.tools_db_export_wizard_page_settings_checkbox_binary_hex, ((GBase8aExportWizard) this.wizard).binariesInHex);
//        this.binaryInHex.addSelectionListener((SelectionListener) selectionAdapter);
//
//        Group outputGroup = UIUtils.createControlGroup(composite, GBase8aMessages.tools_db_export_wizard_page_settings_group_output, 2, 768, 0);
//        this.outputFolderText = DialogUtils.createOutputFolderChooser((Composite) outputGroup, GBase8aMessages.tools_db_export_wizard_page_settings_label_out_text, new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                GBase8aExportWizardPageSettings.this.updateState();
//            }
//        });
//        this.outputFileText = UIUtils.createLabelText((Composite) outputGroup, "File name pattern", ((GBase8aExportWizard) this.wizard).getOutputFilePattern());
//        UIUtils.setContentProposalToolTip((Control) this.outputFileText, "Output file name pattern", new String[]{"host", "database", "table", "timestamp"});
//        UIUtils.installContentProposal(
//                (Control) this.outputFileText,
//                (IControlContentAdapter) new TextContentAdapter(),
//                (IContentProposalProvider) new SimpleContentProposalProvider(new String[]{"${host}", "${database}", "${table}", "${timestamp}"}));
//        this.outputFileText.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                ((GBase8aExportWizard) GBase8aExportWizardPageSettings.this.wizard).setOutputFilePattern(GBase8aExportWizardPageSettings.this.outputFileText.getText());
//            }
//        });
//        if (((GBase8aExportWizard) this.wizard).getOutputFolder() != null) {
//            this.outputFolderText.setText(((GBase8aExportWizard) this.wizard).getOutputFolder().getAbsolutePath());
//        }
//
//        createSecurityGroup(composite);
//
//        setControl((Control) composite);
//    }
//
//
//    private void updateState() {
//        String fileName = this.outputFolderText.getText();
//        ((GBase8aExportWizard) this.wizard).setOutputFolder(CommonUtils.isEmpty(fileName) ? null : new File(fileName));
//        ((GBase8aExportWizard) this.wizard).setOutputFilePattern(this.outputFileText.getText());
//        switch (this.methodCombo.getSelectionIndex()) {
//            case 0:
//                ((GBase8aExportWizard) this.wizard).method = GBase8aExportWizard.DumpMethod.ONLINE;
//                break;
//            case 1:
//                ((GBase8aExportWizard) this.wizard).method = GBase8aExportWizard.DumpMethod.LOCK_ALL_TABLES;
//                break;
//            default:
//                ((GBase8aExportWizard) this.wizard).method = GBase8aExportWizard.DumpMethod.NORMAL;
//                break;
//        }
//
//        ((GBase8aExportWizard) this.wizard).noCreateStatements = this.noCreateStatementsCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).addDropStatements = this.addDropStatementsCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).disableKeys = this.disableKeysCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).extendedInserts = this.extendedInsertsCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).dumpEvents = this.dumpEventsCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).comments = this.commentsCheck.getSelection();
//        ((GBase8aExportWizard) this.wizard).removeDefiner = this.removeDefiner.getSelection();
//        ((GBase8aExportWizard) this.wizard).binariesInHex = this.binaryInHex.getSelection();
//
//        getContainer().updateButtons();
//    }
//}
