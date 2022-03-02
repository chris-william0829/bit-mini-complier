package bit.minisys.minicc.icgen;

import bit.minisys.minicc.parser.ast.ASTNode;

// ��Ԫʽ��ʽ���м����, �������ͷ���ֵ�Ľṹֱ��ʹ��AST�ڵ㣬Ҳ�����Զ���IR�ڵ�
public class Quat {
	private String op;
	private ASTNode res;
	private ASTNode opnd1;
	private ASTNode opnd2;
	public Quat(String op, ASTNode res, ASTNode opnd1, ASTNode opnd2) {
		this.op = op;
		this.res = res;
		this.opnd1 = opnd1;
		this.opnd2 = opnd2;
	}

	public String getOp() {
		return op;
	}
	public ASTNode getOpnd1() {
		return opnd1;
	}
	public ASTNode getOpnd2() {
		return opnd2;
	}
	public ASTNode getRes() {
		return res;
	}
}
