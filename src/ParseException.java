public class ParseException extends Exception {
  private int line;
  private int col;
  private String production;

  // Constructor with a custom message
  public ParseException(String message, int line, int col) {
      super(message + " at " + line + ":" + col + ".");
      this.line = line;
      this.col = col;
  }
  
  // Constructor for "No matching production" errors
  public ParseException(String production, int line, int col, boolean isProductionError) {
      super("No matching production in " + production + " at " + line + ":" + col + ".");
      this.production = production;
      this.line = line;
      this.col = col;
  }
  
  public int getLine() { return line; }
  public int getCol() { return col; }
  public String getProduction() { return production; }
}
