package bit.minisys.minicc.icgen;

import bit.minisys.minicc.parser.ast.ASTNode;
import bit.minisys.minicc.parser.ast.ASTVisitor;

public class LabelGenerator extends ASTNode {
    private Integer id;
    public String Type;
    public Integer num;
    public String name() {
        if(id==0){
            return "@"+Type;
        }
        else return "@"+id+Type;
    }
    @Override
    public void accept(ASTVisitor visitor) throws Exception {

    }
    public LabelGenerator(Integer id, String Type, Integer num) {
        super("TemporaryValue");
        this.id = id;
        this.Type=Type;
        this.num=num;
    }
    public LabelGenerator(String type) {
        super(type);
    }
}
