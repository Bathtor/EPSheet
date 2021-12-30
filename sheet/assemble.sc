#!/usr/bin/env amm

import java.io.File;
import os.{Path, RelPath}

case class Roll20Config(roll20: String, campaignId: String)

def javacp(version: String): Path = os.pwd/RelPath(s"jvm/target/scala-2.13/EP Sheet-assembly-${version}.jar"); 
val jsfileFast = "js/target/scala-2.13/ep-sheet-fastopt.js";
val jsfileFull = "js/target/scala-2.13/ep-sheet-opt.js";
val sheetworkers = "EPWorkers";
val sheet = "com.lkroll.ep.sheet.EPSheet";

val htmlfile = "ep-sheet.html";
val cssfile = "ep-sheet.css";
val translationfile = "translation.json";

val epdev = Roll20Config("roll20", "<campaign id>");
val campaign = epdev;
val rackSessionId = "<insert me>";

val maxRetries = 3;

@main
def main(version: String, full: Boolean = false): Unit = {
	val javacppath = javacp(version);
	if (full) {
		assemble(javacppath, jsfileFull);
	} else {
		assemble(javacppath, jsfileFast);
	}
}

def assemble(javacp: Path, jsfile: String) {
	try {
		println("Assembling...");
		val cmd = os.proc('java, "-jar", javacp.toString, "--sheet", sheet, "--javascript", jsfile, "--sheetworkers", sheetworkers, "--html", htmlfile, "--css", cssfile, "--translation", translationfile).call();
		println("*** ERRORS ***");
		cmd.err.lines.foreach(println);
		println("*** OUT ***");
		cmd.out.lines.foreach(println);
		if (cmd.exitCode == 0) {
			println("Assembly complete.");
		} else {
			Console.err.println("Error while assembling sheet:");
			println(cmd);
		}
	} catch {
		case e: Throwable => e.printStackTrace(Console.err);
	}
}
