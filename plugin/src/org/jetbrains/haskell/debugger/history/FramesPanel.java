package org.jetbrains.haskell.debugger.history;

import com.intellij.ui.components.JBList;
import javax.swing.*;

public class FramesPanel extends JBList {
  private DefaultListModel listModel = new DefaultListModel();

  public FramesPanel(final HistoryManager manager) {
    setModel(listModel);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setValueIsAdjusting(true);
    addListSelectionListener(e -> manager.indexSelected(getSelectedIndex()));
  }

  @Override
  public ListModel getModel() {
    return listModel;
  }

  public void addElement(String line) {
    listModel.addElement(line);
    if (listModel.size() == 1) {
      setSelectedIndex(0);
    }
  }

  public void clear() {
    listModel.clear();
  }

  public int getIndexCount() {
    return listModel.size();
  }

  public boolean isFrameUnknown() {
    return getSelectedIndex() < 0 || listModel.get(getSelectedIndex()).equals("...");
  }
}
