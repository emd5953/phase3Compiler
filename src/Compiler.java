public class Compiler
{
    Parser parser;

    public Compiler(java.io.Reader r) throws Exception
    {
        parser = new Parser(r, this);
    }
    public void Compile() throws Exception
    {
        int parseresult = parser.yyparse();
            // parser.yyparse()
            // 1. parses the input
            // 2. assigns parser._parsetree the parse tree
            // 3. return 0 (indicating success)
            // 4. if syntax error occurs,
            //    assign parser._errormsg the error message
            //    and return -1

        if(parseresult == 0)
        {
            System.out.println("Success: no syntax error is found.");

            ParseTree.Program program = parser._parsetree;
            if(program != null)
            {
                // if parser._parsetree != null
                // print its parse tree as an indented code
                System.out.println();
                System.out.println("Following is the indentation-updated source code:");
                System.out.println("=================================================");
                for(String line : program.ToStringList())
                    // print indented codes
                    System.out.println(line);
            }
        }
        else if(parseresult == -1)
        {
            System.out.println("Error: There is syntax error(s).");
            if(parser._errormsg != null)
                System.out.println(parser._errormsg);
        }
    }
}
