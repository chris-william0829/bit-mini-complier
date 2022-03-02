package bit.minisys.minicc.icgen;

import bit.minisys.minicc.semantic.FuncTable;
import bit.minisys.minicc.semantic.VarTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SymbolPrinter {
    private List<FuncTable> ProcTable;
    private List<VarTable> GlobalVarTable;
    private List<VarTable> Scope;
    public SymbolPrinter() {
        ProcTable = new LinkedList<>();
        GlobalVarTable = new LinkedList<>();
        Scope=new LinkedList<>();
    }
    public void SetProcTable(List<FuncTable> procTable){
        this.ProcTable=procTable;
    }
    public void SetGlobalVarTable(List<VarTable> GlobalVarTable){
        this.GlobalVarTable= GlobalVarTable;
    }
    public void SetScope(List<VarTable> Scope){
        this.GlobalVarTable= Scope;
    }
    public void PrintProcTable(String filename){
        StringBuilder sb = new StringBuilder();
        sb.append("ProcTable\n");
        for(FuncTable funcTable:this.ProcTable){
            sb.append("FuncTable\n");
            sb.append(funcTable.specifiers+" | "+funcTable.funcName+" | "+funcTable.type+"\n");
            if(funcTable.VariableTable.size()!=0){
                sb.append(funcTable.funcName+"'s"+" VariableTable\n");
                for(VarTable varTable:funcTable.VariableTable){
                    sb.append(varTable.specifiers+" | "+varTable.name+" | "+varTable.type+"\n");
                }
            }
        }
        if(GlobalVarTable.size()!=0){
            sb.append("GlobalVarTable\n");
            for(VarTable varTable:GlobalVarTable){
                sb.append(varTable.specifiers+" | "+varTable.name+" | "+varTable.type+"\n");
            }
        }
        try {
            FileWriter fileWriter = new FileWriter(new File(filename));
            fileWriter.write(sb.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
