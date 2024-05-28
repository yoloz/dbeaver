package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPOrderedObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCColumnKeyType;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.meta.IPropertyValueListProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBase8aTableColumn extends JDBCTableColumn<GBase8aTableBase> implements DBSTableColumn, DBPNamedObject2, DBPOrderedObject {
    private String comment;
    private long charLength;
    private static final Log log = Log.getLog(GBase8aTableColumn.class);
    private GBase8aCollation collation;
    private KeyType keyType;
    private String extraInfo;
    private String fullTypeName;
    private static final Pattern enumPattern = Pattern.compile("'([^']*)'");
    private List<String> enumValues;

    public enum KeyType implements JDBCColumnKeyType {
        PRI,
        UNI,
        MUL;

        public boolean isInUniqueKey() {
            return !(this != PRI && this != UNI);
        }

        public boolean isInReferenceKey() {
            return (this == MUL);
        }
    }


    private boolean isHashColumn = false;

    public GBase8aTableColumn(GBase8aTableBase table) {
        super(table, false);
    }

    public GBase8aTableColumn(GBase8aTableBase table, ResultSet dbResult) throws DBException {
        super(table, true);
        loadInfo(dbResult);
        if (!table.isView()) {
            List<String> hashList = ((GBase8aTable) table).getAdditionalInfo(table.getContainer().getDataSource().getMonitor()).getHashCols();
            if (hashList != null) {
                for (String hashColumn : hashList) {
                    if (this.name.equals(hashColumn)) {
                        this.isHashColumn = true;
                        break;
                    }
                }
            }
        }
    }

    public GBase8aTableColumn(GBase8aTableBase table, DBSEntityAttribute source) throws DBException {
        super(table, source, false);
        this.comment = source.getDescription();
        if (source instanceof GBase8aTableColumn mySource) {
            this.charLength = mySource.charLength;
            this.collation = mySource.collation;
            this.keyType = mySource.keyType;
            this.extraInfo = mySource.extraInfo;
            this.fullTypeName = mySource.fullTypeName;
            if (mySource.enumValues != null) {
                this.enumValues = new ArrayList<String>(mySource.enumValues);
            }
            this.isHashColumn = mySource.isHashColumn;
        } else {
            this.collation = table.getContainer().getDefaultCollation();
            this.fullTypeName = DBUtils.getFullTypeName(this);
        }
    }

    private void loadInfo(ResultSet dbResult) throws DBException {
        setName(JDBCUtils.safeGetString(dbResult, "COLUMN_NAME"));
        setOrdinalPosition(JDBCUtils.safeGetInt(dbResult, "ORDINAL_POSITION"));
        String typeName = JDBCUtils.safeGetString(dbResult, "DATA_TYPE");
        assert typeName != null;
        String keyTypeName = JDBCUtils.safeGetString(dbResult, "COLUMN_KEY");
        if (!CommonUtils.isEmpty(keyTypeName)) {
            try {
                this.keyType = KeyType.valueOf(keyTypeName);
            } catch (IllegalArgumentException e) {
                log.debug(e);
            }
        }
        setTypeName(typeName);
        setValueType(GBase8aUtils.typeNameToValueType(typeName));
        getDataSource().getLocalDataType(typeName);
        this.charLength = JDBCUtils.safeGetLong(dbResult, "CHARACTER_MAXIMUM_LENGTH");
        if (this.charLength > 0L) {
            setMaxLength(this.charLength);
        }
        this.comment = JDBCUtils.safeGetString(dbResult, "COLUMN_COMMENT");
        setRequired(!"YES".equals(JDBCUtils.safeGetString(dbResult, "IS_NULLABLE")));
        setScale(JDBCUtils.safeGetInt(dbResult, "NUMERIC_SCALE"));
        setPrecision(JDBCUtils.safeGetInt(dbResult, "NUMERIC_PRECISION"));
        String defaultValue = JDBCUtils.safeGetString(dbResult, "COLUMN_DEFAULT");
        if (defaultValue != null) {
            setDefaultValue(defaultValue);
        }
        this.collation = getDataSource().getCollation(JDBCUtils.safeGetString(dbResult, "COLLATION_NAME"));

        this.extraInfo = JDBCUtils.safeGetString(dbResult, "EXTRA");

        this.fullTypeName = JDBCUtils.safeGetString(dbResult, "COLUMN_TYPE");
        if (!CommonUtils.isEmpty(this.fullTypeName) && (isTypeEnum() || isTypeSet())) {
            this.enumValues = new ArrayList<String>();
            Matcher enumMatcher = enumPattern.matcher(this.fullTypeName);
            while (enumMatcher.find()) {
                String enumStr = enumMatcher.group(1);
                this.enumValues.add(enumStr);
            }
        }
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return getTable().getDataSource();
    }

    @Property(viewable = true, editable = false, updatable = false, order = 20)
    public String getName() {
        return super.getName();
    }

    public String getFullTypeName() {
        return this.fullTypeName;
    }

    public void setFullTypeName(String fullTypeName) throws DBException {
        this.fullTypeName = fullTypeName;
        int divPos = fullTypeName.indexOf('(');
        if (divPos != -1) {
            setTypeName(fullTypeName.substring(0, divPos).trim());
        } else {
            setTypeName(fullTypeName);
        }
    }

    @Property(viewable = true, editable = false, updatable = false, order = 20, listProvider = JDBCTableColumn.ColumnTypeNameListProvider.class)
    public String getTypeName() {
        return super.getTypeName();
    }

    public boolean isTypeSet() {
        return this.typeName.equalsIgnoreCase("set");
    }

    public boolean isTypeEnum() {
        return this.typeName.equalsIgnoreCase("enum");
    }

    @Property(viewable = true, editable = false, updatable = false, order = 21)
    public long getMaxLength() {
        return super.getMaxLength();
    }

    @Property(viewable = true, order = 42)
    public Integer getScale() {
        return super.getScale();
    }

    @Property(viewable = true, order = 41)
    public Integer getPrecision() {
        return super.getPrecision();
    }

    @Property(viewable = true, editable = false, updatable = false, order = 35)
    public boolean isRequired() {
        return super.isRequired();
    }

    @Property(viewable = true, editable = false, updatable = false, order = 37)
    public String getDefaultValue() {
        return super.getDefaultValue();
    }

    public String getExtraInfo() {
        return this.extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public KeyType getKeyType() {
        return this.keyType;
    }

    public List<String> getEnumValues() {
        return this.enumValues;
    }

    @Property(viewable = false, editable = false, updatable = false, listProvider = CharsetListProvider.class, order = 81)
    public GBase8aCharset getCharset() {
        return (this.collation == null) ? null : this.collation.getCharset();
    }

    public void setCharset(GBase8aCharset charset) {
        this.collation = (charset == null) ? null : charset.getDefaultCollation();
    }

    @Property(viewable = false, editable = false, updatable = false, listProvider = CollationListProvider.class, order = 82)
    public GBase8aCollation getCollation() {
        return this.collation;
    }

    public void setCollation(GBase8aCollation collation) {
        this.collation = collation;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 100)
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 120)
    public boolean isHashColumn() {
        return this.isHashColumn;
    }

    public void setHashColumn(boolean isHashColumn) {
        this.isHashColumn = isHashColumn;
    }

    @Nullable
    public String getDescription() {
        return getComment();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof GBase8aTableColumn col) {
            if (col.getName() == null || (col.getName() != null && !col.getName().equalsIgnoreCase(this.name))) {
                return false;
            }
            if (!col.getTypeName().equalsIgnoreCase(this.typeName)) {
                return false;
            }
            switch (GBase8aConstants.getTYPES_INDEX().get(col.getTypeName().toUpperCase()).intValue()) {
                case 2:
                    if (col.getMaxLength() != this.maxLength) {
                        return false;
                    }
                    break;
                case 3:
                    if (col.getScale() != this.scale) {
                        return false;
                    }
                    if (col.getPrecision() != this.precision) {
                        return false;
                    }
                    break;
            }
            if (col.isRequired() != this.required) {
                return false;
            }
            if ((col.getDefaultValue() == null && super.getDefaultValue() != null && super.getDefaultValue().length() > 0) || (
                    col.getDefaultValue() != null && super.getDefaultValue() == null && col.getDefaultValue().length() > 0) || (
                    col.getDefaultValue() != null && super.getDefaultValue() != null && !col.getDefaultValue().equalsIgnoreCase(super.getDefaultValue()))) {
                return false;
            }
            if ((col.getComment() == null && this.comment != null && this.comment.length() > 0) || (
                    col.getComment() != null && this.comment == null && col.getComment().length() > 0) || (
                    col.getComment() != null && this.comment != null && !col.getComment().equalsIgnoreCase(this.comment))) {
                return false;
            }
            return col.isHashColumn() == this.isHashColumn;
        }
        return true;
    }

    public static class CharsetListProvider
            implements IPropertyValueListProvider<GBase8aTableColumn> {
        public boolean allowCustomValue() {
            return false;
        }

        public Object[] getPossibleValues(GBase8aTableColumn object) {
            return object.getDataSource().getCharsets(null).toArray();
        }
    }

    public static class CollationListProvider
            implements IPropertyValueListProvider<GBase8aTableColumn> {
        public boolean allowCustomValue() {
            return false;
        }

        public Object[] getPossibleValues(GBase8aTableColumn object) {
            if (object.getCharset() == null) {
                return new Object[0];
            }
            return object.getCharset().getCollations().toArray();
        }
    }

    public boolean isPersisted() {
        return super.isPersisted();
    }
}
