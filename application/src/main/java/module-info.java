module org.randomcoder.midi.juicer {
  requires java.desktop;
  requires java.management;

  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.graphics;
  requires javafx.media;
  requires javafx.web;
  requires com.sun.jna;

  requires org.apache.logging.log4j;
  requires org.apache.logging.log4j.slf4j;
  requires org.slf4j;
  requires org.jooq.jool;

  exports org.randomcoder.midi;
  exports org.randomcoder.midi.mac.corefoundation to com.sun.jna;
}
