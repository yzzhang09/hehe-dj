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
import httl.util.DfaScanner;
import httl.util.Token;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * VelocitySyntaxFilter. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.translators.CompiledTranslator#setTemplateFilter(Filter)
 * @see httl.spi.translators.InterpretedTranslator#setTemplateFilter(Filter)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class VelocitySyntaxFilter extends AbstractFilter  {

	// ����ĸ����, ��֤״̬��ͼ���

	// END������Ƭ�Σ�������ǰ�ַ�
	private static final int E = DfaScanner.BREAK;

	// BREAK������Ƭ�Σ����˻�һ���ַ�������������ǰ�ַ�
	private static final int B = DfaScanner.BREAK - 1;

	// BACKSPACE������Ƭ�Σ��˻ص�ǰ�ַ����Լ�֮ǰ�����пհ�
	private static final int S = DfaScanner.BACKSPACE - 1;

	// PUSH��ѹջ1����ָ��С����ջ�����ص�״̬4����ָ�����
	private static final int P = DfaScanner.PUSH - 4;

	// POP����ջ1����ָ��С����ջ�����ص�״̬4����ָ�����
	private static final int O = DfaScanner.POP - 4;

	// PUSH��ѹջ2������ֵ������ջ�����ص�״̬7������ֵ����
	private static final int P2 = DfaScanner.PUSH - 7;

	// POP����ջ2������ֵ������ջ�����ص�״̬7������ֵ����
	private static final int O2 = DfaScanner.POP - 7;

	// PUSH��ѹջ3������ֵС����ջ�����ص�״̬28������ֵ����
	private static final int P3 = DfaScanner.PUSH - 28;

	// POP����ջ3������ֵС����ջ�����ص�״̬28������ֵ������ջ�ջص�״̬29�����������
	private static final int O3 = DfaScanner.POP - 28 - DfaScanner.EMPTY * 29;

	// ��ֵ�﷨״̬��ͼ
	// �б�ʾ״̬
	// ���н����ʾ, �ڸ�״̬ʱ, ����ĳ���͵��ַ�ʱ, �л�������һ״̬(�����к�)
	// E/B/T��ʾ����ǰ�澭�����ַ�Ϊһ��Ƭ��, R��ʾ����״̬(��Щ״̬��Ϊ����)
	static final int states[][] = {
				  // 0.\s, 1.a-z, 2.#, 3.$, 4.!, 5.*, 6.(, 7.), 8.[, 9.], 10.{, 11.}, 12.", 13.', 14.`, 15.\, 16.\r\n, 17.., 18.����
		/* 0.��ʼ */ { 1, 1, 2, 5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, }, // ��ʼ״̬����һƬ�ϸս������״̬
		/* 1.�ı� */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 8, 1, 1, 1, }, // ��ָ���ı�����
		
		/* 2.ָ�� */ { 1, 3, 9, B, 6, 10, 1, 1, 12, 1, 26, 1, 1, 1, 1, 1, 1, 1, 1, }, // ָ����ʾ��
		/* 3.ָ�� */ { 30, 3, B, B, B, B, P, B, B, B, B, B, B, B, B, B, B, B, B, }, // ָ����
		/* 4.ָ�� */ { 4, 4, 4, 4, 4, 4, P, O, 4, 4, 4, 4, 14, 16, 18, 4, 4, 4, 4, }, // ָ�����
		
		/* 5.��ֵ */ { 1, 27, B, B, 6, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, 1, }, // ��ֵ��ʾ��
		/* 6.���� */ { 1, 1, B, B, 1, 1, 1, 1, 1, 1, P2, 1, 1, 1, 1, 1, 1, 1, 1, }, // �ǹ��˲�ֵ
		/* 7.��� */ { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, P2, O2, 20, 22, 24, 7, 7, 7, 7, }, // ��ֵ����
		
		/* 8.ת�� */ { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }, // ������Ԫ��ת��
		/* 9.��ע */ { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, B, 9, 9, }, // ˫������ע��
		/* 10.��ע */ { 10, 10, 10, 10, 10, 11, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // ���Ǻſ�ע��
		/* 11.��� */ { 10, 10, E, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, }, // ���Ǻſ�ע�ͽ���
		/* 12.���� */ { 12, 12, 12, 12, 12, 12, 12, 12, 12, 13, 12, 12, 12, 12, 12, 12, 12, 12, 12, }, // �����ſ����治������
		/* 13.���� */ { 12, 12, E, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, }, // �����ſ����治���������
		
		/* 14.�ִ� */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 4, 14, 14, 15, 14, 14, 14, }, // ָ�����˫�����ַ���
		/* 15.ת�� */ { 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, }, // ָ�����˫�����ַ���ת��
		/* 16.�ִ� */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 4, 16, 17, 16, 16, 16, }, // ָ������������ַ���
		/* 17.ת�� */ { 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, }, // ָ������������ַ���ת��
		/* 18.�ִ� */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 4, 19, 18, 18, 18, }, // ָ��������������ַ���
		/* 19.ת�� */ { 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, }, // ָ��������������ַ���ת��
		
		/* 20.�ִ� */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 7, 20, 20, 21, 20, 20, 20, }, // ��ֵ����˫�����ַ���
		/* 21.ת�� */ { 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, }, // ��ֵ����˫�����ַ���ת��
		/* 22.�ִ� */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 7, 22, 23, 22, 22, 22, }, // ��ֵ�����������ַ���
		/* 23.ת�� */ { 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, }, // ��ֵ�����������ַ���ת��
		/* 24.�ִ� */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 7, 25, 24, 24, 24, }, // ��ֵ�������������ַ���
		/* 25.ת�� */ { 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, }, // ��ֵ�������������ַ���ת��
		
		/* 26.ָ�� */ { B, 26, B, B, B, B, P, B, B, B, B, E, B, B, B, B, B, B, B, }, // ��������ָ�������磺#{else}
		/* 27.��� */ { B, 27, B, B, B, B, P3, B, B, B, B, B, B, B, B, B, B, 27, B, }, // �޴����Ų�ֵ�������磺$aaa.bbb
		/* 28.���� */ { 28, 28, 28, 28, 28, 28, P3, O3, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, }, // С���Ų�ֵ�������磺$aaa.bbb((1 + 2) * (3 + 4))
		/* 29.���� */ { B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, B, 28, B, }, // ������Ӳ�ֵ�������磺$aaa(arg1, arg2).bbb(arg1, arg2)
		
		/* 30.ָ��հ� */ { 30, S, S, S, S, S, P, S, S, S, S, S, S, S, S, S, S, S, S, }, // ָ���������ż�Ŀհ�
	};

	static int getCharType(char ch) {
		switch (ch) {
			case ' ': case '\t': case '\f': case '\b':
				return 0;
			case '_' :
			case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : 
			case 'h' : case 'i' : case 'j' : case 'k' : case 'l' : case 'm' : case 'n' : 
			case 'o' : case 'p' : case 'q' : case 'r' : case 's' : case 't' : 
			case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
			case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : 
			case 'H' : case 'I' : case 'J' : case 'K' : case 'L' : case 'M' : case 'N' : 
			case 'O' : case 'P' : case 'Q' : case 'R' : case 'S' : case 'T' : 
			case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
				return 1;
			case '#' : 
				return 2;
			case '$' : 
				return 3;
			case '!' : 
				return 4;
			case '*' : 
				return 5;
			case '(' : 
				return 6;
			case ')' : 
				return 7;
			case '[' : 
				return 8;
			case ']' : 
				return 9;
			case '{' : 
				return 10;
			case '}' : 
				return 11;
			case '\"' : 
				return 12;
			case '\'' : 
				return 13;
			case '`' : 
				return 14;
			case '\\' : 
				return 15;
			case '\r': case '\n':
				return 16;
			case '.':
				return 17;
			default:
				return 18;
		}
	}

	private static DfaScanner scanner = new DfaScanner() {
		@Override
		public int next(int state, char ch) {
			return states[state][getCharType(ch)];
		}
	};

	private static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{?(\\w+)\\}?");
	
	private final AtomicInteger seq = new AtomicInteger();

	public String filter(String key, String value) {
		try {
			StringBuilder buf = new StringBuilder();
			List<Token> tokens = scanner.scan(value, 0);
			for (Token token : tokens) {
				String message = token.getMessage();
				if (message.length() > 1) {
					if (message.charAt(0) == '#') { // ָ��
						boolean isDirective = false;
						if (message.charAt(1) >= 'a' && message.charAt(1) <= 'z') {
							// �� #xxx($item) ת�� #xxx(item)
							message = REFERENCE_PATTERN.matcher(message).replaceAll("$1");
							isDirective = true;
						} else if (message.length() > 2 
								&& message.charAt(1) == '{' && message.charAt(message.length() - 1) == '}'
								&& message.charAt(2) >= 'a' && message.charAt(2) <= 'z') {
							// �� #{else} ת�� #else()
							message = "#" + message.substring(2, message.length() - 1) + "()";
							isDirective = true;
						} else if (message.startsWith("#[[") && message.endsWith("]]#")) {
							// �� #[[ ... ]]# ת�� #[ ... ]#
							message = "#[" + message.substring(3, message.length() - 3) + "]#";
						}
						if (isDirective) {
							int i = message.indexOf('(');
							String name = i < 0 ? message.substring(1) : message.substring(1, i);
							String expression = i < 0 ? "" : message.substring(i + 1, message.length() - 1).trim();
							if ("include".equals(name)) {
								// �� #include("foo.txt") ת�� ${read("foo.txt")}
								message = "${read(" + expression + ")}";
							} else if ("parse".equals(name)) {
								// �� #parse("foo.httl") ת�� ${include("foo.httl")}
								message = "${include(" + expression + ")}";
							} else if ("evaluate".equals(name)) {
								// �� #evaluate("${name}") ת�� ${render("${name}")}
								message = "${render(" + expression + ")}";
							} else if ("foreach".equals(name)) {
								// ��#foreach(item in list) ת�� #for(item : list)
								message = "#for(" + expression + ")#set(int velocityCount = for.count)";
							} else if ("macro".equals(name)) {
								String[] args = expression.split("\\s+");
								StringBuilder sb = new StringBuilder();
								if (args.length > 1) {
									for (String arg : args) {
										if (sb.length() > 0) {
											sb.append(",");
										}
										sb.append("Object ");
										sb.append(arg);
									}
									// �� #macro(name arg1 arg2) ת�� #macro(name(arg1, arg2))
									message = "#macro(" + args[0] + (sb.length() == 0 ? "" : "(" + sb + ")") + ")";
								}
							} else if ("define".equals(name)) {
								// �� #define(name) ת�� #macro(name := _macro_name)
								message = "#macro(" + expression + " := _macro_" + expression + ")";
							} else if ("stop".equals(name)) {
								// �� #stop ת�� #break
								message = "#break";
							} else if (name.startsWith("@")) {
								// �� #@blockmacroname ת�� ${macroname(arg1, arg2)}
								name = name.substring(1);
								String tmp = name + "_" + seq.incrementAndGet();
								message = "#set(String bodyContent = " + tmp + ".evaluate())${" + name + "(" + expression + ")}#macro(" + tmp + ")";
							} else if (! "set".equals(name)
									&& ! "if".equals(name)
									&& ! "elseif".equals(name)
									&& ! "else".equals(name)
									&& ! "break".equals(name)
									&& ! "end".equals(name)) {
								// �� #macroname(arg1, arg2) ת�� ${macroname(arg1, arg2)}
								message = "${" + name + "(" + expression + ")}";
							}
						}
					} else if (message.charAt(0) == '$') { // ��ֵ
						if (message.charAt(1) >= 'a' && message.charAt(1) <= 'z'
								|| message.charAt(1) >= 'A' && message.charAt(1) <= 'Z') {
							// �� $user.name ת�� ${user.name}
							message = message.substring(1);
						} else if (message.length() > 2 && message.charAt(1) == '!'
								&& (message.charAt(2) >= 'a' && message.charAt(2) <= 'z'
								|| message.charAt(2) >= 'A' && message.charAt(2) <= 'Z')) {
							// �� $!user.name ת�� ${user.name}
							message = message.substring(2);
						} else if (message.length() > 3 && message.charAt(0) == '$' && message.charAt(1) == '!'
								&& message.charAt(2) == '{' && message.charAt(message.length() - 1) == '}') {
							// �� ${user.name} ת�� ${user.name}
							message = message.substring(2, message.length() - 1);
						} else if (message.length() > 4 && message.charAt(0) == '$' && message.charAt(1) == '!'
								&& message.charAt(2) == '{' && message.charAt(message.length() - 1) == '}') {
							// �� $!{user.name} ת�� ${user.name}
							message = message.substring(3, message.length() - 1);
						}
						message = "${" + REFERENCE_PATTERN.matcher(message).replaceAll("$1") + "}";
					}
				}
				buf.append(message);
			}
			return buf.toString();
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}