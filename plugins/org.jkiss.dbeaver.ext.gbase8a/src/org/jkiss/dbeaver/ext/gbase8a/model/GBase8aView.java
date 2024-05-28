package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.IPropertyCacheValidator;
import org.jkiss.dbeaver.model.meta.LazyProperty;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.meta.PropertyGroup;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
//import cn.gbase.studio.model.struct.rdb.DBSLocation;
import org.jkiss.dbeaver.model.sql.format.SQLFormatUtils;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableConstraint;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKey;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class GBase8aView extends GBase8aTableBase {

    private static final Log log = Log.getLog(GBase8aView.class);

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getAdditionalInfo(monitor).getDefinition();
    }

    public enum CheckOption {
        NONE(null),
        CASCADE("CASCADED"),
        LOCAL("LOCAL");

        private final String definitionName;

        CheckOption(String definitionName) {
            this.definitionName = definitionName;
        }

        public String getDefinitionName() {
            return this.definitionName;
        }
    }

    public static class AdditionalInfo {
        private volatile boolean loaded = false;
        private String definition;
        private CheckOption checkOption;
        private boolean updatable;
        private String definer;

        public boolean isLoaded() {
            return this.loaded;
        }

        @Property(hidden = true, editable = true, updatable = true, order = -1)
        public String getDefinition() {
            return this.definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }


    public static class AdditionalInfoValidator
            implements IPropertyCacheValidator<GBase8aView> {
        public boolean isPropertyCached(GBase8aView object, Object propertyId) {
            return object.additionalInfo.loaded;
        }
    }

    private final AdditionalInfo additionalInfo = new AdditionalInfo();

    public GBase8aView(GBase8aCatalog catalog) {
        super(catalog);
    }

    public GBase8aView(GBase8aCatalog catalog, ResultSet dbResult) {
        super(catalog, dbResult);
    }

    @Property(viewable = true, editable = true, valueTransformer = DBObjectNameCaseTransformer.class, order = 1)
    @NotNull
    public String getName() {
        return super.getName();
    }

    public boolean isView() {
        return true;
    }


    public AdditionalInfo getAdditionalInfo() {
        return this.additionalInfo;
    }

    @PropertyGroup
    @LazyProperty(cacheValidator = AdditionalInfoValidator.class)
    public AdditionalInfo getAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        synchronized (this.additionalInfo) {
            if (!this.additionalInfo.loaded) {
                loadAdditionalInfo(monitor);
            }
            return this.additionalInfo;
        }
    }

    public List<? extends DBSTableIndex> getIndexes(DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    @Nullable
    public List<? extends DBSTableConstraint> getConstraints(@NotNull DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    public List<? extends DBSTableForeignKey> getAssociations(@NotNull DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    public List<? extends DBSTableForeignKey> getReferences(@NotNull DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    @Nullable
    public String getDescription() {
        return null;
    }

    private void loadAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        if (!isPersisted() || getContainer().isSystem()) {
            this.additionalInfo.loaded = true;
            return;
        }

        try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Load table status")) {
            try (JDBCPreparedStatement dbStat = session.prepareStatement(
                    "show create view " + getContainer().getName() + "." + getName())) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        this.additionalInfo.setDefinition(SQLFormatUtils.formatSQL(getDataSource(), JDBCUtils.safeGetString(dbResult, "Create View")));
                    }
                    this.additionalInfo.loaded = true;
                }
            }
        } catch (SQLException e) {
            throw new DBCException("loadAdditionalInfo", e);
        }
    }

    public void setObjectDefinitionText(String sourceText) throws DBException {
        getAdditionalInfo().setDefinition(sourceText);
    }

//    public Collection<? extends DBSLocation> getLocation(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }
//
//
//    public Collection<? extends DBSTableIndex> getStorages(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }
//
//
//    public Collection<? extends DBSTableIndex> getDesigns(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }
}