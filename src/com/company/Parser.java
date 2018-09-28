package com.company;


import java.io.*;
import java.util.ArrayList;

// 标记语法是否合法
class Valid{
    static boolean isValid = true;
}

// 将token分为9种类型
enum Type {
    Identifier, Operator, ChangeLine, Space,
    Int, Double, ScientificNotation, Str, Bool
}

// 定义token的格式
class Token {
    Type type;
    String value;
    int lineNum;

    public Token(Type type, String value, int lineNum) {
        this.type = type;
        this.value = value;
        this.lineNum = lineNum;
    }
}

// 分词器
public class Parser {
    // 储存token
    private ArrayList<Token> tokens = new ArrayList<Token>();
    // 需要读取的yml文本
    private char[] content;
    // 当前需要处理的字符
    private char processChar;
    // 当前的行号
    private int countLine = 1;
    // 在行所处的位置
    private int countPosition = 0;
    // 当前处理的字符索引
    private int index = 0;
    // 上一个处理好的字符索引
    private int lastIndex = -1;

    // 将目标文档的内容以字符数组的形式读入Parser
    public Parser(String fileName) {
        content = readToCharArray(fileName);
    }

    // 获取所有token
    public ArrayList<Token> getTokens() {
        return tokens;
    }

    // 完成换行相关的所有操作
    private void changeLine() {
        tokens.add(new Token(Type.ChangeLine, "\r\n", countLine));
        countLine++;
        countPosition = 0;
        index++;
        lastIndex = index - 1;
        if (index < content.length)
            processChar = getNextChar();
    }

    // 加入新的token
    private void addToken(Type type) {
        char[] newValue = new char[index - lastIndex - 1];
        int j = 0;
        for (int i = lastIndex + 1; i < index; i++) {
            newValue[j] = content[i];
            j++;
        }
        String value = String.valueOf(newValue);
        switch (type) {
            case ChangeLine:
                changeLine();
                break;
            case Identifier:
                if (value.equals("true") || value.equals("false"))
                    tokens.add(new Token(Type.Bool, value, countLine));
                else
                    tokens.add(new Token(type, value, countLine));
                index++;
                lastIndex = index - 1;
                break;
            case Int:
                tokens.add(new Token(type, value, countLine));
                index++;
                lastIndex = index - 1;
                break;
            case Double:
                tokens.add(new Token(type, value, countLine));
                index++;
                lastIndex = index - 1;
                break;
            case ScientificNotation:
                tokens.add(new Token(type, value, countLine));
                index++;
                lastIndex = index - 1;
                break;
            case Operator:
                tokens.add(new Token(type, String.valueOf(processChar), countLine));
                lastIndex = index - 1;
                processChar = getNextChar();
                break;
            case Space:
                tokens.add(new Token(type, String.valueOf(processChar), countLine));
                lastIndex = index - 1;
                processChar = getNextChar();
                break;
            case Str:
                tokens.add(new Token(type, value, countLine));
                lastIndex = index - 1;
                break;
        }
    }

    // 获取下一个待处理字符
    private char getNextChar() {
        char returnValue = content[index];
        if(index<content.length){
            index++;
        }
        countPosition++;
        return returnValue;
    }

    // 读取文件
    private char[] readToCharArray(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding).toCharArray();
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    // 对于数字相关的类型（整型，浮点数，科学计数法等）的处理
    private void processNumber() throws YAMLException {
        processChar = getNextChar();
        Type numberType = Type.Int;
        while (index < content.length) {
            if ((processChar <= '9' && processChar >= '0') || processChar == '.' || processChar == 'e' || processChar == 'E' || processChar == '-') {
                if (processChar <= '9' && processChar >= '0')
                    processChar = getNextChar();
                else if (processChar == '.' && numberType != Type.Double) {
                    numberType = Type.Double;
                    processChar = getNextChar();
                } else if ((processChar == 'e' || processChar == 'E') && numberType != Type.ScientificNotation) {
                    if (content[index] == '-') {
                        numberType = Type.ScientificNotation;
                        index++;
                        processChar = getNextChar();
                    } else {
                        throw new YAMLException(ErrorType.ScientificNotation, countLine, countPosition);
                    }
                } else {
                    throw new YAMLException(ErrorType.IdentifierOrNumber, countLine, countPosition);
                }
            } else if (processChar == '\r' || processChar == 32 || processChar == '#') {
                index--;
                addToken(numberType);
                break;
            } else {
                throw new YAMLException(ErrorType.IdentifierOrNumber, countLine, countPosition);
            }
        }
    }

    // 处理标识符
    private void processIdentifier() throws YAMLException {
        processChar = getNextChar();
        boolean isUnderLine = false;
        while (index < content.length) {
            if ((processChar <= 'z' && processChar >= 'a') || (processChar <= 'Z' && processChar >= 'A') || (processChar <= '9' && processChar >= '0') || processChar == '_') {
                if (processChar == '_')
                    isUnderLine = true;
                else
                    isUnderLine = false;
                processChar = getNextChar();
            } else if (processChar == ':' || processChar == '\r' || processChar == '#') {
                if (!isUnderLine) {
                    index--;
                    addToken(Type.Identifier);
                } else
                    throw new YAMLException(ErrorType.IdentifierEnd, countLine, countPosition);
                break;
            } else {
                throw new YAMLException(ErrorType.Identifier, countLine, countPosition);
            }
        }
    }

    // 处理字符串类型
    private void processString() {
        processChar = getNextChar();
        while (processChar != '"') {
            processChar = getNextChar();
        }
        index--;
        lastIndex++;
        addToken(Type.Str);
        index++;
        processChar = getNextChar();
    }

    public void process() {
        System.out.println("词法分析开始");
        processChar = getNextChar();
        while (index < content.length) {
            if (processChar <= '9' && processChar >= '0') {
                try {
                    processNumber();
                } catch (YAMLException e) {
                    e.printMessage();
                }
            } else if ((processChar <= 'z' && processChar >= 'a') || (processChar <= 'Z' && processChar >= 'A')) {
                try {
                    processIdentifier();
                } catch (YAMLException e) {
                    e.printMessage();
                }
            } else if (processChar == '"') {
                processString();
            } else if (processChar == '\r') {
                addToken(Type.ChangeLine);
            } else if (processChar == ':' || processChar == '-') {
                addToken(Type.Operator);
            } else if (processChar == 32) {
                addToken(Type.Space);
            } else if (processChar == '#') {
                while (processChar != '\r') {
                    processChar = getNextChar();
                }
            }
        }
        clearSpace();
        clearChangeLine();
        System.out.println("词法分析完成");
        if (Valid.isValid) {
            System.out.println("词法分析无误");
        }
    }


    // 去除无用的空格
    private void clearSpace() {
        boolean canBeDelete = false;
        int total = tokens.size();
        for (int i = total - 1; i >= 0; i--) {
            if (tokens.get(i).type == Type.ChangeLine)
                canBeDelete = true;
            else if (tokens.get(i).type == Type.Space) {
                if (canBeDelete)
                    tokens.remove(i);
            } else
                canBeDelete = false;
        }
    }

    // 去除无用的换行
    private void clearChangeLine() {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == Type.ChangeLine) {
                i++;
                while (i < tokens.size() && tokens.get(i).type == Type.ChangeLine) {
                    tokens.remove(i);
                }
            }
        }
    }
}

