package com.company;

import java.io.*;
import com.company.Parser;
public class Main {

    public static void main(String[] args) throws Exception{
        Parser parser = null;
        Analyzer analyzer = null;
        Func func = null;
        switch (args.length){
            case 1:
                parser = new Parser(args[0]);
                parser.process();
                analyzer = new Analyzer(parser);
                analyzer.process();
                break;
            case 2:
                if(!args[0].equals("-json")){
                    parser = new Parser(args[1]);
                    System.out.println(args[1]);
                    parser.process();
                    analyzer = new Analyzer(parser);
                    analyzer.process();
                }
                else{
                    parser = new Parser(args[1]);
                    parser.process();
                    analyzer = new Analyzer(parser);
                    analyzer.process();
                    func = new Func(analyzer);
                    String[] strings = args[1].split("\\.");
                    func.wrtieToFile(strings[0]);
                }
                break;
            case 3:
                if(args[0].equals("-find")){
                    parser = new Parser(args[2]);
                    parser.process();
                    analyzer = new Analyzer(parser);
                    analyzer.process();
                    func = new Func(analyzer);
                    Object value = func.find(args[1]);
                    if(value==null){
                        System.out.println("未找到该值");
                    }
                    else {
                        System.out.println(func.find(args[1]));
                    }

                }
                break;
        }
    }
}
