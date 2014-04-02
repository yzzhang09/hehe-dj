/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package httl.ast.custom;

import httl.Context;
import httl.Node;
import httl.Visitor;
import httl.ast.BlockDirective;
import httl.ast.Expression;
import httl.ast.Text;
import httl.spi.translators.templates.InterpretedVisitor;
import httl.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author zhangyi
 */
public class BlockExDirective extends BlockDirective {

    final static Logger    logger         = LoggerFactory.getLogger(BlockExDirective.class);

    private final String   name;

    private SuperDirective superDirective = null;

    public BlockExDirective(String name, int offset) {
        super(offset);
        this.name = name;
    }

    public void accept(Visitor visitor) throws IOException, ParseException {
        visitor.visit(this);
    }

    /** 
     * @see httl.ast.BlockDirective#setChildren(java.util.List)
     */
    @Override
    public void setChildren(List<Node> children) throws ParseException {
        super.setChildren(children);
        for (Node node : children) {
            if (node instanceof SuperDirective) {
                superDirective = (SuperDirective) node;
            }
        }
    }

    /**
     * Getter method for property <tt>superDirective</tt>.
     * 
     * @return property value of superDirective
     */
    public boolean hasSuperDirective() {
        return superDirective != null;
    }

    /**
     * Getter method for property <tt>name</tt>.
     * 
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /** 
     * @see httl.ast.Statement#interpretedVisit(httl.spi.translators.templates.InterpretedVisitor)
     */
    @Override
    public void interpretedVisit(InterpretedVisitor visitor) throws IOException, ParseException {

        Context context = Context.getContext();
        String blockString = null;
        if (context.containsKey(DirectiveEnum.EXTENDS.getCode())) {
            if (context.containsKey(DirectiveEnum.BLOCK.getVariableName(name))) {
                ((Block) context.get(DirectiveEnum.BLOCK.getVariableName(name))).addNode(this);
            } else {
                context.put(DirectiveEnum.BLOCK.getVariableName(name), new Block(name, this));
            }
        } else if (context.containsKey(DirectiveEnum.BLOCK.getVariableName(name))) {
            //判断是否包含super确定如何渲染页面
            Block block = (Block) context.get(DirectiveEnum.BLOCK.getVariableName(name));
            block.addNode(this);
            blockString = block.render(visitor);
        } else {
            blockString = doAcceptWithReturn(visitor);
        }

        if (StringUtils.isNotBlank(blockString)) {
            visitor.visit(new Text(blockString, false, 0));
        }
    }

    private String doAcceptWithReturn(InterpretedVisitor visitor) throws IOException, ParseException {
        Object out = visitor.getOut();

        StringWriter sw = new StringWriter();
        visitor.setOut(sw);

        Expression expression = getExpression();
        if (expression != null) {
            expression.accept(visitor);
        }
        if (visitor.visit(this)) {
            if (getChildren() != null) {
                for (Node node : getChildren()) {
                    if (node instanceof SuperDirective) {
                        Object text = Context.getContext().get(DirectiveEnum.SUPER.getVariableName(name));
                        if (text != null && StringUtils.isNotBlank((String) text)) {
                            visitor.visit(new Text((String) text, false, 0));
                        }
                    } else {
                        node.accept(visitor);
                    }

                }
            }
            if (getEnd() != null) {
                getEnd().accept(visitor);
            }
        }
        //end
        visitor.setOut(out);

        String result = sw.toString();
        logger.trace("#blockStart{}#blockEnd", result);
        return result;
    }

    class Block {
        String                      name;
        ArrayList<BlockExDirective> nodes = new ArrayList<BlockExDirective>();

        /**
         * @param name
         * @param value
         */
        Block(String name, BlockExDirective node) {
            super();
            this.name = name;
            addNode(node);
        }

        void addNode(BlockExDirective node) {
            this.nodes.add(node);
        }

        String render(InterpretedVisitor visitor) throws IOException, ParseException {

            LinkedList<BlockExDirective> blocks = new LinkedList<BlockExDirective>();
            for (BlockExDirective block : nodes) {
                if (block.hasSuperDirective()) {
                    blocks.addFirst(block);
                } else {
                    //最后一个node处理
                    blocks.addFirst(block);
                    break;
                }
            }

            String varName = DirectiveEnum.SUPER.getVariableName(name);
            while (!blocks.isEmpty()) {
                BlockExDirective block = blocks.pop();
                Context.getContext().put(varName, block.doAcceptWithReturn(visitor));
            }
            Object result = Context.getContext().get(varName);
            return result == null ? "" : (String) result;
        }
    }

}
