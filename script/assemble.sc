#!/usr/bin/env amm

import java.io.File;
import os.{RelPath, Path}

val jsfileFast = os.pwd / RelPath("target/scala-2.13/ep-api-script-fastopt.js");
val jsfileFull = os.pwd / RelPath("target/scala-2.13/ep-api-script-opt.js");
val jsDeps = os.pwd / RelPath("target/scala-2.13/ep-api-script-jsdeps.js");
val deployfile = os.pwd / RelPath("ep-script.js");
val rootscript = "EPScripts";

@main
def main(version: String, full: Boolean = false): Unit = {
	if (full) {
		assemble(version, jsfileFull);
	} else {
		assemble(version, jsfileFast);
	}
}

def assemble(version: String, jsfile: Path) {
	try {
		println(s"Assembling v${version}...");
		if (os.exists(deployfile)) {
			os.remove(deployfile);
		}
		val jsraw = os.read(jsDeps) ++ os.read(jsfile);
		val jsfull = jsraw ++ "\n" ++ s"${rootscript}.load();";
		os.write(deployfile, jsfull);
		val info = os.stat(deployfile);
      	val size = info.size.toDouble / (1000.0 * 1000.0);
		println(s"Done! Wrote ${size}MB.");
	} catch {
		case e: Throwable => e.printStackTrace(Console.err);
	}
}
