#!/usr/bin/env amm

import java.io.File;
import ammonite.ops._
import ammonite.ops.ImplicitWd._
import scalaj.http._

case class Roll20Config(roll20: String, campaignId: String)

def javacp(version: String): Path = pwd/RelPath(s"jvm/target/scala-2.11/EP Sheet JVM-assembly-${version}.jar"); 
val jsfileFast = "js/target/scala-2.11/ep-sheet-js-fastopt.js";
val jsfileFull = "js/target/scala-2.11/ep-sheet-js-opt.js";
val sheetworkers = "com.larskroll.ep.sheet.EPWorkers";
val sheet = "com.larskroll.ep.sheet.EPSheet";

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
		val cmd = %%('java, "-jar", javacp.toString, "--sheet", sheet, "--javascript", jsfile, "--sheetworkers", sheetworkers, "--html", htmlfile, "--css", cssfile, "--translation", translationfile);
		println("*** ERRORS ***");
		cmd.err.lines.foreach(println);
		println("*** OUT ***");
		cmd.out.lines.foreach(println);
		if (cmd.exitCode == 0) {
			println("Assembly complete.");
			var attempt = 0;
			while (attempt < maxRetries) {
				try {
					upload(attempt);
					return;
				} catch {
					case e: Throwable => e.printStackTrace(Console.err);
				}
				attempt += 1;
			}
		} else {
			Console.err.println("Error while assembling sheet:");
			println(cmd);
		}
	} catch {
		case e: Throwable => e.printStackTrace(Console.err);
	}
}

def upload(attempt: Int) {
	if (attempt >= maxRetries) {
		println("Reached maximum retries on upload. Aborting..."); return;
	}
	// read files
	val html = read! pwd/htmlfile;
	val css = read! pwd/cssfile;
	val translation = read! pwd/translationfile;
	// upload
	println(s"#${attempt}: Uploading to ${campaign.roll20} -> ${campaign.campaignId}...");
	val requrl = s"https://app.${campaign.roll20}.net/campaigns/savesettings/${campaign.campaignId}";
	val respurl = s"https://app.${campaign.roll20}.net/campaigns/campaignsettings/${campaign.campaignId}";
	val res = Http(requrl).option(HttpOptions.connTimeout(5000)).option(HttpOptions.readTimeout(30000)).postForm(Seq(
		"customcharsheet_layout" -> html,
	    "customcharsheet_style" -> css,
	    "customcharsheet_translation" -> translation,
	    "allowcharacterimport" -> "true",
	    "bgimage" -> "none",
	    "publicaccess" -> "false",
	    "charsheettype" -> "custom",
	    "compendium_override" -> "none"
	)).cookie("rack.session", rackSessionId).asString;
	if ((res.code != 303) || !res.headers("Location")(0).equals(respurl)) {
		Console.err.println(s"Problem submitting sheet (${res.code}), response headers:\n${res.headers}\nbody:\n${res.body}");
	} else {
		println("Sheet uploaded!");
	}
}