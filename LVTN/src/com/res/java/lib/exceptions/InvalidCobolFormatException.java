package com.res.java.lib.exceptions;
/*****************************************************************************
Copyright 2009 Venkat Krishnamurthy
This file is part of RES.

RES is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RES is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RES.  If not, see <http://www.gnu.org/licenses/>.

@author VenkatK mailto: open.cobol.to.java at gmail.com
******************************************************************************/

@SuppressWarnings("serial")
public class InvalidCobolFormatException extends RuntimeException {

	private String message=null;
	
	public InvalidCobolFormatException(String msg) {
		this.message=msg;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
