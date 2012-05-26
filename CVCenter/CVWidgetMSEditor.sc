/* (c) 2010-2012 Stefan Nussbaumer */
/* 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/
CVWidgetMSEditor {
	
	classvar <allMSEditors;
	var thisMSEditor, <window, <tabs, msEditorEnv, labelStringColors;
	var <specField, <specsList, <specsListSpecs;
	var actionName, enterAction, enterActionBut, <actionsList;
	var name;
	var flow0, flow1, flow2, flow3;

	*new { |widget, widgetName, tab|
		^super.new.init(widget, widgetName, tab);
	}
	
	init { |widget, widgetName, tab|
		
	}
	
	front { |tab|
		thisMSEditor.window.front;
		tab !? { 
			thisMSEditor.tabs.stringFocusedColor_(labelStringColors[tab]);
			thisMSEditor.tabs.focus(tab);
		}
	}
	
	close { |slot|
		thisMSEditor.window.close;
		allMSEditors.removeAt(name);
	}
	
	isClosed { 
		var ret;
		thisMSEditor.window !? {
			ret = defer { thisMSEditor.window.isClosed };
			^ret.value;
		}
	}
	
	
}