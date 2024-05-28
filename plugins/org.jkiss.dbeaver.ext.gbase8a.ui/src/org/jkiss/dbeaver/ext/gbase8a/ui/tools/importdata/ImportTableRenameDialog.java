//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.model.DBPImage;
//import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Layout;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//
//
//public class ImportTableRenameDialog  extends BaseDialog {
//    private Composite container;
//    private Text tablenameText;
//    private String tableName;
//
//    public ImportTableRenameDialog(Shell parentShell, String title, DBPImage icon, String tableName) {
//        super(parentShell, GBase8aMessages.import_properties_shape_type_dialog_name, icon);
//        this.tableName = tableName;
//    }
//
//
//    protected Composite createDialogArea(Composite parent) {
//        this.container = new Composite(parent, 0);
//        this.container.setLayout((Layout) new GridLayout(1, false));
//
//
//        Label tipLabel = new Label(this.container, 0);
//        tipLabel.setLayoutData(new GridData(4, 16777216, false, false, 1,
//                1));
//        tipLabel.setText(GBase8aMessages.import_properties_shape_type_dialog_text);
//
//        this.tablenameText = new Text(this.container, 2048);
//        GridData grid_text = new GridData(4, 4, true, false, 1, 1);
//        grid_text.widthHint = 250;
//        this.tablenameText.setLayoutData(grid_text);
//        this.tablenameText.setText(this.tableName);
//
//        return this.container;
//    }
//
//
//    protected void createButtonsForButtonBar(Composite parent) {
//        super.createButtonsForButtonBar(parent);
//    }
//
//
//    protected void buttonPressed(int buttonId) {
//        if (buttonId == 0) {
//            if (this.tablenameText.getText().trim().equals("")) {
//                MessageDialog.openError(this.container.getShell(), "Error", "������Ϊ�ա�");
//                return;
//            }
//            this.tableName = this.tablenameText.getText();
//        }
//        super.buttonPressed(buttonId);
//    }
//
//    public String getTableName() {
//        return this.tableName;
//    }
//}
