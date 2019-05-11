/**
 * Core Glimpse data structures for plots, axes, layouts and painters.
 */
module com.metsci.glimpse.core
{
	exports com.metsci.glimpse.support.texture.mutator;
	exports com.metsci.glimpse.context;
	exports com.metsci.glimpse.painter.base;
	exports com.metsci.glimpse.axis.listener;
	exports com.metsci.glimpse.event.key;
	exports com.metsci.glimpse.support.font;
	exports com.metsci.glimpse.event.mouse;
	exports com.metsci.glimpse.painter.group;
	exports com.metsci.glimpse.support.atlas;
	exports com.metsci.glimpse.painter.texture;
	exports com.metsci.glimpse.painter.shape;
	exports com.metsci.glimpse.support.shader.colormap;
	exports com.metsci.glimpse.painter.track;
	exports com.metsci.glimpse.painter.plot;
	exports com.metsci.glimpse.support.wrapped;
	exports com.metsci.glimpse.support.polygon;
	exports com.metsci.glimpse.event.mouse.swing;
	exports com.metsci.glimpse.plot.timeline.group;
	exports com.metsci.glimpse.support.swing;
	exports com.metsci.glimpse.plot.timeline.event;
	exports com.metsci.glimpse.plot.timeline.layout;
	exports com.metsci.glimpse.support;
	exports com.metsci.glimpse.painter.info;
	exports com.metsci.glimpse.support.shader;
	exports com.metsci.glimpse.painter;
	exports com.metsci.glimpse.layout.matcher;
	exports com.metsci.glimpse.axis.painter;
	exports com.metsci.glimpse.gl;
	exports com.metsci.glimpse.support.interval;
	exports com.metsci.glimpse.plot.timeline.event.paint;
	exports com.metsci.glimpse.axis;
	exports com.metsci.glimpse.axis.painter.label.time;
	exports com.metsci.glimpse.plot.timeline.data;
	exports com.metsci.glimpse.event.touch;
	exports com.metsci.glimpse.support.colormap;
	exports com.metsci.glimpse.support.atlas.painter;
	exports com.metsci.glimpse.painter.geo;
	exports com.metsci.glimpse.gl.shader;
	exports com.metsci.glimpse.axis.painter.label;
	exports com.metsci.glimpse.canvas;
	exports com.metsci.glimpse.support.settings;
	exports com.metsci.glimpse.plot.timeline;
	exports com.metsci.glimpse.plot.timeline.listener;
	exports com.metsci.glimpse.support.shader.line;
	exports com.metsci.glimpse.support.atlas.support;
	exports com.metsci.glimpse.event.key.newt;
	exports com.metsci.glimpse.layout;
	exports com.metsci.glimpse.painter.decoration;
	exports com.metsci.glimpse.gl.util;
	exports com.metsci.glimpse.plot.timeline.event.listener;
	exports com.metsci.glimpse.support.color;
	exports com.metsci.glimpse.support.popup;
	exports com.metsci.glimpse.plot.stacked;
	exports com.metsci.glimpse.event;
	exports com.metsci.glimpse.support.texture;
	exports com.metsci.glimpse.axis.listener.touch;
	exports com.metsci.glimpse.axis.listener.mouse;
	exports com.metsci.glimpse.support.atlas.shader;
	exports com.metsci.glimpse.axis.tagged.painter;
	exports com.metsci.glimpse.event.mouse.newt;
	exports com.metsci.glimpse.plot;
	exports com.metsci.glimpse.gl.texture;
	exports com.metsci.glimpse.painter.treemap;
	exports com.metsci.glimpse.plot.timeline.animate;
	exports com.metsci.glimpse.support.selection;
	exports com.metsci.glimpse;
	exports com.metsci.glimpse.axis.tagged;
	exports com.metsci.glimpse.support.projection;
	exports com.metsci.glimpse.axis.factory;
	exports com.metsci.glimpse.support.shader.triangle;
	exports com.metsci.glimpse.plot.timeline.painter;
	exports com.metsci.glimpse.support.shader.point;

	requires transitive com.metsci.glimpse.platformFixes;
	requires transitive com.metsci.glimpse.text;
	requires transitive com.metsci.glimpse.util;
	
	requires transitive jogl.all.main;
	requires transitive jogl.all;
	requires transitive gluegen.rt.main;
	requires transitive gluegen.rt;
	
	requires transitive miglayout.core;
}