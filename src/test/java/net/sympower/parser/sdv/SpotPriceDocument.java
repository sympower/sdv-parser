package net.sympower.parser.sdv;

import java.util.ArrayList;
import java.util.List;

public class SpotPriceDocument {

  public LastUpdatedRow lastUpdated;
  @SdvIgnore public boolean lastUpdatedSetViaMethod = false;
  List<AreaDescriptionRow> areas;
  public final ArrayList<PricesRow> prices = new ArrayList<>();
  @SdvIgnore public boolean priceAddedViaMethod = false;
  LineCountRow lineCount;

  private void setLastUpdated(LastUpdatedRow lastUpdated) {
    this.lastUpdated = lastUpdated;
    this.lastUpdatedSetViaMethod = true;
  }

  public void addPrice(PricesRow price) {
    this.prices.add(price);
    this.priceAddedViaMethod = true;
  }

  @SdvIgnore
  public void addBlaah(Object foo) {
    // method for testing @SdvIgnore annotation
  }

}
