/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.oracle.debug.core;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.debug.DBGConstants;
import org.jkiss.dbeaver.ext.oracle.debug.OracleDebugConstants;
import org.jkiss.dbeaver.ext.oracle.model.OracleDataSource;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureStandalone;
import org.jkiss.dbeaver.ext.oracle.model.OracleSchema;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.util.Map;

public class OracleSqlDebugCore {

	public static final String BUNDLE_SYMBOLIC_NAME = "org.jkiss.dbeaver.ext.postgresql.debug.core"; //$NON-NLS-1$

	public static void saveFunction(OracleProcedureStandalone procedure, Map<String, Object> configuration) {
		OracleDataSource dataSource = procedure.getDataSource();
		DBPDataSourceContainer dataSourceContainer = dataSource.getContainer();

		String schemaName = procedure.getSchema().getName();

		configuration.put(DBGConstants.ATTR_PROJECT_NAME, dataSourceContainer.getProject().getName());
		configuration.put(DBGConstants.ATTR_DATASOURCE_ID, dataSourceContainer.getId());
		configuration.put(DBGConstants.ATTR_DEBUG_TYPE, OracleDebugConstants.DEBUG_TYPE_FUNCTION);
		configuration.put(OracleDebugConstants.ATTR_SCHEMA_NAME, schemaName);
		configuration.put(OracleDebugConstants.ATTR_FUNCTION_OID, String.valueOf(procedure.getObjectId()));
		configuration.put(OracleDebugConstants.ATTR_FUNCTION_NAME, procedure.getName());
	}

	public static OracleProcedureStandalone resolveFunction(DBRProgressMonitor monitor,
			DBPDataSourceContainer dsContainer, Map<String, Object> configuration) throws DBException {
		if (!dsContainer.isConnected()) {
			dsContainer.connect(monitor, true, true);
		}
		String functionName = String.valueOf(configuration.get(OracleDebugConstants.ATTR_FUNCTION_NAME));
		String schemaName = (String) configuration.get(OracleDebugConstants.ATTR_SCHEMA_NAME);
		OracleDataSource ds = (OracleDataSource) dsContainer.getDataSource();
		OracleSchema schema = ds.getSchema(monitor, schemaName);
		if (schema != null) {
			OracleProcedureStandalone function = schema.getProcedure(monitor, functionName);
			if (function != null) {
				return function;
			}
			throw new DBException("Function " + functionName + " not found in schema " + schemaName);
		} else {
			throw new DBException("Schema '" + schemaName + "' not found ");
		}
	}
}
