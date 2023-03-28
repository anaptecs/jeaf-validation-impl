package com.anaptecs.jeaf.validation.impl.test;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class ClassA {
  @NotNull
  public String name;

  @Null
  public String nullProperty;

}
