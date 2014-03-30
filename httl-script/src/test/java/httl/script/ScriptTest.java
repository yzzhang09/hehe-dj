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
package httl.script;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScriptTest {

	@Test
	public void testScript() throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		manager.put("welcome", "hello"); // ����ȫ�ֱ���
		ScriptEngine engine = manager.getEngineByName("httl");
		engine.put("page", "test"); // �����������
		Bindings bindings = engine.createBindings();
		bindings.put("user", "liangfei"); // ����ִ�б���
		String result = (String) engine.eval("#set(String welcome, String page, String user)${welcome}, ${user}, this is ${page} page."); // ִ�б��ʽ
		assertEquals("hello, liangfei, this is test page.", result);
	}

}