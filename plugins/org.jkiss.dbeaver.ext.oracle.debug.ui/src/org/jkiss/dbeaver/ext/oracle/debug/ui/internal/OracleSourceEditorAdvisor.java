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

import org.jkiss.dbeaver.debug.ui.DBGEditorAdvisor;

public class OracleSourceEditorAdvisor implements DBGEditorAdvisor {

    private static final String ORACLE_SOURCE_DECLARATION = "oracle.source.declaration"; //$NON-NLS-1$

    @Override
    public String getSourceFolderId() {
        return ORACLE_SOURCE_DECLARATION;
    }

}