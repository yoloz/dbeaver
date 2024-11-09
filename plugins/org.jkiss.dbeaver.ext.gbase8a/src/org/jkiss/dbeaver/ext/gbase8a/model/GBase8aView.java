package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
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
import org.jkiss.dbeaver.model.sql.format.SQLFormatUtils;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableConstraint;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKey;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;
import org.jkiss.dbeaver.model.struct.rdb.DBSView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class GBase8aView extends GBase8aTableBase implements DBSView {

    private static final Log log = Log.getLog(GBase8aView.class);

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

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        @Property(viewable = true, editable = true, updatable = true, order = 4)
        public CheckOption getCheckOption() {
            return checkOption;
        }

        public void setCheckOption(CheckOption checkOption) {
            this.checkOption = checkOption;
        }

        @Property(viewable = true, order = 5)
        public boolean isUpdatable() {
            return updatable;
        }

        public void setUpdatable(boolean updatable) {
            this.updatable = updatable;
        }

        @Property(viewable = true, order = 6)
        public String getDefiner() {
            return definer;
        }

        public void setDefiner(String definer) {
            this.definer = definer;
        }
    }


    public static class AdditionalInfoValidator implements IPropertyCacheValidator<GBase8aView> {
        @Override
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

    @NotNull
    @Property(viewable = true, editable = true, valueTransformer = DBObjectNameCaseTransformer.class, order = 1)
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean isView() {
        return true;
    }

    public AdditionalInfo getAdditionalInfo() {
        return additionalInfo;
    }

    @PropertyGroup()
    @LazyProperty(cacheValidator = AdditionalInfoValidator.class)
    public AdditionalInfo getAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        synchronized (additionalInfo) {
            if (!additionalInfo.loaded) {
                loadAdditionalInfo(monitor);
            }
            return additionalInfo;
        }
    }

    @Override
    public List<? extends DBSTableIndex> getIndexes(@NotNull DBRProgressMonitor monitor) throws DBException {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public List<? extends DBSTableConstraint> getConstraints(@NotNull DBRProgressMonitor monitor) throws DBException {
        return Collections.emptyList();
    }

    @Override
    public List<? extends DBSTableForeignKey> getAssociations(@NotNull DBRProgressMonitor monitor) throws DBException {
        return Collections.emptyList();
    }

    @Override
    public List<? extends DBSTableForeignKey> getReferences(@NotNull DBRProgressMonitor monitor) throws DBException {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }


    private void loadAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        if (!isPersisted() || getContainer().isSystemCatalog()) {
            this.additionalInfo.loaded = true;
            return;
        }

        try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Load table status");
             JDBCPreparedStatement dbStat = session.prepareStatement("SHOW CREATE VIEW " + getFullyQualifiedName(DBPEvaluationContext.DDL));
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            if (dbResult.next()) {
                this.additionalInfo.setDefinition(SQLFormatUtils.formatSQL(getDataSource(), JDBCUtils.safeGetString(dbResult, "Create View")));
            }
            this.additionalInfo.loaded = true;
        } catch (SQLException e) {
            throw new DBCException("loadAdditionalInfo", e);
        }
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        String definition = getAdditionalInfo(monitor).getDefinition();
        if (definition == null && !isPersisted()) {
            return "";
        }
        return definition;
    }

    @Override
    public void setObjectDefinitionText(String sourceText) throws DBException {
        getAdditionalInfo().setDefinition(sourceText);
    }
}