/*
 * FindBugs - Find bugs in Java programs
 * Copyright (C) 2005 Dave Brosius <dbrosius@users.sourceforge.net>
 * Copyright (C) 2005 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.findbugs.validatortask;

import java.io.File;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SchemaValidatorTask extends Task
{
	public static final int NONE = 0;
	public static final int WARN = 1;
	public static final int ERROR = 2;
	public static final int FATAL = 3;

	private String xmlPath = null;
	private boolean failOnError = false;
	private SAXParseException ex = null;
	private int exceptionLevel = NONE;
	
	public void setXml(String xml)
	{
		xmlPath = xml;
	}
	
	public void setFailonerror(boolean fail)
	{
		failOnError = fail;
	}

	public void execute() throws BuildException
	{
		int exLineNo = 0;
		
		try {
			System.out.println("Validating: " + xmlPath);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(true);
			spf.setNamespaceAware(true);
			SAXParser parser = spf.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setFeature("http://apache.org/xml/features/validation/schema", true); 
			reader.setContentHandler(new DefaultHandler());
			reader.setErrorHandler( new ErrorHandler() {							
				public void error(SAXParseException exception) {
					if (exceptionLevel < ERROR) {
						ex = exception;
						exceptionLevel = ERROR;
					}
				}
				
				public void fatalError(SAXParseException exception) {
					if (exceptionLevel < FATAL) {
						ex = exception;
						exceptionLevel = FATAL;
					}
				}
				
				public void warning(SAXParseException exception) {
					if (exceptionLevel < WARN) {
						ex = exception;
						exceptionLevel = WARN;
					}
				}
			});
			reader.parse(new InputSource( xmlPath ));
			if (ex != null) {
				exLineNo = ex.getLineNumber();
				throw ex;
			}
		}
		catch (Exception e) {
			if (failOnError) {
				String sep = System.getProperty("line.separator");
				String msg = sep + e.getMessage() + sep + "on line " + exLineNo;
				BuildException be = new BuildException(msg);
				be.setStackTrace( e.getStackTrace());

				throw be;
			}
			else {
				e.printStackTrace();
			}
		}
		
	}
	
}