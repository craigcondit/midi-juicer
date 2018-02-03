package org.randomcoder.midi;

import java.io.File;

import org.jline.reader.*;
import org.randomcoder.midi.terminal.Shell;

public class MidiShell {

	public static void main(String[] args) throws Exception {
		File homeDir = new File(System.getProperty("user.home"));
		File historyFile = new File(homeDir, ".midihistory");
		try (Shell shell = new Shell("midi-shell", System.in, System.out, historyFile)) {
			LineReader reader = shell.getReader();

			String prompt = "> ";

			while (true) {
				String line = null;
				try {
					line = reader.readLine(prompt);
				} catch (UserInterruptException e) {
					e.printStackTrace();
					// Ignore
				} catch (EndOfFileException e) {
					return;
				}
			}
			// TODO keep this alive
		}
	}

}
