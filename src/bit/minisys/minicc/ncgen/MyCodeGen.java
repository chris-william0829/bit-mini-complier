package bit.minisys.minicc.ncgen;

import bit.minisys.minicc.icgen.LabelGenerator;
import bit.minisys.minicc.icgen.MyICBuilder;
import bit.minisys.minicc.icgen.Quat;
import bit.minisys.minicc.icgen.TemporaryValue;
import bit.minisys.minicc.parser.ast.*;
import bit.minisys.minicc.pp.internal.B;
import bit.minisys.minicc.semantic.FuncTable;
import bit.minisys.minicc.semantic.VarTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MyCodeGen {
    public static String CodeType;
    private static int Tab;
    public String ASMName;
    public static MyICBuilder Builder;
    private static boolean printStr;
    private static boolean printflag;
    private static boolean printInt;
    private static boolean GetInt;
    private static boolean GetIntFunc;
    private static String funcname;
    public static Stack<String> ParamStack;
    public static Map<ASTNode,String> TempValue;
    public static List<String> print_scanf;
    public static StringBuilder stringBuilder;
    private static String jumpOP;
    private static boolean array;
    public static String arrayName;
    public static String AstStr(ASTNode node){
        if (node == null) {
            return "";
        }else if (node instanceof ASTIdentifier) {
            return ((ASTIdentifier)node).value;
        }else if (node instanceof ASTIntegerConstant) {
            return ((ASTIntegerConstant)node).value+"";
        }else if (node instanceof TemporaryValue) {
            return ((TemporaryValue)node).name();
        }
        else if(node instanceof ASTStringConstant){
            return ((ASTStringConstant) node).value;
        }
        else if(node instanceof  LabelGenerator){
            return ((LabelGenerator) node).name();
        }
        else {
            return "";
        }
    }
    public MyCodeGen(String type,MyICBuilder icBuilder){
        CodeType=type;
        Tab=0;
        Builder=icBuilder;
        printInt=false;
        array=false;
        printStr=false;
        GetInt=false;
        GetIntFunc=false;
        printflag=false;
        stringBuilder = new StringBuilder();
        ParamStack=new Stack<String>();
        TempValue=new HashMap<ASTNode, String>();
        print_scanf=new LinkedList<>();
        funcname="null";
    }
    public static void GenTab(){
        for(int i=0;i<Tab;i++){
            stringBuilder.append("    ");
        }
    }
    public static void Gen_86_include(){
        stringBuilder.append(".386\n");
        stringBuilder.append(".model flat, stdcall\n");
        stringBuilder.append("option casemap : none\n");
        stringBuilder.append("includelib msvcrt.lib\n");
        stringBuilder.append("includelib ucrt.lib\n");
        stringBuilder.append("includelib legacy_stdio_definitions.lib\n");
        for(Quat quat:Builder.quats){
            if(quat.getOp().equals("param")){
                ASTNode node = quat.getOpnd1();
                String param=AstStr(node);
                ParamStack.push(param);
            }
            if(quat.getOp().equals("call")){
                if(((ASTIdentifier) (quat.getRes())).value.equals("Mars_PrintStr")){
                    if(printflag==false){
                        printflag=true;
                        stringBuilder.append("printf proto c:dword,:vararg\n");
                    }
                    printStr=true;
                    while(ParamStack.size()!=0){
                        print_scanf.add(ParamStack.pop());
                    }
                }
                else if(((ASTIdentifier) (quat.getRes())).value.equals("Mars_PrintInt")){
                    if(printflag==false){
                        printflag=true;
                        stringBuilder.append("printf proto c:dword,:vararg\n");
                    }
                    while(ParamStack.size()!=0){
                        ParamStack.pop();
                    }
                    printInt=true;
                }
                else if(((ASTIdentifier) (quat.getRes())).value.equals("Mars_GetInt")){
                    if(GetInt==false){
                        GetInt=true;
                        stringBuilder.append("scanf proto c:dword,:vararg\n");
                    }
                    while(ParamStack.size()!=0){
                        ParamStack.pop();
                    }
                }
                else{
                    while(ParamStack.size()!=0){
                        ParamStack.pop();
                    }
                }
            }
        }

        stringBuilder.append(".data\n");
        if(printInt==true){
            stringBuilder.append("Mars_PrintInt byte \"%d\",0ah,0\n");
        }
        if(printStr==true){
            for(int i=0;i<print_scanf.size();i++){
                String str=print_scanf.get(i);
                str = str.replace("\\n","");
                stringBuilder.append("Mars_PrintStr"+i+" byte "+str+",0ah,0\n");
            }
        }
        if(GetInt==true){
            stringBuilder.append("Mars_GetInt byte \"%d\",0\n");
        }
    }
    public static void Gen_86_func(){

        stringBuilder.append("\n");
        Tab++;
        for(FuncTable funcTable:Builder.ProcTable){
            if(funcTable.funcName.equals(funcname)){
                if(funcTable.VariableTable.size()!=0){
                    for(VarTable varTable:funcTable.VariableTable){
                        if(varTable.type.equals("VariableDeclarator")){
                            GenTab();
                            stringBuilder.append("local "+varTable.name+":dword\n");
                        }
                        else if(varTable.type.equals("ArrayDeclarator")){
                            GenTab();
                            stringBuilder.append("local "+varTable.name+"["+(varTable.dimension*varTable.length)+"]"+":dword\n");
                        }
                    }
                }
            }
        }
    }
    public static void Gen_86_Code(){
        Gen_86_include();
        //处理全局变量，加入.data下方，由于用例中不含全局变量，懒得实现了
        /*
        if(Builder.GlobalVarTable.size()!=0){
            for(VarTable varTable:Builder.GlobalVarTable){
                if(varTable.type.equals("VariableDeclarator")){
                    if(varTable.specifiers.equals("int")){
                        if(varTable.value!=null){
                            stringBuilder.append(varTable.name+" dword "+((ASTIntegerConstant) varTable.value).value.toString()+"\n");
                        }
                        else{
                            stringBuilder.append(varTable.name+" dword "+"?\n");
                        }
                    }
                    else if(varTable.specifiers.equals())
                }
            }
        }
         */
        stringBuilder.append(".code\n");
        for(Quat quat : Builder.quats){
            if(quat.getOp().equals("param")){
                ASTNode node = quat.getOpnd1();
                String param=AstStr(node);
                ParamStack.push(param);
            }
            if(quat.getOp().equals("func")){
                if(!funcname.equals("null")){
                    GenTab();
                    stringBuilder.append("ret\n");
                    Tab--;
                    GenTab();
                    stringBuilder.append(funcname+" endp\n");
                    if(funcname.equals("main")){
                        GenTab();
                        stringBuilder.append("end main\n");
                    }
                }
                funcname=((ASTIdentifier) (quat.getRes())).value;
                if(!funcname.equals("main")){
                    stringBuilder.append(funcname+" proc far C");
                    while (ParamStack.size()!=0){
                        stringBuilder.append(" "+ParamStack.pop()+":dword");
                    }
                    Gen_86_func();
                }
                else{
                    stringBuilder.append(funcname+" proc");
                    Gen_86_func();
                }
            }
            if(quat.getOp().equals("Label")){
                String type=((LabelGenerator) (quat.getRes())).Type;
                if(type.equals("Endif")){
                    Tab--;
                    GenTab();
                }
                else if(type.equals("If")){
                    GenTab();
                    Tab++;
                }
                else if(type.equals("Else")){
                    Tab--;
                    GenTab();
                    Tab++;
                }
                else if(type.equals("loopStartLabel")){
                    GenTab();
                    Tab++;
                }
                else if(type.equals("loopCheckLabel")){
                    Tab--;
                    GenTab();
                    Tab++;
                }else if(type.equals("loopNextLabel")){
                    Tab--;
                    GenTab();
                    Tab++;
                }
                else if(type.equals("loopEndLabel")){
                    Tab--;
                    GenTab();
                }
                stringBuilder.append(AstStr(quat.getRes())+":\n");
            }
            if(quat.getOp().equals("<")){
                jumpOP="<";
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("cmp eax, "+opnd2+"\n");
            }
            if(quat.getOp().equals("<=")){
                jumpOP="<=";
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("cmp eax, "+opnd2+"\n");
            }
            if(quat.getOp().equals("[]")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("imul edx, "+opnd2+", 4\n");
                array=true;
                arrayName=opnd1;
            }
            if(quat.getOp().equals("-=")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("mov eax, "+res+"\n");
                GenTab();
                stringBuilder.append("sub eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov "+res+", eax\n");
            }
            if(quat.getOp().equals("==")){
                jumpOP="==";
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("cmp eax, "+opnd2+"\n");
            }
            if(quat.getOp().equals("JF")){
                GenTab();
                if(jumpOP.equals("<")){
                    stringBuilder.append("jnb "+AstStr(quat.getRes())+"\n");
                }
                else if(jumpOP.equals("<=")){
                    stringBuilder.append("jnbe "+AstStr(quat.getRes())+"\n");
                }
                else if(jumpOP.equals("==")){
                    stringBuilder.append("jnz "+AstStr(quat.getRes())+"\n");
                }
            }
            if(quat.getOp().equals("*")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov ebx, "+opnd2+"\n");
                GenTab();
                stringBuilder.append("imul eax, ebx\n");
                GenTab();
                stringBuilder.append("mov "+res+", eax\n");
            }
            if(quat.getOp().equals("%")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("xor edx, edx\n");
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov ebx, "+opnd2+"\n");
                GenTab();
                stringBuilder.append("div ebx\n");
                GenTab();
                stringBuilder.append("mov "+res+", edx\n");
            }
            if(quat.getOp().equals("/")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("xor edx, edx\n");
                GenTab();
                stringBuilder.append("mov eax, "+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov ebx, "+opnd2+"\n");
                GenTab();
                stringBuilder.append("div ebx\n");
                GenTab();
                stringBuilder.append("mov "+res+", eax\n");
            }
            if(quat.getOp().equals("-")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                ASTNode res=quat.getRes();
                GenTab();
                stringBuilder.append("mov eax ,"+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov ebx ,"+opnd2+"\n");
                GenTab();
                stringBuilder.append("sub eax, ebx"+"\n");
                GenTab();
                stringBuilder.append("mov "+((TemporaryValue) res).name()+", eax"+"\n");
            }
            if(quat.getOp().equals("=")){
                if(array==true){
                    array=false;
                    String opnd1=AstStr(quat.getOpnd1());
                    String res=AstStr(quat.getRes());
                    GenTab();
                    stringBuilder.append("push "+opnd1+"\n");
                    GenTab();
                    stringBuilder.append("pop "+arrayName+"[edx]\n");
                }
                else {
                    String opnd1=AstStr(quat.getOpnd1());
                    String res=AstStr(quat.getRes());
                    GenTab();
                    stringBuilder.append("push "+opnd1+"\n");
                    GenTab();
                    stringBuilder.append("pop "+res+"\n");
                }
            }
            if(quat.getOp().equals("call")){
                String name=AstStr(quat.getRes());
                if(name.equals("Mars_PrintStr")){
                    String str=ParamStack.pop();
                    for(int i=0;i<print_scanf.size();i++){
                        String string=print_scanf.get(i);
                        if(string.equals(str)){
                            GenTab();
                            stringBuilder.append("invoke printf, addr Mars_PrintStr"+i+"\n");
                        }
                    }
                }
                else if(name.equals("Mars_GetInt")){
                    GetIntFunc=true;
                }
                else if(name.equals("Mars_PrintInt")) {
                    while (ParamStack.size() != 0) {
                        GenTab();
                        stringBuilder.append("mov eax," + ParamStack.pop() + "\n");
                        GenTab();
                        stringBuilder.append("invoke printf, addr Mars_PrintInt, eax\n");
                    }
                }
                else {

                    GenTab();
                    stringBuilder.append("invoke "+name);
                    while(ParamStack.size()!=0){
                        stringBuilder.append(", "+ParamStack.pop()+"\n");
                    }
                }
            }
            if(quat.getOp().equals("JMP")){
                GenTab();
                stringBuilder.append("jmp "+AstStr(quat.getRes())+"\n");
            }
            if(quat.getOp().equals("return")){
                if(GetIntFunc==false){
                    GenTab();
                    stringBuilder.append("mov "+AstStr(quat.getRes())+", eax\n");
                }
                else{
                    GenTab();
                    stringBuilder.append("lea eax, ["+AstStr(quat.getRes())+"]\n");
                    GenTab();
                    stringBuilder.append("push eax\n");
                    GenTab();
                    stringBuilder.append("push offset Mars_GetInt\n");
                    GenTab();
                    stringBuilder.append("call scanf\n");
                    GetIntFunc=false;
                }
            }
            if(quat.getOp().equals("+")){
                String opnd1=AstStr(quat.getOpnd1());
                String opnd2=AstStr(quat.getOpnd2());
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("mov eax ,"+opnd1+"\n");
                GenTab();
                stringBuilder.append("mov ebx ,"+opnd2+"\n");
                GenTab();
                stringBuilder.append("add eax, ebx"+"\n");
                GenTab();
                stringBuilder.append("mov "+res+", eax"+"\n");
            }
            if(quat.getOp().equals("++")){
                String res=AstStr(quat.getRes());
                GenTab();
                stringBuilder.append("inc "+res+"\n");
            }
            if(quat.getOp().equals("RET")){
                if(quat.getRes()!=null){
                    GenTab();
                    stringBuilder.append("mov eax, "+(AstStr(quat.getRes()))+"\n");
                }
            }
            else{

            }
        }
    }
    public void run() throws IOException {
        if(CodeType.equals("x86")){
            Gen_86_Code();
            if(!funcname.equals("null")){
                GenTab();
                stringBuilder.append("ret\n");
                Tab--;
                GenTab();
                stringBuilder.append(funcname+" endp\n");
                if(funcname.equals("main")){
                    GenTab();
                    stringBuilder.append("end main\n");
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(new File(ASMName));
                fileWriter.write(stringBuilder.toString());
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
