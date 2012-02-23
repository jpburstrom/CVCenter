/* (c) Stefan Nussbaumer */
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

CVWidgetEditor {
	classvar <allEditors;
	var thisEditor, <window, <tabs, labelStringColors;
	var <specField, <specsList, <specsListSpecs;
	var <midiModeSelect, <midiMeanNB, <softWithinNB, <ctrlButtonBankField, <midiResolutionNB;
	var <midiLearnBut, <midiSrcField, <midiChanField, <midiCtrlField;
	var <calibBut, <calibNumBoxes;
	var <deviceListMenu, <cmdListMenu, <addDeviceBut, thisCmdNames;
	var <ipField, <portField, <nameField, <indexField;
	var <inputConstraintLoField, <inputConstraintHiField, <alwaysPosField;
	var <mappingSelect;
	var <connectorBut;
	var <actionName, <enterAction, <enterActionBut, <actionsList;
	var <name;
	var flow0, flow1, flow2, flow3;
	
	*new { |widget, widgetName, tab, slot|
		^super.new.init(widget, widgetName, tab, slot)
	}

	init { |widget, widgetName, tab, slot|
		var tabs, cvString, slotHiLo;
		var staticTextFont, staticTextColor, textFieldFont, textFieldFontColor, textFieldBg;
		var msrc = "source", mchan = "chan", mctrl = "ctrl", margs;
		var addr, wcmHiLo, thisGuiEnv, labelColors; 
		var midiModes;
		var thisMidiMode, thisMidiMean, thisMidiResolution, thisSoftWithin, thisCtrlButtonBank;
		var mappingSelectItems;
		var wdgtActions;
		var cmdNames, orderedCmds, orderedCmdSlots;
		var tmp; // multipurpose short-term var
						
		name = widgetName.asSymbol;
		
		cmdNames ?? { cmdNames = OSCCommands.deviceCmds };
		thisCmdNames ?? { thisCmdNames = [nil] };
				
		actionsList ?? { actionsList = () };
		
		if(slot.isNil, { thisGuiEnv = widget.guiEnv }, { thisGuiEnv = widget.guiEnv[slot] });

		widget ?? {
			Error("CVWidgetEditor is a utility-GUI-class that should only be used in connection with an existing CVWidget").throw;
		};

		if(slot.notNil, {
			widget.wdgtControllersAndModels[slot] !? { 
				wcmHiLo = widget.wdgtControllersAndModels[slot];
			};
			thisMidiMode = widget.getMidiMode(slot);
			thisMidiMean = widget.getMidiMean(slot);
			thisMidiResolution = widget.getMidiResolution(slot);
			thisSoftWithin = widget.getSoftWithin(slot);
			thisCtrlButtonBank = widget.getCtrlButtonBank(slot);
		}, { 
			widget.wdgtControllersAndModels !? { 
				wcmHiLo = widget.wdgtControllersAndModels;
			};
			thisMidiMode = widget.getMidiMode;
			thisMidiMean = widget.getMidiMean;
			thisMidiResolution = widget.getMidiResolution;
			thisSoftWithin = widget.getSoftWithin;
			thisCtrlButtonBank = widget.getCtrlButtonBank;
		});
						

		staticTextFont = Font(Font.defaultSansFace, 10);
		staticTextColor = Color(0.2, 0.2, 0.2);
		textFieldFont = Font(Font.defaultMonoFace, 9);
		textFieldFontColor = Color.black;
		textFieldBg = Color.white;
		
		if(slot.notNil, {
			slotHiLo = "["++slot.asString++"]";
		}, {
			slotHiLo = "";
		});
		
		allEditors ?? { allEditors = IdentityDictionary() };
		
		if(thisEditor.isNil or:{ thisEditor.window.isClosed }, {
			
			window = Window("Widget Editor:"+widgetName++slotHiLo, Rect(Window.screenBounds.width/2-150, Window.screenBounds.height/2-100, 270, 265));

			if(slot.isNil, { 
				allEditors.put(name, (window: window, name: widgetName)) 
			}, {
				tmp = (); tmp.put(slot, (window: window, name: widgetName));
				if(allEditors[name].isNil, { 
					allEditors.put(name, tmp);
				}, { 
					allEditors[name].put(slot, (window: window, name: widgetName));
				});
			});
			
			if(slot.notNil, { thisEditor = allEditors[name][slot] }, { thisEditor = allEditors[name] });

			if(Quarks.isInstalled("wslib"), { window.background_(Color.white) });
			tabs = TabbedView(window, Rect(0, 1, window.bounds.width, window.bounds.height), ["Specs", "MIDI", "OSC", "Actions"], scroll: true);
			tabs.view.resize_(5);
			tabs.tabCurve_(4);
			tabs.labelColors_(Color.white!4);
			labelColors = [
				Color(1.0, 0.3), //specs
				Color.red, //midi
				Color(0.0, 0.5, 0.5), //osc
				Color(0.32, 0.67, 0.76), //actions
			];
			labelStringColors = labelColors.collect({ |c| Color(c.red * 0.8, c.green * 0.8, c.blue * 0.8) });
			tabs.unfocusedColors_(labelColors);
			tabs.stringColor_(Color.white);
			tabs.views[0].decorator = flow0 = FlowLayout(window.view.bounds, 7@7, 3@3);
			tabs.views[1].decorator = flow1 = FlowLayout(window.view.bounds, 7@7, 3@3);
			tabs.views[2].decorator = flow2 = FlowLayout(window.view.bounds, 7@7, 3@3);
			tabs.views[3].decorator = flow3 = FlowLayout(window.view.bounds, 7@7, 3@3);
			(0..3).do({ |t| tabs.focusActions[t] = { tabs.stringFocusedColor_(labelStringColors[t]) } });
			tabs.stringFocusedColor_(labelStringColors[tab]);

			thisEditor.tabs = tabs;
			
			thisEditor.tabs.view.keyDownAction_({ |view, char, modifiers, unicode, keycode|
				switch(unicode,
					111, { thisEditor.tabs.focus(2) }, // osc
					109, { thisEditor.tabs.focus(1) }, // midi
					97, { thisEditor.tabs.focus(3) }, // actions
					115, { thisEditor.tabs.focus(0) }, // specs
					79, { CVCenterControllersMonitor(1) }, // osc-controllers monitor
					77, { CVCenterControllersMonitor(0) }, // midi-controllers monitor
					120, { this.close(slot) } // close editor
				)
			});
						
			StaticText(thisEditor.tabs.views[0], flow0.bounds.width-20@95)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("Enter a ControlSpec in the textfield:\ne.g. ControlSpec(20, 20000, \\exp, 0.0, 440, \"Hz\")\nor \\freq.asSpec \nor [20, 20000, \\exp].asSpec.\nOr select a suitable ControlSpec from the List below.\nIf you don't know what this all means have a look\nat the ControlSpec-helpfile.")
			;

			cvString = widget.getSpec(slot).asString.split($ );

			cvString = cvString[1..cvString.size-1].join(" ");
			
			specField = TextField(thisEditor.tabs.views[0], flow0.bounds.width-20@15)
				.font_(staticTextFont)
				.string_(cvString)
				.action_({ |tf|
					widget.setSpec(tf.string.interpret, slot)
				})
			;
			
			flow0.shift(0, 5);

			specsList = PopUpMenu(thisEditor.tabs.views[0], flow0.bounds.width-20@20)
				.action_({ |sl|
					widget.setSpec(specsListSpecs[sl.value], slot);
				})
			;
			
			if(widget.editorEnv.specsListSpecs.isNil, { 
				specsListSpecs = List() 
			}, {
				specsListSpecs = widget.editorEnv.specsListSpecs;
			});
			
			if(widget.editorEnv.specsListItems.notNil, {
				specsList.items_(widget.editorEnv.specsListItems);
			}, {
				Spec.specs.pairsDo({ |k, v|
					if(v.isKindOf(ControlSpec), {
						specsList.items_(specsList.items.add(k++":"+v));
						specsListSpecs.add(v);
					})
				})
			});
						
			tmp = specsListSpecs.detectIndex({ |spec, i| spec == widget.getSpec(slot) });
			if(tmp.notNil, {
				specsList.value_(tmp);
			}, {
				specsListSpecs.array_([widget.getSpec(slot)]++specsListSpecs.array);
				specsList.items = List["custom:"+widget.getSpec(slot).asString]++specsList.items;
			});
			
			window.onClose_({
				widget.editorEnv.specsListSpecs = specsListSpecs;
				widget.editorEnv.specsListItems = specsList.items;
			});
						
			// MIDI editing
						
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width/2+40@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("MIDI-mode: 0-127 or in/decremental")
			;
			
			flow1.shift(5, 0);
			
			midiModes = ["0-127", "+/-"];
						
			midiModeSelect = PopUpMenu(thisEditor.tabs.views[1], flow1.bounds.width/2-70@15)
				.font_(staticTextFont)
				.items_(midiModes)
				.value_(thisMidiMode)
				.action_({ |ms|
					widget.setMidiMode(ms.value, slot);
				})
			;
			
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width/2+60@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("MIDI-mean (in/decremental mode only)")
			;
			
			flow1.shift(5, 0);
			
			midiMeanNB = NumberBox(thisEditor.tabs.views[1], flow1.bounds.width/2-90@15)
				.font_(staticTextFont)
				.value_(thisMidiMean)
				.action_({ |mb|
					widget.setMidiMean(mb.value, slot);
				})
				.step_(1.0)
				.clipLo_(0.0)
			;
			
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width/2+60@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("minimum distance for the slider (0-127 only)")
			;
			
			flow1.shift(5, 0);
			
			softWithinNB = NumberBox(thisEditor.tabs.views[1], flow1.bounds.width/2-90@15)
				.font_(staticTextFont)
				.value_(thisSoftWithin)
				.action_({ |mb|
					widget.setSoftWithin(mb.value, slot);
				})
				.step_(0.005)
				.clipLo_(0.01)
				.clipHi_(0.5)
			;
			
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width/2+60@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("MIDI-resolution (+/- only)")
			;
			
			flow1.shift(5, 0);

			midiResolutionNB = NumberBox(thisEditor.tabs.views[1], flow1.bounds.width/2-90@15)
				.font_(staticTextFont)
				.value_(thisMidiResolution)
				.action_({ |mb|
					widget.setMidiResolution(mb.value, slot);
				})
				.step_(0.05)
				.clipLo_(0.001)
				.clipHi_(10.0)
			;
			
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width/2+60@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("number of sliders per bank")
			;

			flow1.shift(5, 0);
			
			ctrlButtonBankField = TextField(thisEditor.tabs.views[1], flow1.bounds.width/2-90@15)
				.font_(staticTextFont)
				.string_(thisCtrlButtonBank)
				.action_({ |mb|
					if(mb.string != "nil", {
						widget.setCtrlButtonBank(mb.string.asInt, slot);
					}, {
						widget.setCtrlButtonBank(nil);
					})
				})
			;
			
			flow1.shift(0, 10);
			
			StaticText(thisEditor.tabs.views[1], flow1.bounds.width-20@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("(learn | connect) / source-ID (device) / chan / ctrl-nr.")
			;

			midiLearnBut = Button(thisEditor.tabs.views[1], 15@15)
				.font_(staticTextFont)
				.states_([
					["L", Color.white, Color.blue],
					["X", Color.white, Color.red]
				])
				.action_({ |ml|
					ml.value.switch(
						1, {
							margs = [
								[thisGuiEnv.midiSrc.string, msrc], 
								[thisGuiEnv.midiChan.string, mchan], 
								[thisGuiEnv.midiCtrl.string, mctrl]
							].collect({ |pair| if(pair[0] != pair[1], { pair[0].asInt }, { nil }) });
							if(margs.select({ |i| i.notNil }).size > 0, {
								widget.midiConnect(uid: margs[0], chan: margs[1], num: margs[2], key: slot)
							}, {
								widget.midiConnect(key: slot)
							})
						},
						0, { widget.midiDisconnect(slot) }
					)
				})
			;
			
			flow1.shift(0, 0);
			
			midiSrcField = TextField(thisEditor.tabs.views[1], flow1.bounds.width-165@15)
				.font_(staticTextFont)
				.string_(msrc)
				.background_(Color.white)
				.action_({ |tf|
					if(tf.string != msrc, {
						wcmHiLo.midiDisplay.model.value_((
							learn: "C",
							src: tf.string,
							chan: wcmHiLo.midiDisplay.model.value.chan,
							ctrl: wcmHiLo.midiDisplay.model.value.ctrl
						)).changed(\value)
					})
				})
				.mouseDownAction_({ |tf|
					tf.stringColor_(Color.red)
				})
				.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
					if(unicode == 13, {
						tf.stringColor_(Color.black);
					})
				}) 
			;
						
			flow1.shift(0, 0);
			
			midiChanField = TextField(thisEditor.tabs.views[1], 60@15)
				.font_(staticTextFont)
				.string_(mchan)
				.background_(Color.white)
				.action_({ |tf|
					if(tf.string != mchan, {
						wcmHiLo.midiDisplay.model.value_((
							learn: "C",
							src: wcmHiLo.midiDisplay.model.value.src,
							chan: tf.string,
							ctrl: wcmHiLo.midiDisplay.model.value.ctrl
						)).changed(\value)
					})
				})
				.mouseDownAction_({ |tf|
					tf.stringColor_(Color.red)
				})
				.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
					if(unicode == 13, {
						tf.stringColor_(Color.black);
					})
				}) 
			;
			
			flow1.shift(0, 0);
			
			midiCtrlField = TextField(thisEditor.tabs.views[1], 60@15)
				.font_(staticTextFont)
				.string_(mctrl)
				.background_(Color.white)
				.action_({ |tf|
					if(tf.string != mctrl, {
						wcmHiLo.midiDisplay.model.value_((
							learn: "C",
							src: wcmHiLo.midiDisplay.model.value.src,
							chan: wcmHiLo.midiDisplay.model.value.chan,
							ctrl: tf.string
						)).changed(\value)
					})
				})
				.mouseDownAction_({ |tf|
					tf.stringColor_(Color.red)
				})
				.keyUpAction_({ |tf, char, modifiers, unicode, keycode|
					if(unicode == 13, {
						tf.stringColor_(Color.black);
					})
				}) 
			;
			
			// OSC editting
			
			StaticText(thisEditor.tabs.views[2], flow2.bounds.width-20@12)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("device-IP/port")
			;
						
			ipField = TextField(thisEditor.tabs.views[2], flow2.bounds.width-60@15)
				.font_(textFieldFont)
				.stringColor_(textFieldFontColor)
				.background_(textFieldBg)
				.string_("")
			;
			
			flow2.shift(5, 0);

			portField = TextField(thisEditor.tabs.views[2], 36@15)
				.font_(textFieldFont)
				.stringColor_(textFieldFontColor)
				.background_(textFieldBg)
				.string_("")
			;
				
			flow2.shift(0, 0);

			StaticText(thisEditor.tabs.views[2], flow2.bounds.width-20@40)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("OSC command-name, e.g.: /my/cmd/name / OSC message slot: Either choose from a list of command-names (as set by the selected device) or add your custom one ")
			;
	
			flow2.shift(0, 0);
			
			deviceListMenu = PopUpMenu(thisEditor.tabs.views[2], flow2.bounds.width/2-40@15)
				.items_(["select device..."])
				.font_(Font("Helvetica", 10))
				.action_({ |m|
					cmdListMenu.items_(["command-names..."]);
					thisCmdNames = [nil];
					if(m.value != 0, {
						orderedCmds = cmdNames[m.items[m.value].asSymbol].order;
						orderedCmdSlots = cmdNames[m.items[m.value].asSymbol].atAll(orderedCmds);
						orderedCmds.do({ |cmd, i|
							cmdListMenu.items_(cmdListMenu.items.add(cmd.asString+"("++orderedCmdSlots[i]++")"));
							thisCmdNames = thisCmdNames.add(cmd.asString);
						})
					})
				})
				.mouseDownAction_({ |m|
					cmdNames = OSCCommands.deviceCmds;
					deviceListMenu.items_(["select device..."]);
					cmdNames.pairsDo({ |dev, cmds|
						deviceListMenu.items_(deviceListMenu.items ++ dev);
					})
				})
			;
			
			flow2.shift(0, 0);
			
			cmdListMenu = PopUpMenu(thisEditor.tabs.views[2], flow2.bounds.width/2-11@15)
				.items_(["command-names..."])
				.font_(Font("Helvetica", 10))
				.action_({ |m|
					if(nameField.enabled, {
						nameField.string_(thisCmdNames[m.value]);
						indexField.clipHi_(orderedCmdSlots[m.value-1]);
					})
				})
			;
			
			cmdNames.pairsDo({ |dev, cmds|
				deviceListMenu.items = deviceListMenu.items ++ dev;
			});
			
			flow2.shift(0, 0);
			
			addDeviceBut = Button(thisEditor.tabs.views[2], 29@15)
				.states_([
					["new", Color.white, Color(0.15, 0.5, 0.15)]
				])
				.font_(staticTextFont)
				.action_({ OSCCommands.gui })
			;

			nameField = TextField(thisEditor.tabs.views[2], flow2.bounds.width-60@15)
				.font_(textFieldFont)
				.stringColor_(textFieldFontColor)
				.background_(textFieldBg)
				.string_("/my/cmd/name")
			;
						
			flow2.shift(5, 0);
			
			indexField = NumberBox(thisEditor.tabs.views[2], 36@15)
				.font_(textFieldFont)
				.normalColor_(textFieldFontColor)
				.clipLo_(1)
				.clipHi_(inf)
				.shift_scale_(1)
				.ctrl_scale_(1)
				.alt_scale_(1)
				.value_(1)
			;
			
			flow2.shift(0, 0);
	
			StaticText(thisEditor.tabs.views[2], flow2.bounds.width-15@15)
				.font_(staticTextFont)
				.stringColor_(staticTextColor)
				.string_("OSC-input constraints + compensation")
			;
			
			inputConstraintLoField = NumberBox(thisEditor.tabs.views[2], flow2.bounds.width/2-66@15)
				.font_(textFieldFont)
				.normalColor_(textFieldFontColor)
				.value_(wcmHiLo.oscInputRange.model.value[0])
				.enabled_(false)
			;
			
			flow2.shift(5, 0);
			
			inputConstraintHiField = NumberBox(thisEditor.tabs.views[2], flow2.bounds.width/2-66@15)
				.font_(textFieldFont)
				.normalColor_(textFieldFontColor)
				.value_(wcmHiLo.oscInputRange.model.value[0])
				.enabled_(false)
			;
			
			flow2.shift(5, 0);
			
			alwaysPosField = StaticText(thisEditor.tabs.views[2], 32@15)
				.font_(staticTextFont)
				.string_(" +"++widget.alwaysPositive)
				.stringColor_(Color(0.5))
				.background_(Color(0.95, 0.95, 0.95))
			;
						
			flow2.shift(5, 0);

			calibBut = Button(thisEditor.tabs.views[2], 60@15)
				.font_(staticTextFont)
				.states_([
					["calibrating", Color.white, Color.red],
					["calibrate", Color.black, Color.green]
				])
			;
	
			flow2.shift(0, 0);
	
			StaticText(thisEditor.tabs.views[2], flow2.bounds.width-15@15)
				.font_(staticTextFont)
				.string_("Input to Output mapping")
			;
			
			flow2.shift(0, 0);
	
			StaticText(thisEditor.tabs.views[2], flow2.bounds.width-15@15)
				.font_(staticTextFont)
				.background_(Color.white)
				.string_(" current widget-spec constraints lo / hi:"+widget.getSpec(slot).minval+"/"+widget.getSpec(slot).maxval)
			;
	
			flow2.shift(5, 0);
			
			mappingSelectItems = ["linlin", "linexp", "explin", "expexp"];
			
			mappingSelect = PopUpMenu(thisEditor.tabs.views[2], flow2.bounds.width-15@20)
				.font_(Font("Helvetica", 12))
				.items_(mappingSelectItems)
				.action_({ |ms|
					widget.setOscMapping(ms.item, slot);
				})
			;
			
			if(widget.getOscMapping(slot).notNil, {
				mappingSelectItems.do({ |item, i|
					if(item.asSymbol === widget.getOscMapping(slot), {
						mappingSelect.value_(i);
					});
				}, {
					mappingSelect.value_(0);
				})
			});
						
			flow2.shift(0, 0);
	
			connectorBut = Button(thisEditor.tabs.views[2], flow2.bounds.width-15@25)
				.font_(staticTextFont)
				.states_([
					["connect OSC-controller", Color.white, Color.blue],
					["disconnect OSC-controller", Color.white, Color.red]
				])
				.action_({ |cb|
					cb.value.switch(
						1, { 
							widget.oscConnect(
								ipField.string,
								portField.value,
								nameField.string, 
								indexField.value.asInt,
								slot
							);
						},
						0, { widget.oscDisconnect(slot) }
					)
				})
			;

			calibNumBoxes = (lo: inputConstraintLoField, hi: inputConstraintHiField);
			
			calibBut.action_({ |but|
				but.value.switch(
					0, { 
						widget.setCalibrate(true, slot);
						wcmHiLo.calibration.model.value_(true).changed(\value);
					},
					1, { 
						widget.setCalibrate(false, slot);
						wcmHiLo.calibration.model.value_(false).changed(\value);
					}
				)
			});
	
			widget.getCalibrate(slot).switch(
				true, { calibBut.value_(0) },
				false, { calibBut.value_(1) }
			);
			
			actionName = TextField(thisEditor.tabs.views[3], flow3.bounds.width-100@20)
				.string_("action-name")
				.font_(textFieldFont)
			;
			
			flow3.shift(5, 0);
			
			enterActionBut = Button(thisEditor.tabs.views[3], 57@20)
				.font_(staticTextFont)
				.states_([
					["add Action", Color.white, Color.blue],
				])
				.action_({ |ab|
					if(actionName.string != "action-name" and:{
						enterAction.string != "{ |cv| /* do something */ }"
					}, {
						widget.addAction(actionName.string.asSymbol, enterAction.string, slot.asSymbol);
					})
				})
			;

			flow3.shift(0, 0);

			enterAction = TextView(thisEditor.tabs.views[3], flow3.bounds.width-35@50)
				.background_(Color.white)
				.font_(textFieldFont)
				.string_("{ |cv| /* do something */ }")
				.syntaxColorize
			;
			
			if(slot.notNil, {
				wdgtActions = widget.wdgtActions[slot];
			}, {
				wdgtActions = widget.wdgtActions;
			});
			
			wdgtActions.pairsDo({ |name, action|
												
				actionsList = actionsList.put(name, ());
				
				flow3.shift(0, 5);
				
				actionsList[name].nameField = StaticText(thisEditor.tabs.views[3], flow3.bounds.width-173@15)
					.font_(staticTextFont)
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.string_(""+name.asString)
				;
				
				flow3.shift(5, 0);
				
				actionsList[name].activate = Button(thisEditor.tabs.views[3], 60@15)
					.font_(staticTextFont)
					.states_([
						["activate", Color(0.1, 0.3, 0.15), Color(0.99, 0.77, 0.11)],
						["deactivate", Color.white, Color(0.1, 0.30, 0.15)],
					])
					.action_({ |rb|
						switch(rb.value, 
							0, { widget.activateAction(name, false, slot) },
							1, { widget.activateAction(name, true, slot) }
						)
					})
				;
				
				switch(action.asArray[0][1], 
					true, {
						actionsList[name].activate.value_(1);
					},
					false, {
						actionsList[name].activate.value_(0);
					}
				);
				
				flow3.shift(5, 0);
				
				actionsList[name].removeBut = Button(thisEditor.tabs.views[3], 60@15)
					.font_(staticTextFont)
					.states_([
						["remove", Color.white, Color.red],
					])
					.action_({ |rb|
						widget.removeAction(name.asSymbol, slot.asSymbol);
					})
				;
				
				flow3.shift(0, 0);
				
				actionsList[name].actionView = TextView(thisEditor.tabs.views[3], flow3.bounds.width-35@50)
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.font_(textFieldFont)
					.string_(action.asArray[0][0])
					.syntaxColorize
					.editable_(false)
				;
			})
			
		});
		
		tab !? { 
			thisEditor.tabs.focus(tab);
		};
		thisEditor.window.front;
	}
	
	front { |tab|
		thisEditor.window.front;
		tab !? { 
			thisEditor.tabs.stringFocusedColor_(labelStringColors[tab]);
			thisEditor.tabs.focus(tab);
		}
	}
	
	close { |slot|
		thisEditor.window.close;
		switch(allEditors[name].class,
			Event, { 
				allEditors[name].removeAt(slot);
				if(allEditors[name].isEmpty, { allEditors.removeAt(name) });
			},
			{ allEditors.removeAt(name) };
		)
	}
	
	isClosed { 
		var ret;
		thisEditor.window !? {
			ret = defer { thisEditor.window.isClosed };
			^ret.value;
		}
	}
	
	// not to be used directly!
	
	amendActionsList { |widget, addRemove, name, action, slot, active|
		
		var staticTextFont = Font(Font.defaultSansFace, 10);

		switch(addRemove, 
			\add, {
				
				actionsList.put(name, ());
				flow3.shift(0, 5);
				
				actionsList[name].nameField = StaticText(thisEditor.tabs.views[3], flow3.bounds.width-173@15)
					.font_(staticTextFont)
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.string_(""+name.asString)
				;
				
				flow3.shift(5, 0);
				
				actionsList[name].activate = Button(thisEditor.tabs.views[3], 60@15)
					.font_(staticTextFont)
					.states_([
						["activate", Color(0.1, 0.3, 0.15), Color(0.99, 0.77, 0.11)],
						["deactivate", Color.white, Color(0.1, 0.30, 0.15)],
					])
					.action_({ |rb|
						switch(rb.value, 
							0, { widget.activateAction(name, false, slot) },
							1, { widget.activateAction(name, true, slot) }
						)
					})
				;

				switch(active, 
					true, {
						actionsList[name].activate.value_(1);
					},
					false, {
						actionsList[name].activate.value_(0);
					}
				);
				
				flow3.shift(5, 0);
				
				actionsList[name].removeBut = Button(thisEditor.tabs.views[3], 60@15)
					.font_(staticTextFont)
					.states_([
						["remove", Color.white, Color.red],
					])
					.action_({ |ab|
						widget.removeAction(name.asSymbol, slot.asSymbol);
					})
				;
				
				flow3.shift(0, 0);
				
				actionsList[name].actionView = TextView(thisEditor.tabs.views[3], flow3.bounds.width-35@50)
					.background_(Color(1.0, 1.0, 1.0, 0.5))
					.font_(Font("Helvetica", 9))
					.string_(action.asArray[0][0])
					.syntaxColorize
					.editable_(false)
				;
			},
			\remove, {
				[
					actionsList[name].nameField, 
					actionsList[name].activate, 
					actionsList[name].removeBut, 
					actionsList[name].actionView
				].do(_.remove);
				flow3.reFlow(thisEditor.tabs.views[3]);
			}		
		)
			
	}
			
}