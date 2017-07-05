package com.wcong.dataupload.log;

import java.util.List;

/**
 * Created by wangcong on 2017/6/22.
 * <p>
 */

public class CellModel {
  private List<CellField> cells;

  public List<CellField> getCells() {
    return cells;
  }

  public void setCells(List<CellField> cells) {
    this.cells = cells;
  }

  public static class CellField {
    private int cid;
    private int lac;
    private int mnc;
    private int mcc;

    public int getCid() {
      return cid;
    }

    public void setCid(int cid) {
      this.cid = cid;
    }

    public int getLac() {
      return lac;
    }

    public void setLac(int lac) {
      this.lac = lac;
    }

    public int getMnc() {
      return mnc;
    }

    public void setMnc(int mnc) {
      this.mnc = mnc;
    }

    public int getMcc() {
      return mcc;
    }

    public void setMcc(int mcc) {
      this.mcc = mcc;
    }
  }
}
