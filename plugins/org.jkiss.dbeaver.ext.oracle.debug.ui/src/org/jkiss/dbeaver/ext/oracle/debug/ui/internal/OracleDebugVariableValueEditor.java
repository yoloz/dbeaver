package org.jkiss.dbeaver.ext.oracle.debug.ui.internal;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.actions.IVariableValueEditor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.Log;

/**
 * @author yolo
 */
public class OracleDebugVariableValueEditor implements IVariableValueEditor {

    Log log = Log.getLog(OracleDebugVariableValueEditor.class);

    @Override
    public boolean editVariable(IVariable variable, Shell shell) {
        try {
            InputDialog dialog = new InputDialog(shell, "Variable Expression", "Input:name,value(optional)]",
                    variable.getValue().getValueString(), null) {
                @Override
                protected Control createDialogArea(Composite parent) {
                    return super.createDialogArea(parent);
                }
            };
            if (dialog.open() == Window.OK) {
                String stringValue = dialog.getValue();
                if (!stringValue.isBlank() && !stringValue.isEmpty()) {
                    variable.setValue(stringValue);
                }
            }
        } catch (DebugException e) {
            log.error("editVariable fail.", e);
        }
        return true;
    }

    @Override
    public boolean saveVariable(IVariable variable, String expression, Shell shell) {
        return false;
    }
}
