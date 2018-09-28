package com.company;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

// 用于存值的存储结构
class ValueStore{
    String name;
    String value;
    int indentLayer;
    Type type;
    int lineNum;
    public ValueStore(String name, String value, int indentLayer,Type type, int lineNum) {
        this.name = name;
        this.value = value;
        this.indentLayer = indentLayer;
        this.type = type;
        this.lineNum = lineNum;
    }
}

// 语法语义分析器
public class Analyzer {
    private ArrayList<Token> tokens;
    private int curIndex;
    private int curLine;
    private int curIndent;
    private int indentSpace;
    private Token curToken;
    private int totalTokens;

    public ArrayList<ValueStore> values = new ArrayList<ValueStore>();
    Queue<ValueStore> queue = new LinkedList<ValueStore>();

    Analyzer(Parser parser) {
        curIndent = 0;
        indentSpace = 2;
        tokens = parser.getTokens();
        curIndex = 0;
        curLine = 0;
        totalTokens = tokens.size();
        getNextToken();
    }

    private void getNextToken() {
        curToken = tokens.get(curIndex);
        curIndex++;
    }

    private void addQueue(ValueStore element)throws YAMLException{
        ValueStore lastElement = queue.poll();
        if(lastElement!=null){
            values.add(lastElement);
            if(!lastElement.name.equals("-")&&lastElement.value!=null&&lastElement.indentLayer==0){
                if(element.indentLayer==0){
                    queue.offer(element);
                }
                else{
                    throw new YAMLException(ErrorType.Indent,element.lineNum,-1);
                }
            }
            else if((element.indentLayer-lastElement.indentLayer>=2)||(lastElement.indentLayer-element.indentLayer>=2&&element.indentLayer!=0)){
                throw new YAMLException(ErrorType.Indent,element.lineNum,-1);
            }
            else if(lastElement.value==null&&element.value==null&&element.indentLayer==lastElement.indentLayer){
                throw new YAMLException(ErrorType.Indent,element.lineNum,-1);
            }
            else if(lastElement.value==null&&element.indentLayer<=lastElement.indentLayer){
                throw new YAMLException(ErrorType.Indent,element.lineNum,-1);
            }
            else if(lastElement.name.equals("-")&&lastElement.value!=null&&element.name.equals("-")&&element.value!=null&&element.indentLayer>lastElement.indentLayer){
                throw new YAMLException(ErrorType.Indent,element.lineNum,-1);
            }
            else{
                queue.offer(element);
            }
        }
        else{
            queue.offer(element);
        }
    }
    private int countIndentLayer() throws YAMLException{
        int counter = 0;
        int indentLayer = 0;
        while(curToken.type==Type.Space){
            counter++;
            getNextToken();
        }
        if(counter%2!=0){
            throw new YAMLException(ErrorType.Indent,curToken.lineNum,-1);
        }
        else{
            indentLayer = counter/2;
        }
        return indentLayer;
    }

    private String checkIfValue(){
        String returnValue = null;
        if(curToken.value.equals(":")){
            getNextToken();
            if(curToken.type==Type.Space){
                getNextToken();
                if(curToken.type==Type.ScientificNotation||curToken.type==Type.Int||curToken.type==Type.Str
                        ||curToken.type==Type.Double||curToken.type==Type.Bool){
                    returnValue = curToken.value;
                }
            }
        }
        return returnValue;
    }
    private String isValidArray() throws YAMLException{
        if(curToken.type==Type.ChangeLine){
            return "ChangeLine";
        }
        else if(curToken.type==Type.Space){
            getNextToken();
            if(curToken.type!=Type.Space||curToken.type!=Type.ChangeLine||curToken.type!=Type.Operator||curToken.type!=Type.Identifier){
                return curToken.value;
            }
        }
        else{
            throw new YAMLException(ErrorType.Array,curToken.lineNum,-1);
        }
        return null;
    }
    private boolean endValid(){
        if(curToken.type==Type.ChangeLine){
            return true;
        }
        else {
            return false;
        }
    }
    private void processLine()throws YAMLException{
        int indentLayer = countIndentLayer();
        switch (curToken.type){
            case Identifier:
                String indentifier = curToken.value;
                getNextToken();
                String value = checkIfValue();
                if(value!=null){
                    ValueStore element = new ValueStore(indentifier,value,indentLayer,curToken.type, curToken.lineNum);
                    addQueue(element);
                    getNextToken();
                    if(!endValid()){
                        throw new YAMLException(ErrorType.Element,curToken.lineNum,-1);
                    }
                }
                else{
                    if(curToken.type!=Type.ChangeLine){
                        throw new YAMLException(ErrorType.KeyValue,curToken.lineNum,-1);
                    }
                    else{
                        ValueStore element = new ValueStore(indentifier,null,indentLayer,curToken.type, curToken.lineNum);
                        addQueue(element);
                    }
                }
                break;
            case Operator:
                if(curToken.value.equals("-")){
                    getNextToken();
                    String returnValue = isValidArray();
                    if(!returnValue.equals("ChangeLine")){
                        String name = "-";
                        ValueStore element = new ValueStore(name,returnValue,indentLayer,curToken.type, curToken.lineNum);
                        addQueue(element);
                        getNextToken();
                    }
                    else{
                        ValueStore element = new ValueStore("-",null,indentLayer,curToken.type, curToken.lineNum);
                        addQueue(element);
                    }
                }
                break;
            case Double:
            case ScientificNotation:
            case Int:
            case Str:
            case Bool:
            case Space:
                throw new YAMLException(ErrorType.LineStart,curToken.lineNum,-1);
            case ChangeLine:
                break;
        }
    }

    public void process()throws Exception{
        System.out.println("语法分析开始");
        try{
            processLine();
        }catch (YAMLException e){
            e.printMessage();
        }
        while(curIndex<totalTokens&&curToken.type==Type.ChangeLine){
            getNextToken();
            try{
                processLine();
            }catch (YAMLException e){
                e.printMessage();
            }
        }
        while(!queue.isEmpty()){
            values.add(queue.poll());
        }
        System.out.println("语法分析结束");
        if (Valid.isValid) {
            System.out.println("语法与语义分析无误");
        }
    }
}
