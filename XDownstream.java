package de.steini2000.simple;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
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
 * Servlet implementation class Downstream
 */
@WebServlet(urlPatterns = { "/downstream" }, asyncSupported = true)
public class XDownstream extends HttpServlet {

	private static final Logger LOG = Logger.getLogger("de.steini2000");

	static AtomicInteger reqCounter = new AtomicInteger(0);

	private static final long serialVersionUID = 1L;

	public XDownstream() {
		super();
		LOG.info("create XDownstream");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		LOG.info("init XDownstream");
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doAsync(request);
	}

	private void doAsync(HttpServletRequest request) {

		int reqNum = reqCounter.incrementAndGet();
		LOG.info("Downstream Request number: " + reqNum);

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
					Thread.sleep(3 * 1000L);
				} catch (InterruptedException e1) {
					Thread.currentThread().interrupt();
				}

				LOG.info("Send answer after 3 seconds.. Request number: " + reqNum);
				try {
					sendImmediate(ctx);
				} catch (IOException e) {
					LOG.log(Level.WARNING, "IOException: ", e);
				}
				ctx.complete();

			}
		});
	}

	/* IMMEDIATE */
	private void sendImmediate(final AsyncContext ctx) throws IOException {
		HttpServletResponse hsr = (HttpServletResponse) ctx.getResponse();
		String datum = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		hsr.getWriter().append(datum);
		for (int i = 0; i < 4086; i++) {
			hsr.getWriter().append('y');
		}
		hsr.setStatus(HttpServletResponse.SC_OK);
	}

}
