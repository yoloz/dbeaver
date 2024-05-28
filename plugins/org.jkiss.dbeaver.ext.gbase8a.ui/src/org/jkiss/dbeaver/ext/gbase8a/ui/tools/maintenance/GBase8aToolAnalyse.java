 package org.jkiss.dbeaver.ext.gbase8a.ui.tools.maintenance;

 import org.jkiss.dbeaver.DBException;
 import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
 import org.jkiss.dbeaver.ext.gbase8a.ui.tools.IExternalTool;
 import org.jkiss.dbeaver.model.DBPEvaluationContext;
 import org.jkiss.dbeaver.model.struct.DBSObject;
 import java.util.Collection;
 import java.util.List;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.jkiss.utils.CommonUtils;


 public class GBase8aToolAnalyse implements IExternalTool
 {
   public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
     List<GBase8aTable> tables = CommonUtils.filterCollection(objects, GBase8aTable.class);
     if (!tables.isEmpty()) {
       SQLDialog dialog = new SQLDialog(activePart.getSite(), tables);
       dialog.open();
     }
   }

   static class SQLDialog
     extends TableToolDialog
   {
     public SQLDialog(IWorkbenchPartSite partSite, Collection<GBase8aTable> selectedTables) {
       super(partSite, "Analyse table(s)", selectedTables);
     }


     protected void generateObjectCommand(List<String> lines, GBase8aTable object) {
       lines.add("ANALYZE TABLE " + object.getFullyQualifiedName(DBPEvaluationContext.DDL));
     }


     protected void createControls(Composite parent) {
       createObjectsSelector(parent);
     }
   }
 }
