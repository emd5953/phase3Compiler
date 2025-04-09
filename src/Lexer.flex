%%
%class Lexer
%byaccj

%{
  // User code section
  public Parser parser;
  public int lineno;
  public int column;
  public int tokenStart; // Starting column of current token

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
    this.tokenStart = 1;
  }
%}

digit        = [0-9]
letter       = [a-zA-Z]
identifier   = {letter}({letter}|{digit}|_)*
newline      = \r|\n|\r\n
whitespace   = [ \t\f]+

linecomment  = "//"[^\n]*

blockcomment = "/\*"(~"\*"|"\*"+~"/")*"\*/"

minc_linecomment  = "%%"[^\n]*
minc_blockcomment = "%*"([^*]|\*+[^%])*\*+%

float_lit   = {digit}+"."+{digit}+

%%

<YYINITIAL> {

  "\uFEFF" {
  }

  {newline} {
    lineno++;
    column = 1;
  }

  {whitespace} {
   
    column += yytext().length();
  }

  {linecomment} {
  
    column += yytext().length();
  }

  {blockcomment} {
   
    for (int i = 0; i < yytext().length(); i++) {
      if (yytext().charAt(i) == '\n') {
        lineno++;
        column = 1;
      } else {
        column++;
      }
    }
  }

  {minc_linecomment} {
  
    column += yytext().length();
  }

  {minc_blockcomment} {
  
    for (int i = 0; i < yytext().length(); i++) {
      if (yytext().charAt(i) == '\n') {
        lineno++;
        column = 1;
      } else {
        column++;
      }
    }
  }


  "num" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM;
  }
  "bool" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL;
  }
  "true" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL_LIT;
  }
  "false" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BOOL_LIT;
  }
  "new" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NEW;
  }
  "size" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.SIZE;
  }
  "print" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.PRINT;
  }
  "return" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RETURN;
  }
  "if" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.IF;
  }
  "else" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.ELSE;
  }
  "while" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.WHILE;
  }

  /* ------------------ RELOP ( <, <=, >, >=, =, <> ) ------------------ */
  "<=" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;  
  }
  ">=" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;
  }
  "<>" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;
  }
  "<" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;
  }
  ">" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;
  }
  "=" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RELOP;
  }

  /* ------------------ EXPROP (+, -, or) ------------------ */
  \+ {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.EXPROP;
  }
  "-" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.EXPROP;
  }
  "or" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.EXPROP;
  }

  /* ------------------ TERMOP (*, /, and) ------------------ */
  \* {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TERMOP;
  }
  "/" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TERMOP;
  }
  "and" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.TERMOP;
  }

  /* ------------------ Other Punctuation ------------------ */
  "{" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.BEGIN;
  }
  "}" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.END;
  }
  "(" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LPAREN;
  }
  ")" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RPAREN;
  }
  ";" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.SEMI;
  }
  "[" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LBRACKET;
  }
  "]" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.RBRACKET;
  }
  "<-" {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.ASSIGN;
  }
  "," {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.COMMA;
  }
  "\." {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.DOT;
  }

  /* ------------------ Numeric Literals ------------------ */
  {float_lit} {
    // e.g. 123.45
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.FLOAT_LIT;
  }
  {digit}+ {
    // e.g. 123
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.NUM_LIT;
  }

  /* ------------------ Identifiers ------------------ */
  {identifier} {
    tokenStart = column;
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.IDENT;
  }

  /* ------------------ Error Handling ------------------ */
  \b {
    System.err.println("Sorry, backspace doesn't work");
  }
  [^] {
    // For any unrecognized single character
    System.err.println("Lexer Error: unexpected character '"
                       + yytext() + "' at line " + lineno
                       + ", column " + column);
    parser.yylval = new ParserVal(yytext());
    column += yytext().length();
    return Parser.LEXERROR;
  }
}
