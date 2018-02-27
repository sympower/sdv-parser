package net.sympower.parser.sdv;

@SdvRow("BE")
public class AreaDescriptionRow {

  public final String alias;
  public final String description;

  public AreaDescriptionRow(String alias, String description) {
    this.alias = alias;
    this.description = description;
  }

}
