/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.components.properties.type;

import java.util.LinkedList;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * A property for percentages.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PercentProperty
    extends AbstractQuantity<PercentProperty.Unit> {

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   */
  public PercentProperty(ModelComponent model) {
    this(model, false);
  }

  /**
   * Creates a new instance.
   *
   * @param model The model component.
   * @param isInteger Whether only the integer part of the value is relevant.
   */
  public PercentProperty(ModelComponent model, boolean isInteger) {
    this(model, Double.NaN, Unit.PERCENT, isInteger);
  }

  /**
   * Creates a new instance with value.
   *
   * @param model The model component.
   * @param value The property's value.
   * @param unit The unit in which the value is measured.
   * @param isInteger Whether only the integer part of the value is relevant.
   */
  public PercentProperty(ModelComponent model, double value, Unit unit, boolean isInteger) {
    super(model, value, unit, Unit.class, new LinkedList<Relation<Unit>>());
    setInteger(isInteger);
  }

  @Override // Property
  public Object getComparableValue() {
    return String.valueOf(fValue) + getUnit();
  }

  @Override
  protected void initValidRange() {
    validRange.setMin(0).setMax(100);
  }

  /**
   * The supported units.
   */
  public enum Unit {

    /**
     * Percent.
     */
    PERCENT("%");

    private final String displayString;

    Unit(String displayString) {
      this.displayString = displayString;
    }

    @Override
    public String toString() {
      return displayString;
    }
  }
}
