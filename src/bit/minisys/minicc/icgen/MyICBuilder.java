package bit.minisys.minicc.icgen;

import bit.minisys.minicc.parser.ast.*;
import bit.minisys.minicc.semantic.FuncTable;
import bit.minisys.minicc.semantic.VarTable;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MyICBuilder {
    public String Errorfilename;
    public String Icfilename;
    public String Symbolname;
    public static List<FuncTable> ProcTable = new LinkedList<>();
    public static List<VarTable> GlobalVarTable = new LinkedList<>();
    public static List<String> ErrorTable = new LinkedList<>();
    public static int tmpRegID=0;
    public static int tmpLabelID=1;
    public static int IfLabelID=0;
    public static Map<ASTNode, ASTNode> map;
    public static List<Quat> quats;
    private static ASTNode loopStartLabel=null;
    private static ASTNode loopNextLabel=null;
    private static ASTNode loopEndLabel=null;
    private static Map<String,ASTNode> Label;
    public static List<VarTable> Scope=new LinkedList<>();
    public static Stack<List<VarTable>> Var = new Stack<>();
    public MyICBuilder() {
        map = new HashMap<ASTNode, ASTNode>();
        Label=new HashMap<String,ASTNode>();
        quats = new LinkedList<Quat>();
        tmpRegID = 0;
    }

    public static void visit(ASTCompilationUnit astCompilationUnit){
        for(ASTNode child : astCompilationUnit.items){
            if(child instanceof ASTDeclaration){
                visit((ASTDeclaration) child);
            }
            else if(child instanceof ASTFunctionDefine){
                visit((ASTFunctionDefine) child);
            }
        }
    }
    public static void GetfuncDeclara(FuncTable funcTable,ASTFunctionDeclarator declarator){
        ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator) declarator.declarator;
        ASTIdentifier identifier=variableDeclarator.identifier;
        funcTable.funcName=identifier.value;
        funcTable.type=declarator.getType();
    }
    public static void GetVarTable(VarTable varTable,ASTInitList node){
        ASTNode child = node.declarator;
        if(child.getClass()==ASTVariableDeclarator.class){
            ASTIdentifier identifier=((ASTVariableDeclarator) child).identifier;
            varTable.name=identifier.value;
            varTable.dimension=1;
            varTable.length=1;
            varTable.type=child.getType();
            if(node.exprs!=null){
                varTable.value = node.exprs.get(0);
                visit(varTable.value);
                ASTNode opnd=map.get(varTable.value);
                Quat quat = new Quat("=",identifier,opnd,null);
                quats.add(quat);
            }
        }
        else if(child.getClass()==ASTArrayDeclarator.class){
            varTable.type=child.getType();
            int dim=0;
            int length=1;
            while (child.getClass()!=ASTVariableDeclarator.class){
                ASTNode integer=((ASTArrayDeclarator) child).expr;
                length=length*((ASTIntegerConstant) integer).value;
                dim=dim+1;
                child=((ASTArrayDeclarator) child).declarator;
            }
            ASTIdentifier identifier=((ASTVariableDeclarator) child).identifier;
            varTable.name=identifier.value;
            varTable.dimension=dim;
            varTable.length=length;
        }
    }
    public static void visit(ASTDeclaration astDeclaration){
        if(astDeclaration.parent.getClass()==ASTCompilationUnit.class){
            ASTToken astToken = astDeclaration.specifiers.get(0);
            for(ASTInitList astInitList : astDeclaration.initLists){
                ASTDeclarator declarator = astInitList.declarator;
                if(declarator.getClass()==ASTFunctionDeclarator.class){
                    FuncTable funcTable = new FuncTable();
                    funcTable.VariableTable=new LinkedList<>();
                    funcTable.specifiers=astDeclaration.specifiers.get(0).value;
                    GetfuncDeclara(funcTable,(ASTFunctionDeclarator) declarator);
                    ProcTable.add(funcTable);
                }
                else{
                    VarTable varTable = new VarTable();
                    varTable.specifiers=astToken.value;
                    GetVarTable(varTable,astInitList);
                    boolean flag=true;
                    for (VarTable var:GlobalVarTable){
                        if(var.name.equals(varTable.name)){
                            flag=false;
                            break;
                        }
                    }
                    if(!flag){
                        String Error="ES02: var_"+varTable.name+"_defined_again\n";
                        ErrorTable.add(Error);
                    }
                    GlobalVarTable.add(varTable);
                }
            }
        }
        else if(astDeclaration.parent.getClass()==ASTIterationDeclaredStatement.class){
            ASTToken astToken = astDeclaration.specifiers.get(0);
            for(ASTInitList astInitList : astDeclaration.initLists){
                ASTDeclarator declarator = astInitList.declarator;
                VarTable varTable = new VarTable();
                varTable.specifiers=astToken.value;
                GetVarTable(varTable,astInitList);
                boolean flag=true;
                for (VarTable var:Scope){
                    if(var.name.equals(varTable.name)){
                        flag=false;
                        break;
                    }
                }
                if(!flag){
                    String Error="ES02: var_"+varTable.name+"_defined_again\n";
                    ErrorTable.add(Error);
                }
                Scope.add(varTable);
            }
        }
        else if(astDeclaration.parent.getClass()==ASTCompoundStatement.class){
            ASTToken astToken = astDeclaration.specifiers.get(0);
            for(ASTInitList astInitList : astDeclaration.initLists){
                ASTDeclarator declarator = astInitList.declarator;
                VarTable varTable = new VarTable();
                varTable.specifiers=astToken.value;
                GetVarTable(varTable,astInitList);
                boolean flag=true;
                for (VarTable var:Scope){
                    if(var.name.equals(varTable.name)){
                        flag=false;
                        break;
                    }
                }
                if(!flag){
                    String Error="ES02: var_"+varTable.name+"_defined_again\n";
                    ErrorTable.add(Error);
                }
                Scope.add(varTable);
            }
        }
        else{
            ASTCompoundStatement astCompoundStatement = (ASTCompoundStatement) astDeclaration.parent;
            ASTFunctionDefine astFunctionDefine = (ASTFunctionDefine) astCompoundStatement.parent;
            ASTFunctionDeclarator astFunctionDeclarator = (ASTFunctionDeclarator) astFunctionDefine.declarator;
            ASTVariableDeclarator astVariableDeclarator = (ASTVariableDeclarator) astFunctionDeclarator.declarator;
            ASTIdentifier astIdentifier = astVariableDeclarator.identifier;
            for(ASTInitList astInitList : astDeclaration.initLists){
                VarTable varTable = new VarTable();
                varTable.specifiers=astDeclaration.specifiers.get(0).value;
                GetVarTable(varTable,astInitList);
                for(FuncTable funcTable : ProcTable){
                    if(funcTable.funcName.equals(astIdentifier.value)){
                        funcTable.VariableTable.add(varTable);
                    }
                }
            }
        }
    }
    public static void visit(ASTFunctionDefine astFunctionDefine){
        FuncTable funcTable = new FuncTable();
        funcTable.VariableTable=new LinkedList<>();
        funcTable.type=astFunctionDefine.getType();
        ASTFunctionDeclarator astFunctionDeclarator = (ASTFunctionDeclarator) astFunctionDefine.declarator;
        ASTVariableDeclarator astVariableDeclarator = (ASTVariableDeclarator) astFunctionDeclarator.declarator;
        for(int i=0;i<astFunctionDeclarator.params.size();i++){
            VarTable varTable = new VarTable();
            ASTParamsDeclarator astParamsDeclarator=astFunctionDeclarator.params.get(i);
            ASTVariableDeclarator param = (ASTVariableDeclarator) astParamsDeclarator.declarator;
            ASTIntegerConstant astIntegerConstant=new ASTIntegerConstant();
            astIntegerConstant.value=i;
            ASTNode opnd1 = param.identifier;
            varTable.type=astParamsDeclarator.getType();
            varTable.specifiers=astParamsDeclarator.specfiers.get(0).value;
            varTable.name=param.identifier.value;

            funcTable.VariableTable.add(varTable);
            Quat quat = new Quat("param",astIntegerConstant,opnd1,null);
            quats.add(quat);
        }
        ASTIdentifier astIdentifier = astVariableDeclarator.identifier;
        funcTable.funcName=astIdentifier.value;
        boolean flag=true;
        for(FuncTable func:ProcTable){
            if(func.funcName.equals(funcTable.funcName)){
                flag=false;
                break;
            }
        }
        if(!flag){
            String Error="ES02: Func_"+funcTable.funcName+"_defined_again\n";
            ErrorTable.add(Error);
        }
        funcTable.specifiers=astFunctionDefine.specifiers.get(0).value;
        ProcTable.add(funcTable);
        quats.add(new Quat("func",astIdentifier,null,null));
        ASTCompoundStatement astCompoundStatement = astFunctionDefine.body;

        visit(astCompoundStatement);
    }

    /**
     * Expression
     *    @JsonSubTypes.Type(value = ASTIdentifier.class,name = "Identifier"),
     *    @JsonSubTypes.Type(value = ASTArrayAccess.class,name = "ArrayAccess"),
     *    @JsonSubTypes.Type(value = ASTBinaryExpression.class,name="BinaryExpression"),
     *    @JsonSubTypes.Type(value = ASTCastExpression.class,name = "CastExpression"),              TODO
     *    @JsonSubTypes.Type(value = ASTCharConstant.class,name = "CharConstant"),                  UNDO
     *    @JsonSubTypes.Type(value = ASTConditionExpression.class,name = "ConditionExpression"),    UNDO
     *    @JsonSubTypes.Type(value = ASTFloatConstant.class,name = "FloatConstant"),                UNDO
     *    @JsonSubTypes.Type(value = ASTFunctionCall.class,name = "FunctionCall"),
     *    @JsonSubTypes.Type(value = ASTIntegerConstant.class,name = "IntegerConstant"),
     *    @JsonSubTypes.Type(value = ASTMemberAccess.class,name = "MemberAccess"),                  UNDO
     *    @JsonSubTypes.Type(value = ASTPostfixExpression.class,name = "PostfixExpression"),
     *    @JsonSubTypes.Type(value = ASTStringConstant.class,name = "StringConstant"),
     *    @JsonSubTypes.Type(value = ASTUnaryExpression.class,name = "UnaryExpression"),
     *    @JsonSubTypes.Type(value = ASTUnaryTypename.class,name = "UnaryTypename")                 UNDO
     */


    public static void visit(ASTExpression expression){
        if(expression instanceof ASTArrayAccess) {
            visit((ASTArrayAccess)expression);
        }else if(expression instanceof ASTBinaryExpression) {
            visit((ASTBinaryExpression)expression);
        }else if(expression instanceof ASTCastExpression) {
            visit((ASTCastExpression)expression);
        }else if(expression instanceof ASTCharConstant) {
            visit((ASTCharConstant)expression);
        }else if(expression instanceof ASTConditionExpression) {
            visit((ASTConditionExpression)expression);
        }else if(expression instanceof ASTFloatConstant) {
            visit((ASTFloatConstant)expression);
        }else if(expression instanceof ASTFunctionCall) {
            visit((ASTFunctionCall)expression);
        }else if(expression instanceof ASTIdentifier) {
            visit((ASTIdentifier)expression);
        }else if(expression instanceof ASTIntegerConstant) {
            visit((ASTIntegerConstant)expression);
        }else if(expression instanceof ASTMemberAccess) {
            visit((ASTMemberAccess)expression);
        }else if(expression instanceof ASTPostfixExpression) {
            visit((ASTPostfixExpression)expression);
        }else if(expression instanceof ASTStringConstant) {
            visit((ASTStringConstant)expression);
        }else if(expression instanceof ASTUnaryExpression) {
            visit((ASTUnaryExpression)expression);
        }else if(expression instanceof ASTUnaryTypename){
            visit((ASTUnaryTypename)expression);
        }
    }
    public static void visit(ASTUnaryExpression astUnaryExpression){
        String op = astUnaryExpression.op.value;
        ASTNode res = null;
        ASTNode opnd1 = null;
        ASTNode opnd2 = null;
        visit(astUnaryExpression.expr);
        opnd1=map.get(astUnaryExpression.expr);
        Quat quat = new Quat(op,opnd1,null,opnd1);
        quats.add(quat);
        map.put(astUnaryExpression,opnd1);
        /*
        if(op.equals("++")||op.equals("--")){
            visit(astUnaryExpression.expr);
            opnd1=map.get(astUnaryExpression.expr);
            Quat quat = new Quat(op,opnd1,null,opnd1);
            quats.add(quat);
            map.put(astUnaryExpression,opnd1);
        }
        else {
            res = new TemporaryValue(++tmpRegID);
            visit(astUnaryExpression.expr);
            opnd1=map.get(astUnaryExpression.expr);
            Quat quat = new Quat(op,res,null,opnd1);
            quats.add(quat);
            map.put(astUnaryExpression,res);
        }*/
    }
    public static void visit(ASTPostfixExpression astPostfixExpression){
        String op = astPostfixExpression.op.value;
        ASTNode res = null;
        ASTNode opnd1 = null;
        ASTNode opnd2 = null;
        visit(astPostfixExpression.expr);
        opnd1=map.get(astPostfixExpression.expr);
        Quat quat = new Quat(op,opnd1,null,opnd1);
        quats.add(quat);
        map.put(astPostfixExpression,opnd1);
        /*
        if(op.equals("++")||op.equals("--")){
            visit(astPostfixExpression.expr);
            opnd1=map.get(astPostfixExpression.expr);
            Quat quat = new Quat(op,opnd1,opnd1,null);
            quats.add(quat);
            map.put(astPostfixExpression,opnd1);
        }
        else {
            res = new TemporaryValue(++tmpRegID);
            visit(astPostfixExpression.expr);
            opnd1=map.get(astPostfixExpression.expr);
            Quat quat = new Quat(op,res,opnd1,null);
            quats.add(quat);
            map.put(astPostfixExpression,res);
        }*/
    }
    public static void visit(ASTBinaryExpression astBinaryExpression){
        String op = astBinaryExpression.op.value;
        ASTNode res = null;
        ASTNode opnd1 = null;
        ASTNode opnd2 = null;
        if (op.equals("=")||op.equals("+=")||op.equals("-=")||op.equals("*=") ||op.equals("/=") || op.equals("%=") || op.equals("<<=") ||op.equals(">>=") ||op.equals("&=") ||op.equals("^=") ||op.equals("|=")) {
            visit(astBinaryExpression.expr1);
            res = map.get(astBinaryExpression.expr1);
            if (astBinaryExpression.expr2 instanceof ASTIdentifier) {
                opnd1 = astBinaryExpression.expr2;
            }

            else if(astBinaryExpression.expr2 instanceof ASTIntegerConstant) {
                opnd1 = astBinaryExpression.expr2;
            }
            else if(astBinaryExpression.expr2 instanceof ASTStringConstant) {
                opnd1 = astBinaryExpression.expr2;
            }
            else if(astBinaryExpression.expr2 instanceof ASTBinaryExpression) {
                ASTBinaryExpression value = (ASTBinaryExpression) astBinaryExpression.expr2;
                op = value.op.value;
                visit(value.expr1);
                opnd1 = map.get(value.expr1);
                visit(value.expr2);
                opnd2 = map.get(value.expr2);
            }

            else if(astBinaryExpression.expr2 instanceof ASTUnaryExpression){
                ASTUnaryExpression value = (ASTUnaryExpression) astBinaryExpression.expr2;
                op = value.op.value;
                visit(value);
                opnd1 = map.get(value.expr);
            }
            else if(astBinaryExpression.expr2 instanceof ASTPostfixExpression){
                ASTPostfixExpression value = (ASTPostfixExpression) astBinaryExpression.expr2;
                op = value.op.value;
                visit(value);
                opnd2 = map.get(value.expr);
            }
            else {
                visit(astBinaryExpression.expr2);
                opnd1=map.get(astBinaryExpression.expr2);
                // else ...
            }
        }
        else {
            res = new TemporaryValue(++tmpRegID);
            VarTable varTable = new VarTable();
            varTable.name=((TemporaryValue) res).name();
            varTable.type="VariableDeclarator";
            Scope.add(varTable);
            visit(astBinaryExpression.expr1);
            opnd1 = map.get(astBinaryExpression.expr1);
            visit(astBinaryExpression.expr2);
            opnd2 = map.get(astBinaryExpression.expr2);
        }

        // build quat
        Quat quat = new Quat(op, res, opnd1, opnd2);
        quats.add(quat);
        map.put(astBinaryExpression, res);
    }
    public static void visit(ASTIdentifier astIdentifier){
        boolean flag=false;
        if(Scope.size()>0){
            for(VarTable varTable:Scope){
                if(astIdentifier.value.equals(varTable.name)){
                    flag=true;
                    break;
                }
            }
        }
        FuncTable funcTable=ProcTable.get(ProcTable.size()-1);
        if(funcTable.VariableTable.size()>0&&(!flag)){
            for(VarTable varTable : funcTable.VariableTable){
                if(astIdentifier.value.equals(varTable.name)){
                    flag=true;
                    break;
                }
            }
        }
        if(GlobalVarTable.size()>0&&(!flag)){
            for(VarTable varTable:GlobalVarTable){
                if(astIdentifier.value.equals(varTable.name)){
                    flag=true;
                    break;
                }
            }
        }
        if(!flag){
            String ERROR="ES01: "+astIdentifier.value+"_is_not_defined\n";
            ErrorTable.add(ERROR);
        }
        map.put(astIdentifier, astIdentifier);
    }
    public static void visit(ASTIntegerConstant astIntegerConstant){
        map.put(astIntegerConstant,astIntegerConstant);
    }
    public static void visit(ASTStringConstant astStringConstant){
        map.put(astStringConstant,astStringConstant);
    }
    public static void visit(ASTFunctionCall astFunctionCall){
        String type="";
        String name="";
        if(astFunctionCall.argList!=null){
            for(int i =0;i<astFunctionCall.argList.size();i++){
                ASTExpression astExpression = astFunctionCall.argList.get(i);
                visit(astExpression);
                ASTNode opnd1=map.get(astExpression);
                ASTIntegerConstant astIntegerConstant=new ASTIntegerConstant();
                astIntegerConstant.value=i;
                Quat quat = new Quat("param",astIntegerConstant,opnd1,null);
                quats.add(quat);
            }
        }
        boolean flag=false;
        for(FuncTable funcTable:ProcTable){
            if(funcTable.funcName.equals(((ASTIdentifier) astFunctionCall.funcname).value)){
                flag=true;
                type=funcTable.specifiers;
                break;
            }
        }
        if(!flag){
            String ERROR = "ES01: "+((ASTIdentifier) astFunctionCall.funcname).value+"_is_not_defined\n";
            ErrorTable.add(ERROR);
        }
        ASTNode res = astFunctionCall.funcname;
        name=((ASTIdentifier) astFunctionCall.funcname).value;
        Quat quat = new Quat("call",res,null,null);
        quats.add(quat);
        if(type.equals("int")||name.equals("Mars_GetInt")){
            ASTNode tmp= new TemporaryValue(++tmpRegID);
            VarTable varTable = new VarTable();
            varTable.name=((TemporaryValue) tmp).name();
            varTable.type="VariableDeclarator";
            Scope.add(varTable);
            map.put(astFunctionCall,tmp);
            Quat quat1 = new Quat("return",tmp,null,null);
            quats.add(quat1);
        }
        else{
            //Quat quat1 = new Quat("return",null,null,null);
            //quats.add(quat1);
        }
    }
    public static void visit(ASTArrayAccess astArrayAccess){
        ASTNode res=null;
        ASTNode opnd1=null;
        ASTNode opnd2=null;
        visit(astArrayAccess.elements.get(0));
        opnd2=map.get(astArrayAccess.elements.get(0));
        ASTExpression astExpression = astArrayAccess.arrayName;
        if(astExpression.getClass()==ASTIdentifier.class){
            boolean flag=false;
            if(Scope.size()>0){
                for(VarTable varTable:Scope){
                    if(((ASTIdentifier) astExpression).value.equals(varTable.name)){
                        flag=true;
                        break;
                    }
                }
            }
            FuncTable funcTable=ProcTable.get(ProcTable.size()-1);
            if(funcTable.VariableTable.size()>0&&(!flag)){
                for(VarTable varTable : funcTable.VariableTable){
                    if(((ASTIdentifier) astExpression).value.equals(varTable.name)){
                        flag=true;
                        break;
                    }
                }
            }
            if(GlobalVarTable.size()>0&&(!flag)){
                for(VarTable varTable:GlobalVarTable){
                    if(((ASTIdentifier) astExpression).value.equals(varTable.name)){
                        flag=true;
                        break;
                    }
                }
            }
            if(!flag){
                String ERROR="ES01: "+((ASTIdentifier) astExpression).value+"_is_not_defined\n";
                ErrorTable.add(ERROR);
            }
            opnd1=astExpression;
            map.put(astArrayAccess.arrayName,astExpression);
        }
        else{
            visit(astExpression);
            opnd1=map.get(astExpression);
        }
        res = new TemporaryValue(++tmpRegID);
        VarTable varTable = new VarTable();
        varTable.name=((TemporaryValue) res).name();
        varTable.type="VariableDeclarator";
        Scope.add(varTable);
        map.put(astArrayAccess,res);
        Quat quat = new Quat("[]",res,opnd1,opnd2);
        quats.add(quat);
    }


    /**
     * Statement
     *    @JsonSubTypes.Type(value = ASTBreakStatement.class,name = "BreakStatement"),
     *    @JsonSubTypes.Type(value = ASTCompoundStatement.class,name = "CompoundStatement"),
     *    @JsonSubTypes.Type(value = ASTContinueStatement.class,name="ContinueStatement"),
     *    @JsonSubTypes.Type(value = ASTExpressionStatement.class,name = "ExpressionStatement"),
     *    @JsonSubTypes.Type(value = ASTGotoStatement.class,name = "GotoStatement"),
     *    @JsonSubTypes.Type(value = ASTIterationDeclaredStatement.class,name = "IterationDeclaredStatement"),
     *    @JsonSubTypes.Type(value = ASTIterationStatement.class,name = "IterationStatement"),
     *    @JsonSubTypes.Type(value = ASTLabeledStatement.class,name = "LabeledStatement"),
     *    @JsonSubTypes.Type(value = ASTReturnStatement.class,name = "ReturnStatement"),
     *    @JsonSubTypes.Type(value = ASTSelectionStatement.class,name = "SelectionStatement")
     */
    public static void visit(ASTStatement statement) {
        if(statement instanceof ASTIterationDeclaredStatement) {
            visit((ASTIterationDeclaredStatement)statement);
        }else if(statement instanceof ASTIterationStatement) {
            visit((ASTIterationStatement)statement);
        }else if(statement instanceof ASTCompoundStatement) {
            visit((ASTCompoundStatement)statement);
        }else if(statement instanceof ASTSelectionStatement) {
            visit((ASTSelectionStatement)statement);
        }else if(statement instanceof ASTExpressionStatement) {
            visit((ASTExpressionStatement)statement);
        }else if(statement instanceof ASTBreakStatement) {
            visit((ASTBreakStatement)statement);
        }else if(statement instanceof ASTContinueStatement) {
            visit((ASTContinueStatement)statement);
        }else if(statement instanceof ASTReturnStatement) {
            visit((ASTReturnStatement)statement);
        }else if(statement instanceof ASTGotoStatement) {
            visit((ASTGotoStatement)statement);
        }else if(statement instanceof ASTLabeledStatement) {
            visit((ASTLabeledStatement)statement);
        }
    }
    public static void visit(ASTSelectionStatement astSelectionStatement){
        IfLabelID++;
        ASTNode StartCheckIfLabel=new LabelGenerator(IfLabelID,"If",0);
        quats.add(new Quat("Label",StartCheckIfLabel,null,null));
        for(ASTExpression astExpression:astSelectionStatement.cond){
            visit(astExpression);
        }
        ASTNode res = map.get(astSelectionStatement.cond.get(0));
        ASTNode OtherwiseLabel=new LabelGenerator(IfLabelID,"Else",0);
        ASTNode EndifLabel=new LabelGenerator(IfLabelID,"Endif",0);
        if(astSelectionStatement.otherwise!=null)
        {
            quats.add(new Quat("JF",OtherwiseLabel,res,null));
        }
        else{
            quats.add(new Quat("JF",EndifLabel,null,null));
        }
        visit(astSelectionStatement.then);
        //ASTNode EndifLabel=new LabelGenerator(IfLabelID,"Endif",0);
        if(astSelectionStatement.otherwise!=null){
            quats.add(new Quat("JMP",EndifLabel,null,null));
            quats.add(new Quat("Label",OtherwiseLabel,null,null));
            visit(astSelectionStatement.otherwise);
        }
        quats.add(new Quat("Label",EndifLabel,null,null));
    }
    public static void visit(ASTCompoundStatement astCompoundStatement){
        boolean flag=false;
        if(astCompoundStatement.parent.getClass()==ASTFunctionDefine.class){
            flag=true;
        }
        Var.push(new LinkedList<>());
        for(ASTNode blockItem : astCompoundStatement.blockItems){
            if(blockItem instanceof ASTDeclaration){
                Scope=Var.peek();
                visit((ASTDeclaration) blockItem);
            }
            else if(blockItem instanceof ASTStatement){
                visit((ASTStatement) blockItem);
            }
        }
        if(flag){
            FuncTable funcTable=ProcTable.get(ProcTable.size()-1);
            Scope=Var.peek();
            if(Scope.size()!=0){
                for(int i = 0;i<Scope.size();i++){
                    funcTable.VariableTable.add(Scope.get(i));
                }
                Scope.clear();
            }
        }
        Var.pop();
    }
    public static void visit(ASTIterationDeclaredStatement astIterationDeclaredStatement){
        ASTNode preStartLabel=loopStartLabel;
        ASTNode preNextLabel=loopNextLabel;
        ASTNode preEndLabel=loopEndLabel;
        ASTNode loopCheckLabel=new LabelGenerator(tmpLabelID,"loopCheckLabel",0);
        loopStartLabel=new LabelGenerator(tmpLabelID,"loopStartLabel",0);
        loopNextLabel=new LabelGenerator(tmpLabelID,"loopNextLabel",0);
        loopEndLabel = new LabelGenerator(tmpLabelID,"loopEndLabel",0);
        tmpLabelID++;
        quats.add(new Quat("Label",loopStartLabel,null,null));
        if(astIterationDeclaredStatement.init!=null){
            visit(astIterationDeclaredStatement.init);
        }
        quats.add(new Quat("Label",loopCheckLabel,null,null));
        if(astIterationDeclaredStatement.cond!=null){
            for (ASTExpression astExpression:astIterationDeclaredStatement.cond){
                visit(astExpression);
            }
            ASTNode res=map.get(astIterationDeclaredStatement.cond.get(0));
            quats.add(new Quat("JF",loopEndLabel,res,null));
        }
        if(astIterationDeclaredStatement.stat!=null){
            visit(astIterationDeclaredStatement.stat);
        }
        quats.add(new Quat("Label",loopNextLabel,null,null));
        if(astIterationDeclaredStatement.step!=null){
            for(ASTExpression astExpression:astIterationDeclaredStatement.step){
                visit(astExpression);
            }
        }
        quats.add(new Quat("JMP",loopCheckLabel,null,null));
        quats.add(new Quat("Label",loopEndLabel,null,null));
        loopStartLabel=preStartLabel;
        loopNextLabel=preNextLabel;
        loopEndLabel=preEndLabel;
        Scope.remove(Scope.size()-1);
    }
    public static void visit(ASTIterationStatement astIterationStatement){
        ASTNode preStartLabel=loopStartLabel;
        ASTNode preNextLabel=loopNextLabel;
        ASTNode preEndLabel=loopEndLabel;
        ASTNode loopCheckLabel=new LabelGenerator(tmpLabelID,"loopCheckLabel",0);
        loopStartLabel=new LabelGenerator(tmpLabelID,"loopStartLabel",0);
        loopNextLabel=new LabelGenerator(tmpLabelID,"loopNextLabel",0);
        loopEndLabel = new LabelGenerator(tmpLabelID,"loopEndLabel",0);
        tmpLabelID++;
        quats.add(new Quat("Label",loopStartLabel,null,null));
        if(astIterationStatement.init!=null){
            for (ASTExpression astExpression:astIterationStatement.init){
                visit(astExpression);
            }
        }
        Quat quat = new Quat("Label",loopCheckLabel,null,null);
        quats.add(quat);
        if(astIterationStatement.cond!=null){
            for (ASTExpression astExpression:astIterationStatement.cond){
                visit(astExpression);
            }
            ASTNode res=map.get(astIterationStatement.cond.get(0));
            Quat quat1=new Quat("JF",loopEndLabel,res,null);
            quats.add(quat1);
        }
        if(astIterationStatement.stat!=null){
            visit(astIterationStatement.stat);
        }
        Quat quat2=new Quat("Label",loopNextLabel,null,null);
        quats.add(quat2);
        if(astIterationStatement.step!=null){
            for(ASTExpression astExpression:astIterationStatement.step){
                visit(astExpression);
            }
        }
        quats.add(new Quat("JMP",loopCheckLabel,null,null));
        quats.add(new Quat("Label",loopEndLabel,null,null));
        loopStartLabel=preStartLabel;
        loopNextLabel=preNextLabel;
        loopEndLabel=preEndLabel;
        tmpLabelID--;
    }
    public static void visit(ASTExpressionStatement astExpressionStatement){
        for (ASTExpression astExpression : astExpressionStatement.exprs) {
            visit(astExpression);
        }
    }
    public static void visit(ASTBreakStatement astBreakStatement){
        if(loopEndLabel==null){
            String ERROR="ES03: break_is_not_in_loop\n";
            ErrorTable.add(ERROR);
        }
        quats.add(new Quat("JMP",loopEndLabel,null,null));
    }
    public static void visit(ASTContinueStatement astContinueStatement){
        if(loopNextLabel==null){
            String ERROR="ES09: continue_is_not_in_loop\n";
            ErrorTable.add(ERROR);
        }
        quats.add(new Quat("JMP",loopNextLabel,null,null));
    }
    public static void visit(ASTReturnStatement astReturnStatement){
        if(astReturnStatement.expr==null||astReturnStatement.expr.isEmpty()){
            quats.add(new Quat("RET",null,null,null));
        }
        else {
            for(ASTExpression astExpression:astReturnStatement.expr){
                visit(astExpression);
            }
            ASTNode res = map.get(astReturnStatement.expr.get(0));
            quats.add(new Quat("RET",res,null,null));
        }
    }
    public static void visit(ASTLabeledStatement astLabeledStatement){
        if(astLabeledStatement.label!=null){
            ASTNode res=new LabelGenerator(0,((ASTIdentifier) astLabeledStatement.label).value,0);
            Label.put(astLabeledStatement.label.value,res);
            quats.add(new Quat("Label",res,null,null));
        }
        if(astLabeledStatement.stat!=null){
            visit(astLabeledStatement.stat);
        }
    }
    public static void visit(ASTGotoStatement astGotoStatement){
        if(astGotoStatement.label!=null){
            ASTNode res = Label.get(astGotoStatement.label.value);
            quats.add(new Quat("goto",res,null,null));
        }
    }

    public static void printIc(String icfilename){
        MyICPrinter myICPrinter = new MyICPrinter(quats);
        myICPrinter.print(icfilename);
    }
    public static void printSymbol(String symbolname){
        SymbolPrinter symbolPrinter = new SymbolPrinter();
        symbolPrinter.SetProcTable(ProcTable);
        symbolPrinter.SetGlobalVarTable(GlobalVarTable);
        symbolPrinter.PrintProcTable(symbolname);
    }


    public void test(ASTNode node) throws IOException {
        visit((ASTCompilationUnit) node);
        printIc(Icfilename);
        printSymbol(Symbolname);
        FileWriter fileWriter = new FileWriter(new File(Errorfilename));
        for (String Error : ErrorTable){
            fileWriter.write(Error);
        }
        fileWriter.close();
    }
}
