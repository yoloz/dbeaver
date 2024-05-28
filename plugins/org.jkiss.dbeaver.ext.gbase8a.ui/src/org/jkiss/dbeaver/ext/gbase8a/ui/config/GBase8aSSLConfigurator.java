package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.model.net.DBWHandlerConfiguration;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.TextWithOpen;
import org.jkiss.dbeaver.ui.controls.TextWithOpenFile;
import org.jkiss.dbeaver.ui.dialogs.net.SSLConfiguratorAbstractUI;
import org.jkiss.utils.CommonUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


public class GBase8aSSLConfigurator extends SSLConfiguratorAbstractUI {
    private Button requireSSQL;
    private TextWithOpen trustKeyText;
    private Text trustPasswordText;
    private TextWithOpen clientKeyText;
    private Text clientPasswordText;

    @Override
    public void createControl(Composite parent, Object object, Runnable propertyChangeListener) {
        Composite composite = new Composite(parent, 0);
        composite.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(1808);
        gd.minimumHeight = 200;
        composite.setLayoutData(gd);


        Group certGroup = UIUtils.createControlGroup(composite, GBase8aMessages.dialog_ssl_certificate, 2, 768, -1);
        UIUtils.createControlLabel(certGroup, GBase8aMessages.dialog_ssl_trust_certificate_file);
        gd = new GridData(768);
        gd.minimumWidth = 130;
        this.trustKeyText = new TextWithOpenFile(certGroup, "trustfile", new String[]{"*.*", "*.crt", "*.cert", "*.pem", "*"});
        this.trustKeyText.setLayoutData(new GridData(768));

        this.trustPasswordText = UIUtils.createLabelText(certGroup, GBase8aMessages.dialog_ssl_trust_certificate_password, "", 4196352);
        this.trustPasswordText.setToolTipText("");

        UIUtils.createControlLabel(certGroup, GBase8aMessages.dialog_ssl_client_certificate_file);
        gd = new GridData(768);
        gd.minimumWidth = 130;
        this.clientKeyText = new TextWithOpenFile(certGroup, "clientfile", new String[]{"*.*", "*.cert", "*.pem", "*"});
        this.clientKeyText.setLayoutData(new GridData(768));

        this.clientPasswordText = UIUtils.createLabelText(certGroup, GBase8aMessages.dialog_ssl_client_certificate_password, "", 4196352);
        this.clientPasswordText.setToolTipText("");
    }

    public void loadSettings(DBWHandlerConfiguration configuration) {
        this.trustKeyText.setText(CommonUtils.notEmpty((String) configuration.getProperties().get("javax.net.ssl.trustStore")));
        this.trustPasswordText.setText(CommonUtils.notEmpty((String) configuration.getProperties().get("javax.net.ssl.trustStorePassword")));
        this.clientKeyText.setText(CommonUtils.notEmpty((String) configuration.getProperties().get("javax.net.ssl.keyStore")));
        this.clientPasswordText.setText(CommonUtils.notEmpty((String) configuration.getProperties().get("javax.net.ssl.keyStorePassword")));
    }


    public void saveSettings(DBWHandlerConfiguration configuration) {
        configuration.getProperties().put("javax.net.ssl.trustStore", this.trustKeyText.getText());
        configuration.getProperties().put("javax.net.ssl.trustStorePassword", this.trustPasswordText.getText());
        configuration.getProperties().put("javax.net.ssl.keyStore", this.clientKeyText.getText());
        configuration.getProperties().put("javax.net.ssl.keyStorePassword", this.clientPasswordText.getText());
    }
}