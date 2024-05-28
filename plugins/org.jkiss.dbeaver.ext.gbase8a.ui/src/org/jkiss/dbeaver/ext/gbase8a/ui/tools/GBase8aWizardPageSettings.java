//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
//import org.jkiss.dbeaver.runtime.encode.EncryptionException;
//import org.jkiss.dbeaver.runtime.encode.SecuredPasswordEncrypter;
//import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeToolWizardPage;
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ui.dialogs.BaseAuthDialog;
//import org.jkiss.dbeaver.ui.dialogs.tools.AbstractToolWizard;
//import org.jkiss.dbeaver.ui.dialogs.tools.AbstractToolWizardPage;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Label;
//
//public abstract class GBase8aWizardPageSettings extends AbstractNativeToolWizardPage<GBase8aScriptExecuteWizard> {
//
//    public GBase8aWizardPageSettings(GBase8aScriptExecuteWizard wizard, String title) {
//        super(wizard, title);
//    }
//
//
//    public void createSecurityGroup(Composite parent) {
//        try {
//            final SecuredPasswordEncrypter encrypter = new SecuredPasswordEncrypter();
//            final DBPConnectionConfiguration connectionInfo = this.wizard.getConnectionInfo();
//            final String authProperty = "@gbasedata--auth-" + this.wizard.getObjectsName() + "@";
//            String authUser = null;
//            String authPassword = null;
//
//            String authValue = connectionInfo.getProviderProperty(authProperty);
//            if (authValue != null) {
//                String authCredentials = encrypter.decrypt(authValue);
//                int divPos = authCredentials.indexOf(':');
//                if (divPos != -1) {
//                    authUser = authCredentials.substring(0, divPos);
//                    authPassword = authCredentials.substring(divPos + 1);
//                }
//            }
//
//
//            this.wizard.setToolUserName((authUser == null) ? connectionInfo.getUserName() : authUser);
//            this.wizard.setToolUserPassword((authPassword == null) ? connectionInfo.getUserPassword() : authPassword);
//            final boolean savePassword = (authUser != null);
//            Group securityGroup = UIUtils.createControlGroup(
//                    parent, "Security", 2, 32, 0);
//            Label infoLabel = new Label((Composite) securityGroup, 0);
//            infoLabel.setText("Override user credentials (" + this.wizard.getConnectionInfo().getUserName() +
//                    ") for objects '" + this.wizard.getObjectsName() + "'.\nExternal tools like 'gbasedump' may require different set of permissions.");
//            GridData gd = new GridData(768);
//            gd.horizontalSpan = 2;
//            infoLabel.setLayoutData(gd);
//            Button authButton = new Button((Composite) securityGroup, 8);
//            authButton.setText("Authentication");
//            authButton.addSelectionListener((SelectionListener) new SelectionAdapter() {
//                public void widgetSelected(SelectionEvent e) {
//                    BaseAuthDialog authDialog = new BaseAuthDialog(GBase8aWizardPageSettings.this.getShell(), "Authentication", false);
//                    authDialog.setUserName(GBase8aWizardPageSettings.this.wizard.getToolUserName());
//                    authDialog.setUserPassword(GBase8aWizardPageSettings.this.wizard.getToolUserPassword());
//                    authDialog.setSavePassword(savePassword);
//                    if (authDialog.open() == 0) {
//                        GBase8aWizardPageSettings.this.wizard.setToolUserName(authDialog.getUserName());
//                        GBase8aWizardPageSettings.this.wizard.setToolUserPassword(authDialog.getUserPassword());
//                        if (authDialog.isSavePassword()) {
//                            try {
//                                connectionInfo.setProviderProperty(
//                                        authProperty,
//                                        encrypter.encrypt(String.valueOf(GBase8aWizardPageSettings.this.wizard.getToolUserName()) + ':' + GBase8aWizardPageSettings.this.wizard.getToolUserPassword()));
//                            } catch (EncryptionException encryptionException) {
//                            }
//                        }
//                    }
//                }
//            });
//
//            Button resetButton = new Button((Composite) securityGroup, 8);
//            resetButton.setText("Reset to default");
//            resetButton.addSelectionListener((SelectionListener) new SelectionAdapter() {
//                public void widgetSelected(SelectionEvent e) {
//                    connectionInfo.getProperties().remove(authProperty);
//                    GBase8aWizardPageSettings.this.wizard.setToolUserName(connectionInfo.getUserName());
//                    GBase8aWizardPageSettings.this.wizard.setToolUserPassword(connectionInfo.getUserPassword());
//                }
//            });
//        } catch (EncryptionException encryptionException) {
//        }
//    }
//}
