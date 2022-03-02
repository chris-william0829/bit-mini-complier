package bit.minisys.minicc.semantic;

import bit.minisys.minicc.parser.ast.ASTExpression;

public class VarTable {
    public String name;
    public String type;
    public String specifiers;
    public ASTExpression value;
    public int dimension;
    public int length;
}
