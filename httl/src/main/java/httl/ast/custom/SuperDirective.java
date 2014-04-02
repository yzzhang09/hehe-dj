/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package httl.ast.custom;

import httl.ast.Directive;

/**
 * 
 * @author zhangyi
 */
public class SuperDirective extends Directive {

    /**
     * @param offset
     */
    public SuperDirective(int offset) {
        super(offset);
    }

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "block.super";
    }

}
