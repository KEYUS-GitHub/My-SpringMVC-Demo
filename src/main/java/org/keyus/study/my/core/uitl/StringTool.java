package org.keyus.study.my.core.uitl;

public class StringTool {

    public static String toLowerFirstWord(String string){
        if (string == null)
            throw new NullPointerException();
        if ("".equals(string))
            return string;
        // 获得需要的首字母小写
        char c = Character.toLowerCase(string.charAt(0));
        StringBuilder builder = new StringBuilder(string);
        // 替换原来的首字母为小写字母
        builder.deleteCharAt(0);
        builder.insert(0, c);
        return builder.toString();
    }
}
