
import java.util.Set;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpConstants;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

public class ResponseSender implements Runnable {

	protected ClientRequest _clientRequest;

	protected ClientHandle _clientHandle;

	protected StringBuffer _strbufResponse;

	public ResponseSender(ClientHandle clientHandle, ClientRequest clientRequest, StringBuffer strbufResponse) {
		_clientHandle = clientHandle;
		_clientRequest = clientRequest;
		_strbufResponse = strbufResponse;
	}

	public void run() {

		HttpRequest request = _clientHandle.getRequest();
		ChannelHandlerContext ctx = _clientHandle.getContext();

		// Decide whether to close the connection or not.
		boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);

		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
				_clientHandle.getRequest().decoderResult().isSuccess() ? HttpResponseStatus.OK : HttpResponseStatus.BAD_REQUEST,
				Unpooled.copiedBuffer(_strbufResponse.toString(), CharsetUtil.UTF_8));

		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			// Add keep alive header as per:
			// -
			// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}

		// Encode the cookie.
		String cookieString = request.headers().getAndConvert(HttpHeaderNames.COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = ServerCookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				for (Cookie cookie : cookies) {
					response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.encode(cookie));
				}
			}
		} else {
			// Browser sent no cookie. Add some.
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
		}

		// Write the response.
		ctx.write(response);

	}
}
