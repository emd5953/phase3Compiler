/////////////////////////////////////////////////////////////////////////////////////////
//  MIT License                                                                        //
//                                                                                     //
//  Copyright (c) 2022 Hyuntae Na                                                      //
//                                                                                     //
//  Permission is hereby granted, free of charge, to any person obtaining a copy       //
//  of this software and associated documentation files (the "Software"), to deal      //
//  in the Software without restriction, including without limitation the rights       //
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell          //
//  copies of the Software, and to permit persons to whom the Software is              //
//  furnished to do so, subject to the following conditions:                           //
//                                                                                     //
//  The above copyright notice and this permission notice shall be included in all     //
//  copies or substantial portions of the Software.                                    //
//                                                                                     //
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR         //
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,           //
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE        //
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER             //
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,      //
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE      //
//  SOFTWARE.                                                                          //
/////////////////////////////////////////////////////////////////////////////////////////

import java.util.List;
import java.util.ArrayList;

public class ParseTree
{
    public static abstract class Node
    {
        abstract public String[] ToStringList() throws Exception; // This is used to print conde with indentation and comments
    }
    public static abstract class NodeString extends Node
    {
        abstract public String ToString();
        public String[] ToStringList() throws Exception
        {
            return new String[] { ToString() };
        }
    }
    public static <T> String NodeListToString(List<? extends NodeString> nodes, String separator)
    {
        String str = "";
        for(int i=0; i<nodes.size(); i++)
        {
            if(i == 0) str +=           nodes.get(i).ToString();
            else       str += separator+nodes.get(i).ToString();
        }
        return str;
    }

    public static class Program extends Node
    {
        //public ParseTreeInfo.ProgramInfo info = new ParseTreeInfo.ProgramInfo();
        public List<FuncDecl> funcs;
        public Program(List<FuncDecl> funcs)
        {
            this.funcs = funcs;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            for(var func : funcs)
            {
                if(strs.size() != 0)
                    strs.add("");
                for(String str : func.ToStringList())
                    strs.add(str);
            }
            return strs.toArray(String[]::new);
        }
    }
    public static class FuncDecl extends Node
    {
        //public ParseTreeInfo.FuncDefnInfo info = new ParseTreeInfo.FuncDefnInfo();
        public String          ident     ;
        public TypeSpec        rettype   ;
        public List<Param    > params    ;
        public List<LocalDecl> localdecls;
        public List<Stmt     > stmtlist  ;
        public FuncDecl(String ident, TypeSpec rettype, List<Param> params, List<LocalDecl> localdecls, List<Stmt> stmtlist)
        {
            this.ident      = ident     ;
            this.rettype    = rettype   ;
            this.params     = params    ;
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public String[] ToStringList() throws Exception
        {
            String head = rettype.ToString() + " " + ident + "(" + NodeListToString(params,", ") + ")";

            ArrayList<String> strs = new ArrayList<String>();
            strs.add(head);
            strs.add("{");
            for(var localdecl : localdecls)
                strs.add("    " + localdecl.ToString());
            for(var stmt : stmtlist)
                for(String str : stmt.ToStringList())
                    strs.add("    "+str);
            strs.add("}");

            return strs.toArray(String[]::new);
        }
    }
    public static class Param extends NodeString
    {
        //public ParseTreeInfo.ParamInfo info = new ParseTreeInfo.ParamInfo();
        public String   ident   ;
        public TypeSpec typespec;
        public Param(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String ToString() { return typespec.ToString() + " " + ident; }
    }
    public static class TypeSpec extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public PrimType  type;
        public TypeSpec_ spec;
        public TypeSpec(PrimType type, TypeSpec_ spec) { this.type = type; this.spec = spec; }
        public String ToString() { return type.ToString()+spec.ToString(); }
    }
    public abstract static class TypeSpec_ extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        abstract public String ToString();
    }
    public static class TypeSpec_Value extends TypeSpec_
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public String ToString() { return ""; }
    }
    public static class TypeSpec_Array extends TypeSpec_
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        public String ToString() { return "[]"; }
    }
    public abstract static class PrimType extends NodeString
    {
        //public ParseTreeInfo.TypeSpecInfo info = new ParseTreeInfo.TypeSpecInfo();
        abstract public String ToString();
    }
    public static class PrimTypeNum  extends PrimType { public String ToString() { return "num" ; } }
    public static class PrimTypeBool extends PrimType { public String ToString() { return "bool"; } }
    public static class PrimTypeVoid extends PrimType { public String ToString() { return "void"; } }
    public static class LocalDecl extends Node
    {
        //public ParseTreeInfo.LocalDeclInfo info = new ParseTreeInfo.LocalDeclInfo();
        public String   ident   ;
        public TypeSpec typespec;
        public LocalDecl(String ident, TypeSpec typespec)
        {
            this.ident    = ident   ;
            this.typespec = typespec;
        }
        public String[] ToStringList() throws Exception { return new String[] { ToString() }; }
        public String ToString()
        {
            String str = typespec.ToString() + " " + ident + ";";
            return str;
        }
    }
    public abstract static class Stmt extends Node
    {
        //public ParseTreeInfo.StmtStmtInfo info = new ParseTreeInfo.StmtStmtInfo();
        abstract public String[] ToStringList() throws Exception;
    }
    public static class StmtAssign extends Stmt
    {
        public String  ident;
        public Expr    expr ;
        public StmtAssign(String ident, Expr expr)
        {
            this.ident = ident;
            this.expr  = expr ;
        }
        public String[] ToStringList() throws Exception 
        {
            String str = ident;
            str  += " <- " + expr.ToString() + ";";
            return new String[] { str };
        }
    }
    public static class StmtPrint extends Stmt
    {
        public Expr expr;
        public StmtPrint(Expr expr)
        {
            this.expr = expr;
        }
        public String[] ToStringList() throws Exception
        {
            return new String[]
            {
                "print " + expr.ToString() + ";"
            };
        }
    }
    public static class StmtReturn extends Stmt
    {
        public Expr expr;
        public StmtReturn(Expr expr)
        {
            this.expr = expr;
        }
        public String[] ToStringList() throws Exception
        {
            return new String[]
            {
                "return " + expr.ToString() + ";"
            };
        }
    }
    public static class StmtIf extends Stmt
    {
        public Expr       cond     ;
        public Stmt       thenstmt ;
        public Stmt       elsestmt ;
        public StmtIf(Expr cond, Stmt thenstmt, Stmt elsestmt)
        {
            this.cond      = cond     ;
            this.thenstmt  = thenstmt ;
            this.elsestmt  = elsestmt ;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("if ( " + cond.ToString() + " )");
            {
                String indent = (thenstmt instanceof StmtCompound)? "" : "    ";
                for(String str : thenstmt.ToStringList())
                    strs.add(indent+str);
            }
            strs.add("else");
            {
                String indent = (elsestmt instanceof StmtCompound)? "" : "    ";
                for(String str : elsestmt.ToStringList())
                    strs.add(indent+str);
            }
            return strs.toArray(String[]::new);
        }
    }
    public static class StmtWhile extends Stmt
    {
        public Expr       cond ;
        public Stmt       stmt ;
        public StmtWhile(Expr cond, Stmt stmt)
        {
            this.cond  = cond ;
            this.stmt  = stmt ;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("while ( " + cond.ToString() + " )");
            {
                String indent = (stmt instanceof StmtCompound)? "" : "    ";
                for(String str : stmt.ToStringList())
                    strs.add(indent+str);
            }
            return strs.toArray(String[]::new);
        }
    }
    public static class StmtCompound extends Stmt
    {
        public List<LocalDecl> localdecls;
        public List<Stmt     > stmtlist  ;
        public StmtCompound(List<LocalDecl> localdecls, List<Stmt> stmtlist)
        {
            this.localdecls = localdecls;
            this.stmtlist   = stmtlist  ;
        }
        public String[] ToStringList() throws Exception
        {
            ArrayList<String> strs = new ArrayList<String>();
            strs.add("{");
            for(LocalDecl localdecl : localdecls)
                strs.add("    " + localdecl.ToString());
            for(Stmt stmt : stmtlist)
                for(String str : stmt.ToStringList())
                    strs.add("    "+str);
            strs.add("}");
            return strs.toArray(String[]::new);
        }
    }
    public static class Arg extends NodeString
    {
        //public ParseTreeInfo.ArgInfo info = new ParseTreeInfo.ArgInfo();
        public Expr expr;
        public Arg(Expr expr)    { this.expr = expr;       }
        public String ToString() { return expr.ToString(); }
    }
    public static class Expr extends NodeString
    {
        public Term  term ;
        public Expr_ expr_;
        public Expr(Term term, Expr_ expr_)
        {
            this.term  = term ;
            this.expr_ = expr_;
        }
        public String ToString()
        {
            String str = term.ToString();
            if(expr_ != null) str += expr_.ToString();
            return str;
        }
    }
    public static class Expr_ extends NodeString
    {
        public String op   ;
        public Term   term ;
        public Expr_  expr_;
        public Expr_()
        {
            // for "expr_ -> eps"
            this.op    = null;
            this.term  = null;
            this.expr_ = null;
        }
        public Expr_(String op, Term term, Expr_ expr_)
        {
            // for "expr_ -> EXPROP term expr_"
            // for "expr_ -> RELOP term expr_"
            this.op    = op   ;
            this.term  = term ;
            this.expr_ = expr_;
        }
        public String ToString()
        {
            if(op==null && term==null && expr_==null)
            {
                return "";
            }
            else
            {
                String opstr = " " + op + " ";
                String str = opstr + term.ToString();
                if(expr_ != null) str += expr_.ToString();
                return str;
            }
        }
    }
    public static class Term extends NodeString
    {
        public Factor factor;
        public Term_  term_ ;
        public Term(Factor factor, Term_  term_)
        {
            this.factor = factor;
            this.term_  = term_ ;
        }
        public String ToString()
        {
            String str = factor.ToString();
            if(term_ != null) str += term_.ToString();
            return str;
        }
    }
    public static class Term_ extends NodeString
    {
        public String op;
        public Factor factor;
        public Term_  term_ ;
        public Term_()
        {
            // for "term_ -> eps"
            this.op     = null;
            this.factor = null;
            this.term_  = null;
        }
        public Term_(String op, Factor factor, Term_  term_)
        {
            // for "term_ -> TERMOP factor term_"
            this.op     = op    ;
            this.factor = factor;
            this.term_  = term_ ;
        }
        public String ToString()
        {
            if(op==null && factor==null && term_==null)
            {
                return "";
            }
            else
            {
                String opstr = " " + op + " ";
                String str = opstr + factor.ToString();
                if(term_ != null) str += term_.ToString();
                return str;
            }
        }
    }
    public static abstract class Factor extends NodeString
    {
    }
    public static class FactorParen extends Factor
    {
        public Expr expr;
        public FactorParen(Expr expr) { this.expr = expr; }
        public String ToString() { return "( " + expr.ToString() + " )"; }
    }
    public static class FactorIdentExt extends Factor
    {
        public String  ident;
        public Factor_ factor_;
        public FactorIdentExt(String ident, Factor_ factor_) { this.ident = ident; this.factor_ = factor_; }
        public String ToString() { return ident + factor_.ToString(); }
    }
    public static class FactorNumLit extends Factor
    {
        public Double numlit;
        public FactorNumLit(double numlit) { this.numlit = numlit; }
        public String ToString()
        {
            if(numlit.intValue() == numlit)
                return (Integer.valueOf(numlit.intValue())).toString();
            return numlit.toString();
        }
    }
    public static class FactorBoolLit extends Factor
    {
        public Boolean boollit;
        public FactorBoolLit(boolean boollit) { this.boollit = boollit; }
        public String ToString() { return boollit.toString(); }
    }
    public static class FactorNew extends Factor
    {
        public PrimType type;
        public Expr     expr;
        public FactorNew(PrimType type, Expr expr) { this.type = type; this.expr = expr; }
        public String ToString()
        {
            String str = "new " + type.ToString() + " [ " + expr.ToString() + " ]";
            return str;
        }
    }
    public static abstract class Factor_ extends NodeString
    {
    }
    public static class FactorIdent_ParenArgs extends Factor_
    {
        public List<Arg> args ;
        public FactorIdent_ParenArgs(List<Arg> args) { this.args = args; }
        public String ToString()
        {
            String str = "( " + NodeListToString(args,", ") + " )";
            return str;
        }
    }
    public static class FactorIdent_BrackExpr extends Factor_
    {
        public Expr      expr;
        public FactorIdent_BrackExpr(Expr expr) { this.expr = expr; }
        public String ToString()
        {
            String str = "[ " + expr.ToString() + " ]";
            return str;
        }
    }
    public static class FactorIdent_DotSize extends Factor_
    {
        public FactorIdent_DotSize() { }
        public String ToString()     { return ".size"; }
    }
    public static class FactorIdent_Eps extends Factor_
    {
        public FactorIdent_Eps() { }
        public String ToString() { return ""; }
    }
}
