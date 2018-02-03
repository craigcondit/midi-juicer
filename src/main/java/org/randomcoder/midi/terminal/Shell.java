package org.randomcoder.midi.terminal;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.jline.reader.*;
import org.jline.terminal.*;

public class Shell implements Closeable {

    private final Terminal terminal;
    private final LineReader reader;

    public Shell(String appName, InputStream in, PrintStream out, File historyFile) throws IOException {
	terminal = TerminalBuilder.builder()
		.name(appName)
		.type(System.getenv("TERM"))
		.system(false)
		.streams(in, out)
		.encoding(StandardCharsets.UTF_8)
		.jansi(true)
		.build();

	reader = LineReaderBuilder.builder()
		.appName(appName)
		.variable(LineReader.HISTORY_FILE, historyFile.getAbsolutePath())
		.terminal(terminal)
		.build();
    }

    public LineReader getReader() {
	return reader;
    }

    @Override
    public void close() throws IOException {
	terminal.close();
    }
}
