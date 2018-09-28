package com.company;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Func {
    ArrayList<ValueStore> values;

    public Func(Analyzer analyzer) {
        this.values = analyzer.values;
    }
    public String jsonAppend(String json,ValueStore element,boolean flag){
        if(!element.name.equals("-")&&element.value!=null){
            if(flag){
                json+="{";
            }
            if(element.type==Type.Str){
                json=json+"\""+element.name+"\"";
                json+=":\"";
                json+=element.value;
                json+="\",";
            }
            else{
                json=json+"\""+element.name+"\"";
                json+=":";
                json+=element.value;
                json+=",";
            }
        }
        else if(element.type==Type.Identifier&&element.value==null){
            json=json+"\""+element.name+"\"";
            json+=":\"";
        }
        else if(element.type==Type.Str){
            json+="\"";
            json+=element.value;
            json+="\"";
            json+=",";
        }
        else if(element.name.equals("-")&&element.value!=null){
            json+=element.value;
            json+=",";
        }
        else if(element.equals(null)){
            char[] jsonArray=json.toCharArray();
            jsonArray[jsonArray.length-1]=']';
            json = new String(jsonArray);
        }
        if(flag){
            char[] jsonArray=json.toCharArray();
            jsonArray[jsonArray.length-1]='}';
            json = new String(jsonArray);
            json+=",";
        }
        return json;
    }
    public String printJson(){
        String json = "{";
        for(int i=0;i<values.size();i++){
            ValueStore curValue = values.get(i);
            if(!curValue.name.equals("-")&&curValue.value!=null){
                json=jsonAppend(json,curValue,false);
            }
            else if(!curValue.name.equals("-")&&curValue.value==null){
                json=json+"\""+curValue.name+"\"";
                json+=":";
                i++;
                curValue=values.get(i);
                Stack<Integer> indents = new Stack<Integer>();
                if(curValue.name.equals("-")&&curValue.value!=null){
                    json+="[";
                    json+=curValue.value;
                    json+=",";
                    indents.push(0);
                    indents.push(1);
                    int curIndent = 0;
                    while(!indents.isEmpty()){
                        if(i==values.size()-1){
                            char []jsonArray = json.toCharArray();
                            jsonArray[jsonArray.length-1]=']';
                            json = new String(jsonArray);
                            json+=",";
                            break;
                        }
                        i++;
                        curValue=values.get(i);
                        curIndent = curValue.indentLayer;
                        if(curIndent==indents.peek()){
                            json=jsonAppend(json,curValue,false);
                        }
                        else if(curIndent>indents.peek()){
                            if(curValue.type!=Type.Identifier&&curValue.type!=Type.Str
                                    &&curValue.type!=Type.Bool&&curValue.type!=Type.Double
                                    &&curValue.type!=Type.ScientificNotation){
                                json+="[";
                                json = jsonAppend(json, curValue,false);
                                indents.push(curValue.indentLayer);
                            }
                            else{
                                json = jsonAppend(json,curValue,true);
                            }
                        }
                        else{
                            indents.pop();
                            if(indents.peek()==0){
                                indents.pop();
                            }
                            i--;
                            char []jsonArray = json.toCharArray();
                            jsonArray[jsonArray.length-1]=']';
                            json = new String(jsonArray);
                            json+=",";
                        }
                    }
                }
            }
        }
        char []jsonArray = json.toCharArray();
        jsonArray[jsonArray.length-1]='}';
        json = new String(jsonArray);
        return json;
    }
    public void wrtieToFile(String name){
        String fileName = name+".json";
        try{
            String json = printJson();
            System.out.println(json);
            File file =new File(fileName);
            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
            }
            //true = append file
            FileWriter fileWritter = new FileWriter(file.getName(),true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(json);
            bufferWritter.close();
            System.out.println("写入文件成功");
        }catch(IOException e){
            e.printStackTrace();
        }


    }
    public Object find(String msg){
        String []parameters = msg.split("\\.");
            List<String> list=new ArrayList<String>();
            Pattern p = Pattern.compile("(\\[[^\\]]*\\])");
            Matcher m = p.matcher(msg);
            while(m.find()){
                list.add(m.group().substring(1, m.group().length()-1));
            }
            if(list.size()>0){
                if(parameters.length==1){
                    return findTool(msg.substring(0,msg.indexOf("[")),Integer.valueOf(list.get(0)),null);
                }
                else{
                    return findTool(msg.substring(0,msg.indexOf("[")),Integer.valueOf(list.get(0)),parameters[1]);
                }
            }

            else{
                if(parameters.length==1){
                    return findTool(msg,-1,null);
                }
                else{
                    return findTool(msg,-1,parameters[1]);
                }
            }
    }
    public Object findTool(String searchKey,int counter,String key){
        Object result=null;
        for(int i=0;i<values.size();i++){
            ValueStore value = values.get(i);
            if(value.name.equals(searchKey)){
                if(value.value!=null){
                    result = value.value;
                }
                else{
                    int elementCount = 0;
                    for(int j=i+1;j<values.size();j++){
                        ValueStore  tmpValue = values.get(j);
                        if(tmpValue.indentLayer==value.indentLayer+1||!tmpValue.name.equals("-")){
                            elementCount++;
                        }
                        else if(tmpValue.indentLayer<value.indentLayer){
                            break;
                        }
                        if(elementCount==counter+1){
                            if(tmpValue.name.equals("-")){
                                result = tmpValue.value;
                            }
                            else if(key!=null&&tmpValue.name.equals(key)&&tmpValue.value!=null){
                                result = tmpValue.value;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
}
