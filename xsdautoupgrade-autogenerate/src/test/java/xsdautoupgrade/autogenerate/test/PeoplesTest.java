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
package xsdautoupgrade.autogenerate.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import xsdautoupgrade.autogenerate.jaxbutils.CreateJarFromSchema;

/**
 * JUnit test for peoples update.
 * @author Gabriel Dimitriu
 *
 */
public class PeoplesTest {

	@Test
	public void testUnmarshallPeoplesNormal() {
		testUnmarshallPeople();
	}
	
	@Test
	public void testUnmarshallPeopleThread() {
		Thread thread = new Thread() {
			@Override 
			public void run() {
				testUnmarshallPeople();
			}
		};
		thread.run();
		try {
			thread.join();
		} catch (InterruptedException e) {
			fail("uninterupted Exception at join");
		}
	}

	private void testUnmarshallPeople() {
		//setdebug mode
		CreateJarFromSchema.setDebugMode(false);
		//generate the package
		CreateJarFromSchema creator = new CreateJarFromSchema(this.getClass().getClassLoader().getResourceAsStream("peoples/peoples.xsd"),
				this.getClass().getClassLoader().getResourceAsStream("peoples/bindings.xml"),
				"auto_generated");
		creator.createJar("peoples.jar");
		assertTrue("jar has not been created", creator.getJarFullName()!=null);
		try {			
			File jar = new File(creator.getJarFullName());
			URLClassLoader urlClassLoader
			 = new URLClassLoader(new URL[]{ jar.toURI().toURL()},
					 ClassLoader.getSystemClassLoader());
			JAXBContext jxbContext = JAXBContext.newInstance("auto_generated",urlClassLoader);
			Unmarshaller unMarshaller = jxbContext.createUnmarshaller();			
			Object people = unMarshaller.unmarshal(this.getClass().getClassLoader().getResourceAsStream("peoples/peoples.xml"));
			assertTrue("unmarshall is not null", people!=null);
			Field[] flds = people.getClass().getDeclaredFields();
			for(int i = 0; i < flds.length; i++) {
				flds[i].setAccessible(true);
				Object value =flds[i].get(people);
				assertTrue("field" + flds[i].getName() + " is null", value!=null);
			}
		} catch (JAXBException | MalformedURLException | IllegalArgumentException | IllegalAccessException e1) {
			fail("exception was cought");
		}
	}

}
