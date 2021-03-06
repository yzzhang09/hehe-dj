/*
 * Copyright 2011-2013 HTTL Team.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package httl.ast;

import httl.Node;
import httl.Visitor;
import httl.spi.translators.templates.CompiledVisitor;
import httl.spi.translators.templates.InterpretedVisitor;

import java.io.IOException;
import java.text.ParseException;

/**
 * Expression. (API, Prototype, Immutable, ThreadSafe)
 * 
 * @see httl.spi.parsers.ExpressionParser#parse(String, int)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class Expression implements Node {

	private final int offset;
	
	private Expression parent;

	public Expression(int offset) {
		this.offset = offset;
	}

	public void accept(Visitor visitor) throws IOException, ParseException {
		visitor.visit(this);
	}

	public int getOffset() {
		return offset;
	}

	public Expression getParent() {
		return parent;
	}

	public void setParent(Expression parent) {
		if (this.parent != null)
			throw new IllegalStateException("Can not modify parent.");
		this.parent = parent;
	}

    /** 
     * @see httl.Node#interpretedVisit(httl.spi.translators.templates.InterpretedVisitor)
     */
    @Override
    public void interpretedVisit(InterpretedVisitor visitor) {
    }

    /** 
     * @see httl.Node#compiledVisit(httl.spi.translators.templates.CompiledVisitor)
     */
    @Override
    public void compiledVisit(CompiledVisitor visitor) {
    }

}