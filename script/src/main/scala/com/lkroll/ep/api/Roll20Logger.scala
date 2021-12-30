package com.lkroll.ep.api

import scribe._;
import scribe.writer.Writer;
import scribe.output.LogOutput;
import scribe.output.format.OutputFormat;
import com.lkroll.roll20.api.facade.Roll20API;

class Roll20Logger extends Writer {
  override def write[M](record: LogRecord[M], output: LogOutput, outputFormat: OutputFormat): Unit = {
    val text = output.plainText;
    Roll20API.log(text);
  }

  override def dispose(): Unit = {
    // do nothing
  }
}
