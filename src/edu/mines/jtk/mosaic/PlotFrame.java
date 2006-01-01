/****************************************************************************
Copyright (c) 2005, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.mosaic;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import edu.mines.jtk.gui.*;
import static java.lang.Math.*;

/**
 * A plot frame is a window containing one or two plot panels. 
 * A plot frame (like any JFrame) has a content pane with a border layout, 
 * and it adds a panel (JPanel) containing its plot panel(s) to the center 
 * of that content pane. Menu and tool bars can be added as for any other 
 * JFrame.
 * <p>
 * Plot frames that contain two plot panels also contain a split pane
 * with either a horizontal (side by side) or vertical (above and below)
 * orientation. The split pane enables interactive resizing of the plot 
 * panels.
 * <p>
 * A plot frame has a single mode manager 
 * ({@link edu.mines.jtk.gui.ModeManager}).
 * When constructed, a plot frame adds and sets active a tile zoom mode 
 * ({@link edu.mines.jtk.mosaic.TileZoomMode}) to that mode manager. Of
 * course, additional modes of interaction can be added as well.
 * @author Dave Hale, Colorado School of Mines
 * @version 2005.12.31
 */
public class PlotFrame extends JFrame {
  private static final long serialVersionUID = 1L;

  /**
   * Orientation of the split pane (if any) containing two plot panels.
   * If horizontal, two panels are placed side by side. If vertical,
   * two panels are place one above the other. This orientation is
   * unused for plot frames with only one plot panel.
   */
  public enum Split {
    HORIZONTAL,
    VERTICAL
  }

  /**
   * Constructs a plot frame for the specified plot panel.
   * @param panel
   */
  public PlotFrame(PlotPanel panel) {
    _panelTL = panel;
    _panelBR = panel;
    _panelMain = new MainPanel();
    _panelMain.setLayout(new BorderLayout());
    _panelMain.add(_panelTL,BorderLayout.CENTER);
    this.setSize(_panelMain.getPreferredSize());
    this.add(_panelMain,BorderLayout.CENTER);
    addModeManager();
  }

  /**
   * Constructs a plot frame with two plot panels in a split pane.
   * @param panelTL the top-left panel.
   * @param panelBR the bottom-right panel.
   * @param split the split pane orientation.
   */
  public PlotFrame(PlotPanel panelTL, PlotPanel panelBR, Split split) {
    _panelTL = panelTL;
    _panelBR = panelBR;
    _split = split;
    _panelMain = new MainPanel();
    _panelMain.setLayout(new BorderLayout());
    if (_split==Split.HORIZONTAL) {
      _splitPane = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,_panelTL,_panelBR);
    } else {
      _splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT,_panelTL,_panelBR);
    }
    _splitPane.setOneTouchExpandable(true);
    _splitPane.setResizeWeight(0.5);
    _panelMain.add(_splitPane,BorderLayout.CENTER);
    this.setSize(_panelMain.getPreferredSize());
    this.add(_panelMain,BorderLayout.CENTER);
    addModeManager();
  }

  /**
   * Gets the plot panel in this frame. If this frame contains more 
   * than one plot panel, this method returns the top-left panel.
   * @return the plot panel.
   */
  public PlotPanel getPlotPanel() {
    return _panelTL;
  }

  /**
   * Gets the top-left plot panel in this frame. If this panel contains only 
   * one panel, then the top-left and bottom-right panels are the same.
   * @return the top-left plot panel.
   */
  public PlotPanel getPlotPanelTopLeft() {
    return _panelTL;
  }

  /**
   * Gets the bottom-right plot panel in this frame. If this panel contains 
   * only one panel, then the top-left and bottom-right panels are the same.
   * @return the bottom-right plot panel.
   */
  public PlotPanel getPlotPanelBottomRight() {
    return _panelBR;
  }

  /**
   * Gets the mode manager for this plot frame.
   * @return the mode manager.
   */
  public ModeManager getModeManager() {
    return _modeManager;
  }

  /**
   * Paints this panel to a PNG image with specified resolution and width.
   * The image height is computed so that the image has the same aspect 
   * ratio as this panel.
   * @param dpi the image resolution, in dots per inch.
   * @param win the image width, in inches.
   * @param fileName the name of the file to contain the PNG image.  
   */
  public void paintToPng(double dpi, double win, String fileName) 
    throws IOException 
  {
    _panelMain.paintToPng(dpi,win,fileName);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private PlotPanel _panelTL; // top-left panel
  private PlotPanel _panelBR; // bottom-right panel
  private Split _split; // orientation of split pane; null, if one plot panel
  private JSplitPane _splitPane; // null, if only one plot panel
  private MainPanel _panelMain; // main panel may contain split pane
  private ModeManager _modeManager; // mode manager for this plot frame

  /**
   * A main panel contains either a plot panel or a split pane that
   * contains two plot panels. Note that a JSplitPane is not an IPanel.
   * This class exists to override the method paintToRect of IPanel, so 
   * that the IPanel children of any JSplitPane are painted.
   */
  private class MainPanel extends IPanel {
    public void paintToRect(Graphics2D g2d, int x, int y, int w, int h) {
      if (_split==null) {
        _panelTL.paintToRect(g2d,x,y,w,h);
      } else {
        double ws = (double)w/(double)_splitPane.getWidth();
        double hs = (double)h/(double)_splitPane.getHeight();
        int nc = _splitPane.getComponentCount();
        for (int ic=0; ic<nc; ++ic) {
          Component c = _splitPane.getComponent(ic);
          int xc = c.getX();
          int yc = c.getY();
          int wc = c.getWidth();
          int hc = c.getHeight();
          xc = (int)round(xc*ws);
          yc = (int)round(yc*hs);
          wc = (int)round(wc*ws);
          hc = (int)round(hc*hs);
          if (c instanceof IPanel) {
            IPanel ip = (IPanel)c;
            ip.paintToRect(g2d,xc,yc,wc,hc);
          }
        }
      }
    }
  }

  private void addModeManager() {
    _modeManager = new ModeManager();
    _panelTL.getMosaic().setModeManager(_modeManager);
    if (_panelBR!=_panelTL)
      _panelBR.getMosaic().setModeManager(_modeManager);
    TileZoomMode zoomMode = new TileZoomMode(_modeManager);
    zoomMode.setActive(true);
  }
}