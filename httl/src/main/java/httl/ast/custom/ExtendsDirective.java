/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package httl.ast.custom;

import httl.Context;
import httl.Visitor;
import httl.ast.LineDirective;
import httl.spi.translators.templates.InterpretedVisitor;
import httl.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;

/**
 * 
 * @author zhangyi
 */
public class ExtendsDirective extends LineDirective {

    private final String parentName;

    public ExtendsDirective(String parentName, int offset) {
        super(offset);
        if (StringUtils.isNotBlank(parentName)) {
            this.parentName = parentName.replaceAll("\"", "");
        } else {
            this.parentName = null;
        }

    }

    public String getParentName() {
        return parentName;
    }
    
    /** 
     * @see httl.ast.Directive#accept(httl.Visitor)
     */
    public void accept(Visitor visitor) throws IOException, ParseException {
        visitor.visit(this);
        //可以删除parent中的Text对象
        if (StringUtils.isNotBlank(parentName)) {
            Context.getContext().put(DirectiveEnum.EXTENDS.getCode(), parentName);
        }
    }

    /** 
     * @see httl.ast.Statement#interpretedVisit(httl.spi.translators.templates.InterpretedVisitor)
     */
    @Override
    public void interpretedVisit(InterpretedVisitor visitor) throws IOException, ParseException {
        super.interpretedVisit(visitor);

    }

}
