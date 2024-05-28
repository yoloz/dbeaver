package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBECommandFilter;
import org.jkiss.dbeaver.model.edit.DBECommandQueue;
import org.jkiss.dbeaver.model.edit.DBEObjectMaker;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.edit.prop.DBECommandComposite;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.AbstractObjectManager;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAbstract;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.edit.SQLScriptCommand;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.Map;


public class GBase8aUserManager extends AbstractObjectManager<GBase8aUser>
        implements DBEObjectMaker<GBase8aUser, GBase8aDataSource>, DBECommandFilter<GBase8aUser> {

    private static class CommandDropUser
            extends DBECommandComposite<GBase8aUser, UserPropertyHandler> {
        protected CommandDropUser(GBase8aUser user) {
            super(user, GBase8aMessages.edit_user_manager_command_drop_user);
        }

        public DBEPersistAction[] getPersistActions() {
            return new DBEPersistAction[]{
                    new SQLDatabasePersistAction(GBase8aMessages.edit_user_manager_command_drop_user, "DROP USER " + getObject().getFullName()) {
                        public void handleExecute(DBCSession session, Throwable error) {
                            if (error == null)
                                CommandDropUser.this.getObject().setPersisted(false);
                        }
                    }
            };
        }
    }

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return 4L;
    }

    @Nullable
    public DBSObjectCache<? extends DBSObject, GBase8aUser> getObjectsCache(GBase8aUser object) {
        return null;
    }

    @Override
    public boolean canCreateObject(Object container) {
        return true;
    }

    public boolean canDeleteObject(GBase8aUser object) {
        return true;
    }

    @Override
    public GBase8aUser createNewObject(DBRProgressMonitor monitor, DBECommandContext commandContext, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aUser newUser = new GBase8aUser((GBase8aDataSource) container, null);
        if (copyFrom instanceof GBase8aUser tplUser) {
            newUser.setUserName(tplUser.getUserName());
            newUser.setHost(tplUser.getHost());
            newUser.setMaxQuestions(tplUser.getMaxQuestions());
            newUser.setMaxUpdates(tplUser.getMaxUpdates());
            newUser.setMaxConnections(tplUser.getMaxConnections());
            newUser.setMaxUserConnections(tplUser.getMaxUserConnections());
        }
        commandContext.addCommand(new CommandCreateUser(newUser), new CreateObjectReflector(this), true);
        return newUser;
    }

    public void deleteObject(DBECommandContext commandContext, GBase8aUser user, Map<String, Object> options) {
        commandContext.addCommand(new CommandDropUser(user), new DeleteObjectReflector(this), true);
    }

    public void filterCommands(DBECommandQueue<GBase8aUser> queue) {
        if (!queue.isEmpty())
            queue.add(new SQLScriptCommand(queue.getObject(), GBase8aMessages.edit_user_manager_command_flush_privileges, "FLUSH PRIVILEGES"));
    }

    private static class CommandCreateUser extends DBECommandAbstract<GBase8aUser> {
        protected CommandCreateUser(GBase8aUser user) {
            super(user, GBase8aMessages.edit_user_manager_command_create_user);
        }
    }
}
