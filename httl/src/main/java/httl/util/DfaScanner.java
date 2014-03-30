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
package httl.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public abstract class DfaScanner {

	// BREAK������Ƭ�Σ����ص���ʼ״̬�����ɻ���50���ַ����˻ص��ַ������¶�ȡ
	// state = BREAK - �˻��ַ���
	// state = BREAK - 1 // �������˻�1���ַ�������������ǰ�ַ�
	public static final int BREAK = -1;
	
	// BACKSPACE������Ƭ�Σ����ص���ʼ״̬���������пհ׷����˻ص��ַ������¶�ȡ
	// state = BACKSPACE - �������пհ׷�
	// state = BACKSPACE - 1 // �������˻�1���ַ���������֮ǰ���пհ׷�
	public static final int BACKSPACE = -50;

	// PUSH��ѹջ�����ص�ָ��״̬�����900��״̬
	// state = PUSH - ѹջ��ص�״̬��
	// state = PUSH - 4 // ѹ���2��ջ��ѹջ��ص�״̬4
	public static final int PUSH = -100;

	// POP����ջ�����ص�ָ��״̬��ջ�ջص���ʼ״̬0����ʾ����Ƭ��
	// state = POP - ��ջ��ص�״̬�� - EMPTY * ջ�ջص�״̬��
	// state = POP - 4 - EMPTY * 5 // ��ջ��ص�״̬4��ջ�ջص�״̬5
	public static final int POP = -1000000;
	public static final int EMPTY = -POP;

	// ERROR�����������׳��쳣
	// state = ERROR - ������
	// state = ERROR - 1 // ���������ش�����Ϊ1���쳣��Ϣ��
	public static final int ERROR = -100000000;
	
	public List<Token> scan(String charStream, int offset) throws ParseException {
		return scan(charStream, offset, false);
	}

	public List<Token> scan(String charStream, int offset, boolean errorWithSource) throws ParseException {
		List<Token> tokens = new ArrayList<Token>();
		// ����ʱ״̬ ----
		StringBuilder buffer = new StringBuilder(); // �����ַ�
		StringBuilder remain = new StringBuilder(); // �д��ַ�
		int pre = 0; // ��һ״̬
		int state = 0; // ��ǰ״̬
		char ch; // ��ǰ�ַ�

		// ���ֽ��� ----
		int i = 0;
		int p = 0;
		for(;;) {
			if (remain.length() > 0) { // �ȴ���д��ַ�
				ch = remain.charAt(0);
				remain.deleteCharAt(0);
			} else { // û�вд��ַ����ȡ�ַ���
				if (i >= charStream.length()) {
					break;
				}
				ch = charStream.charAt(i ++);
				offset ++;
			}

			buffer.append(ch); // ���ַ����뻺��
			state = next(state, ch); // ��״̬��ͼ��ȡ��һ״̬
			if (state <= ERROR) {
				throw new ParseException("DFAScanner.state.error, error code: " + (ERROR - state) + (errorWithSource ? ", source: " + charStream : ""), offset - buffer.length());
			}
			if (state <= POP) {
				int n = - (state % POP);
				int e = (state - n) / POP - 1;
				if (p <= 0) {
					throw new ParseException("DFAScanner.mismatch.stack" + (errorWithSource ? ", source: " + charStream : ""), offset - buffer.length());
				}
				p --;
				if (p == 0) {
					state = e;
					if (state == 0) {
						state = BREAK;
					}
				} else {
					state = n;
					continue;
				}
			} else if (state <= PUSH) {
				p ++;
				state = PUSH - state;
				continue;
			}
			if (state <= BREAK) { // ������ʾ����״̬
				int acceptLength;
				if (state <= BACKSPACE) {
					acceptLength = buffer.length() + state - BACKSPACE;
					if (acceptLength > 0) {
						int space = 0;
						for (int s = acceptLength - 1; s >= 0; s --) {
							if (Character.isSpaceChar(buffer.charAt(s))) {
								space ++;
							} else {
								break;
							}
						}
						acceptLength = acceptLength - space;
					}
				} else {
					acceptLength = buffer.length() + state - BREAK;
				}
				if (acceptLength < 0 || acceptLength > buffer.length())
					throw new ParseException("DFAScanner.accepter.error" + (errorWithSource ? ", source: " + charStream : ""), offset - buffer.length());
				if (acceptLength != 0) {
					String message = buffer.substring(0, acceptLength);
					Token token = new Token(message, offset - buffer.length(), pre);
					tokens.add(token);// ��ɽ���
				}
				if (acceptLength != buffer.length())
					remain.insert(0, buffer.substring(acceptLength)); // ��δ���յĻ������д�
				buffer.setLength(0); // ��ջ���
				state = 0; // �ع鵽��ʼ״̬
			}
			pre = state;
		}
		// ������󻺴��е�����
		if (buffer.length() > 0) {
			String message = buffer.toString();
			tokens.add(new Token(message, offset - message.length(), pre));
		}
		return tokens;
	}

	public abstract int next(int state, char ch);

}