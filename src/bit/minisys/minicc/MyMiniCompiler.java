package bit.minisys.minicc;

import MyCGrammer.MyCGrammerLexer;
import MyCGrammer.MyCGrammerParser;
import bit.minisys.minicc.icgen.MyICBuilder;
import bit.minisys.minicc.ncgen.MyCodeGen;
import bit.minisys.minicc.parser.MyListener;
import bit.minisys.minicc.parser.ast.ASTNode;
import bit.minisys.minicc.scanner.MyScanner;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
public class MyMiniCompiler {
    public static void main(String[] args)throws IOException {
        String inputFile = "1_Fibonacci.c";
        InputStream is = System.in;
        is = new FileInputStream(inputFile);
        ANTLRInputStream input = new ANTLRInputStream(is);
        MyCGrammerLexer lexer = new MyCGrammerLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MyCGrammerParser parser = new MyCGrammerParser(tokens);
        ParseTree tree = parser.compilationUnit();

        String fName = inputFile.trim();
        String temp[] = fName.split("\\\\");
        String tokenFileName =temp[temp.length - 1] + ".tokens";
        MyScanner myScanner = new MyScanner(tokenFileName,tokens);

        String jsonFileName = temp[temp.length - 1] + ".json";
        ParseTreeWalker walker = new ParseTreeWalker();
        MyListener listener = new MyListener();
        listener.oFile=jsonFileName;
        walker.walk(listener, tree);

        String icfileName = temp[temp.length - 1] + ".ic";
        String errorfileName = temp[temp.length - 1] + ".error";
        String symbolName = temp[temp.length - 1] + ".symbol";

        MyICBuilder icBuilder = new MyICBuilder();
        icBuilder.Errorfilename=errorfileName;
        icBuilder.Icfilename=icfileName;
        icBuilder.Symbolname=symbolName;
        ASTNode node = listener.NodeStack.peek();
        icBuilder.test(node);

        String asmName=temp[temp.length - 1]+".asm";
        MyCodeGen codeGen = new MyCodeGen("x86",icBuilder);
        codeGen.ASMName=asmName;
        codeGen.run();
    }
}
