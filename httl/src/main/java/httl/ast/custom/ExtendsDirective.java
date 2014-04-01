/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package httl.ast.custom;

import httl.ast.BlockDirective;
import httl.spi.translators.templates.CompiledVisitor;
import httl.spi.translators.templates.InterpretedVisitor;

/**
 * 
 * @author zhangyi
 * @version $Id: ExtendsDirective.java, v 0.1 2014年4月1日 下午6:27:57 zhangyi Exp $
 */
public class ExtendsDirective extends BlockDirective {

    private final String parentName;

    public ExtendsDirective(String parentName, int offset) {
        super(offset);
        this.parentName = parentName;
    }

    /**
     * Getter method for property <tt>parentName</tt>.
     * 
     * @return property value of parentName
     */
    public String getParentName() {
        return parentName;
    }

    /** 
     * @see httl.ast.Statement#interpretedVisit(httl.spi.translators.templates.InterpretedVisitor)
     */
    @Override
    public void interpretedVisit(InterpretedVisitor visitor) {
        super.interpretedVisit(visitor);
    }

    /** 
     * @see httl.ast.Statement#compiledVisit(httl.spi.translators.templates.CompiledVisitor)
     */
    @Override
    public void compiledVisit(CompiledVisitor visitor) {
        super.compiledVisit(visitor);
    }
}
