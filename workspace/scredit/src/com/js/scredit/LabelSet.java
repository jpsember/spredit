package com.js.scredit;


import java.util.ArrayList;

public class LabelSet {
  public LabelSet(String label) {
    this.label = label;
  }
  public String label() {
    return label;
  }
  public void add(Script scr) {
    scripts.add(scr);
  }
  public Script script(int i) {
    return scripts.get(i);
  }

  public int size() {
    return scripts.size();
  }
  private String label;
  private ArrayList<Script> scripts = new ArrayList();

}
