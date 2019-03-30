/**
 * General utility methods.
 */
module com.metsci.glimpse.util
{
	exports com.metsci.glimpse.util.geo;
	exports com.metsci.glimpse.util.math.stochastic.pdfcont2d;
	exports com.metsci.glimpse.util.primitives.rangeset;
	exports com.metsci.glimpse.util.math.fast;
	exports com.metsci.glimpse.util.primitives.sorted;
	exports com.metsci.glimpse.util.units;
	exports com.metsci.glimpse.util.geo.projection;
	exports com.metsci.glimpse.util.geo.util;
	exports com.metsci.glimpse.util.jnlu;
	exports com.metsci.glimpse.util.concurrent;
	exports com.metsci.glimpse.util.logging.format;
	exports com.metsci.glimpse.util;
	exports com.metsci.glimpse.util.math.stat;
	exports com.metsci.glimpse.util.quadtree.longvalued;
	exports com.metsci.glimpse.util.logging;
	exports com.metsci.glimpse.util.math.stochastic.pdfcont;
	exports com.metsci.glimpse.util.units.time;
	exports com.metsci.glimpse.util.math.stochastic.pdfcont3d;
	exports com.metsci.glimpse.util.quadtree;
	exports com.metsci.glimpse.util.geo.format;
	exports com.metsci.glimpse.util.math.stochastic;
	exports com.metsci.glimpse.util.geo.datum;
	exports com.metsci.glimpse.util.math.approx;
	exports com.metsci.glimpse.util.var;
	exports com.metsci.glimpse.util.math.stochastic.pdfdiscrete;
	exports com.metsci.glimpse.util.units.time.format;
	exports com.metsci.glimpse.util.primitives.algorithms;
	exports com.metsci.glimpse.util.math;
	exports com.metsci.glimpse.util.io.datapipe;
	exports com.metsci.glimpse.util.io;
	exports com.metsci.glimpse.util.vector;
	exports com.metsci.glimpse.util.buffer;
	exports com.metsci.glimpse.util.primitives;

	requires transitive fastutil;
	requires transitive guava;
	requires transitive java.desktop;
	requires transitive java.logging;
	requires transitive java.base;
	requires jdk.unsupported;
}
