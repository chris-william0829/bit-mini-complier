

package bit.minisys.minicc.parser;
import MyCGrammer.MyCGrammerBaseListener;
import java.io.*;
import java.util.*;

import MyCGrammer.MyCGrammerParser;
import bit.minisys.minicc.internal.util.MiniCCUtil;
import bit.minisys.minicc.parser.ast.*;
import MyCGrammer.MyCGrammerBaseListener;
import MyCGrammer.MyCGrammerLexer;
import MyCGrammer.MyCGrammerParser;
import bit.minisys.minicc.pp.internal.A;
import bit.minisys.minicc.pp.internal.P;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;
import javax.xml.soap.Node;

public class MyListener extends MyCGrammerBaseListener{
    public String oFile=new String();
    public Stack<ASTNode> NodeStack=new Stack<>();
    @Override public void enterCompilationUnit(MyCGrammerParser.CompilationUnitContext ctx) {
        ASTCompilationUnit node = new ASTCompilationUnit();
        NodeStack.push(node);
    }
    @Override public void exitCompilationUnit(MyCGrammerParser.CompilationUnitContext ctx) {
        ASTNode node=NodeStack.pop();
        node.children.addAll(((ASTCompilationUnit)node).items);

        String[] dummyStrs = new String[16];
        TreeViewer viewr = new TreeViewer(Arrays.asList(dummyStrs), node);
        //viewr.open();

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(oFile), node);
        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeStack.push(node);
    }
    /*
    @Override public void enterExternalDeclaration(MyCGrammerParser.ExternalDeclarationContext ctx) {
        ASTDeclaration node=new ASTDeclaration();
        NodeStack.push(node);
    }

    @Override public void exitExternalDeclaration(MyCGrammerParser.ExternalDeclarationContext ctx) {
        ASTNode node =NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTCompilationUnit.class){
            if(((ASTCompilationUnit) ParentNode).items==null){
                ((ASTCompilationUnit) ParentNode).items=new LinkedList<>();
            }
            ((ASTCompilationUnit)ParentNode).items.add(node);
        }
    }
    */
    @Override public void enterInitDeclaratorList(MyCGrammerParser.InitDeclaratorListContext ctx) {
        ASTNode node = new ASTInitList();
        NodeStack.push(node);
    }
    @Override public void exitInitDeclaratorList(MyCGrammerParser.InitDeclaratorListContext ctx) {
        ASTNode node =NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(((ASTInitList) node).declarator!=null){
            node.children.add(((ASTInitList) node).declarator);
        }
        if(((ASTInitList) node).exprs!=null){
            node.children.addAll(((ASTInitList) node).exprs);
        }
        if(ParentNode.getClass()==ASTDeclaration.class){
            if(((ASTDeclaration) ParentNode).initLists==null){
                ((ASTDeclaration) ParentNode).initLists=new LinkedList<>();
            }
            ((ASTDeclaration) ParentNode).initLists.add((ASTInitList) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            Stack<ASTNode> tmpStack=new Stack<>();
            while (ParentNode.getClass()!=ASTDeclaration.class){
                tmpStack.push(NodeStack.pop());
                ParentNode=NodeStack.peek();
            }
            if(((ASTDeclaration) ParentNode).initLists==null){
                ((ASTDeclaration) ParentNode).initLists=new LinkedList<>();
            }
            ((ASTDeclaration) ParentNode).initLists.add((ASTInitList) node);
            node.parent=ParentNode;
            while (tmpStack.size()!=0){
                NodeStack.push(tmpStack.pop());
            }
        }
    }
    /**
     * Declaration
     * @param ctx
     */
    @Override public void enterDeclaration(MyCGrammerParser.DeclarationContext ctx) {
        ASTNode ParentNode=NodeStack.peek();
        if(ParentNode.getClass()==ASTDeclaration.class){
            return;
        }
        else{
            ASTDeclaration node=new ASTDeclaration();
            NodeStack.push(node);
        }
    }
    @Override public void exitDeclaration(MyCGrammerParser.DeclarationContext ctx) {
        ASTNode node =NodeStack.pop();
        if(((ASTDeclaration) node).specifiers!=null){
            node.children.addAll(((ASTDeclaration) node).specifiers);
        }
        if(((ASTDeclaration) node).initLists!=null){
            node.children.addAll(((ASTDeclaration) node).initLists);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTCompilationUnit.class){
            if(((ASTCompilationUnit) ParentNode).items==null){
                ((ASTCompilationUnit) ParentNode).items=new LinkedList<>();
            }
            ((ASTCompilationUnit)ParentNode).items.add(node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).init==null){
                ((ASTIterationDeclaredStatement) ParentNode).init=(ASTDeclaration) node;
            }
            node.parent=ParentNode;
        }
    }
    @Override public void enterFunctionDefinition(MyCGrammerParser.FunctionDefinitionContext ctx) {
        ASTNode ParentNode=NodeStack.peek();
        if(ParentNode.getClass()==ASTDeclaration.class){
            ParentNode=NodeStack.pop();
        }
        ASTNode node=new ASTFunctionDefine();
        NodeStack.push(node);
    }
    @Override public void exitFunctionDefinition(MyCGrammerParser.FunctionDefinitionContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTFunctionDefine) node).specifiers!=null){
            node.children.addAll(((ASTFunctionDefine) node).specifiers);
        }
        if(((ASTFunctionDefine) node).declarator!=null){
            node.children.add(((ASTFunctionDefine) node).declarator);
        }
        if(((ASTFunctionDefine) node).body!=null){
            node.children.add(((ASTFunctionDefine) node).body);
        }
        if(ParentNode.getClass()==ASTCompilationUnit.class){
            if(((ASTCompilationUnit) ParentNode).items==null){
                ((ASTCompilationUnit) ParentNode).items=new LinkedList<>();
            }
            ((ASTCompilationUnit)ParentNode).items.add(node);
            node.parent=ParentNode;
        }
    }

    /**
     * Declarator
     *ARRAY,FUNC,PARAM,VARIABLE
     */

    @Override public void enterDirectDeclarator_array(MyCGrammerParser.DirectDeclarator_arrayContext ctx) {
        ASTNode node=new ASTArrayDeclarator();
        NodeStack.push(node);
    }
    @Override public void exitDirectDeclarator_array(MyCGrammerParser.DirectDeclarator_arrayContext ctx) {
        ASTNode node=NodeStack.pop();
        if(((ASTArrayDeclarator) node).declarator!=null){
            node.children.add(((ASTArrayDeclarator) node).declarator);
        }
        if(((ASTArrayDeclarator) node).expr!=null){
            node.children.add(((ASTArrayDeclarator) node).expr);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            ((ASTArrayDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            ((ASTInitList) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDeclarator.class){
            ((ASTFunctionDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDefine.class){
            ((ASTFunctionDefine) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTParamsDeclarator.class){
            ((ASTParamsDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTTypename.class){
            ((ASTTypename) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
    }

    @Override public void enterDirectDeclarator_func(MyCGrammerParser.DirectDeclarator_funcContext ctx) {
        ASTNode node = new ASTFunctionDeclarator();
        NodeStack.push(node);
    }
    @Override public void exitDirectDeclarator_func(MyCGrammerParser.DirectDeclarator_funcContext ctx) {
        ASTNode node=NodeStack.pop();
        if(((ASTFunctionDeclarator) node).declarator!=null){
            node.children.add(((ASTFunctionDeclarator) node).declarator);
        }
        if(((ASTFunctionDeclarator) node).params!=null){
            node.children.addAll(((ASTFunctionDeclarator) node).params);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            ((ASTArrayDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            ((ASTInitList) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }

        else if(ParentNode.getClass()==ASTFunctionDeclarator.class){
            ((ASTFunctionDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDefine.class){
            ((ASTFunctionDefine) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTParamsDeclarator.class){
            ((ASTParamsDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTTypename.class){
            ((ASTTypename) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
    }

    @Override public void enterParameterDeclaration(MyCGrammerParser.ParameterDeclarationContext ctx) {
        ASTNode node = new ASTParamsDeclarator();
        NodeStack.push(node);
    }
    @Override public void exitParameterDeclaration(MyCGrammerParser.ParameterDeclarationContext ctx) {
        ASTNode node=NodeStack.pop();
        if(((ASTParamsDeclarator) node).specfiers!=null){
            node.children.addAll(((ASTParamsDeclarator) node).specfiers);
        }
        if(((ASTParamsDeclarator) node).declarator!=null){
            node.children.add(((ASTParamsDeclarator) node).declarator);
        }
        ASTNode ParentNode =NodeStack.peek();
        if(ParentNode.getClass()==ASTFunctionDeclarator.class){
            if(((ASTFunctionDeclarator) ParentNode).params==null){
                ((ASTFunctionDeclarator) ParentNode).params=new LinkedList<>();
            }
            ((ASTFunctionDeclarator) ParentNode).params.add((ASTParamsDeclarator) node);
            node.parent=ParentNode;
        }
    }

    @Override public void enterDirectDeclarator_pass(MyCGrammerParser.DirectDeclarator_passContext ctx) {
        ASTNode node = new ASTVariableDeclarator();
        NodeStack.push(node);
    }
    @Override public void exitDirectDeclarator_pass(MyCGrammerParser.DirectDeclarator_passContext ctx) {
        ASTNode node=NodeStack.pop();
        if(((ASTVariableDeclarator) node).identifier!=null){
            node.children.add(((ASTVariableDeclarator) node).identifier);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            ((ASTArrayDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            ((ASTInitList) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDeclarator.class){
            ((ASTFunctionDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDefine.class){
            ((ASTFunctionDefine) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTParamsDeclarator.class){
            ((ASTParamsDeclarator) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTTypename.class){
            ((ASTTypename) ParentNode).declarator=(ASTDeclarator) node;
            node.parent=ParentNode;
        }
    }


    /**
     * Expression
     *    @JsonSubTypes.Type(value = ASTIdentifier.class,name = "Identifier"),
     *    @JsonSubTypes.Type(value = ASTArrayAccess.class,name = "ArrayAccess"),
     *    @JsonSubTypes.Type(value = ASTBinaryExpression.class,name="BinaryExpression"),
     *    @JsonSubTypes.Type(value = ASTCastExpression.class,name = "CastExpression"),
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
    @Override public void enterTokenId(MyCGrammerParser.TokenIdContext ctx) {
        ASTNode node =new ASTIdentifier();
        NodeStack.push(node);
    }
    @Override public void exitTokenId(MyCGrammerParser.TokenIdContext ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
    }

    @Override public void enterPostfixExpression_arrayaccess(MyCGrammerParser.PostfixExpression_arrayaccessContext ctx) {
        ASTNode node = new ASTArrayAccess();
        NodeStack.push(node);
    }
    @Override public void exitPostfixExpression_arrayaccess(MyCGrammerParser.PostfixExpression_arrayaccessContext ctx) {
        ASTNode node = NodeStack.pop();
        if(((ASTArrayAccess) node).arrayName!=null){
            node.children.add(((ASTArrayAccess) node).arrayName);
        }
        if(((ASTArrayAccess) node).elements!=null){
            node.children.addAll(((ASTArrayAccess) node).elements);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }

    //BinaryExpression
    public void enterBinaryExpression(){
        ASTNode node =new ASTBinaryExpression();
        NodeStack.push(node);
    }
    public void exitBinaryExpression(){
        ASTNode node = NodeStack.pop();
        if(((ASTBinaryExpression) node).expr1!=null){
            node.children.add(((ASTBinaryExpression) node).expr1);
        }
        if(((ASTBinaryExpression) node).expr2!=null){
            node.children.add(((ASTBinaryExpression) node).expr2);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }
    @Override public void enterMultiplicativeExpression_(MyCGrammerParser.MultiplicativeExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitMultiplicativeExpression_(MyCGrammerParser.MultiplicativeExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterAdditiveExpression_(MyCGrammerParser.AdditiveExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitAdditiveExpression_(MyCGrammerParser.AdditiveExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterShiftExpression_(MyCGrammerParser.ShiftExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitShiftExpression_(MyCGrammerParser.ShiftExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterRelationalExpression_(MyCGrammerParser.RelationalExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitRelationalExpression_(MyCGrammerParser.RelationalExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterEqualityExpression_(MyCGrammerParser.EqualityExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitEqualityExpression_(MyCGrammerParser.EqualityExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterAndExpression_(MyCGrammerParser.AndExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitAndExpression_(MyCGrammerParser.AndExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterInclusiveOrExpression_(MyCGrammerParser.InclusiveOrExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitInclusiveOrExpression_(MyCGrammerParser.InclusiveOrExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterLogicalAndExpression_(MyCGrammerParser.LogicalAndExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitLogicalAndExpression_(MyCGrammerParser.LogicalAndExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterLogicalOrExpression_(MyCGrammerParser.LogicalOrExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitLogicalOrExpression_(MyCGrammerParser.LogicalOrExpression_Context ctx) {
        exitBinaryExpression();
    }
    @Override public void enterAssignmentExpression_(MyCGrammerParser.AssignmentExpression_Context ctx) {
        enterBinaryExpression();
    }
    @Override public void exitAssignmentExpression_(MyCGrammerParser.AssignmentExpression_Context ctx) {
        exitBinaryExpression();
    }


    @Override public void enterCastExpression_(MyCGrammerParser.CastExpression_Context ctx) {
        ASTNode node =new ASTCastExpression();
        NodeStack.push(node);
    }
    @Override public void exitCastExpression_(MyCGrammerParser.CastExpression_Context ctx) {
        ASTNode node = NodeStack.pop();
        if(((ASTCastExpression) node).typename!=null){
            node.children.add(((ASTCastExpression) node).typename);
        }
        if(((ASTCastExpression) node).expr!=null){
            node.children.add(((ASTCastExpression) node).expr);
        }
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterTokenConstant(MyCGrammerParser.TokenConstantContext ctx) {
        ASTNode node = new ASTIntegerConstant();
        NodeStack.push(node);
    }
    @Override public void exitTokenConstant(MyCGrammerParser.TokenConstantContext ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterPostfixExpression_funcall(MyCGrammerParser.PostfixExpression_funcallContext ctx) {
        ASTNode node = new ASTFunctionCall();
        NodeStack.push(node);
    }
    @Override public void exitPostfixExpression_funcall(MyCGrammerParser.PostfixExpression_funcallContext ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(((ASTFunctionCall) node).funcname!=null){
            node.children.add(((ASTFunctionCall) node).funcname);
        }
        if(((ASTFunctionCall) node).argList!=null){
            node.children.addAll(((ASTFunctionCall) node).argList);
        }
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterPostfixExpression_(MyCGrammerParser.PostfixExpression_Context ctx) {
        ASTNode node = new ASTPostfixExpression();
        NodeStack.push(node);
    }
    @Override public void exitPostfixExpression_(MyCGrammerParser.PostfixExpression_Context ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(((ASTPostfixExpression) node).expr!=null){
            node.children.add(((ASTPostfixExpression) node).expr);
        }
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterTokenStringLiteral(MyCGrammerParser.TokenStringLiteralContext ctx) {
        ASTNode node = new ASTStringConstant();
        NodeStack.push(node);
    }
    @Override public void exitTokenStringLiteral(MyCGrammerParser.TokenStringLiteralContext ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();

        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterUnaryExpression_(MyCGrammerParser.UnaryExpression_Context ctx) {
        ASTNode node = new ASTUnaryExpression();
        NodeStack.push(node);
    }
    @Override public void exitUnaryExpression_(MyCGrammerParser.UnaryExpression_Context ctx) {
        ASTNode node = NodeStack.pop();
        ASTNode ParentNode = NodeStack.peek();
        if(((ASTUnaryExpression) node).expr!=null){
            node.children.add(((ASTUnaryExpression) node).expr);
        }
        if(ParentNode.getClass()==ASTArrayDeclarator.class){
            if(((ASTArrayDeclarator) ParentNode).expr==null){
                ((ASTArrayDeclarator) ParentNode).expr=(ASTExpression) node;
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTArrayAccess.class){
            if(((ASTArrayAccess) ParentNode).arrayName==null){
                ((ASTArrayAccess) ParentNode).arrayName=(ASTExpression) node;
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements==null){
                ((ASTArrayAccess) ParentNode).elements=new LinkedList<>();
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
            else if(((ASTArrayAccess) ParentNode).elements!=null){
                ((ASTArrayAccess) ParentNode).elements.add((ASTExpression) node);
                node.parent=ParentNode;
            }
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            if(((ASTBinaryExpression) ParentNode).expr1==null){
                ((ASTBinaryExpression) ParentNode).expr1=(ASTExpression) node;

            }
            else if(((ASTBinaryExpression) ParentNode).expr2==null){
                ((ASTBinaryExpression) ParentNode).expr2=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCastExpression.class){
            if(((ASTCastExpression) ParentNode).expr==null){
                ((ASTCastExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionCall.class){
            if(((ASTFunctionCall) ParentNode).funcname==null){
                ((ASTFunctionCall) ParentNode).funcname=(ASTExpression) node;
            }
            else if(((ASTFunctionCall) ParentNode).argList==null){
                ((ASTFunctionCall) ParentNode).argList=new LinkedList<>();
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            else if(((ASTFunctionCall) ParentNode).argList!=null){
                ((ASTFunctionCall) ParentNode).argList.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            if(((ASTPostfixExpression) ParentNode).expr==null){
                ((ASTPostfixExpression) ParentNode).expr=(ASTExpression) node;
            }
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            if(((ASTUnaryExpression) ParentNode).expr==null){
                ((ASTUnaryExpression) ParentNode).expr=(ASTExpression) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTExpressionStatement.class){
            if(((ASTExpressionStatement) ParentNode).exprs==null){
                ((ASTExpressionStatement) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTExpressionStatement) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTInitList.class){
            if(((ASTInitList) ParentNode).exprs==null){
                ((ASTInitList) ParentNode).exprs=new LinkedList<>();
            }
            ((ASTInitList) ParentNode).exprs.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).cond==null){
                ((ASTIterationDeclaredStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationDeclaredStatement) ParentNode).step==null){
                ((ASTIterationDeclaredStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationDeclaredStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).init==null){
                ((ASTIterationStatement) ParentNode).init=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).init.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).cond==null){
                ((ASTIterationStatement) ParentNode).cond=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).cond.add((ASTExpression) node);
            }
            else if(((ASTIterationStatement) ParentNode).step==null){
                ((ASTIterationStatement) ParentNode).step=new LinkedList<>();
                ((ASTIterationStatement) ParentNode).step.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add((ASTExpression) node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).cond==null){
                ((ASTSelectionStatement) ParentNode).cond=new LinkedList<>();
                ((ASTSelectionStatement) ParentNode).cond.add((ASTExpression) node);
            }
            node.parent=ParentNode;
        }
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

    @Override public void enterJumpStatement_break(MyCGrammerParser.JumpStatement_breakContext ctx) {
        ASTNode node = new ASTBreakStatement();
        NodeStack.push(node);
    }
    @Override public void exitJumpStatement_break(MyCGrammerParser.JumpStatement_breakContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterCompoundStatement(MyCGrammerParser.CompoundStatementContext ctx) {
        ASTNode node =new ASTCompoundStatement();
        NodeStack.push(node);
    }
    @Override public void exitCompoundStatement(MyCGrammerParser.CompoundStatementContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTCompoundStatement) node).blockItems!=null){
            node.children.addAll(((ASTCompoundStatement) node).blockItems);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTFunctionDefine.class){
            if(((ASTFunctionDefine) ParentNode).body==null){
                ((ASTFunctionDefine) ParentNode).body=(ASTCompoundStatement) node;
            }
            node.parent=ParentNode;
        }
    }


    @Override public void enterJumpStatement_continue(MyCGrammerParser.JumpStatement_continueContext ctx) {
        ASTNode node = new ASTContinueStatement();
        NodeStack.push(node);
    }
    @Override public void exitJumpStatement_continue(MyCGrammerParser.JumpStatement_continueContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterExpressionStatement(MyCGrammerParser.ExpressionStatementContext ctx) {
        ASTNode node = new ASTExpressionStatement();
        NodeStack.push(node);
    }
    @Override public void exitExpressionStatement(MyCGrammerParser.ExpressionStatementContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTExpressionStatement) node).exprs!=null){
            node.children.addAll(((ASTExpressionStatement) node).exprs);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterJumpStatement_goto(MyCGrammerParser.JumpStatement_gotoContext ctx) {
        ASTNode node = new ASTGotoStatement();
        NodeStack.push(node);
    }
    @Override public void exitJumpStatement_goto(MyCGrammerParser.JumpStatement_gotoContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTGotoStatement) node).label!=null){
            node.children.add(((ASTGotoStatement) node).label);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterIterationStatement_forDeclared(MyCGrammerParser.IterationStatement_forDeclaredContext ctx) {
        ASTNode node = new ASTIterationDeclaredStatement();
        NodeStack.push(node);
    }
    @Override public void exitIterationStatement_forDeclared(MyCGrammerParser.IterationStatement_forDeclaredContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTIterationDeclaredStatement) node).init!=null){
            node.children.add(((ASTIterationDeclaredStatement) node).init);
        }
        if(((ASTIterationDeclaredStatement) node).cond!=null){
            node.children.addAll(((ASTIterationDeclaredStatement) node).cond);
        }
        if(((ASTIterationDeclaredStatement) node).step!=null){
            node.children.addAll(((ASTIterationDeclaredStatement) node).step);
        }
        if(((ASTIterationDeclaredStatement) node).stat!=null){
            node.children.add(((ASTIterationDeclaredStatement) node).stat);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterIterationStatement_for(MyCGrammerParser.IterationStatement_forContext ctx) {
        ASTNode node = new ASTIterationStatement();
        NodeStack.push(node);
    }
    @Override public void exitIterationStatement_for(MyCGrammerParser.IterationStatement_forContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTIterationStatement) node).init!=null){
            node.children.addAll(((ASTIterationStatement) node).init);
        }
        if(((ASTIterationStatement) node).cond!=null){
            node.children.addAll(((ASTIterationStatement) node).cond);
        }
        if(((ASTIterationStatement) node).step!=null){
            node.children.addAll(((ASTIterationStatement) node).step);
        }
        if(((ASTIterationStatement) node).stat!=null){
            node.children.add(((ASTIterationStatement) node).stat);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterLabeledStatement(MyCGrammerParser.LabeledStatementContext ctx) {
        ASTNode node = new ASTLabeledStatement();
        NodeStack.push(node);
    }
    @Override public void exitLabeledStatement(MyCGrammerParser.LabeledStatementContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTLabeledStatement) node).label!=null){
            node.children.add(((ASTLabeledStatement) node).label);
        }
        if(((ASTLabeledStatement) node).stat!=null){
            node.children.add(((ASTLabeledStatement) node).stat);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterJumpStatement_return(MyCGrammerParser.JumpStatement_returnContext ctx) {
        ASTNode node = new ASTReturnStatement();
        NodeStack.push(node);
    }
    @Override public void exitJumpStatement_return(MyCGrammerParser.JumpStatement_returnContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTReturnStatement) node).expr!=null){
            node.children.addAll(((ASTReturnStatement) node).expr);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    @Override public void enterSelectionStatement_if(MyCGrammerParser.SelectionStatement_ifContext ctx) {
        ASTNode node = new ASTSelectionStatement();
        NodeStack.push(node);
    }
    @Override public void exitSelectionStatement_if(MyCGrammerParser.SelectionStatement_ifContext ctx) {
        ASTNode node=NodeStack.pop();
        ASTNode ParentNode=NodeStack.peek();
        if(((ASTSelectionStatement) node).cond!=null){
            node.children.addAll(((ASTSelectionStatement) node).cond);
        }
        if(((ASTSelectionStatement) node).then!=null){
            node.children.add(((ASTSelectionStatement) node).then);
        }
        if(((ASTSelectionStatement) node).otherwise!=null){
            node.children.add(((ASTSelectionStatement) node).otherwise);
        }
        if(ParentNode.getClass()==ASTSelectionStatement.class){
            if(((ASTSelectionStatement) ParentNode).then==null){
                ((ASTSelectionStatement) ParentNode).then=(ASTStatement) node;
            }
            else if(((ASTSelectionStatement) ParentNode).otherwise==null){
                ((ASTSelectionStatement) ParentNode).otherwise=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationStatement.class){
            if(((ASTIterationStatement) ParentNode).stat==null) {
                ((ASTIterationStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).stat==null){
                ((ASTLabeledStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTIterationDeclaredStatement.class){
            if(((ASTIterationDeclaredStatement) ParentNode).stat==null){
                ((ASTIterationDeclaredStatement) ParentNode).stat=(ASTStatement) node;
            }
            node.parent=ParentNode;
        }
        else if(ParentNode.getClass()==ASTCompoundStatement.class){
            if(((ASTCompoundStatement) ParentNode).blockItems==null){
                ((ASTCompoundStatement) ParentNode).blockItems=new LinkedList<>();
            }
            ((ASTCompoundStatement) ParentNode).blockItems.add(node);
            node.parent=ParentNode;
        }
    }


    /**
     * Terminal
     */
    @Override public void visitTerminal(TerminalNode node) {
        int id=node.getSymbol().getType();
        if(id==12||id==13||id==16||id==17||id==18||id==20||id==24||id==25||id==26||id==32||id==38||(id>=43&&id<=60)||id==83){
            return;
        }
        ASTNode ParentNode=NodeStack.peek();
        if(ParentNode.getClass()==ASTFunctionDefine.class){
            if(((ASTFunctionDefine) ParentNode).specifiers==null){
                ((ASTFunctionDefine) ParentNode).specifiers=new LinkedList<>();
            }
            ((ASTFunctionDefine)ParentNode).specifiers.add(new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex()));
        }
        else if(ParentNode.getClass()==ASTParamsDeclarator.class){
            if(((ASTParamsDeclarator) ParentNode).specfiers==null){
                ((ASTParamsDeclarator) ParentNode).specfiers=new LinkedList<>();
            }
            ((ASTParamsDeclarator) ParentNode).specfiers.add(new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex()));
        }
        else if(ParentNode.getClass()==ASTDeclaration.class){
            if(((ASTDeclaration) ParentNode).specifiers==null){
                ((ASTDeclaration) ParentNode).specifiers=new LinkedList<>();
            }
            ((ASTDeclaration) ParentNode).specifiers.add(new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex()));
        }
        else if(ParentNode.getClass()==ASTIdentifier.class){
            ((ASTIdentifier) ParentNode).value=node.getSymbol().getText();
            ((ASTIdentifier) ParentNode).tokenId=node.getSymbol().getTokenIndex();
        }
        else if(ParentNode.getClass()==ASTIntegerConstant.class){
            ((ASTIntegerConstant) ParentNode).value=Integer.parseInt(node.getSymbol().getText());
            ((ASTIntegerConstant) ParentNode).tokenId=node.getSymbol().getTokenIndex();
        }
        else if(ParentNode.getClass()==ASTStringConstant.class){
            ((ASTStringConstant) ParentNode).value=(node.getSymbol().getText());
            ((ASTStringConstant) ParentNode).tokenId=node.getSymbol().getTokenIndex();
        }
        else if(ParentNode.getClass()==ASTBinaryExpression.class){
            ((ASTBinaryExpression) ParentNode).op=new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
        }
        else if(ParentNode.getClass()==ASTPostfixExpression.class){
            ((ASTPostfixExpression) ParentNode).op=new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
        }
        else if(ParentNode.getClass()==ASTUnaryExpression.class){
            ((ASTUnaryExpression) ParentNode).op=new ASTToken(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
        }
        else if(ParentNode.getClass()==ASTVariableDeclarator.class){
            ((ASTVariableDeclarator) ParentNode).identifier = new ASTIdentifier(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
        }
        else if(ParentNode.getClass()==ASTLabeledStatement.class){
            if(((ASTLabeledStatement) ParentNode).label==null){
                ((ASTLabeledStatement) ParentNode).label=new ASTIdentifier(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
            }
        }
        else if(ParentNode.getClass()==ASTGotoStatement.class){
            if(((ASTGotoStatement) ParentNode).label==null){
                ((ASTGotoStatement) ParentNode).label=new ASTIdentifier(node.getSymbol().getText(),node.getSymbol().getTokenIndex());
            }
        }
        else if(ParentNode.getClass()==ASTReturnStatement.class){
            if(((ASTReturnStatement) ParentNode).expr==null){
                ((ASTReturnStatement) ParentNode).expr=new LinkedList<>();
            }
            ((ASTReturnStatement) ParentNode).expr.add(new ASTIdentifier(node.getSymbol().getText(),node.getSymbol().getTokenIndex()));
        }
    }
}

