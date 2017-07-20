/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.filters;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class RequestResponseLoggingFilter implements Filter {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("org.openecomp.sdnc.filters.request.response");

	private static class ByteArrayServletStream extends ServletOutputStream {

		ByteArrayOutputStream baos;

		ByteArrayServletStream(ByteArrayOutputStream baos) {
			this.baos = baos;
		}

		@Override
		public void write(int param) throws IOException {
			baos.write(param);
		}
	}

	private static class ByteArrayPrintWriter {

		private ByteArrayOutputStream baos = new ByteArrayOutputStream();

		private PrintWriter pw = new PrintWriter(baos);

		private ServletOutputStream sos = new ByteArrayServletStream(baos);

		public PrintWriter getWriter() {
			return pw;
		}

		public ServletOutputStream getStream() {
			return sos;
		}

		byte[] toByteArray() {
			return baos.toByteArray();
		}
	}

	private class BufferedServletInputStream extends ServletInputStream {

		ByteArrayInputStream bais;

		public BufferedServletInputStream(ByteArrayInputStream bais) {
			this.bais = bais;
		}

		@Override
		public int available() {
			return bais.available();
		}

		@Override
		public int read() {
			return bais.read();
		}

		@Override
		public int read(byte[] buf, int off, int len) {
			return bais.read(buf, off, len);
		}

	}

	private class BufferedRequestWrapper extends HttpServletRequestWrapper {

		ByteArrayInputStream bais;

		ByteArrayOutputStream baos;

		BufferedServletInputStream bsis;

		byte[] buffer;

		public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
			super(req);

			InputStream is = req.getInputStream();
			baos = new ByteArrayOutputStream();
			byte buf[] = new byte[1024];
			int letti;
			while ((letti = is.read(buf)) > 0) {
				baos.write(buf, 0, letti);
			}
			buffer = baos.toByteArray();

		}

		@Override
		public ServletInputStream getInputStream() {
			try {
				bais = new ByteArrayInputStream(buffer);
				bsis = new BufferedServletInputStream(bais);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return bsis;
		}

		public byte[] getBuffer() {
			return buffer;
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest);

		StringBuilder requestHeaders = new StringBuilder("REQUEST|");
		requestHeaders.append(httpRequest.getMethod());
		requestHeaders.append(":");
		requestHeaders.append(httpRequest.getRequestURL().toString());
		requestHeaders.append("|");
		String header;
		for (Enumeration<String> e = httpRequest.getHeaderNames(); e.hasMoreElements();) {
			header = e.nextElement();
			requestHeaders.append(header);
			requestHeaders.append(":");
			requestHeaders.append(httpRequest.getHeader(header));
			requestHeaders.append(";");

		}
		log.info(requestHeaders.toString());

		log.info("REQUEST BODY|" + new String(bufferedRequest.getBuffer()));

		final HttpServletResponse response = (HttpServletResponse) servletResponse;

		final ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
		HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
			@Override
			public PrintWriter getWriter() {
				return pw.getWriter();
			}

			@Override
			public ServletOutputStream getOutputStream() {
				return pw.getStream();
			}

		};

		try {

		filterChain.doFilter(bufferedRequest, wrappedResp);

		}catch (Exception e){
			log.error("Chain Exception",e);
			throw e;
		} finally {
		byte[] bytes = pw.toByteArray();
		response.getOutputStream().write(bytes);
		response.getOutputStream().flush();

		StringBuilder responseHeaders = new StringBuilder("RESPONSE HEADERS|");

		for (String headerName : response.getHeaderNames()) {
			responseHeaders.append(headerName);
			responseHeaders.append(":");
			responseHeaders.append(response.getHeader(headerName));
			responseHeaders.append(";");

		}
		log.info(responseHeaders.toString());

		if ("gzip".equals(response.getHeader("Content-Encoding"))) {

			log.info("UNGZIPED RESPONSE BODY|" + decompressGZIPByteArray(bytes));

		} else {

			log.info("RESPONSE BODY|" + new String(bytes));
		}
		}
	}

	@Override
	public void destroy() {
	}

	private String decompressGZIPByteArray(byte[] bytes) {

		BufferedReader in = null;
		InputStreamReader inR = null;
		ByteArrayInputStream byteS = null;
		GZIPInputStream gzS = null;
		StringBuilder str = new StringBuilder();
		try {
			byteS = new ByteArrayInputStream(bytes);
			gzS = new GZIPInputStream(byteS);
			inR = new InputStreamReader(gzS);
			in = new BufferedReader(inR);

			if (in != null) {

				String content;

				while ((content = in.readLine()) != null) {
					str.append(content);
				}
			}

		} catch (Exception e) {
			log.error("Failed get read GZIPInputStream", e);
		} finally {

			if (byteS != null)
				try {
					byteS.close();
				} catch (IOException e1) {
					log.error("Failed to close ByteStream", e1);
				}
			if (gzS != null)
				try {
					gzS.close();
				} catch (IOException e2) {
					log.error("Failed to close GZStream", e2);
				}
			if (inR != null)
				try {
					inR.close();
				} catch (IOException e3) {
					log.error("Failed to close InputReader", e3);
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					log.error("Failed to close BufferedReader", e);
				}
		}
		return str.toString();
	}
}
