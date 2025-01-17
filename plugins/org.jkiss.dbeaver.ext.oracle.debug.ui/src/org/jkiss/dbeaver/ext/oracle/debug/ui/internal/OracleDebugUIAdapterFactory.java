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
package org.jkiss.dbeaver.ext.oracle.debug.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.jkiss.dbeaver.debug.ui.DBGEditorAdvisor;
import org.jkiss.dbeaver.ext.oracle.model.OracleDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;

public class OracleDebugUIAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] CLASSES = new Class[]{DBGEditorAdvisor.class};

    private final DBGEditorAdvisor debugEditorAdvisor = new OracleSourceEditorAdvisor();

    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType == DBGEditorAdvisor.class) {
            if (adaptableObject instanceof DBPDataSourceContainer sourceContainer) {
                DBPDataSource dataSource = sourceContainer.getDataSource();
                if (dataSource instanceof OracleDataSource) {
                    return adapterType.cast(debugEditorAdvisor);
                }
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return CLASSES;
    }

}
