package plugins.playlist;

import java.awt.Component;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MyCellRenderer extends JLabel implements ListCellRenderer {

    private boolean showLineNumbers = true;
    
    public Component getListCellRendererComponent(JList list, Object value, // value
                                                                            // to
                                                                            // display
    int index, // cell index
    boolean isSelected, // is the cell selected
    boolean cellHasFocus) { // the list and the cell have the focus

        String s = ((File) value).getName();
        if (showLineNumbers)
            setText((index + 1) + ". " + s);
        else
            setText(s);
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setOpaque(true);
        
        return this;
    }

    public boolean isShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }
}
