/**
Copyright (c) 2017 Gabriel Dimitriu All rights reserved.
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of xsdautoupgrade project.

xsdautoupgrade is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

xsdautoupgrade is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with xsdautoupgrade.  If not, see <http://www.gnu.org/licenses/>.
*/
package xsdautoupgrade.autogenerate.jaxbutils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.tools.xjc.Driver;

/**
 * Create the jar from the schema.
 * 
 * @author Gabriel Dimitriu
 *
 */
public class CreateJarFromSchema {
	private static final int BUFFER_SIZE = 8192;
	private String schema;
	private String rootDirectory;
	private String packageName;
	private String[] javaFilesList;
	private File sourceDirectory = null;
	private File outputDir = null;
	private String bindings;
	private String jarFullName;
	private static boolean debugMode = true;

	/**
	 * Create the jar from a xsd schema.
	 * 
	 * @param schema
	 *            the schema location path is absolute
	 * @param bindings
	 *            the binding operation path
	 * @param packageName
	 *            the package name which will be created.
	 */
	public CreateJarFromSchema(final String schema, final String bindings,
			final String packageName) {
		super();
		this.schema = schema;
		this.bindings = bindings;
		this.rootDirectory = System.getProperty("java.io.tmpdir") + "/jaxb";
		this.packageName = packageName;
	}
	
	public CreateJarFromSchema(final InputStream schema, final InputStream bindings,
			final String packageName) {
		super();
		this.rootDirectory = System.getProperty("java.io.tmpdir") + "/jaxb";
		this.packageName = packageName;
		this.schema = this.rootDirectory + "/schema.xsd";
		this.bindings = this.rootDirectory + "/bindings.xml";
		File dir = new File(this.rootDirectory);
		dir.mkdirs();
		writeToFileAndInit(schema, this.schema);
		writeToFileAndInit(bindings, this.bindings);
	}

	private void writeToFileAndInit(final InputStream schema, final String pathName) {
		try (FileOutputStream bos = new FileOutputStream(pathName)){
			byte[] buf = new byte[BUFFER_SIZE];
	        int n;
	        while ((n = schema.read(buf)) > 0) {
	            bos.write(buf, 0, n);
	        }
		} catch (IOException e) {
			this.schema = null;
			this.bindings = null;
			e.printStackTrace();
		}
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public String getJarFullName() {
		return jarFullName;
	}

	public static void setDebugMode(final boolean debug) {
		debugMode = debug;
	}

	public static boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * generate sources from xsd and then compile it.
	 * 
	 * @return true is the compilation was correct.
	 */
	public boolean generateAndCompile() {
		if (generate()) {
			return compile();
		}
		return false;
	}

	/**
	 * Create the jar from the compiled auto-generated java classes.
	 * 
	 * @param jarName
	 *            the name of the jar.
	 * @return true if the jar has been created.
	 */
	public boolean createJar(final String jarName) {
		BufferedInputStream in = null;
		FileInputStream fis = null;
		jarFullName = rootDirectory + "/" + jarName;
		FileOutputStream os = null;
		JarOutputStream jos = null;
		if (generateAndCompile()) {
			try {
				// write the manifest file
				Manifest manifest = new Manifest();
				Attributes global = manifest.getMainAttributes();
				global.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
				global.put(new Attributes.Name("Created-by"),
						"Gabriel Dimitriu");

				// create the jar file
				os = new FileOutputStream(jarFullName);
				jos = new JarOutputStream(os, manifest);

				File output = new File(outputDir.getAbsolutePath() + "\\"
						+ packageName);
				File[] classes = output.listFiles();
				for (int i = 0; i < classes.length; i++) {
					JarEntry entry = new JarEntry(packageName + "/"
							+ classes[i].getName());
					entry.setTime(classes[i].lastModified());
					try {
						fis = new FileInputStream(classes[i]);
						// only put here after we had open the file
						jos.putNextEntry(entry);
						in = new BufferedInputStream(fis);
						byte[] buffer = new byte[2048];
						while (true) {
							int count = in.read(buffer);
							if (count == -1) {
								break;
							}
							jos.write(buffer, 0, count);
						}
					} catch (FileNotFoundException e) {
						continue;
					}
					in.close();
					jos.closeEntry();
				}
				jos.flush();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (jos != null) {
					try {
						jos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generate the java classes from schema using jaxb.
	 * 
	 * @return true is success false otherwise
	 */
	private boolean generate() {
		String directory = rootDirectory + "\\src";
		File dir = new File(directory);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		try {
			String[] arguments = {
					"-d",
					directory,
					"-p",
					packageName,
					"-b",
					bindings,
					"-cp",
					this.getClass().getProtectionDomain().getCodeSource()
							.getLocation().toURI().getPath(), "-extension",
					"-Xextends", "-Ximplements", schema };
			// generate the classes from xsd using jxb-ri
			Driver.run(arguments, null, null);
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}

		// for debug purpose
		sourceDirectory = new File(directory + "\\" + packageName);
		javaFilesList = sourceDirectory.list();
		if (javaFilesList == null) {
			return false;
		}
		if (debugMode) {
			if (javaFilesList != null) {
				for (int i = 0; i < javaFilesList.length; i++) {
					System.out.println(javaFilesList[i]);
				}
			}
		}
		return true;
	}

	/**
	 * Compile the generated classes
	 * 
	 * @return true if success and false otherwise
	 */
	private boolean compile() {
		if (javaFilesList == null) {
			return false;
		}
		// compile the auto-generated files
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);

		// prepare the source file(s) to compile
		List<File> sourceFileList = new ArrayList<File>();
		for (int i = 0; i < javaFilesList.length; i++) {
			sourceFileList.add(new File(sourceDirectory.getAbsolutePath()
					+ "\\" + javaFilesList[i]));
		}

		// create the compilation units
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(sourceFileList);
		List<String> options = new ArrayList<String>();
		options.add("-d");
		outputDir = new File(rootDirectory + "\\build\\classes");
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		options.add(outputDir.getAbsolutePath());
		// compile
		CompilationTask task = compiler.getTask(null, fileManager, null,
				options, null, compilationUnits);
		boolean result = task.call();
		if (debugMode) {
			if (result) {
				System.out.println("Compilation was successful");
			} else {
				System.out.println("Compilation failed");
			}
		}
		try {
			fileManager.close();
		} catch (IOException e) {
		}
		return result;
	}

	/**
	 * delete the created files when we receive string or streams.
	 */
	public void clean() {
		new File(this.schema).delete();
		new File(this.bindings).delete();
	}
}
