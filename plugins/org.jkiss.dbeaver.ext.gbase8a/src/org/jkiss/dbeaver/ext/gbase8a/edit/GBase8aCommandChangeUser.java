package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GBase8aCommandChangeUser extends DBECommandComposite<GBase8aUser, UserPropertyHandler> {

    protected GBase8aCommandChangeUser(GBase8aUser user) {
        super(user, GBase8aMessages.edit_command_change_user_name);
    }

    public void updateModel() {
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            switch (UserPropertyHandler.valueOf((String) entry.getKey())) {
                case MAX_QUERIES:
                    getObject().setMaxQuestions(CommonUtils.toInt(entry.getValue()));
                case MAX_UPDATES:
                    getObject().setMaxUpdates(CommonUtils.toInt(entry.getValue()));
                case MAX_CONNECTIONS:
                    getObject().setMaxConnections(CommonUtils.toInt(entry.getValue()));
                case MAX_USER_CONNECTIONS:
                    getObject().setMaxUserConnections(CommonUtils.toInt(entry.getValue()));
            }
        }
    }

    public void validateCommand() throws DBException {
        String passValue = CommonUtils.toString(getProperty(UserPropertyHandler.PASSWORD));
        String confirmValue = CommonUtils.toString(getProperty(UserPropertyHandler.PASSWORD_CONFIRM));
        if (!CommonUtils.isEmpty(passValue) && !CommonUtils.equalObjects(passValue, confirmValue)) {
            throw new DBException("Password confirmation value is invalid");
        }
    }

    public DBEPersistAction[] getPersistActions() {
        boolean hasSet;
        List<DBEPersistAction> actions = new ArrayList<>();
        boolean newUser = !getObject().isPersisted();
        if (newUser) {
            actions.add(
                    new SQLDatabasePersistAction(GBase8aMessages.edit_command_change_user_action_create_new_user, "CREATE USER "
                            + getObject().getFullName()
                            + " IDENTIFIED BY '"
                            + CommonUtils.toString(getProperties().get(UserPropertyHandler.PASSWORD.name())).replaceAll("'", "'")
                            + "'") {
                        public void handleExecute(DBCSession session, Throwable error) {
                            if (error == null) {
                                (GBase8aCommandChangeUser.this.getObject()).setPersisted(true);
                            }
                        }
                    });
        }
        StringBuilder script = new StringBuilder();
        GBase8aDataSource dataSource = getObject().getDataSource();
        if (!dataSource.isMariaDB() && dataSource.isServerVersionAtLeast(5, 7)) {
            hasSet = generateAlterScript(script, newUser);
        } else {
            hasSet = generateUpdateScript(script);
        }
        if (hasSet) {
            actions.add(new SQLDatabasePersistAction(GBase8aMessages.edit_command_change_user_action_update_user_record, script.toString()));
        }
        return actions.toArray(new DBEPersistAction[actions.size()]);
    }

    private boolean generateUpdateScript(StringBuilder script) {
        script.append("UPDATE gbase.user SET ");
        boolean hasSet = false;
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            if (entry.getKey() == UserPropertyHandler.PASSWORD_CONFIRM) {
                continue;
            }
            String delim = hasSet ? "," : "";
            switch (UserPropertyHandler.valueOf((String) entry.getKey())) {
                case PASSWORD:
                    script.append(delim).append("Password=PASSWORD('").append(CommonUtils.toString(entry.getValue()).replaceAll("'", "'")).append("')");
                    hasSet = true;
                case MAX_QUERIES:
                    script.append(delim).append("Max_Questions=").append(CommonUtils.toInt(entry.getValue()));
                    hasSet = true;
                case MAX_UPDATES:
                    script.append(delim).append("Max_Updates=").append(CommonUtils.toInt(entry.getValue()));
                    hasSet = true;
                case MAX_CONNECTIONS:
                    script.append(delim).append("Max_Connections=").append(CommonUtils.toInt(entry.getValue()));
                    hasSet = true;
                case MAX_USER_CONNECTIONS:
                    script.append(delim).append("Max_User_Connections=").append(CommonUtils.toInt(entry.getValue()));
                    hasSet = true;
            }
        }
        script.append(" WHERE User='").append(getObject().getUserName()).append("' AND Host='").append(getObject().getHost()).append("'");
        return hasSet;
    }

    private boolean generateAlterScript(StringBuilder script, boolean newUser) {
        boolean hasSet = false, hasResOptions = false;
        script.append("ALTER USER ").append(getObject().getFullName());
        if (!newUser && getProperties().containsKey(UserPropertyHandler.PASSWORD.name())) {
            script.append("\nIDENTIFIED BY '").append(CommonUtils.toString(getProperties().get(UserPropertyHandler.PASSWORD.name())).replaceAll("'", "'")).append("' ");
            hasSet = true;
        }
        StringBuilder resOptions = new StringBuilder();
        for (Map.Entry<Object, Object> entry : getProperties().entrySet()) {
            switch (UserPropertyHandler.valueOf((String) entry.getKey())) {
                case MAX_QUERIES:
                    resOptions.append(" MAX_QUERIES_PER_HOUR ").append(CommonUtils.toInt(entry.getValue()));
                    hasResOptions = true;
                case MAX_UPDATES:
                    resOptions.append(" MAX_UPDATES_PER_HOUR ").append(CommonUtils.toInt(entry.getValue()));
                    hasResOptions = true;
                case MAX_CONNECTIONS:
                    resOptions.append(" MAX_CONNECTIONS_PER_HOUR ").append(CommonUtils.toInt(entry.getValue()));
                    hasResOptions = true;
                case MAX_USER_CONNECTIONS:
                    resOptions.append(" MAX_USER_CONNECTIONS ").append(CommonUtils.toInt(entry.getValue()));
                    hasResOptions = true;
            }
        }
        if (!resOptions.isEmpty()) {
            script.append("\nWITH ").append(resOptions);
        }
        return !(!hasSet && !hasResOptions);
    }
}
