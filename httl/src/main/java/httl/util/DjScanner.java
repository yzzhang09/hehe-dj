/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package httl.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author zhangyi
 * @version $Id: DjScanner.java, v 0.1 2014年3月29日 下午10:26:28 Administrator Exp $
 */
public class DjScanner {

    public static void main(String[] args) throws ParseException, IOException {
        System.out.println(new DjScanner().scan("fafafafa{{FFFF}}FfFF{%FFFFFFF%}fFf", 0));
    }

    public List<Token> scan(String charStream, int offset) throws ParseException, IOException {
        List<Token> tokens = new ArrayList<Token>();
        Reader reader = new StringReader(charStream);
        StringBuffer sb = new StringBuffer();

        int c;
        while ((c = reader.read()) != -1) {
            if (c == '{') {
                reader.mark(1);
                c = reader.read();
                if (c == '{') {
                    sbToToken(tokens, sb, Type.UNKNOWN);
                    sb = new StringBuffer();
                    //读取linetoken
                    readLineToken(tokens, reader);
                } else if (c == '%') {
                    sbToToken(tokens, sb, Type.UNKNOWN);
                    sb = new StringBuffer();
                    //读取blockToken
                    readBlockToken(tokens, reader);
                } else {
                    sb.append('{');
                    reader.reset();
                }
            } else {
                sb.append((char) c);
            }
        }

        sbToToken(tokens, sb, Type.UNKNOWN);
        return tokens;
    }
    
    private void readLineToken(List<Token> tokens, Reader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '}') {
                reader.mark(1);
                c = reader.read();
                if (c == '}') {
                    break;
                } else {
                    sb.append('}');
                    reader.reset();
                }
            }
            if (c != '\n' && c != '\r') {
                sb.append((char) c);
            }
        }
        sbToToken(tokens, sb, Type.LINE);
    }

    private void readBlockToken(List<Token> tokens, Reader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '%') {
                reader.mark(1);
                c = reader.read();
                if (c == '}') {
                    break;
                } else {
                    sb.append('%');
                    reader.reset();
                }
            }
            if (c != '\n' && c != '\r') {
                sb.append((char) c);
            }
        }
        sbToToken(tokens, sb, Type.BLOCK);
    }

    private void sbToToken(List<Token> tokens, StringBuffer sb, Type type) {
        String s = sb.toString();
        if (StringUtils.isNotBlank(s)) {
            tokens.add(new Token(s, 0, type.getValue()));
        }
    }

    public enum Type {
        LINE(1),

        BLOCK(2),

        UNKNOWN(-1);
        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
