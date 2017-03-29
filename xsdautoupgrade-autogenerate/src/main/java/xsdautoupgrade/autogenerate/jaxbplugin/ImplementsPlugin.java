package xsdautoupgrade.autogenerate.jaxbplugin;
/** 
* jaxb-inherit - (C) 2013 - J.W. Janssen &lt;j.w.janssen@lxtreme.nl&gt;. 
*   
* Licensed under the Apache-2.0 license. 
* Modified to have only implements and option for serializable by Gabriel Dimitriu 2017
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
 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.List; 

 
import org.w3c.dom.Element; 
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
 * an interface. 
 */ 
public class ImplementsPlugin extends Plugin { 
 
 
     public static final String NS = "gdimitriu.utils/implements"; 
 
 
     public static final String IMPLEMENTS = "implements"; 
     public static final String NAME = "name"; 
 

     @Override 
     public List<String> getCustomizationURIs() { 
         return Arrays.asList(NS); 
     } 
 
 
     @Override 
     public String getOptionName() { 
         return "Ximplements"; 
     } 
 
 
     @Override 
     public String getUsage() { 
         return "  -Ximplements          : enable custom inheritance from a base class or interface."; 
     } 
 

     @Override 
     public boolean isCustomizationTagName(String nsUri, String localName) { 
         return NS.equals(nsUri) && IMPLEMENTS.equals(localName); 
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
 
     private CPluginCustomization[] findImplementsCustomization(CCustomizations customizations, ClassOutline classOutline) { 
         List<CPluginCustomization> results = new ArrayList<CPluginCustomization>(); 
         for (CPluginCustomization candidate : customizations) { 
             Element element = candidate.element; 
             if (NS.equals(element.getNamespaceURI()) && IMPLEMENTS.equals(element.getLocalName()) 
                 && !isNullOrEmpty(element.getAttribute(NAME))) {
            	 candidate.markAsAcknowledged();
            	 //put the serial version
            	 if(classOutline!=null){
            		 if(element.getAttribute(NAME).equals("java.io.Serializable"))
            		 {
            			 JDefinedClass implClass = classOutline.implClass;
            			 if(!implClass.isInterface()) {
            				 classOutline.implClass.direct("private static final long serialVersionUID = -8995864401639215257L;");
            			 }
            		 }
            	 }
                 results.add(candidate); 
             } 
         } 
         return results.toArray(new CPluginCustomization[results.size()]); 
     } 
 
     private void generateImplements(JDefinedClass implClass, CPluginCustomization... customizations) { 
         if (customizations == null || customizations.length < 1) { 
             return; 
         } 
 
 
         for (CPluginCustomization customization : customizations) { 
             String className = customization.element.getAttribute(NAME); 
             if (className != null) { 
                 JClass iface = implClass.owner().ref(className); 
 
 
                 if (implClass.isInterface()) { 
                     implClass._extends(iface); 
                 } else { 
                     implClass._implements(iface); 
                 } 
             } 
         } 
     } 
 
 
     private boolean isNullOrEmpty(String value) { 
         return value == null || "".equals(value.trim()); 
     } 
 
 
     private void processClassOutline(ClassOutline classOutline) { 
         JDefinedClass implClass = classOutline.implClass; 
         CCustomizations customizations = classOutline.target.getCustomizations(); 
 
 
         generateImplements(implClass, findImplementsCustomization(customizations,classOutline)); 
     } 
 

     private void processElementOutline(ElementOutline elementOutline) { 
         JDefinedClass implClass = elementOutline.implClass; 
         CCustomizations customizations = elementOutline.target.getCustomizations(); 
 
 
         generateImplements(implClass, findImplementsCustomization(customizations,null)); 
     } 
 
 
     private void processEnumOutline(EnumOutline enumOutline) { 
         JDefinedClass implClass = enumOutline.clazz; 
         CCustomizations customizations = enumOutline.target.getCustomizations(); 
 
 
         generateImplements(implClass, findImplementsCustomization(customizations,null)); 
     } 
 } 
