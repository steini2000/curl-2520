package de.steini2000.simple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Upstream
 */
@WebServlet(urlPatterns = { "/upstream" }, asyncSupported = true)
public class XUpstream extends HttpServlet {

	private static final Logger LOG = Logger.getLogger("de.steini2000");

	static AtomicInteger reqCounter = new AtomicInteger(0);

	private static final long serialVersionUID = 1L;

	public XUpstream() {
		super();
		LOG.info("create XUpstream");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.info("init XUpstream");
	}

	@Override
	public void destroy() {
		super.destroy();
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doAsync(request, response);
	}

	private void doAsync(HttpServletRequest request, HttpServletResponse response) {

		int reqNum = reqCounter.incrementAndGet();
		LOG.info("Upstream request number: " + reqNum);

		// create the async context, otherwise getAsyncContext() will be null
		final AsyncContext ctx = request.startAsync();

		ctx.setTimeout(0);

		// attach listener to respond to lifecycle events of this AsyncContext
		ctx.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				LOG.info("onComplete called");
			}

			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				LOG.info("onTimeout called");
			}

			@Override
			public void onError(AsyncEvent event) throws IOException {
				LOG.info("onError called");
			}

			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
				LOG.info("onStartAsync called");
			}
		});

		// spawn some task in a background thread
		ctx.start(new Runnable() {

			public void run() {

				try {
					long total = getBytesFromInputStream(request.getInputStream());
					LOG.info("received data length: " + total);
				} catch (IOException e) {
					LOG.info("InputStream IOException: " + e.toString());
				} catch (NullPointerException e) {
					LOG.info("InputStream NullPointerException: " + e.toString());
				}

				LOG.info("Send OK");
				response.setStatus(HttpServletResponse.SC_OK);

				ctx.complete();
			}
		});
	}

	public static long getBytesFromInputStream(InputStream is) throws IOException {

		long total = 0;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
			byte[] buffer = new byte[0xFFFF];

			LOG.info("Reading from InputStream to buffer .. buffer size 0xFFFF");
			for (int len = 0; (len = is.read(buffer)) != -1;) {
				LOG.info("read " + len + " bytes from InputStream");
				total += len;
			}
			LOG.info("read returned -1 End Of Stream");

		}

		return total;
	}
}
