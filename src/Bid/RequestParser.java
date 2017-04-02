package Bid;

import java.util.*;

import org.boon.json.*;

import com.google.gson.Gson;

import io.netty.handler.codec.http.multipart.MixedAttribute;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

public class RequestParser implements Runnable {

	protected ParseListener _parseListener;
	protected ClientHandle _clientHandle;

	public RequestParser(ParseListener parseListener, ClientHandle clientHandle) {
		_parseListener = parseListener;
		_clientHandle = clientHandle;
	}

	public void run() {
		try {
			HttpRequest request = _clientHandle.getRequest();

			System.out.println("STARTING PARSEING REQUEST........");
			if (request.method().compareTo(HttpMethod.POST) == 0) {
				System.out.println("Post Request Parser........");
				HttpPostRequestDecoder postDecoder;
				List<InterfaceHttpData> lst;

				postDecoder = new HttpPostRequestDecoder(request);
				lst = postDecoder.getBodyHttpDatas();

				System.out.println("PRINT POST DATA: " + lst.toString());
				int i = 1;
				for (InterfaceHttpData temp : lst) {
					System.out.println("POST DECODER: " + temp + " END POST DECODER");
					System.err.println(i + " " + temp);
					i++;
				}
				// for (InterfaceHttpData temp : lst) {
				// System.err.println(i + " " + temp);
				// if (temp instanceof MixedAttribute) {
				// MixedAttribute attribute = (MixedAttribute) temp;
				// String value = attribute.getValue();
				//
				// Gson gson = new Gson();
				// Object obj = gson.fromJson(value, Object.class);
				//
				// System.out.println("VALUE OF THE BITCH: " + obj.toString());
				//
				// }
				// }
				// ClientRequest clientRequest = new ClientRequest(strAction,
				// strSessionID, mapRequestData)
				// }
			}

		} catch (Exception exp) {
			_parseListener.parsingFailed(_clientHandle, "Exception while parsing JSON object " + exp.toString());
		}
	}

}