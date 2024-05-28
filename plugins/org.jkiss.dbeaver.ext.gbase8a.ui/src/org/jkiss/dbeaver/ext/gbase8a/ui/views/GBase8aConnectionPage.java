package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIActivator;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.ui.IDialogPageProvider;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.connection.ClientHomesSelector;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionPageAbstract;
import org.jkiss.dbeaver.ui.dialogs.connection.DriverPropertiesDialogPage;
import org.jkiss.utils.CommonUtils;

import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class GBase8aConnectionPage extends ConnectionPageAbstract implements IDialogPageProvider {
    private Text hostText;
    private Text portText;
    private Text dbText;
    private Text usernameText;
    private Text passwordText;
    private ClientHomesSelector homesSelector;
    private Button isSpecifiedVC;
    private Text vcName;
    private boolean activated = false;
    private static final ImageDescriptor GBASE8A_LOGO_IMG = GBase8aUIActivator.getImageDescriptor("icons/gbase8a_logo.png");
    private static final ImageDescriptor DB_LOGO_IMG = GBase8aUIActivator.getImageDescriptor("icons/db_logo.png");


    public void dispose() {
        super.dispose();
    }

    public void createControl(Composite composite) {
        ModifyListener textListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (GBase8aConnectionPage.this.activated) {
                    GBase8aConnectionPage.this.saveSettings(GBase8aConnectionPage.this.site.getActiveDataSource());
                    GBase8aConnectionPage.this.site.updateButtons();
                }
            }
        };
        int fontHeight = UIUtils.getFontHeight(composite);

        Composite addrGroup = UIUtils.createPlaceholder(composite, 2);
        GridLayout gl = new GridLayout(2, false);
        addrGroup.setLayout(gl);
        GridData gd = new GridData(1808);
        addrGroup.setLayoutData(gd);

        Label hostLabel = UIUtils.createControlLabel(addrGroup, GBase8aMessages.dialog_connection_host);
        hostLabel.setLayoutData(new GridData(128));

        this.hostText = new Text(addrGroup, 2048);
        this.hostText.setLayoutData(new GridData(768));
        this.hostText.addModifyListener(textListener);

        Label portLabel = UIUtils.createControlLabel(addrGroup, GBase8aMessages.dialog_connection_port);
        portLabel.setLayoutData(new GridData(128));

        this.portText = new Text(addrGroup, 2048);
        gd = new GridData(32);
        gd.widthHint = fontHeight * 10;
        this.portText.setLayoutData(gd);
        this.portText.addVerifyListener(UIUtils.getIntegerVerifyListener(Locale.getDefault()));
        this.portText.addModifyListener(textListener);

        Label dbLabel = UIUtils.createControlLabel(addrGroup, GBase8aMessages.dialog_connection_database);
        dbLabel.setLayoutData(new GridData(128));

        this.dbText = new Text(addrGroup, 2048);
        this.dbText.setLayoutData(new GridData(768));
        this.dbText.addModifyListener(textListener);

        Label usernameLabel = UIUtils.createControlLabel(addrGroup, GBase8aMessages.dialog_connection_user_name);
        usernameLabel.setLayoutData(new GridData(128));

        this.usernameText = new Text(addrGroup, 2048);
        gd = new GridData(32);
        gd.widthHint = fontHeight * 20;
        this.usernameText.setLayoutData(gd);
        this.usernameText.addModifyListener(textListener);

        Label passwordLabel = UIUtils.createControlLabel(addrGroup, GBase8aMessages.dialog_connection_password);
        passwordLabel.setLayoutData(new GridData(128));

        this.passwordText = new Text(addrGroup, 4196352);
        gd = new GridData(32);
        gd.widthHint = fontHeight * 20;
        this.passwordText.setLayoutData(gd);
        this.passwordText.addModifyListener(textListener);


        Composite clientPanel = UIUtils.createPlaceholder(addrGroup, 1);
        gd = new GridData(768);
        gd.horizontalSpan = 2;
        clientPanel.setLayoutData(gd);

        UIUtils.createHorizontalLine(clientPanel);

        this.homesSelector = new ClientHomesSelector(clientPanel, GBase8aMessages.dialog_connection_local_client);
        gd = new GridData(800);
        this.homesSelector.getPanel().setLayoutData(gd);


        this.isSpecifiedVC = new Button(addrGroup, 32);
        this.isSpecifiedVC.setText(GBase8aMessages.dialog_connection_is_specified_vc);
        this.isSpecifiedVC.addSelectionListener(new SelectionAdapter() {


            public void widgetSelected(SelectionEvent e) {
                GBase8aConnectionPage.this.vcName.setEnabled(GBase8aConnectionPage.this.isSpecifiedVC.getSelection());
                if (!GBase8aConnectionPage.this.isSpecifiedVC.getSelection()) {
                    GBase8aConnectionPage.this.vcName.setText("");
                }
            }
        });


        Group vcNameGroup = new Group(addrGroup, 0);
        gd = new GridData(768);
        gd.horizontalSpan = 2;
        vcNameGroup.setLayoutData(gd);
        vcNameGroup.setLayout(new GridLayout(2, false));

        Label vcNameLabel = UIUtils.createControlLabel(vcNameGroup, GBase8aMessages.dialog_connection_vc_name);
        vcNameLabel.setLayoutData(new GridData(128));

        this.vcName = new Text(vcNameGroup, 2048);
        gd = new GridData(32);
        gd.widthHint = fontHeight * 20;
        this.vcName.setLayoutData(gd);
        this.vcName.setEnabled(false);
        this.vcName.addModifyListener(textListener);


        createDriverPanel(addrGroup);
        setControl(addrGroup);
    }


    public boolean isComplete() {
        return (this.hostText != null && this.portText != null &&
                !CommonUtils.isEmpty(this.hostText.getText()) &&
                !CommonUtils.isEmpty(this.portText.getText()));
    }


    public void loadSettings() {
        super.loadSettings();

        DBPDriver driver = getSite().getDriver();
        if (!this.activated) {

            if (driver != null && driver.getId().equalsIgnoreCase("mariaDB")) {
                setImageDescriptor(DB_LOGO_IMG);
            } else {
                setImageDescriptor(GBASE8A_LOGO_IMG);
            }
        }


        DBPConnectionConfiguration connectionInfo = this.site.getActiveDataSource().getConnectionConfiguration();
        if (this.hostText != null) {
            if (!CommonUtils.isEmpty(connectionInfo.getHostName())) {
                this.hostText.setText(connectionInfo.getHostName());
            } else {
                this.hostText.setText("localhost");
            }
        }
        if (this.portText != null) {
            if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
                this.portText.setText(String.valueOf(connectionInfo.getHostPort()));
            } else if (this.site.getDriver().getDefaultPort() != null) {
                this.portText.setText(this.site.getDriver().getDefaultPort());
            } else {
                this.portText.setText("");
            }
        }
        if (this.dbText != null) {
            this.dbText.setText(CommonUtils.notEmpty(connectionInfo.getDatabaseName()));
        }
        if (this.usernameText != null) {
            this.usernameText.setText(CommonUtils.notEmpty(connectionInfo.getUserName()));
        }
        if (this.passwordText != null) {
            this.passwordText.setText(CommonUtils.notEmpty(connectionInfo.getUserPassword()));
        }
        this.homesSelector.populateHomes(this.site.getDriver(), connectionInfo.getClientHomeId(), false);
        String vcNameStr = connectionInfo.getProperty("vcName");
        if (vcNameStr != null && !vcNameStr.equals("")) {
            this.isSpecifiedVC.setSelection(true);
            this.vcName.setEnabled(true);
            this.vcName.setText(CommonUtils.notEmpty(vcNameStr));
        } else {
            this.isSpecifiedVC.setSelection(false);
            this.vcName.setEnabled(false);
        }
        this.activated = true;
    }


    public void saveSettings(DBPDataSourceContainer dataSource) {
        DBPConnectionConfiguration connectionInfo = dataSource.getConnectionConfiguration();
        if (this.hostText != null) {
            connectionInfo.setHostName(this.hostText.getText().trim());
        }
        if (this.portText != null) {
            connectionInfo.setHostPort(this.portText.getText().trim());
        }
        if (this.dbText != null) {
            connectionInfo.setDatabaseName(this.dbText.getText().trim());
        }
        if (this.usernameText != null) {
            connectionInfo.setUserName(this.usernameText.getText().trim());
        }
        if (this.passwordText != null) {
            connectionInfo.setUserPassword(this.passwordText.getText());
        }
        if (this.homesSelector != null) {
            connectionInfo.setClientHomeId(this.homesSelector.getSelectedHome());
        }

        if (this.vcName != null) {
            connectionInfo.setProperty("vcName", this.vcName.getText());
        }
        super.saveSettings(dataSource);
    }

    @Override
    public IDialogPage[] getDialogPages(boolean extrasOnly, boolean forceCreate) {
        return new IDialogPage[]{new DriverPropertiesDialogPage(this)
        };
    }
}