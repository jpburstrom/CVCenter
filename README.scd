CVCenter s supposed to be a graphical control-interface that lets you connect synth-controls or controls within patterns to hardware midi-sliders or OSC-controllers. CVCenter itself is more or less a container of CVs, each represented by CVWidgets (currently CVWidgetKnob and CVWidget2D). Each of them can be connected to an unlimited number GUI-elements or/and control parameters in running Synths or set values in Patterns. As CV inherits from Stream any instance can be used as any other Pattern (Pseq, Prand, Pwhite etcetc.).

For more info check out CVCenter.html resp. CVWidget.html.

Installation: 
1) get SuperCollider from http://supercollider.sourceforge.net/downloads/
2) install the required extensions via the Quarks-mechanism:
	a) Conductor
	b) TabbedView
	c) wslib (optional)
	see the Quarks-helpfile for more information on how to do this.
2) after installing SuperCollider and the required extensions put all content of CVCenter in
your user-app-support directory. Execute the following line SuperCollder to see where that is:

Platform.userAppSupportDir

Under OSX this will resolve to:
~/Library/Application Support/SuperCollider/Extensions/

Under Linux, using the latest Developer-Version of SuperCollider this will resolve to
~/.local/share/SuperCollider

Note: if you're on Linux you will need to have installed the developer-version of SuperCollider (3.5~dev) as CVCenter needs QtGUI to be installed.