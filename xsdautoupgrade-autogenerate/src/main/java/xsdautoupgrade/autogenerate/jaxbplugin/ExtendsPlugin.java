package xsdautoupgrade.autogenerate.jaxbplugin;
/** 
* jaxb-inherit - (C) 2013 - J.W. Janssen &lt;j.w.janssen@lxtreme.nl&gt;. 
*   
* Licensed under the Apache-2.0 license.
* Modified to only extends by Gabriel Dimitriu 2017 
*/ 
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
 
import java.util.Arrays;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JClass; 
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.ElementOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline; 
 
 
/** 
 * Provides a simple plugin for XJC that allows you to inherit JAXB generated classes from 
 * a class. 
 */ 
public class ExtendsPlugin extends Plugin { 
 
 
     public static final String NS = "gdimitriu.utils/extends"; 
 
 
     public static final String EXTENDS = "extends"; 
     public static final String NAME = "name"; 
 

     @Override 
     public List<String> getCustomizationURIs() { 
         return Arrays.asList(NS); 
     } 
 
 
     @Override 
     public String getOptionName() { 
         return "Xextends"; 
     } 
 
 
     @Override 
     public String getUsage() { 
         return "  -Xextends          : enable custom extends from a base class or interface."; 
     } 
 

     @Override 
     public boolean isCustomizationTagName(String nsUri, String localName) { 
         return NS.equals(nsUri) && (EXTENDS.equals(localName)); 
     } 
 
 
     @Override 
     public boolean run(Outline outline, Options options, ErrorHandler handler) throws SAXException { 
         for (ClassOutline classOutline : outline.getClasses()) { 
             processClassOutline(classOutline); 
         } 
         for (EnumOutline enumOutline : outline.getEnums()) { 
             processEnumOutline(enumOutline); 
         } 
         for (CElementInfo elementInfo : outline.getModel().getAllElements()) { 
             ElementOutline elementOutline = outline.getElement(elementInfo); 
             if (elementOutline != null) { 
                 processElementOutline(elementOutline); 
             } 
         } 
         return true; 
     } 
 
 
     private CPluginCustomization findExtendsCustomization(CCustomizations customizations) { 
         CPluginCustomization result = customizations.find(NS, EXTENDS); 
         if (result != null && !isNullOrEmpty(result.element.getAttribute(NAME))) { 
             result.markAsAcknowledged(); 
         } 
         return result; 
     } 
 
     private void generateExtends(JDefinedClass implClass, CPluginCustomization customization) { 
        if (customization == null) { 
             return; 
         } 
 
 
         String className = customization.element.getAttribute(NAME); 
         if (className != null) { 
             JClass iface = implClass.owner().ref(className); 
 
 
             implClass._extends(iface); 
         } 
     } 
 
     private boolean isNullOrEmpty(String value) { 
         return value == null || "".equals(value.trim()); 
     } 
 
 
     private void processClassOutline(ClassOutline classOutline) { 
         JDefinedClass implClass = classOutline.implClass; 
         CCustomizations customizations = classOutline.target.getCustomizations(); 
 
 
         generateExtends(implClass, findExtendsCustomization(customizations)); 
     } 
 

     private void processElementOutline(ElementOutline elementOutline) { 
         JDefinedClass implClass = elementOutline.implClass; 
         CCustomizations customizations = elementOutline.target.getCustomizations(); 
 
 
         generateExtends(implClass, findExtendsCustomization(customizations)); 
     } 
 
 
     private void processEnumOutline(EnumOutline enumOutline) { 
         JDefinedClass implClass = enumOutline.clazz; 
         CCustomizations customizations = enumOutline.target.getCustomizations(); 
 
 
         generateExtends(implClass, findExtendsCustomization(customizations)); 
     } 
 } 
