package net.sympower.parser.sdv;

import java.util.ArrayList;
import java.util.List;

public class SpotPriceDocument {

  public LastUpdatedRow lastUpdated;
  List<AreaDescriptionRow> areas;
  public final ArrayList<PricesRow> prices = new ArrayList<>();
  LineCountRow lineCount;

  public void setLastUpdated(LastUpdatedRow lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public void addPrice(PricesRow price) {
    this.prices.add(price);
  }

}
