package com.timtiptoes.helpers;

public class SheetHelper {
    public static StringBuilder buildHeader(StringBuilder stringBuilder,int clueLength){
        stringBuilder.append("<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <style>\n" +
                        "    .number {\n" +
                        "      position: relative;\n" +
                        "      display: inline-block;\n" +
                        "      font-size: 24px;\n" +
                        "      font-weight: bold;\n" +
                        "    }\n" +
                        "    .number:before {\n" +
                        "      content: \"\";\n" +
                        "      position: absolute;\n" +
                        "      top: 50%;\n" +
                        "      left: 40;\n" +
                        "      width: 100%;\n" +
                        "      height: 2px;\n" +
                        "      background-color: black;\n" +
                        "      transform: translateY(-700%);\n" +
                        "    }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "  <br><br><br>");
        stringBuilder.append("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
        for (int i = 0; i < clueLength; i++) {
            stringBuilder.append(String.format("&nbsp&nbsp&nbsp<div class=\"number\">&nbsp&nbsp%s&nbsp&nbsp</div>",i+1));
        }
        stringBuilder.append("<TABLE>");
        stringBuilder.append("<TR><TH>");
        stringBuilder.append("<br><br><br><br><br><br><br><img src=\"http://73.162.98.176:8081/board_key.png\" alt=\"chess key\" width=\"350\" height=\"350\">");
        return stringBuilder;
    }
}
