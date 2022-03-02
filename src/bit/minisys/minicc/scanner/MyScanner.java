package bit.minisys.minicc.scanner;

import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MyScanner {
    public MyScanner(String tokenFileName,CommonTokenStream tokens) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(tokenFileName));
        for(int i=0;i<tokens.getNumberOfOnChannelTokens();i++){
            fileWriter.write(tokens.get(i).toString());
            fileWriter.write("\n");
        }
        fileWriter.close();
    }
}
