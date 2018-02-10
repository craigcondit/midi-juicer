package org.randomcoder.midi.samples;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.randomcoder.libusb.Usb;
import org.randomcoder.libusb.UsbDeviceDescriptor;
import org.randomcoder.libusb.UsbDeviceDescriptors;
import org.randomcoder.libusb.UsbException;
import org.randomcoder.libusb.UsbLogLevel;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class AbletonPush2 implements Closeable {

	private static final UsbLogLevel LOG_LEVEL = UsbLogLevel.INFO;

	private static final short ABLETON_VENDOR_ID = (short) 0x2982;
	private static final short PUSH2_PRODUCT_ID = (0x1967);
	private static final byte LIBUSB_CLASS_PER_INTERFACE = 0;
	private static final byte PUSH2_BULK_EP_OUT = 0x01;
	private static final int TRANSFER_TIMEOUT = 1000; // milliseconds

	private static final int ROWS = 160;
	private static final int COLS = 960;
	private static final int BYTES_PER_PIXEL = 2;
	private static final int PADDING_PER_ROW = 128;

	private static final int BYTES_PER_ROW = COLS * BYTES_PER_PIXEL + PADDING_PER_ROW;
	private static final int BUFFER_SIZE = BYTES_PER_ROW * ROWS;

	private static final int TRANSFER_SIZE = 16384;

	private static final int SOURCE_BYTES_PER_PIXEL = 4;
	private static final int SOURCE_ROW_SIZE = COLS * SOURCE_BYTES_PER_PIXEL;
	private static final int SOURCE_BUFFER_SIZE = SOURCE_ROW_SIZE * ROWS;

	private static final WritablePixelFormat<ByteBuffer> BGRA_FORMAT = WritablePixelFormat.getByteBgraPreInstance();

	private static final byte[] FRAME_HEADER = new byte[] {
			(byte) 0xff, (byte) 0xcc, (byte) 0xaa, (byte) 0x88,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00
	};

	private final Usb usb;
	private Memory frameHeader;
	private byte[][] bgraBuffers;
	private byte[][] rgb16Buffers;
	private byte[][] maskedBuffers;
	private Memory[] nativeBuffers;
	private volatile int activeBuffer = 0;
	private Pointer usbContext;
	private Pointer pushDevice;
	private volatile boolean claimed = false;
	private volatile boolean init = false;

	public AbletonPush2() throws IOException {
		usb = Usb.getInstance();
		try {
			frameHeader = initFrameHeader();
			bgraBuffers = initBgraBuffers();
			rgb16Buffers = initRgb16Buffers();
			maskedBuffers = initRgb16Buffers();
			nativeBuffers = initNativeBuffers();
			usbContext = initUsbContext(usb);
			usb.setLogLevel(usbContext, LOG_LEVEL);
			pushDevice = openPush(usb, usbContext);
			claimInterface(usb, pushDevice);
			claimed = true;

			init = true;
		} finally {
			if (!init) {
				close();
			}
		}
	}

	public void sendFrameHeader() throws IOException {
		try {
			IntByReference transferred = new IntByReference();

			usb.bulkTransfer(
					pushDevice,
					PUSH2_BULK_EP_OUT,
					frameHeader,
					FRAME_HEADER.length,
					transferred,
					TRANSFER_TIMEOUT);

			int bytesSent = transferred.getValue();

			if (bytesSent != FRAME_HEADER.length) {
				throw new IOException(String.format(
						"Error while sending frame header. Expected to send %d bytes, actually sent %d",
						FRAME_HEADER.length, bytesSent));
			}

		} catch (UsbException e) {
			throw new IOException("Unable to send frame header", e);
		}
	}

	public void writeImageToInactiveBGRASourceBuffer(Image image) {
		synchronized (nativeBuffers) {
			int index = (activeBuffer + 1) % 2;

			byte[] buffer = bgraBuffers[index];
			PixelReader pr = image.getPixelReader();
			pr.getPixels(0, 0, COLS, ROWS, BGRA_FORMAT, buffer, 0, SOURCE_ROW_SIZE);
		}
	}

	public void convertInactiveBufferFromBgraToRGB16() {
		synchronized (nativeBuffers) {
			int index = (activeBuffer + 1) % 2;

			byte[] src = bgraBuffers[index];
			byte[] dest = rgb16Buffers[index];

			int srcOffset = 0;
			int dstOffset = 0;

			for (int y = 0; y < ROWS; y++) {

				for (int x = 0; x < COLS; x++) {
					int blue = (src[srcOffset] >> 3) & 0b0001_1111;
					int green = (src[srcOffset + 1] >> 2) & 0b0011_1111;
					int red = (src[srcOffset + 2] >> 3) & 0b0001_1111;
					
					int color = red | (green << 5) | (blue << 11);

					dest[dstOffset] = (byte) (color & 0xff);
					dest[dstOffset + 1] = (byte) ((color >> 8) & 0xff);

					srcOffset += 4;
					dstOffset += 2;
				}
				
				dstOffset += PADDING_PER_ROW;
			}
		}
	}

	public void maskInactiveBuffer() {
		synchronized (nativeBuffers) {
			int index = (activeBuffer + 1) % 2;

			byte[] src = rgb16Buffers[index];
			byte[] dest = maskedBuffers[index];

			int offset = 0;

			for (int y = 0; y < ROWS; y++) {
				for (int x = 0; x < COLS; x += 2) {
					dest[offset] = (byte) (src[offset] ^ 0xe7);
					dest[offset + 1] = (byte) (src[offset + 1] ^ 0xf3);
					dest[offset + 2] = (byte) (src[offset + 2] ^ 0xe7);
					dest[offset + 3] = (byte) (src[offset + 3] ^ 0xff);

					offset += 4;
				}

				offset += PADDING_PER_ROW;
			}
		}
	}

	public void copyInactiveMaksedBufferToNative() {
		synchronized (nativeBuffers) {
			int index = (activeBuffer + 1) % 2;

			byte[] src = maskedBuffers[index];
			Memory dest = nativeBuffers[index];
			dest.write(0L, src, 0, src.length);
		}
	}

	public void toggleActiveFramebuffer() throws IOException {
		synchronized (nativeBuffers) {
			activeBuffer = (activeBuffer + 1) % 2;
		}
	}

	public void sendActiveFramebuffer() throws IOException {
		Memory active;
		synchronized (nativeBuffers) {
			active = nativeBuffers[activeBuffer];
		}

		sendFrameHeader();

		for (int i = 0; i < BUFFER_SIZE; i += TRANSFER_SIZE) {
			try {
				IntByReference transferred = new IntByReference();

				usb.bulkTransfer(
						pushDevice,
						PUSH2_BULK_EP_OUT,
						active.share(i),
						TRANSFER_SIZE,
						transferred,
						TRANSFER_TIMEOUT);

				int bytesSent = transferred.getValue();

				if (bytesSent != TRANSFER_SIZE) {
					throw new IOException(String.format(
							"Error while sending frame. Expected to send %d bytes, actually sent %d",
							TRANSFER_SIZE, bytesSent));
				}

			} catch (UsbException e) {
				throw new IOException("Unable to send frame", e);
			}
		}
	}

	private static Pointer openPush(Usb usb, Pointer usbContext) throws IOException {

		UsbDeviceDescriptors udds = usb.getDeviceDescriptors(usbContext);

		try {

			UsbDeviceDescriptor pushDescriptor = Arrays.stream(udds.descriptors)
					.filter(udd -> udd.bDeviceClass == LIBUSB_CLASS_PER_INTERFACE)
					.filter(udd -> udd.idVendor == ABLETON_VENDOR_ID)
					.filter(udd -> udd.idProduct == PUSH2_PRODUCT_ID)
					.findFirst().orElseThrow(() -> new IOException("Ableton Push 2 device not found"));

			try {
				return usb.openDevice(pushDescriptor);
			} catch (UsbException e) {
				throw new IOException("Could not open Ableton Push 2 device", e);
			}

		} finally {
			usb.destroyDeviceDescriptors(udds);
		}

	}

	private static byte[][] initBgraBuffers() {
		byte[][] bufs = new byte[2][];
		bufs[0] = new byte[SOURCE_BUFFER_SIZE];
		bufs[1] = new byte[SOURCE_BUFFER_SIZE];
		return bufs;
	}

	private static byte[][] initRgb16Buffers() {
		byte[][] bufs = new byte[2][];
		bufs[0] = new byte[BUFFER_SIZE];
		bufs[1] = new byte[BUFFER_SIZE];
		return bufs;
	}

	private static Memory[] initNativeBuffers() {
		Memory[] bufs = new Memory[2];
		bufs[0] = new Memory(BUFFER_SIZE);
		bufs[1] = new Memory(BUFFER_SIZE);
		return bufs;
	}

	private static Memory initFrameHeader() {
		Memory m = new Memory(FRAME_HEADER.length);
		m.write(0, FRAME_HEADER, 0, FRAME_HEADER.length);
		return m;
	}

	private static Pointer initUsbContext(Usb usb) throws IOException {
		try {
			return usb.init();
		} catch (UsbException e) {
			throw new IOException("Unable to initialize USB", e);
		}
	}

	private static void claimInterface(Usb usb, Pointer pushDevice) throws IOException {
		try {
			usb.claimInterface(pushDevice, 0);
		} catch (UsbException e) {
			throw new IOException("Could not claim interface 0 of Push 2 device", e);
		}
	}

	public static int colorToArgb(int red, int green, int blue) {
		return ((0xFF & 0xFF) << 24) |
				((red & 0xFF) << 16) |
				((green & 0xFF) << 8) |
				((blue & 0xFF) << 0);
	}

	private static void writeColor(PixelWriter pw, int argb) {
		int[] buf = new int[COLS * ROWS];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = argb;
		}
		pw.setPixels(0, 0, COLS, ROWS, PixelFormat.getIntArgbInstance(), buf, 0, 0);
	}

	public static class JavaFX extends Application {

		@Override
		public void start(Stage stage) throws Exception {
			WebView webview = new WebView();
			webview.getEngine().load(
					"https://player.vimeo.com/video/48070605?autoplay=1");
//					"http://www.youtube.com/embed/SPqlnSVDpXQ?autoplay=1");
			webview.setPrefSize(COLS, ROWS);

			stage.setScene(new Scene(webview));
			stage.show();

			boolean shutdown = false;

			WritableImage image = new WritableImage(COLS, ROWS);
			PixelWriter pw = image.getPixelWriter();

			writeColor(pw, colorToArgb(0, 255, 0));

			Runnable eventLoop = () -> {
				try {
					try (AbletonPush2 push = new AbletonPush2()) {
						System.out.println("Ableton Push 2 initialized");

						while (!shutdown) {

							CountDownLatch latch = new CountDownLatch(1);
							Platform.runLater(() -> {
								stage.getScene().snapshot(image);
								latch.countDown();
							});
							latch.await();
							push.writeImageToInactiveBGRASourceBuffer(image);
							push.convertInactiveBufferFromBgraToRGB16();
							push.maskInactiveBuffer();
							push.copyInactiveMaksedBufferToNative();
							push.toggleActiveFramebuffer();
							push.sendActiveFramebuffer();

//							Thread.sleep(10L);
						}
					}
					System.out.println("Ableton Push 2 closed");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

			Thread eventLoopThread = new Thread(eventLoop);
			eventLoopThread.start();
		}

	}

	public static void main(String[] args) throws Exception {
		Application.launch(JavaFX.class, args);
	}

	public void close() {
		init = false;

		if (nativeBuffers != null) {
			for (int i = 0; i < nativeBuffers.length; i++) {
				nativeBuffers[i] = null;
			}
			nativeBuffers = null;
		}

		if (frameHeader != null) {
			frameHeader = null;
		}

		if (claimed) {
			try {
				usb.releaseInterface(pushDevice, 0);
			} catch (Exception ignored) {
			}
			claimed = false;
		}

		if (pushDevice != null) {
			usb.closeDevice(pushDevice);
			pushDevice = null;
		}

		if (usbContext != null) {
			usb.destroy(usbContext);
			usbContext = null;
		}
	}
}
