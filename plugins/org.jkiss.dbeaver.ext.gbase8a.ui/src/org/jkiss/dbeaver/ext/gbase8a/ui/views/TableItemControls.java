package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


public class TableItemControls {
    private Text nameText;
    private Combo typeCombo;
    private Spinner lengthSpinner;
    private Button isNotNull;
    private Text defaultText;
    private Spinner scaleSpinner;
    private Spinner precisionSpinner;
    private Text commentText;
    private TableEditor nameEditor;
    private TableEditor typeEditor;
    private TableEditor lengthEditor;
    private TableEditor isNullEditor;
    private TableEditor defaultEditor;
    private TableEditor scaleEditor;
    private TableEditor precisionEditor;
    private TableEditor commentEditor;

    public TableItemControls(Text nameText, Combo typeCombo, Spinner lengthSpinner, Button isNotNull, Text defaultText, Spinner scaleSpinner, Spinner precisionSpinner, Text commentText, TableEditor nameEditor, TableEditor typeEditor, TableEditor lengthEditor, TableEditor isNullEditor, TableEditor defaultEditor, TableEditor scaleEditor, TableEditor precisionEditor, TableEditor commentEditor) {
        this.nameText = nameText;
        this.typeCombo = typeCombo;
        this.lengthSpinner = lengthSpinner;
        this.isNotNull = isNotNull;
        this.defaultText = defaultText;
        this.scaleSpinner = scaleSpinner;
        this.precisionSpinner = precisionSpinner;
        this.commentText = commentText;
        this.nameEditor = nameEditor;
        this.typeEditor = typeEditor;
        this.lengthEditor = lengthEditor;
        this.isNullEditor = isNullEditor;
        this.defaultEditor = defaultEditor;
        this.scaleEditor = scaleEditor;
        this.precisionEditor = precisionEditor;
        this.commentEditor = commentEditor;
    }

    public Text getNameText() {
        return this.nameText;
    }

    public void setNameText(Text nameText) {
        this.nameText = nameText;
    }

    public Combo getTypeCombo() {
        return this.typeCombo;
    }

    public void setTypeCombo(Combo typeCombo) {
        this.typeCombo = typeCombo;
    }

    public Spinner getLengthSpinner() {
        return this.lengthSpinner;
    }

    public void setLengthSpinner(Spinner lengthSpinner) {
        this.lengthSpinner = lengthSpinner;
    }

    public Spinner getScaleSpinner() {
        return this.scaleSpinner;
    }

    public void setScaleSpinner(Spinner scaleSpinner) {
        this.scaleSpinner = scaleSpinner;
    }

    public Spinner getPrecisionSpinner() {
        return this.precisionSpinner;
    }

    public void setPrecisionSpinner(Spinner precisionSpinner) {
        this.precisionSpinner = precisionSpinner;
    }

    public Button getIsNotNull() {
        return this.isNotNull;
    }

    public void setIsNotNull(Button isNotNull) {
        this.isNotNull = isNotNull;
    }

    public Text getDefaultText() {
        return this.defaultText;
    }

    public void setDefaultText(Text defaultText) {
        this.defaultText = defaultText;
    }

    public Text getCommentText() {
        return this.commentText;
    }

    public void setCommentText(Text commentText) {
        this.commentText = commentText;
    }

    public void dispose() {
        this.nameText.dispose();
        this.typeCombo.dispose();
        this.lengthSpinner.dispose();
        this.isNotNull.dispose();
        this.scaleSpinner.dispose();
        this.precisionSpinner.dispose();
    }
}