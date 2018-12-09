module org.randomcoder.midi.juicer {
  requires java.desktop;

  requires javafx.graphics;
  requires javafx.controls;
  requires javafx.web;

  requires org.slf4j;
  requires org.apache.logging.log4j;

  requires com.sun.jna;

  exports org.randomcoder.midi;
  exports org.randomcoder.midi.mac.corefoundation to com.sun.jna;
}