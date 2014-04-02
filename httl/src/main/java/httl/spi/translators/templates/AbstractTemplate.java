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
package httl.spi.translators.templates;

import httl.Context;
import httl.Engine;
import httl.Node;
import httl.Resource;
import httl.Template;
import httl.Visitor;
import httl.ast.BlockDirective;
import httl.ast.MacroDirective;
import httl.ast.custom.DirectiveEnum;
import httl.spi.Converter;
import httl.spi.Interceptor;
import httl.spi.Listener;
import httl.util.ClassUtils;
import httl.util.StringUtils;
import httl.util.UnsafeStringWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AbstractTemplate. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public abstract class AbstractTemplate implements Template {

    private final Resource resource;

    private final Node     root;

    private Template       parent;

    private final String   name;

    private final String   encoding;

    private final Locale   locale;

    private final long     lastModified;

    private final long     length;

    public AbstractTemplate(Resource resource, Node root, Template parent) {
        this.resource = resource;
        this.root = root;
        this.parent = parent;
        this.name = buildName(resource, root);
        this.encoding = resource.getEncoding();
        this.locale = resource.getLocale();
        // ע�⣺lastModified����������ĸ���������resource.getLastModified()���������뻺��
        this.lastModified = resource.getLastModified();
        this.length = resource.getLength();
    }

    private static String buildName(Resource resource, Node root) {
        StringBuilder builder = new StringBuilder();
        builder.append(resource.getName());
        while (root instanceof MacroDirective) {
            builder.append("#");
            builder.append(((MacroDirective) root).getName());
            root = root.getParent();
        }
        return builder.toString();
    }

    private Converter<Object, Object> mapConverter;

    private Converter<Object, Object> outConverter;

    private Interceptor               interceptor;

    private Object convertOut(Object out) throws IOException, ParseException {
        if (outConverter != null && out != null && !(out instanceof OutputStream) && !(out instanceof Writer)) {
            return outConverter.convert(out, getVariables());
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertMap(Object context) throws ParseException {
        if (mapConverter != null && context != null && !(context instanceof Map)) {
            try {
                context = mapConverter.convert(context, getVariables());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        if (context == null || context instanceof Map) {
            return (Map<String, Object>) context;
        } else {
            throw new IllegalArgumentException("No such Converter to convert the " + context.getClass().getName()
                                               + " to Map.");
        }
    }

    public void render(Object parameters, Object out) throws IOException, ParseException {
        Map<String, Object> map = convertMap(parameters);
        out = convertOut(out);
        Context context = Context.pushContext(map);
        try {
            context.setTemplate(this);
            if (out instanceof OutputStream) {
                context.setOut((OutputStream) out);
            } else if (out instanceof Writer) {
                context.setOut((Writer) out);
            } else {
                throw new IllegalArgumentException("No such Converter to convert the " + out.getClass().getName()
                                                   + " to OutputStream or Writer.");
            }
            if (interceptor != null) {
                interceptor.render(context, new Listener() {
                    public void render(Context context) throws IOException, ParseException {
                        _render(context);
                    }
                });
            } else {
                _render(context);
            }
        } catch (ParseException e) {
            throw toLocatedParseException(e, this);
        } finally {
            Context.popContext();
        }
    }

    private void _render(Context context) throws IOException, ParseException {
        try {
            doRender(context);
        } catch (RuntimeException e) {
            throw (RuntimeException) e;
        } catch (IOException e) {
            throw (IOException) e;
        } catch (ParseException e) {
            throw (ParseException) e;
        } catch (Exception e) {
            throw new RuntimeException(ClassUtils.toString(e), e);
        }
    }

    protected abstract void doRender(Context context) throws Exception;

    protected Resource getResource() {
        return resource;
    }

    protected Node getRoot() {
        return root;
    }

    protected Converter<Object, Object> getMapConverter() {
        return mapConverter;
    }

    protected Converter<Object, Object> getOutConverter() {
        return outConverter;
    }

    protected Interceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void setMapConverter(Converter<Object, Object> mapConverter) {
        this.mapConverter = mapConverter;
    }

    public void setOutConverter(Converter<Object, Object> outConverter) {
        this.outConverter = outConverter;
    }

    public String getName() {
        return name;
    }

    public String getEncoding() {
        return encoding;
    }

    public Locale getLocale() {
        return locale;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getLength() {
        return length;
    }

    public String getSource() throws IOException {
        return resource.getSource();
    }

    public Reader openReader() throws IOException {
        return resource.openReader();
    }

    public InputStream openStream() throws IOException {
        return resource.openStream();
    }

    public Engine getEngine() {
        return resource.getEngine();
    }

    public int getOffset() {
        return root.getOffset();
    }

    public Template getParent() {
        return parent;
    }

    public void setParent(Template parent) {
        this.parent = parent;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Node> getChildren() {
        return (List) ((BlockDirective) root).getChildren();
    }

    public boolean isMacro() {
        return root instanceof MacroDirective;
    }

    public void accept(Visitor visitor) throws IOException, ParseException {
        if (visitor.visit(this)) {
            for (Node node : getChildren()) {
                node.accept(visitor);
            }
        }
        if (parent != null && parent instanceof AbstractTemplate) {
            Context.getContext().remove(DirectiveEnum.EXTENDS.getCode());
            parent.accept(visitor);
        }
    }

    public Object evaluate() throws ParseException {
        return evaluate(null);
    }

    public void render(Object out) throws IOException, ParseException {
        render(null, out);
    }

    public void render() throws IOException, ParseException {
        render(null, Context.getContext().getOut());
    }

    public Object evaluate(Object context) throws ParseException {
        UnsafeStringWriter writer = new UnsafeStringWriter();
        try {
            render(context, writer);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return writer.toString();
    }

    public static ParseException toLocatedParseException(ParseException e, Resource resource) {
        if (e.getMessage() != null && e.getMessage().contains("Occur to offset:")) {
            return e;
        }
        int offset = e.getErrorOffset();
        if (offset <= 0) {
            return e;
        }
        String location = null;
        try {
            Reader reader = resource.openReader();
            try {
                location = StringUtils.getLocationMessage(resource.getName(), reader, offset);
            } finally {
                reader.close();
            }
        } catch (Throwable t) {
        }
        return new ParseException(e.getMessage() + "\nOccur to offset: " + offset
                                  + (StringUtils.isEmpty(location) ? "" : ", " + location) + ", stack: "
                                  + ClassUtils.toString(e), offset);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        String name = getName();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractTemplate other = (AbstractTemplate) obj;
        String name = getName();
        String otherName = other.getName();
        if (name == null) {
            if (otherName != null)
                return false;
        } else if (!name.equals(otherName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getName();
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