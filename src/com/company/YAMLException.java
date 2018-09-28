package com.company;

enum ErrorType {
    ScientificNotation, IdentifierOrNumber, IdentifierEnd, Identifier,
    Indent, Array, Element, LineStart, KeyValue
}

public class YAMLException extends Exception {
    private static String[] errorMessage = {"非法科学计数法形式", "非法标识符或数字形式", "非法标识符形式，标识符不能以'_'结尾", "非法标识符形式",
    "非法缩进","非法数组","多余的未知元素","非法的行开头","非法的键值对"};
    private int lineNum;
    private int position;
    private ErrorType errorType;

    public YAMLException() {
        super();
        Valid.isValid = false;
    }

    public YAMLException(String msg) {
        super(msg);
        Valid.isValid = false;
    }

    public YAMLException(String msg, int lineNum, int position) {
        super(msg);
        this.lineNum = lineNum;
        this.position = position;
        Valid.isValid = false;
    }

    public YAMLException(ErrorType errorType, int lineNum, int position) {
        super();
        this.lineNum = lineNum;
        this.position = position;
        this.errorType = errorType;
        Valid.isValid = false;
    }

    public void printMessage() {
        if (position == -1)
            System.out.println("错误:" + "行 " + lineNum + "信息：" + errorMessage[errorType.ordinal()]);
        else
            System.out.println("错误:" + "行" + lineNum + ",位置" + position+ "信息：" + errorMessage[errorType.ordinal()]);
    }
}
