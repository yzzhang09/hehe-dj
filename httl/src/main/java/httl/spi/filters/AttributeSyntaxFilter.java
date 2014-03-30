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
package httl.spi.filters;

import httl.spi.Filter;
import httl.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

/**
 * AttributeSyntaxFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTemplateFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTemplateFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AttributeSyntaxFilter extends AbstractFilter {

    private String ifattr = "ifattr";

    private String setattr = "setattr";

    private String[] setDirective = new String[]{"var"};

    private String[] ifDirective = new String[]{"if"};

    private String[] elseifDirective = new String[]{"elseif"};

    private String[] elseDirective = new String[]{"else"};

    private String[] forDirective = new String[]{"for"};

    private String[] breakifDirective = new String[]{"breakif"};

    private String[] macroDirective = new String[]{"macro"};

    private String[] endDirective = new String[]{"end"};

    private String attributeNamespace;

    /**
     * httl.properties: set.directive=set
     */
    public void setSetDirective(String[] setDirective) {
        this.setDirective = setDirective;
    }

    /**
     * httl.properties: if.directive=if
     */
    public void setIfDirective(String[] ifDirective) {
        this.ifDirective = ifDirective;
    }

    /**
     * httl.properties: elseif.directive=elseif
     */
    public void setElseifDirective(String[] elseifDirective) {
        this.elseifDirective = elseifDirective;
    }

    /**
     * httl.properties: else.directive=else
     */
    public void setElseDirective(String[] elseDirective) {
        this.elseDirective = elseDirective;
    }

    /**
     * httl.properties: for.directive=for
     */
    public void setForDirective(String[] forDirective) {
        this.forDirective = forDirective;
    }

    /**
     * httl.properties: breakif.directive=breakif
     */
    public void setBreakifDirective(String[] breakifDirective) {
        this.breakifDirective = breakifDirective;
    }

    /**
     * httl.properties: macro.directive=macro
     */
    public void setMacroDirective(String[] macroDirective) {
        this.macroDirective = macroDirective;
    }

    /**
     * httl.properties: end.directive=end
     */
    public void setEndDirective(String[] endDirective) {
        this.endDirective = endDirective;
    }

    /**
     * httl.properties: attribute.namespace=httl
     */
    public void setAttributeNamespace(String attributeNamespace) {
        if (!attributeNamespace.endsWith(":")) {
            attributeNamespace = attributeNamespace + ":";
        }
        this.attributeNamespace = attributeNamespace;
    }

    private boolean isDirective(String name) {
        return StringUtils.inArray(name, setDirective)
                || StringUtils.inArray(name, ifDirective) || StringUtils.inArray(name, elseifDirective)
                || StringUtils.inArray(name, elseDirective) || StringUtils.inArray(name, forDirective)
                || StringUtils.inArray(name, breakifDirective) || StringUtils.inArray(name, macroDirective)
                || StringUtils.inArray(name, endDirective);
    }

    private boolean isBlockDirective(String name) {
        return StringUtils.inArray(name, ifDirective) || StringUtils.inArray(name, elseifDirective)
                || StringUtils.inArray(name, elseDirective) || StringUtils.inArray(name, forDirective)
                || StringUtils.inArray(name, macroDirective);
    }

    public String filter(String key, String value) {
        Source source = new Source(value);
        OutputDocument document = new OutputDocument(source);
        replaceChildren(source, source, document);
        return document.toString();
    }

    // �滻��Ԫ���е�ָ������
    private void replaceChildren(Source source, Segment segment, OutputDocument document) {
        // ������Ԫ�أ��������
        List<Element> elements = segment.getChildElements();
        if (elements != null) {
            for (Element element : elements) {
                if (element != null) {
                    // ---- ��ǩ���Դ��� ----
                    List<String> directiveNames = new ArrayList<String>();
                    List<String> directiveValues = new ArrayList<String>();
                    List<Attribute> directiveAttributes = new ArrayList<Attribute>();
                    // ������ǩ���ԣ�����ָ������
                    Attributes attributes = element.getAttributes();
                    if (attributes != null) {
                        for (Attribute attribute : attributes) {
                            if (attribute != null) {
                                String name = attribute.getName();
                                if (name != null && (isDirective(name) || (attributeNamespace != null && name.startsWith(attributeNamespace)) && isDirective(name.substring(attributeNamespace.length())))) { // ʶ�����ƿռ�
                                    String directiveName = attributeNamespace != null ? name.substring(attributeNamespace.length()) : name;
                                    String value = attribute.getValue();
                                    directiveNames.add(directiveName);
                                    directiveValues.add(value);
                                    directiveAttributes.add(attribute);
                                }
                            }
                        }
                    }
                    // ---- ָ��� ----
                    if (directiveNames.size() > 0) {
                        StringBuffer buf = new StringBuffer();
                        for (int i = 0; i < directiveNames.size(); i++) { // ��˳����ӿ�ָ��
                            String directiveName = (String) directiveNames.get(i);
                            String directiveValue = (String) directiveValues.get(i);
                            buf.append("#");
                            buf.append(directiveName);
                            buf.append("(");
                            buf.append(directiveValue);
                            buf.append(")");
                        }
                        document.insert(element.getBegin(), buf.toString()); // �����ָ��
                    }
                    // ---- ָ�����Դ��� ----
                    for (int i = 0; i < directiveAttributes.size(); i++) {
                        Attribute attribute = (Attribute) directiveAttributes.get(i);
                        document.remove(new Segment(source, attribute.getBegin() - 1, attribute.getEnd())); // �Ƴ�����
                    }

                    if (attributes != null) {
                        //�����չ��ifattrָ��
                        for (Attribute attribute : attributes) {
                            if (attribute != null) {
                                String name = attribute.getName();
                                if (ifattr.equals(name)) {
                                    String val = attribute.getValue();
                                    String[] arr = val.split(",");
                                    String attrName = arr[0].trim();
                                    String expression = arr[1].trim();

                                    //�޸�ԭattribute
                                    Attribute oriattr = attributes.get(attrName);
                                    if (oriattr != null) {
                                        String buf = String.format("#if(%s)%s=\"%s\"#end()", expression, oriattr.getName(), oriattr.getValue());
                                        document.replace(new Segment(source, oriattr.getBegin(), oriattr.getEnd()), buf);
                                        document.remove(new Segment(source, attribute.getBegin(), attribute.getEnd())); // �Ƴ�ifattr��������
                                    }
                                }
                            }
                        }

                        //�����չ��setattrָ��
                        for (Attribute attribute : attributes) {
                            if (attribute != null) {
                                String name = attribute.getName();
                                if (setattr.equals(name)) {
                                    String val = attribute.getValue();
                                    String[] arr = val.split(",");
                                    String attrName = arr[0].trim();
                                    String expression = arr[1].trim();

                                    //������ָ��ֱ���滻Ϊ��̬���Ը�ֵ
                                    Attribute oriattr = attributes.get(attrName);
                                    String buf = String.format("%s=\"%s\"", attrName, expression);
                                    document.replace(new Segment(source, attribute.getBegin(), attribute.getEnd()), buf);

                                    //������Ѿ����ڵľ�̬���ԣ�ֱ��ɾȥ����
                                    if (oriattr != null) {
                                        document.remove(new Segment(source, attribute.getBegin(), attribute.getEnd())); // �Ƴ�setattr��������
                                    }
                                }
                            }
                        }
                    }

                    replaceChildren(source, element, document); // �ݹ鴦���ӱ�ǩ
                    // ---- ����ָ��� ----
                    if (directiveNames.size() > 0) {
                        StringBuffer buf = new StringBuffer();
                        for (int i = directiveNames.size() - 1; i >= 0; i--) { // ������ӽ���ָ��
                            String directiveName = (String) directiveNames.get(i);
                            if (isBlockDirective(directiveName)) {
                                buf.append("#");
                                buf.append(endDirective[0]);
                                buf.append("(");
                                buf.append(directiveName);
                                buf.append(")");
                            }
                        }
                        document.insert(element.getEnd(), buf.toString()); // �������ָ��
                    }
                    // ������ʱ����
                    directiveNames.clear();
                    directiveValues.clear();
                    directiveAttributes.clear();
                }
            }
        }
    }

}