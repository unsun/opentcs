/**
 * Copyright (c) 2017 Fraunhofer IML
 */
package org.opentcs.guing.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Shows a dialog with a text area and a label to describe the content of it.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class TextAreaDialog
    extends javax.swing.JDialog {

  private Collection<String> contents;

  /**
   * Creates new form TextAreaDialog.
   *
   * @param parent the parent of this dialog
   * @param modal specifies whether dialog blocks user input to other top-level windows when shown
   * @param description the description of the text area content
   */
  public TextAreaDialog(java.awt.Component parent, boolean modal, String description) {
    super(JOptionPane.getFrameForComponent(parent), modal);
    initComponents();
    contentTextArea.setEditable(false);
    textAreaLabel.setText(description);
  }

  /**
   * Returns the content of the text area.
   *
   * @return the content of the text area
   */
  public Collection<String> getContent() {
    return new LinkedList<>(contents);
  }

  /**
   * Sets the content of the text area.
   *
   * @param contents the content of the text area
   */
  public void setContent(Collection<String> contents) {
    this.contents = Objects.requireNonNull(contents, "contents");
    contentTextArea.setText("");
    contents.stream().map(o -> o + "\n").forEach(contentTextArea::append);
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    contentPane = new javax.swing.JPanel();
    contentScrollPane = new javax.swing.JScrollPane();
    contentTextArea = new javax.swing.JTextArea();
    textAreaLabel = new javax.swing.JLabel();
    buttonPane = new javax.swing.JPanel();
    okButton = new javax.swing.JButton();
    symbolPane = new javax.swing.JPanel();
    symbolLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setPreferredSize(new java.awt.Dimension(600, 400));
    setResizable(false);

    contentPane.setPreferredSize(new java.awt.Dimension(300, 150));
    contentPane.setLayout(new java.awt.BorderLayout());

    contentTextArea.setColumns(20);
    contentTextArea.setRows(5);
    contentScrollPane.setViewportView(contentTextArea);

    contentPane.add(contentScrollPane, java.awt.BorderLayout.CENTER);

    textAreaLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    textAreaLabel.setText(" ");
    contentPane.add(textAreaLabel, java.awt.BorderLayout.NORTH);

    getContentPane().add(contentPane, java.awt.BorderLayout.CENTER);

    buttonPane.setLayout(new java.awt.GridBagLayout());

    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    buttonPane.add(okButton, gridBagConstraints);

    getContentPane().add(buttonPane, java.awt.BorderLayout.SOUTH);

    symbolPane.setPreferredSize(new java.awt.Dimension(100, 100));
    symbolPane.setLayout(new java.awt.GridBagLayout());

    symbolLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
    symbolLabel.setPreferredSize(new java.awt.Dimension(32, 32));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    symbolPane.add(symbolLabel, gridBagConstraints);

    getContentPane().add(symbolPane, java.awt.BorderLayout.WEST);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    setVisible(false);
  }//GEN-LAST:event_okButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonPane;
  private javax.swing.JPanel contentPane;
  private javax.swing.JScrollPane contentScrollPane;
  private javax.swing.JTextArea contentTextArea;
  private javax.swing.JButton okButton;
  private javax.swing.JLabel symbolLabel;
  private javax.swing.JPanel symbolPane;
  private javax.swing.JLabel textAreaLabel;
  // End of variables declaration//GEN-END:variables
}
