package de.fraunhofer.aisec.markmodel.wpds;

import de.breakpoint.pushdown.WPDS;

public class CpgWpds extends WPDS<Stmt, Val, Weight> {

  @Override
  public Stmt epsilon() {
    return new Stmt("EPSILON");
  }

}
