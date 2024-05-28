// package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
// import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
// import org.jkiss.dbeaver.model.DBIcon;
// import org.jkiss.dbeaver.model.DBPImage;
// import org.jkiss.dbeaver.ui.GBaseDataIcons;
// import org.jkiss.dbeaver.ui.UIUtils;
// import org.jkiss.dbeaver.ui.dialogs.DialogUtils;
// import org.jkiss.dbeaver.ui.dialogs.tools.AbstractToolWizard;
// import cn.gbase.utils.CommonUtils;
// import java.io.File;
// import org.eclipse.swt.events.MouseAdapter;
// import org.eclipse.swt.events.MouseEvent;
// import org.eclipse.swt.events.MouseListener;
// import org.eclipse.swt.events.SelectionAdapter;
// import org.eclipse.swt.events.SelectionEvent;
// import org.eclipse.swt.events.SelectionListener;
// import org.eclipse.swt.widgets.Button;
// import org.eclipse.swt.widgets.Combo;
// import org.eclipse.swt.widgets.Composite;
// import org.eclipse.swt.widgets.Control;
// import org.eclipse.swt.widgets.Group;
// import org.eclipse.swt.widgets.Text;
//
// public class GBase8aScriptExecuteWizardPageSettings extends GBase8aWizardPageSettings<GBase8aScriptExecuteWizard> {
//   private Text inputFileText;
//   private Combo logLevelCombo;
//
//   public GBase8aScriptExecuteWizardPageSettings(GBase8aScriptExecuteWizard wizard) {
//     super(wizard, wizard.isImport() ? GBase8aMessages.tools_script_execute_wizard_page_settings_import_configuration : GBase8aMessages.tools_script_execute_wizard_page_settings_script_configuration);
//     setTitle(wizard.isImport() ?
//         GBase8aMessages.tools_script_execute_wizard_page_settings_import_configuration :
//         GBase8aMessages.tools_script_execute_wizard_page_settings_script_configuration);
//     setDescription(wizard.isImport() ?
//         GBase8aMessages.tools_script_execute_wizard_page_settings_set_db_import_settings :
//         GBase8aMessages.tools_script_execute_wizard_page_settings_set_script_execution_settings);
//   }
//
//
//
//   public boolean isPageComplete() {
//     return (super.isPageComplete() && ((GBase8aScriptExecuteWizard)this.wizard).getInputFile() != null);
//   }
//
//
//
//   public void createControl(Composite parent) {
//     Composite composite = UIUtils.createPlaceholder(parent, 1);
//
//     Group outputGroup = UIUtils.createControlGroup(
//         composite, GBase8aMessages.tools_script_execute_wizard_page_settings_group_input, 3, 768, 0);
//     this.inputFileText = UIUtils.createLabelText(
//         (Composite)outputGroup, GBase8aMessages.tools_script_execute_wizard_page_settings_label_input_file, "", 2056);
//     this.inputFileText.addMouseListener((MouseListener)new MouseAdapter()
//         {
//           public void mouseUp(MouseEvent e)
//           {
//             GBase8aScriptExecuteWizardPageSettings.this.chooseInputFile();
//           }
//         });
//     Button browseButton = new Button((Composite)outputGroup, 8);
//     browseButton.setImage(GBaseDataIcons.getImage((DBPImage)DBIcon.TREE_FOLDER));
//     browseButton.addSelectionListener((SelectionListener)new SelectionAdapter()
//         {
//           public void widgetSelected(SelectionEvent e)
//           {
//             GBase8aScriptExecuteWizardPageSettings.this.chooseInputFile();
//           }
//         });
//
//     if (((GBase8aScriptExecuteWizard)this.wizard).getInputFile() != null) {
//       this.inputFileText.setText(((GBase8aScriptExecuteWizard)this.wizard).getInputFile().getName());
//     }
//
//     Group settingsGroup = UIUtils.createControlGroup(
//         composite, GBase8aMessages.tools_script_execute_wizard_page_settings_group_settings, 2, 32, 0);
//     this.logLevelCombo = UIUtils.createLabelCombo(
//         (Composite)settingsGroup, GBase8aMessages.tools_script_execute_wizard_page_settings_label_log_level, 12); byte b; int i; GBase8aScriptExecuteWizard.LogLevel[] arrayOfLogLevel;
//     for (i = (arrayOfLogLevel = GBase8aScriptExecuteWizard.LogLevel.values()).length, b = 0; b < i; ) { GBase8aScriptExecuteWizard.LogLevel logLevel = arrayOfLogLevel[b];
//       this.logLevelCombo.add(logLevel.name()); b++; }
//
//     this.logLevelCombo.select(((GBase8aScriptExecuteWizard)this.wizard).getLogLevel().ordinal());
//     this.logLevelCombo.addSelectionListener((SelectionListener)new SelectionAdapter()
//         {
//           public void widgetSelected(SelectionEvent e)
//           {
//             ((GBase8aScriptExecuteWizard)GBase8aScriptExecuteWizardPageSettings.this.wizard).setLogLevel(GBase8aScriptExecuteWizard.LogLevel.valueOf(GBase8aScriptExecuteWizardPageSettings.this.logLevelCombo.getText()));
//           }
//         });
//
//     createSecurityGroup(composite);
//
//     setControl((Control)composite);
//   }
//
//
//
//
//   private void chooseInputFile() {
//     File file = DialogUtils.openFile(getShell(), new String[] { "*.sql", "*.txt", "*.*" });
//     if (file != null) {
//       this.inputFileText.setText(file.getAbsolutePath());
//     }
//     updateState();
//   }
//
//
//   private void updateState() {
//     String fileName = this.inputFileText.getText();
//     ((GBase8aScriptExecuteWizard)this.wizard).setInputFile(CommonUtils.isEmpty(fileName) ? null : new File(fileName));
//
//     getContainer().updateButtons();
//   }
// }
