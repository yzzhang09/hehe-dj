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
package httl.spi.loaders.resources;

import httl.Engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipFile;

/**
 * ZipResource. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.spi.loaders.ZipLoader#load(String, Locale, String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class ZipResource extends InputStreamResource {

	private static final long serialVersionUID = 1L;

	private final File file;

	public ZipResource(Engine engine, String name, Locale locale, String encoding, File file) {
		super(engine, name, locale, encoding);
		this.file = file;
	}

	public InputStream openStream() throws IOException {
		// ע��ZipFile��File������ǲ�һ���ģ�File�൱��C#��FileInfo��ֻ������Ϣ��
		// ��ZipFile����ʱ������������ÿ�ζ�ȡ����ʱ������new�µ�ʵ����������Ϊ�����ֶγ��С�
		ZipFile zipFile = new ZipFile(file);
		return zipFile.getInputStream(zipFile.getEntry(getName()));
	}

	public long getLastModified() {
		return file.lastModified();
	}

	public long getLength() {
		try {
			ZipFile zipFile = new ZipFile(file);
			try {
				return zipFile.getEntry(getName()).getSize();
			} finally {
				zipFile.close();
			}
		} catch (IOException e) {
			return super.getLength();
		}
	}

}