/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package httl.ast.custom;

/**
 *
 * @author zhangyi
 */
public enum DirectiveEnum {

    EXTENDS("__EXTENDS__"),

    BLOCK("__BLOCK__"),

    SUPER("__SUPER__"),

    CACHE("__CACHE__"),

    CSS("__CSS__"),

    JS("__JS__");

    private String code;

    /**
     *
     */
    private DirectiveEnum(String code) {
        this.code = code;
    }

    /**
     * Getter method for property <tt>code</tt>.
     *
     * @return property value of code
     */
    public String getCode() {
        return code;
    }

    public String getVariableName(String name) {
        return code + name;
    }

}
