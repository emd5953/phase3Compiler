import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Parser {
    /* ------------------------------------------------------------------
       Token Constants (matching your updated lexer)
       ------------------------------------------------------------------ */
    public static final int ENDMARKER = 0;
    public static final int LEXERROR   = 1;

    public static final int NUM        = 10;   // "num"
    public static final int BEGIN      = 11;   // "{"
    public static final int END        = 12;   // "}"
    public static final int LPAREN     = 13;   // "("
    public static final int RPAREN     = 14;   // ")"
    public static final int SEMI       = 15;   // ";"
    public static final int NUM_LIT    = 16;   // e.g. "123" or "3.14"
    public static final int IDENT      = 17;   // e.g. "abc"
    public static final int LBRACKET   = 18;   // "["
    public static final int RBRACKET   = 19;   // "]"
    public static final int BOOL_LIT   = 20;   // "true" or "false"
    public static final int NEW        = 21;   // "new"
    public static final int SIZE       = 22;   // "size"
    public static final int BOOL       = 23;   // "bool"

    public static final int ASSIGN     = 24;   // "<-"
    public static final int PRINT      = 25;   // "print"
    public static final int RETURN     = 26;   // "return"
    public static final int FLOAT_LIT  = 27;   // e.g. "1.23"
    public static final int DOT        = 28;   // "."
    public static final int IF         = 29;   // "if"
    public static final int COMMA      = 30;   // ","
    public static final int ELSE       = 31;   // "else"
    public static final int WHILE      = 32;   // "while"
    
    public static final int RELOP  = 33;  // e.g. <, >, <=, >=, =, <>
    public static final int EXPROP = 34;  // e.g. +, -, or
    public static final int TERMOP = 35;  // e.g. *, /, and

    /* ------------------------------------------------------------------
       Internal Classes / Fields
       ------------------------------------------------------------------ */
    public class Token {
        public int type;
        public ParserVal attr;
        public String lexeme;
        public int col;

        public Token(int type, ParserVal attr, String lexeme, int col) {
            this.type   = type;
            this.attr   = attr;
            this.lexeme = lexeme;
            this.col    = col;
        }
        @Override
        public String toString() {
            return "Token [type=" + tokenToString(type) + "(" + type 
                   + "), lexeme=" + lexeme + ", col=" + col + "]";
        }
    }

    public ParserVal  yylval;
    private Token     _token;
    private Lexer     _lexer;
    private Compiler  _compiler;
    public ParseTree.Program _parsetree;
    public String     _errormsg;

    public Parser(java.io.Reader r, Compiler compiler) throws Exception {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null; 
        Advance(); // read first token
    }

    /* ------------------------------------------------------------------
       Advance and Match
       ------------------------------------------------------------------ */
    public void Advance() throws Exception {
        // Temporarily mute JFlex error output:
        PrintStream originalErr = System.err;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setErr(new PrintStream(baos));

        int token_type = _lexer.yylex();

        // restore System.err
        System.setErr(originalErr);

        String text = _lexer.yytext();
        if (token_type == LEXERROR && text.equals(".")) {
            token_type = DOT;
        }

        _token = new Token(token_type, new ParserVal(text), text, _lexer.tokenStart);
    }

    public String Match(int token_type) throws Exception {
        if (_token.type == LEXERROR) {
            throw new ParseException("Lexical error on \"" + _token.lexeme + "\"", 
                                     _lexer.lineno, _token.col);
        }
        String lexeme = _token.lexeme;
        if (_token.type != token_type) {
            throw new ParseException("\"" + tokenToString(token_type) + "\" is expected instead of \""
                + _token.lexeme + "\"", _lexer.lineno, _token.col);
        }
        if (_token.type != ENDMARKER) {
            Advance();
        }
        return lexeme;
    }

    /* ------------------------------------------------------------------
       The Parse Entry Point
       ------------------------------------------------------------------ */
       public int yyparse() {
        try {
            _parsetree = program();
            return 0; // success
        } catch (ParseException pe) {
            _errormsg = pe.getMessage(); // Just use the message from the exception.
            return -1; // failure
        } catch (Exception ex) {
            _errormsg = ex.getMessage();
            return -1;
        }
    }
    
    
    public ParseTree.Program program() throws Exception {
        // Parse all function declarations
        List<ParseTree.FuncDecl> funcs = decl_list();
    
        // If there's another token here, we want a top-level error:
        if (_token.type != ENDMARKER) {
            throw new ParseException("program", _lexer.lineno, _token.col, true);
        }
        Match(ENDMARKER);
    
        return new ParseTree.Program(funcs);
    }
    
    
    public List<ParseTree.FuncDecl> decl_list() throws Exception {
        List<ParseTree.FuncDecl> list = new ArrayList<>();
        boolean parsedAny = false;
    
        // While the next token is in FIRST(fun_decl) (NUM or BOOL), parse fun_decl.
        while (_token.type == NUM || _token.type == BOOL) {
            list.add(fun_decl());
            parsedAny = true;
        }
    
        // After parsing, if the token is not in FOLLOW(decl_list)...
        if (!isInFollowOfDeclList(_token.type)) {
            if (!parsedAny) {
                throw new ParseException("program", _lexer.lineno, _token.col, true);
            } else {
                throw new ParseException("decl_list'", _lexer.lineno, _token.col, true);
            }
        }
    
        return list;
    }
    
    private boolean isInFollowOfDeclList(int tokenType) {
        // For program -> decl_list ENDMARKER, valid follow is ENDMARKER.
        return (tokenType == ENDMARKER);
    }
    
    
    // fun_decl -> type_spec IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    public ParseTree.FuncDecl fun_decl() throws Exception {
        ParseTree.TypeSpec ts = type_spec();
        String funcName = Match(IDENT);
        Match(LPAREN);
        List<ParseTree.Param> ps = params();
        Match(RPAREN);
        Match(BEGIN);
        List<ParseTree.LocalDecl> locals = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        Match(END);
        return new ParseTree.FuncDecl(funcName, ts, ps, locals, stmts);
    }
    
    // type_spec -> prim_type type_spec'
    public ParseTree.TypeSpec type_spec() throws Exception {
        ParseTree.PrimType base = prim_type();
        ParseTree.TypeSpec_ arrayPart = type_spec_prime();
        return new ParseTree.TypeSpec(base, arrayPart);
    }
    
    // prim_type -> NUM | BOOL
    public ParseTree.PrimType prim_type() throws Exception {
        if (_token.type == NUM) {
            Match(NUM);
            return new ParseTree.PrimTypeNum();
        } else if (_token.type == BOOL) {
            Match(BOOL);
            return new ParseTree.PrimTypeBool();
        }
        throw new ParseException("Expected 'num' or 'bool'", _lexer.lineno, _token.col);
    }
    
    // type_spec' -> LBRACKET RBRACKET | ε
    public ParseTree.TypeSpec_ type_spec_prime() throws Exception {
        if (_token.type == LBRACKET) {
            Match(LBRACKET);
            Match(RBRACKET);
            return new ParseTree.TypeSpec_Array();
        }
        return new ParseTree.TypeSpec_Value(); // ε production
    }
    
    // params -> param_list | ε
    public List<ParseTree.Param> params() throws Exception {
        List<ParseTree.Param> list = new ArrayList<>();
        if (_token.type == NUM || _token.type == BOOL) {
            list.add(param());
            while (_token.type == COMMA) {
                Match(COMMA);
                list.add(param());
            }
        } else {
            if (_token.type != RPAREN) {
                throw new ParseException("params", _lexer.lineno, _token.col, true);
            }
        }
        return list;
    }
    
    // param_list -> param param_list' | ε
    public List<ParseTree.Param> param_list() throws Exception {
        // Not used explicitly if params() handles both cases.
        if (_token.type == NUM || _token.type == BOOL)
            return params();
        else 
            return new ArrayList<ParseTree.Param>();
    }
    
    // param -> type_spec IDENT
    public ParseTree.Param param() throws Exception {
        if (_token.type != NUM && _token.type != BOOL) {
            throw new ParseException("param", _lexer.lineno, _token.col, true);
        }
        ParseTree.TypeSpec ts = type_spec();
        if (_token.type != IDENT) {
            throw new ParseException("param", _lexer.lineno, _token.col, true);
        }
        String id = Match(IDENT);
        return new ParseTree.Param(id, ts);
    }
    
    // local_decls -> local_decls'
    public List<ParseTree.LocalDecl> local_decls() throws Exception {
        List<ParseTree.LocalDecl> list = new ArrayList<>();
        while (_token.type == NUM || _token.type == BOOL) {
            list.add(local_decl());
        }
        return list;
    }
    
    // local_decl -> type_spec IDENT SEMI
    public ParseTree.LocalDecl local_decl() throws Exception {
        ParseTree.TypeSpec ts = type_spec();
        String id = Match(IDENT);
        Match(SEMI);
        return new ParseTree.LocalDecl(id, ts);
    }
    
    // stmt_list -> stmt_list'
    public List<ParseTree.Stmt> stmt_list() throws Exception {
        List<ParseTree.Stmt> list = new ArrayList<>();
        while (isStartOfStmt(_token.type)) {
            list.add(stmt());
        }
        if (!isInFollowOfStmtList(_token.type)) {
            throw new ParseException("stmt_list'", _lexer.lineno, _token.col, true);
        }
        return list;
    }
    
    private boolean isInFollowOfStmtList(int t) {
        // For compound_stmt -> BEGIN local_decls stmt_list END, follow is { END }
        return (t == END);
    }
    
    private boolean isStartOfStmt(int t) {
        return (t == IDENT || t == PRINT || t == RETURN ||
                t == IF || t == WHILE || t == BEGIN);
    }
    
    // stmt -> assign_stmt | print_stmt | return_stmt | if_stmt | while_stmt | compound_stmt
    public ParseTree.Stmt stmt() throws Exception {
        switch (_token.type) {
            case IDENT:
                return assign_stmt();
            case PRINT:
                return print_stmt();
            case RETURN:
                return return_stmt();
            case IF:
                return if_stmt();
            case WHILE:
                return while_stmt();
            case BEGIN:
                return compound_stmt();
            default:
                throw new ParseException("stmt", _lexer.lineno, _token.col, true);
        }
    }
    
    // assign_stmt -> IDENT ASSIGN expr SEMI
    public ParseTree.Stmt assign_stmt() throws Exception {
        String id = Match(IDENT);
        Match(ASSIGN);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtAssign(id, e);
    }
    
    // print_stmt -> PRINT expr SEMI
    public ParseTree.Stmt print_stmt() throws Exception {
        Match(PRINT);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtPrint(e);
    }
    
    // return_stmt -> RETURN expr SEMI
    public ParseTree.Stmt return_stmt() throws Exception {
        Match(RETURN);
        ParseTree.Expr e = expr();
        Match(SEMI);
        return new ParseTree.StmtReturn(e);
    }
    
    // if_stmt -> IF LPAREN expr RPAREN stmt ELSE stmt
    public ParseTree.Stmt if_stmt() throws Exception {
        Match(IF);
        Match(LPAREN);
        ParseTree.Expr cond = expr();
        Match(RPAREN);
        ParseTree.Stmt thenStmt = stmt();
        Match(ELSE);
        ParseTree.Stmt elseStmt = stmt();
        return new ParseTree.StmtIf(cond, thenStmt, elseStmt);
    }
    
    // while_stmt -> WHILE LPAREN expr RPAREN stmt
    public ParseTree.Stmt while_stmt() throws Exception {
        Match(WHILE);
        Match(LPAREN);
        ParseTree.Expr cond = expr();
        Match(RPAREN);
        ParseTree.Stmt body = stmt();
        return new ParseTree.StmtWhile(cond, body);
    }
    
    // compound_stmt -> BEGIN local_decls stmt_list END
    public ParseTree.Stmt compound_stmt() throws Exception {
        Match(BEGIN);
        List<ParseTree.LocalDecl> ldecls = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        Match(END);
        return new ParseTree.StmtCompound(ldecls, stmts);
    }
    
    // expr -> term expr'
    public ParseTree.Expr expr() throws Exception {
        ParseTree.Term t = term();
        ParseTree.Expr_ e_ = expr_();
        return new ParseTree.Expr(t, e_);
    }
    
    // expr' -> EXPROP term expr' | RELOP term expr' | ε
    public ParseTree.Expr_ expr_() throws Exception {
        if (_token.type == EXPROP || _token.type == RELOP) {
            String op = _token.lexeme;
            Match(_token.type);
            ParseTree.Term t2 = term();
            ParseTree.Expr_ e2 = expr_();
            return new ParseTree.Expr_(op, t2, e2);
        }
        return new ParseTree.Expr_();
    }
    
    // term -> factor term'
    public ParseTree.Term term() throws Exception {
        ParseTree.Factor f = factor();
        ParseTree.Term_ t_ = term_();
        return new ParseTree.Term(f, t_);
    }
    
    // term' -> TERMOP factor term' | ε
    public ParseTree.Term_ term_() throws Exception {
        if (_token.type == TERMOP) {
            String op = _token.lexeme;
            Match(TERMOP);
            ParseTree.Factor f2 = factor();
            ParseTree.Term_ t2 = term_();
            return new ParseTree.Term_(op, f2, t2);
        }
        return new ParseTree.Term_();
    }
    
    // factor -> IDENT factor'
    //         | LPAREN expr RPAREN
    //         | NUM_LIT
    //         | BOOL_LIT
    //         | NEW prim_type LBRACKET expr RBRACKET
    //         | FLOAT_LIT (optional)
    public ParseTree.Factor factor() throws Exception {
        switch (_token.type) {
            case IDENT: {
                String id = Match(IDENT);
                ParseTree.Factor_ ext = factor_();
                return new ParseTree.FactorIdentExt(id, ext);
            }
            case LPAREN: {
                Match(LPAREN);
                ParseTree.Expr e = expr();
                Match(RPAREN);
                return new ParseTree.FactorParen(e);
            }
            case NUM_LIT: {
                String numText = Match(NUM_LIT);
                double numVal = Double.parseDouble(numText);
                return new ParseTree.FactorNumLit(numVal);
            }
            case BOOL_LIT: {
                String boolText = Match(BOOL_LIT);
                return new ParseTree.FactorBoolLit(boolText.equals("true"));
            }
            case FLOAT_LIT: {
                String ft = Match(FLOAT_LIT);
                double fv = Double.parseDouble(ft);
                return new ParseTree.FactorNumLit(fv);
            }
            case NEW: {
                Match(NEW);
                // Use PrimType here since prim_type() returns a PrimType
                ParseTree.PrimType p = prim_type();
                Match(LBRACKET);
                ParseTree.Expr arrSize = expr();
                Match(RBRACKET);
                // Assuming the constructor of FactorNew expects (ParseTree.PrimType, ParseTree.Expr)
                return new ParseTree.FactorNew(p, arrSize);
            }
            
            default:
                throw new ParseException("factor", _lexer.lineno, _token.col, true);
        }
    }
    
    // factor' -> LPAREN args RPAREN | LBRACKET expr RBRACKET | DOT SIZE | ε
    public ParseTree.Factor_ factor_() throws Exception {
        switch (_token.type) {
            case LPAREN: {
                Match(LPAREN);
                List<ParseTree.Arg> argList = args();
                Match(RPAREN);
                return new ParseTree.FactorIdent_ParenArgs(argList);
            }
            case LBRACKET: {
                Match(LBRACKET);
                ParseTree.Expr e = expr();
                Match(RBRACKET);
                return new ParseTree.FactorIdent_BrackExpr(e);
            }
            case DOT: {
                Match(DOT);
                Match(SIZE);
                return new ParseTree.FactorIdent_DotSize();
            }
            default:
                return new ParseTree.FactorIdent_Eps();
        }
    }
    
    // args -> arg_list | ε
    public List<ParseTree.Arg> args() throws Exception {
        if (isStartOfExpr(_token.type)) {
            return arg_list();
        } else {
            if (_token.type != RPAREN) {
                throw new ParseException("args", _lexer.lineno, _token.col, true);
            }
            return new ArrayList<ParseTree.Arg>();
        }
    }
    
    // arg_list -> expr arg_list'
    public List<ParseTree.Arg> arg_list() throws Exception {
        List<ParseTree.Arg> list = new ArrayList<>();
        ParseTree.Expr expr = expr();
        list.add(new ParseTree.Arg(expr));
        list.addAll(arg_list_());
        return list;
    }
    
    // arg_list' -> COMMA expr arg_list' | ε
    public List<ParseTree.Arg> arg_list_() throws Exception {
        switch(_token.type) {
            case COMMA:
                Match(COMMA);
                ParseTree.Expr expr = expr();
                List<ParseTree.Arg> rest = arg_list_();
                rest.add(0, new ParseTree.Arg(expr));
                return rest;
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
            default:
                throw new ParseException("arg_list'", _lexer.lineno, _token.col, true);
        }
    }
    
    private boolean isStartOfExpr(int t) {
        return (t == LPAREN || t == IDENT || t == NUM_LIT ||
                t == BOOL_LIT || t == FLOAT_LIT || t == NEW);
    }
    
    private static String tokenToString(int token) {
        switch (token) {
            case ENDMARKER: return "end-of-file";
            case LEXERROR:  return "lexical error";
            case NUM:       return "num";
            case BOOL:      return "bool";
            case BEGIN:     return "{";
            case END:       return "}";
            case LPAREN:    return "(";
            case RPAREN:    return ")";
            case SEMI:      return ";";
            case LBRACKET:  return "[";
            case RBRACKET:  return "]";
            case NUM_LIT:   return "number literal";
            case FLOAT_LIT: return "float literal";
            case IDENT:     return "identifier";
            case BOOL_LIT:  return "bool literal";
            case NEW:       return "new";
            case SIZE:      return "size";
            case ASSIGN:    return "<-";
            case PRINT:     return "print";
            case RETURN:    return "return";
            case DOT:       return ".";
            case IF:        return "if";
            case ELSE:      return "else";
            case COMMA:     return ",";
            case WHILE:     return "while";
            case RELOP:     return "relop (<,>,<=,>=,=,<>)";
            case EXPROP:    return "exprop (+, -, or)";
            case TERMOP:    return "termop (*, /, and)";
            default:        return "unknown token (" + token + ")";
        }
    }
}
