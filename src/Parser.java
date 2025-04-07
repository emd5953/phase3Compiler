import java.util.List;
import java.util.ArrayList;

public class Parser
{
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    public static final int NUM         = 10;
    public static final int BEGIN       = 11;
    public static final int END         = 12;
    public static final int LPAREN      = 13;
    public static final int RPAREN      = 14;
    public static final int SEMI        = 15;
    public static final int NUM_LIT     = 16;
    public static final int IDENT       = 17;

    public class Token
    {
        public int       type;
        public ParserVal attr;
        public Token(int type, ParserVal attr) {
            this.type   = type;
            this.attr   = attr;
        }
    }

    public ParserVal yylval;
    ArrayList<Token> _tokens;
    int              _lah;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String            _errormsg;
    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _tokens    = new ArrayList<Token>();
        _lah       = 0;

        // read all tokens in advance
        Lexer lex = new Lexer(r, this);
        while(true)
        {
            int token_type = lex.yylex();
            if (token_type == 0 ) { _tokens.add(new Token(Parser.ENDMARKER, null)); break; } // end of input
            if (token_type == -1) { _tokens.add(new Token(Parser.ENDMARKER, null)); break; } // error

            _tokens.add(new Token(token_type, yylval));
        }
    }

    public void Advance() throws Exception
    {
        // increase location of lah
        _lah ++;
    }

    public boolean Match(int token_type) throws Exception
    {
        Token token = _tokens.get(_lah);
        if(token.type != ENDMARKER)
            Advance();

        boolean match = (token_type == token.type);
        return  match;
    }
    public int GetTokenLocation()
    {
        // get token location for backtracking
        return _lah;
    }
    public void ResetTokenLocation(int loc)
    {
        // restore token location for backtracking
        _lah = loc;
    }

    public int yyparse() throws Exception
    {
        if(program())
            return 0;

        return -1;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //      program -> decl_list
    //    decl_list -> decl_list'
    //   decl_list' -> fun_decl decl_list'  |  eps
    //     fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
    //    prim_type -> NUM
    //       params -> eps
    //  local_decls -> local_decls'
    // local_decls' -> eps
    //    stmt_list -> stmt_list'
    //   stmt_list' -> eps
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean program() throws Exception
    {
        int loc = GetTokenLocation();

        // program -> decl_list
        ResetTokenLocation(loc);
        if(decl_list())
            if(Match(ENDMARKER))
                return true;

        return false;
    }
    public boolean decl_list() throws Exception
    {
        int loc = GetTokenLocation();

        // decl_list -> decl_list'
        ResetTokenLocation(loc);
        if(decl_list_())
            return true;

        return false;
    }
    public boolean decl_list_() throws Exception
    {
        int loc = GetTokenLocation();

        // decl_list'	-> fun_decl decl_list'
        ResetTokenLocation(loc);
        if(fun_decl())
            if(decl_list_())
                return true;

        // decl_list'	-> epsilon
        ResetTokenLocation(loc);
        return true;
    }
    public boolean fun_decl() throws Exception
    {
        int loc = GetTokenLocation();

        //     fun_decl -> prim_type IDENT LPAREN params RPAREN BEGIN local_decls stmt_list END
        ResetTokenLocation(loc);
        if(prim_type())
            if(Match(IDENT))
                if(Match(LPAREN))
                    if(params())
                        if(Match(RPAREN))
                            if(Match(BEGIN))
                                if(local_decls())
                                    if(stmt_list())
                                        if(Match(END))
                                            return true;

        return false;
    }
    public boolean prim_type() throws Exception
    {
        int loc = GetTokenLocation();

        //    prim_type -> NUM
        ResetTokenLocation(loc);
        if(Match(NUM))
            return true;

        return false;
    }

    public boolean params() throws Exception
    {
        int loc = GetTokenLocation();

        // params -> eps
        ResetTokenLocation(loc);
        return true;
    }
    public boolean local_decls() throws Exception
    {
        int loc = GetTokenLocation();

        // local_decls -> local_decls'
        ResetTokenLocation(loc);
        if(local_decls_())
            return true;

        return false;
    }
    public boolean local_decls_() throws Exception
    {
        int loc = GetTokenLocation();

        // local_decls_ -> eps
        ResetTokenLocation(loc);
        return true;
    }
    public boolean stmt_list() throws Exception
    {
        int loc = GetTokenLocation();

        // stmt_list -> stmt_list'
        ResetTokenLocation(loc);
        if(stmt_list_())
            return true;

        return false;
    }
    public boolean stmt_list_() throws Exception
    {
        int loc = GetTokenLocation();

        // stmt_list_ -> eps
        ResetTokenLocation(loc);
        return true;
    }
}
